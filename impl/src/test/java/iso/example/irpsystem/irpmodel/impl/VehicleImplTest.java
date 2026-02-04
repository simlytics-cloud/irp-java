package iso.example.irpsystem.irpmodel.impl;

import devs.SimulatorProvider;
import devs.iso.PortValue;
import devs.iso.time.LongSimTime;
import devs.msg.state.ScheduleState;
import devs.msg.state.TimeState;
import devs.utils.Schedule;
import iso.example.irpsystem.irpdomain.Coordinate;
import iso.example.irpsystem.irpdomain.DeliveryRoute;
import iso.example.irpsystem.irpdomain.ImmutableCoordinate;
import iso.example.irpsystem.irpdomain.ImmutableDelivery;
import iso.example.irpsystem.irpdomain.ImmutableDeliveryRoute;
import iso.example.irpsystem.irpdomain.ImmutableDeliverySchedule;
import iso.example.irpsystem.irpdomain.ImmutableVehicleCost;
import iso.example.irpsystem.irpmodel.InventoryRouting.AbstractVehicleTest;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableVehicleProperties;
import iso.example.irpsystem.irpmodel.InventoryRouting.Vehicle;
import iso.example.irpsystem.irpmodel.InventoryRouting.VehicleState;
import iso.example.irpsystem.irpmodel.algorithms.TimeUtils;
import iso.example.irpsystem.irpmodel.impl.IrpData;
import iso.example.irpsystem.irpmodel.impl.IrpData.RetailerData;
import iso.example.irpsystem.irpmodel.impl.VehicleImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VehicleImplTest extends AbstractVehicleTest<VehicleImplTest.VehicleAcceptorState> {

  static Path path = Paths.get("src/test/resources/S_abs1n5_2_L3.json");
  static IrpData irpData = IrpData.read(path);

  public static ImmutableDeliverySchedule buildDeliverySchedule() {


    RetailerData retailerData = irpData.retailers().get(1);
    // Deliver to retailers 1 and 2 on first day
    ImmutableDelivery delivery = ImmutableDelivery.builder()
        .retailerId(retailerData.id())
        .retailerLocation(ImmutableCoordinate.builder().x(retailerData.x()).y(retailerData.y()).build())
        .productAmount(retailerData.dailyConsumption())
        .build();
    retailerData = irpData.retailers().get(2);
    ImmutableDelivery delivery2 = ImmutableDelivery.builder()
        .retailerId(retailerData.id())
        .retailerLocation(ImmutableCoordinate.builder().x(retailerData.x()).y(retailerData.y()).build())
        .productAmount(retailerData.dailyConsumption())
        .build();    
    ImmutableDeliveryRoute deliveryRoute1 = ImmutableDeliveryRoute.builder()
        .vehicleId(1)
        .deliveries(List.of(delivery, delivery2))
        .build();
    Map<Integer, ImmutableDeliveryRoute> dayOneDeliveries = Map.of(1, deliveryRoute1);

    // Deliver to retailers 2 and 3 on second day
    retailerData = irpData.retailers().get(2);
    delivery = ImmutableDelivery.builder()
        .retailerId(retailerData.id())
        .retailerLocation(ImmutableCoordinate.builder().x(retailerData.x()).y(retailerData.y()).build())
        .productAmount(retailerData.dailyConsumption())
        .build();
    retailerData = irpData.retailers().get(3);
    delivery2 = ImmutableDelivery.builder()
        .retailerId(retailerData.id())
        .retailerLocation(ImmutableCoordinate.builder().x(retailerData.x()).y(retailerData.y()).build())
        .productAmount(retailerData.dailyConsumption())
        .build();    
    ImmutableDeliveryRoute deliveryRoute2 = ImmutableDeliveryRoute.builder()
        .vehicleId(1)
        .deliveries(List.of(delivery, delivery2))
        .build();
    Map<Integer, ImmutableDeliveryRoute> dayTwoDeliveries = Map.of(1, deliveryRoute2);
    Map<Integer, Map<Integer, ImmutableDeliveryRoute>> deliveriesByDayByVehicle = Map.of(
      1, dayOneDeliveries,
      2, dayTwoDeliveries
    );
    return ImmutableDeliverySchedule.builder()
      .deliveriesByDayByVehicle(deliveriesByDayByVehicle)
      .build();
  }

  class VehicleAcceptorState extends TimeState<LongSimTime> {

    public VehicleAcceptorState(LongSimTime currentTime) {
      super(currentTime);
    }

    ImmutableDeliverySchedule deliverySchedule = buildDeliverySchedule();
    Map<Integer, List<ImmutableDelivery>> deliveriesReceived = new HashMap<>(
      Map.of(1, new ArrayList<>(), 2, new ArrayList<>())
    );
    double totalVehicleCost = 0.0;

    protected ImmutableDelivery findMatchingDelivery(LongSimTime currentTime, ImmutableDelivery delivery) {
      Integer day = currentTime.getT() < 1000 ? 1 : 2;
      ImmutableDeliveryRoute deliveryRoute = deliverySchedule.getDeliveriesByDayByVehicle().get(day).get(1);
      return deliveryRoute.getDeliveries().stream().filter(d -> d.getRetailerId().equals(delivery.getRetailerId())).findFirst().get();
    }
  }


  @Test
  @DisplayName("Test VehicleImpl")
  protected void testVehicleImpl() throws InterruptedException {
    executeExperimentalFrame(LongSimTime.create(0),
        TimeUtils.durationToSimTime(Duration.ofDays(2)), "VehicleTest");
  }

  @Override
  protected ScheduleState<LongSimTime> buildGeneratorState() {
    ScheduleState<LongSimTime> scheduleState = new ScheduleState<>(LongSimTime.create(0));
    ImmutableDeliverySchedule deliverySchedule = buildDeliverySchedule();
    ImmutableDeliveryRoute dayOneRoute = deliverySchedule.getDeliveriesByDayByVehicle().get(1).get(1);
    ImmutableDeliveryRoute dayTwoRoute = deliverySchedule.getDeliveriesByDayByVehicle().get(2).get(1);

    // Schedule day 1 route at 6AM
    LongSimTime day1RouteTime = TimeUtils.durationToSimTime(TimeUtils.OPENING_DURATION);
    scheduleState.getSchedule().scheduleOutput(day1RouteTime, TestGenerator.toAcceptDeliveryRoute, dayOneRoute);

    // Schedule day 2 route 24 hours later
    LongSimTime day2RouteTime = day1RouteTime.plus(TimeUtils.durationToSimTime(Duration.ofDays(1)));
    scheduleState.getSchedule().scheduleOutput(day2RouteTime, TestGenerator.toAcceptDeliveryRoute, dayTwoRoute);
    return scheduleState;
  }

  @Override
  protected VehicleAcceptorState buildAcceptorState() {
    return new VehicleAcceptorState(LongSimTime.create(0));
  }

  @Override
  public void handleAcceptorInput(LongSimTime elapsedTime, VehicleAcceptorState acceptorState,
        List<PortValue<?>> inputs) {
      LongSimTime currentTime = acceptorState.getCurrentTime().plus(elapsedTime);
      acceptorState.setCurrentTime(currentTime);
      int day = (int)TimeUtils.simTimeToDuration(currentTime).toDaysPart() + 1;
      for (PortValue<?> portValue: inputs) {  
        if (portValue.getPortName().equals(AbstractVehicleTest.TestAcceptor.fromDropDelivery.getPortName())) {
          ImmutableDelivery immutableDelivery = AbstractVehicleTest.TestAcceptor.fromDropDelivery.getValue(portValue);
          ImmutableDelivery expectedDelivery = acceptorState.findMatchingDelivery(currentTime, immutableDelivery);
          assert immutableDelivery.equals(expectedDelivery);
          acceptorState.deliveriesReceived.get(day).add(immutableDelivery);          
        } else if (portValue.getPortName().equals(AbstractVehicleTest.TestAcceptor.fromDailyDeliveryCost.getPortName())) {
          ImmutableVehicleCost vehicleCost = AbstractVehicleTest.TestAcceptor.fromDailyDeliveryCost.getValue(portValue);
          acceptorState.totalVehicleCost = acceptorState.totalVehicleCost + vehicleCost.getCost();
          if (day == 2) {
            assert acceptorState.deliveriesReceived.get(1).size() == 2;
            assert acceptorState.deliveriesReceived.get(2).size() == 2;
            assertEquals(3441.0, acceptorState.totalVehicleCost, 1.0);
            System.out.println("Vehicle test completed successfully");
          }
        }
      }
  }

  @Override
  protected SimulatorProvider<LongSimTime> buildDevsModelProvider() {

    ImmutableVehicleProperties vehicleProperties = ImmutableVehicleProperties.builder()
        .vehicleId(0)
        .capacity(irpData.vehicleCapacity())
        .costPerKm(irpData.vehicleCostPerKm())
        .speedKmHr(irpData.vehicleSpeekKmHr())
        .build();
    VehicleState vehicleState = VehicleState.builder()
        .location(Coordinate.builder()
            .x(0.0)
            .y(0.0)
            .build())
        .deliveryRoute(DeliveryRoute.builder().vehicleId(1).build())
        .dailyKmTraveled(0.0)
        .currentTime(LongSimTime.create(0))
        .schedule(new Schedule<>())
        .build();

    VehicleImpl vehicleImpl = new VehicleImpl(vehicleState.toImmutable(), Vehicle.modelIdentifier, vehicleProperties);
    return vehicleImpl.getDevsSimulatorProvider();
  }



}
