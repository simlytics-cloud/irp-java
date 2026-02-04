package iso.example.irpsystem.irpmodel.impl;

import devs.iso.PortValue;
import devs.iso.time.LongSimTime;
import devs.utils.Schedule;
import iso.example.irpsystem.irpdomain.Coordinate;
import iso.example.irpsystem.irpdomain.Delivery;
import iso.example.irpsystem.irpdomain.DeliveryRoute;
import iso.example.irpsystem.irpdomain.ImmutableCoordinate;
import iso.example.irpsystem.irpdomain.ImmutableDelivery;
import iso.example.irpsystem.irpdomain.ImmutableDeliveryRoute;
import iso.example.irpsystem.irpdomain.ImmutableVehicleCost;
import iso.example.irpsystem.irpdomain.VehicleCost;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableVehicleProperties;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableVehicleState;
import iso.example.irpsystem.irpmodel.InventoryRouting.Vehicle;
import iso.example.irpsystem.irpmodel.algorithms.Distance;
import iso.example.irpsystem.irpmodel.algorithms.TimeUtils;

import java.time.Duration;
import java.util.List;
import devs.PDevsCoordinator;
import devs.PDevsSimulator;

public class VehicleImpl extends Vehicle {
  public record DeliveryEvent(ImmutableDelivery delivery) {
  }

  public record ReturnToManufacturerEvent() {}

  public VehicleImpl(
      ImmutableVehicleState initialState,
      String identifier,
      ImmutableVehicleProperties properties) {
    super(initialState, identifier, properties);
  }

  @Override
  protected void handleAcceptDeliveryRoute(ImmutableDeliveryRoute immutableDeliveryRoute,
      LongSimTime elapsedTime) {
    double totalQuantity = immutableDeliveryRoute.getDeliveries().stream()
        .mapToDouble(ImmutableDelivery::getProductAmount)
        .sum();
    if (totalQuantity > properties.getCapacity()) {
      simulator.getContext().getLog().error("Delivery route for Vehcie {} with capacity  {} exceeds vehicle capcity of {}",
          properties.getVehicleId(), totalQuantity, properties.getCapacity());
      // Return the extra load to the manufacturer
      ImmutableDelivery returnDelivery = ImmutableDelivery.builder()
        .retailerId(0)
        .retailerLocation(new Coordinate(0.0, 0.0).toImmutable())
        .productAmount(totalQuantity - properties.getCapacity())
        .build();
      modelState.getSchedule().scheduleOutput(modelState.getCurrentTime(), Vehicle.dropDelivery, returnDelivery);  
      // Reset the daily cost
      modelState.setDailyKmTraveled(0.0);
      
    }
    DeliveryRoute deliveryRoute = immutableDeliveryRoute.toMutable();
    modelState.setDeliveryRoute(deliveryRoute);
    scheduleNextDelivery();
  }

  @Override
  public void internalStateTransitionFunction() {
    LongSimTime currentTime = modelState.getCurrentTime().plus(timeAdvanceFunction());
    modelState.setCurrentTime(currentTime);
    modelState.getSchedule().removeCurrentScheduledOutput(currentTime);
    for (Object event: modelState.getSchedule().removeCurrentScheduledEvents(currentTime)) {
      if (event instanceof DeliveryEvent deliveryEvent) {
        double distance = Distance.distanceBetween(modelState.getLocation().toImmutable(),
            deliveryEvent.delivery().getRetailerLocation());
        modelState.setDailyKmTraveled(modelState.getDailyKmTraveled() + distance);
        modelState.setLocation(deliveryEvent.delivery().getRetailerLocation().toMutable());
        scheduleNextDelivery();
      } else  if (event instanceof ReturnToManufacturerEvent) {
        double distance = Distance.distanceBetween(modelState.getLocation().toImmutable(),
            new ImmutableCoordinate(0.0, 0.0));
        modelState.setDailyKmTraveled(modelState.getDailyKmTraveled() + distance);
        modelState.setLocation(new Coordinate(0.0, 0.0));
        double dailyCost = modelState.getDailyKmTraveled() * properties.getCostPerKm();
        int day = (int)(TimeUtils.simTimeToDuration(currentTime).toDaysPart() + 1);
        ImmutableVehicleCost immutableVehicleCost = ImmutableVehicleCost.builder()
          .vehicleId(properties.getVehicleId())
          .day(day)
          .cost(dailyCost)
          .build();
        modelState.getSchedule().scheduleOutput(currentTime, Vehicle.dailyDeliveryCost, immutableVehicleCost);
      } else {
        throw new IllegalStateException("Unexpected event type: " + event.getClass());
      }
    }
  }

  @Override
  public void confluentStateTransitionFunction(List<PortValue<?>> inputs) {
    internalStateTransitionFunction();
    externalStateTransitionFunction(LongSimTime.create(0), inputs);
  }

  protected void scheduleReturnToManufacturer(LongSimTime currentTime) {
      // Compute delivery time
      double distance = Distance.distanceBetween(modelState.getLocation().toImmutable(),
            new ImmutableCoordinate(0.0, 0.0));
      // Current time plus delivery duration plus travel time to next delivery
      long arrival = currentTime.getT() + TimeUtils.MINUTES_PER_DELIVERY + (long)(distance / (properties.getSpeedKmHr() / 60.0));
      modelState.getSchedule().scheduleInternalEvent(LongSimTime.create(arrival), new ReturnToManufacturerEvent());
  }

  protected void scheduleNextDelivery() {
    LongSimTime currentTime = modelState.getCurrentTime();
    DeliveryRoute deliveryRoute = modelState.getDeliveryRoute();
    Schedule<LongSimTime> schedule = modelState.getSchedule();
    if (!deliveryRoute.getDeliveries().isEmpty()) { // Schedule the first delivery
      ImmutableDelivery nextDelivery = modelState.getDeliveryRoute().getDeliveries().removeFirst().toImmutable();
      // Compute delivery time
      double distance = Distance.distanceBetween(modelState.getLocation().toImmutable(),
            nextDelivery.getRetailerLocation());
      // Current time plus delivery duration plus travel time to next delivery
      long arrival = currentTime.getT() + TimeUtils.MINUTES_PER_DELIVERY + (long)(distance / (properties.getSpeedKmHr() / 60.0));
      Duration arrivalDuration = Duration.ofMinutes(arrival);
      LongSimTime arrivalTime = TimeUtils.durationToSimTime(arrivalDuration);
      // See if arrival is after closing time
      if (arrivalDuration.toHoursPart() < TimeUtils.CLOSING_HOUR) {
        schedule.scheduleOutput(arrivalTime, Vehicle.dropDelivery, nextDelivery);
        schedule.scheduleInternalEvent(arrivalTime, new DeliveryEvent(nextDelivery));
      } else {
        scheduleReturnToManufacturer(currentTime);
      }
    } else { // if the route is empty, report zero delivery cost
      scheduleReturnToManufacturer(currentTime);
    }
  }
}
