package iso.example.irpsystem.irpmodel.impl;

import devs.CoupledModelFactory;
import devs.OutputCouplingHandler;
import devs.PDevsCouplings;
import devs.Port;
import devs.RootCoordinator;
import devs.SimulatorProvider;
import devs.experimentalframe.Acceptor;
import devs.experimentalframe.Generator;
import devs.iso.DevsMessage;
import devs.iso.PortValue;
import devs.iso.SimulationInit;
import devs.iso.time.LongSimTime;
import devs.msg.mutability.ImmutablePort;
import devs.msg.state.ScheduleState;
import devs.msg.state.TimeState;
import devs.utils.Schedule;
import iso.example.irpsystem.irpdomain.Coordinate;
import iso.example.irpsystem.irpdomain.CostType;
import iso.example.irpsystem.irpdomain.Delivery;
import iso.example.irpsystem.irpdomain.DeliveryRoute;
import iso.example.irpsystem.irpdomain.DeliverySchedule;
import iso.example.irpsystem.irpdomain.ImmutableCoordinate;
import iso.example.irpsystem.irpdomain.ImmutableDelivery;
import iso.example.irpsystem.irpdomain.ImmutableDeliveryRoute;
import iso.example.irpsystem.irpdomain.ImmutableDeliverySchedule;
import iso.example.irpsystem.irpdomain.ImmutableVehicleCost;
import iso.example.irpsystem.irpdomain.VehicleCost;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableVehicleProperties;
import iso.example.irpsystem.irpmodel.InventoryRouting.Vehicle;
import iso.example.irpsystem.irpmodel.InventoryRouting.VehicleState;
import iso.example.irpsystem.irpmodel.algorithms.IrpReaderTest;
import iso.example.irpsystem.irpmodel.impl.IrpData;
import iso.example.irpsystem.irpmodel.impl.IrpData.RetailerData;
import iso.example.irpsystem.irpmodel.impl.VehicleImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit;
import org.apache.pekko.actor.testkit.typed.javadsl.TestProbe;
import org.apache.pekko.actor.typed.ActorRef;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VehicleImplTestOriginal {

  protected final CoupledModelFactory<LongSimTime> coupledModelFactory;
  protected IrpData irpData = IrpReaderTest.irpData;

  protected ImmutableDeliverySchedule buildDeliverySchedule() {
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

  class TestGenerator extends Generator<LongSimTime> {

    public static String modelIdentifier = "vehicleTestGenerator";

    public static IrpData irpData = IrpReaderTest.irpData;

    public static ImmutablePort<ImmutableDeliveryRoute> toAcceptDeliveryRoute =
        new ImmutablePort<>("toAcceptDeliveryRoute", ImmutableDeliveryRoute.class);

    protected TestGenerator(ScheduleState<LongSimTime> scheduleState) {
      super(modelIdentifier, scheduleState);
    }

    @Override
    public void handleScheduledEvents(List<Object> events) {
      // No internal events to handle
    }
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


  class VehicleTestAcceptor extends Acceptor<LongSimTime, VehicleAcceptorState> {

    public static String modelIdentifier = "vehicleTestAcceptor";
    public static ImmutablePort<ImmutableDelivery> fromDropDelivery =
        new ImmutablePort<>("fromDropDelivery", ImmutableDelivery.class);
    public static ImmutablePort<ImmutableVehicleCost> fromDailyDeliveryCost =
        new ImmutablePort<>("fromDailyDeliveryCost", ImmutableVehicleCost.class);


    public VehicleTestAcceptor(VehicleAcceptorState initialState) {
      super(initialState, modelIdentifier);
    }

    @Override
    public void internalStateTransitionFunction() {

    }

    @Override
    public void externalStateTransitionFunction(LongSimTime elapsedTime,
        List<PortValue<?>> inputs) {
      modelState.setCurrentTime(modelState.getCurrentTime().plus(elapsedTime));
      // Implement assertions to validate output from schedule
      for (PortValue<?> pv: inputs) {
        if (pv.getPortName().equals(fromDropDelivery.getPortName())) {
          handleDelivery(elapsedTime, fromDropDelivery.getValue(pv));
        } else if (pv.getPortName().equals(fromDailyDeliveryCost.getPortName())) {
          handleDailyCost(elapsedTime, fromDailyDeliveryCost.getValue(pv));
        }
      }
    }

    @Override
    public LongSimTime timeAdvanceFunction() {
      return LongSimTime.create(Long.MAX_VALUE);
    }

    protected void handleDelivery(LongSimTime elapsedTime, ImmutableDelivery delivery) {
      ImmutableDelivery expectedDelivery = modelState.findMatchingDelivery(elapsedTime, delivery);
      assert delivery.equals(expectedDelivery);
      Integer day = elapsedTime.getT() < 1000 ? 1 : 2;
      modelState.deliveriesReceived.get(day).add(delivery);
    }

    protected void handleDailyCost(LongSimTime currentTime, ImmutableVehicleCost vehicleCost) {
      modelState.totalVehicleCost = modelState.totalVehicleCost + vehicleCost.getCost();
      Integer day = currentTime.getT() < 1000 ? 1 : 2;
      if (day == 2) {
        assert modelState.deliveriesReceived.get(1).size() == 2;
        assert modelState.deliveriesReceived.get(2).size() == 2;
        assertEquals(3441.0, modelState.totalVehicleCost, 1.0);
        System.out.println("Vehicle test completed successfully");
      }
    }
  }

  public static class VehicleImplTestOutputCouplingHandler extends OutputCouplingHandler {

    public VehicleImplTestOutputCouplingHandler() {
      super(Optional.empty(), Optional.empty(), Optional.empty());
    }

    @Override
    public void handlePortValue(String sender, PortValue<?> portValue,
        Map<String, List<PortValue<?>>> receiverMap, List<PortValue<?>> outputMessages) {
      // Implement assertions to validate output from model
      if (sender.equals(TestGenerator.modelIdentifier)) {
        if (portValue.getPortName().equals(TestGenerator.toAcceptDeliveryRoute.getPortName())) {
          ImmutableDeliveryRoute immutableDeliveryRoute = TestGenerator.toAcceptDeliveryRoute.getValue(portValue);
          PortValue<?> pv = VehicleImpl.acceptDeliveryRoute.createPortValue(immutableDeliveryRoute);
          addInputPortValue(pv, Vehicle.modelIdentifier, receiverMap);
        } else {
          throw new IllegalArgumentException(
              "Did not expect an input from VehicleTestGenerator with port name "
                  + portValue.getPortName());
        }
      } else if (sender.equals(Vehicle.modelIdentifier)) {
        if (portValue.getPortName().equals(Vehicle.dropDelivery.getPortName())) {
          ImmutableDelivery immutableDelivery = Vehicle.dropDelivery.getValue(portValue);
          PortValue<?> pv = VehicleTestAcceptor.fromDropDelivery.createPortValue(immutableDelivery);
          addInputPortValue(pv, VehicleTestAcceptor.modelIdentifier, receiverMap);
        } else if (portValue.getPortName().equals(Vehicle.dailyDeliveryCost.getPortName())) {
          ImmutableVehicleCost dailyDeliveryCost = Vehicle.dailyDeliveryCost.getValue(portValue);
          PortValue<?> pv = VehicleTestAcceptor.fromDailyDeliveryCost.createPortValue(dailyDeliveryCost);
          addInputPortValue(pv, VehicleTestAcceptor.modelIdentifier, receiverMap);
        } else {
          throw new IllegalArgumentException(
              "Did not expect an input from VehicleAcceptor with port name "
                  + portValue.getPortName());
        }
      } else {
        throw new IllegalArgumentException(
            "Did not expect an input from model with identifier " + sender);
      }
    }
  }

  Schedule<LongSimTime> buildGeneratorSchedule() {
    ImmutableDeliverySchedule deliverySchedule = buildDeliverySchedule();
    ImmutableDeliveryRoute dayOneRoute = deliverySchedule.getDeliveriesByDayByVehicle().get(1).get(1);
    ImmutableDeliveryRoute dayTwoRoute = deliverySchedule.getDeliveriesByDayByVehicle().get(2).get(1);

    Schedule<LongSimTime> schedule = new Schedule<>();
    schedule.scheduleOutput(LongSimTime.create(0), TestGenerator.toAcceptDeliveryRoute, dayOneRoute);
    schedule.scheduleOutput(LongSimTime.create(1000), TestGenerator.toAcceptDeliveryRoute, dayTwoRoute);
    return schedule;
  }




  protected VehicleImpl buildVehicleModel() {
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
        .deliveryRoute(DeliveryRoute.builder().build())
        .dailyKmTraveled(0.0)
        .currentTime(LongSimTime.create(0))
        .schedule(new Schedule<>())
        .build();

    return new VehicleImpl(vehicleState.toImmutable(), Vehicle.modelIdentifier, vehicleProperties) {
      @Override
      public VehicleState getModelState() {
        return vehicleState;
      }
    };
  }

  public VehicleImplTestOriginal() {
    List<SimulatorProvider<LongSimTime>> simulatorProviders = new ArrayList<>();
    ImmutableDeliverySchedule deliverySchedule = buildDeliverySchedule();
    ScheduleState<LongSimTime> scheduleState = new ScheduleState<>(LongSimTime.create(0));
    scheduleState.getSchedule().scheduleOutput(LongSimTime.create(0), TestGenerator.toAcceptDeliveryRoute, deliverySchedule.getDeliveriesByDayByVehicle().get(1).get(1));
    TestGenerator generator = new TestGenerator(scheduleState);
    VehicleAcceptorState acceptorState = new VehicleAcceptorState(LongSimTime.create(0));
    VehicleTestAcceptor acceptor = new VehicleTestAcceptor(acceptorState);

    VehicleImpl vehicleImpl = buildVehicleModel();
    simulatorProviders.add(generator.getDevsSimulatorProvider());
    simulatorProviders.add(vehicleImpl.getDevsSimulatorProvider());
    simulatorProviders.add(acceptor.getDevsSimulatorProvider());
    PDevsCouplings couplings = new PDevsCouplings(Collections.emptyList(),
        Collections.singletonList(new VehicleImplTestOutputCouplingHandler()));
    coupledModelFactory = new CoupledModelFactory<LongSimTime>(
        "vehicleImplTest",
        simulatorProviders,
        couplings);
  }

  //@Test
  //@DisplayName("Test VehicleImpl")
  protected void testVehicleImpl() throws InterruptedException {
    executeExperimentalFrame(LongSimTime.builder().t(0L).build(),
        LongSimTime.builder().t(2000L).build());
  }


  protected void executeExperimentalFrame(LongSimTime startTime, LongSimTime endTime)
      throws InterruptedException {
    ActorTestKit testKit = ActorTestKit.create();
    ActorRef<DevsMessage> testFrame =
        testKit.spawn(coupledModelFactory.create(startTime), "vehicleImplTest");
    ActorRef<DevsMessage> rootCoordinator =
        testKit.spawn(RootCoordinator.create(endTime, testFrame, "vehicleImplTest"), "root");
    rootCoordinator.tell(SimulationInit.<LongSimTime>builder()
        .eventTime(startTime)
        .simulationId("VehicleImplTest")
        .messageId("SimulationInit")
        .senderId("TestActor")
        .receiverId("root")
        .build());
    TestProbe<DevsMessage> testProbe = testKit.createTestProbe();
    testProbe.expectTerminated(rootCoordinator, Duration.ofSeconds(100));
    // Thread.sleep(10 * 1000);
    testKit.shutdownTestKit();
  }
}