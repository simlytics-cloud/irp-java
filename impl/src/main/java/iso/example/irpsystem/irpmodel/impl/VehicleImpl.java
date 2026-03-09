package iso.example.irpsystem.irpmodel.impl;

import devs.iso.PortValue;
import devs.iso.time.LongSimTime;
import devs.utils.Schedule;
import devs.utils.Schedule.ScheduledEvent;
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
import iso.example.irpsystem.irpmodel.InventoryRouting.VehicleState;
import iso.example.irpsystem.irpmodel.algorithms.Distance;
import iso.example.irpsystem.irpmodel.algorithms.TimeUtils;

import java.time.Duration;
import java.util.List;
import devs.PDevsCoordinator;
import devs.PDevsSimulator;

/**
 * Implementation of a Vehicle in the Inventory Routing Problem (IRP) simulation.
 * A vehicle is responsible for delivering products from a manufacturer to various retailers
 * following a prescribed delivery route.
 */
public class VehicleImpl extends Vehicle<ImmutableVehicleProperties, VehicleState, ImmutableVehicleState> {
  /**
   * Event representing a delivery to a retailer.
   *
   * @param delivery The delivery details, including retailer and amount.
   */
  public record DeliveryEvent(ImmutableDelivery delivery) {
  }

  /**
   * Event representing the vehicle returning to the manufacturer.
   */
  public record ReturnToManufacturerEvent() {

  }

  /**
   * Constructs a new VehicleImpl.
   *
   * @param initialState The initial state of the vehicle.
   * @param identifier   The unique identifier for this vehicle model.
   * @param properties   The static properties of the vehicle (capacity, speed, etc.).
   */
  public VehicleImpl(
      ImmutableVehicleState initialState,
      String identifier,
      ImmutableVehicleProperties properties) {
    super(initialState, identifier, properties);
  }

  /**
   * Handles the acceptance of a new delivery route.
   * Validates the route against vehicle capacity and schedules the first delivery.
   *
   * @param immutableDeliveryRoute The new delivery route to follow.
   * @param elapsedTime            The time elapsed since the last state transition.
   */
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
        .retailerLocation(properties.getManufacturerLocation())
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

  /**
   * Processes internal scheduled events such as reaching a delivery location or the manufacturer.
   *
   * @param events The list of events to process.
   */
  @Override
  public void handleScheduledEvents(List<Object> events) {
    for (Object event: events) {
      if (event instanceof DeliveryEvent deliveryEvent) {
        double distance = Distance.distanceBetween(modelState.getLocation().toImmutable(),
            deliveryEvent.delivery().getRetailerLocation());
        modelState.setDailyKmTraveled(modelState.getDailyKmTraveled() + distance);
        modelState.setLocation(deliveryEvent.delivery().getRetailerLocation().toMutable());
        scheduleNextDelivery();
      } else  if (event instanceof ReturnToManufacturerEvent) {
        double distance = Distance.distanceBetween(modelState.getLocation().toImmutable(),
            properties.getManufacturerLocation());
        modelState.setDailyKmTraveled(modelState.getDailyKmTraveled() + distance);
        modelState.setLocation(properties.getManufacturerLocation().toMutable());
        double dailyCost = modelState.getDailyKmTraveled() * properties.getCostPerKm();
        int day = (int)(TimeUtils.simTimeToDuration(modelState.getCurrentTime()).toDaysPart() + 1);
        ImmutableVehicleCost immutableVehicleCost = ImmutableVehicleCost.builder()
          .vehicleId(properties.getVehicleId())
          .day(day)
          .cost(dailyCost)
          .build();
        modelState.getSchedule().scheduleOutput(modelState.getCurrentTime(), Vehicle.dailyDeliveryCost, immutableVehicleCost);
        modelState.setDailyKmTraveled(0.0);
      } else {
        throw new IllegalStateException("Unexpected event type: " + event.getClass());
      }
    }
  }

  /**
   * Handles simultaneous internal and external transitions.
   * In this implementation, it executes the internal transition followed by the external transition.
   *
   * @param inputs The list of port values received as input.
   */
  @Override
  public void confluentStateTransitionFunction(List<PortValue<?>> inputs) {
    internalStateTransitionFunction();
    externalStateTransitionFunction(LongSimTime.create(0), inputs);
  }

  /**
   * Schedules an internal event for the vehicle to return to the manufacturer location.
   * Calculates the arrival time based on current location, distance, and vehicle speed.
   *
   * @param currentTime The current simulation time.
   */
  protected void scheduleReturnToManufacturer(LongSimTime currentTime) {
      // Compute delivery time
      double distance = Distance.distanceBetween(modelState.getLocation().toImmutable(),
            properties.getManufacturerLocation());
      // Current time plus delivery duration plus travel time to next delivery
      long arrival = currentTime.getT() + TimeUtils.MINUTES_PER_DELIVERY + (long)(distance / (properties.getSpeedKmHr() / 60.0));
      modelState.getSchedule().scheduleInternalEvent(LongSimTime.create(arrival),
          new ReturnToManufacturerEvent());
  }

  /**
   * Schedules the next delivery in the current delivery route.
   * If the route is empty or the next delivery would occur after closing time, 
   * it schedules a return to the manufacturer.
   */
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
