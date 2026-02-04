package iso.example.irpsystem.irpmodel.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import devs.SimulatorProvider;
import devs.iso.PortValue;
import devs.iso.time.LongSimTime;
import devs.msg.state.ScheduleState;
import devs.msg.state.TimeState;
import devs.utils.ImmutableSchedule;
import devs.utils.Schedule;
import iso.example.irpsystem.irpdomain.DeliveryRoute;
import iso.example.irpsystem.irpdomain.ImmutableCoordinate;
import iso.example.irpsystem.irpdomain.ImmutableDelivery;
import iso.example.irpsystem.irpdomain.ImmutableDeliveryRoute;
import iso.example.irpsystem.irpdomain.ImmutableDeliverySchedule;
import iso.example.irpsystem.irpdomain.ImmutableInventoryCost;
import iso.example.irpsystem.irpmodel.InventoryRouting.AbstractManufacturerTest;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableFacilityProps;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableManufacturerProperties;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableManufacturerState;
import iso.example.irpsystem.irpmodel.InventoryRouting.Manufacturer;
import iso.example.irpsystem.irpmodel.algorithms.IrpReaderTest;
import iso.example.irpsystem.irpmodel.algorithms.TimeUtils;

public class ManufacturerImplTest extends AbstractManufacturerTest<ManufacturerImplTest.ManufacturerAcceptoState>{

    static IrpData irpData = IrpData.read(IrpReaderTest.path);
    @Override
    protected ScheduleState buildGeneratorState() {
        ImmutableDeliverySchedule immutableDeliverySchedule = VehicleImplTest.buildDeliverySchedule();
        Schedule<LongSimTime> schedule = new Schedule<>();
        // Post the delivery schedule at time 0
        schedule.scheduleOutput(LongSimTime.create(0), TestGenerator.toAcceptDeliverySchedule, immutableDeliverySchedule);
        // Return stock to manufacturer at 5pm on first day
        ImmutableDelivery delivery = ImmutableDelivery.builder()
            .retailerId(0)
            .retailerLocation(new ImmutableCoordinate(0.0, 0.0))
            .productAmount(100.0)
            .build();
        schedule.scheduleOutput(TimeUtils.durationToSimTime(Duration.ofHours(17)), TestGenerator.toAcceptDelivery, delivery);
        return new ScheduleState<>(LongSimTime.create(0), schedule);
    }

    class ManufacturerAcceptoState extends TimeState<LongSimTime> {

        Map<Integer, List<ImmutableDeliveryRoute>> deliveriesRoutesReceived = new HashMap<>(
            Map.of(1, new ArrayList<>(), 2, new ArrayList<>()));
        boolean day1CostReceived = false;
    

        public ManufacturerAcceptoState(LongSimTime currentTime) {
            super(currentTime);
        }
        
    }

    @Override
    protected ManufacturerAcceptoState buildAcceptorState() {
        return new ManufacturerAcceptoState(LongSimTime.create(0));
    }

    @Override
    protected SimulatorProvider<LongSimTime> buildDevsModelProvider() {
        ImmutableManufacturerProperties properties = ImmutableManufacturerProperties.builder()
            .dailyProduction(irpData.manufacturer().dailyProduction())
            .facilityProperties(ImmutableFacilityProps.builder()
                .coordinate(new ImmutableCoordinate(0.0, 0.0))
                .startingInventory(irpData.manufacturer().startingInventory())
                .inventoryCost(irpData.manufacturer().inventoryCost())
                .build())
            .build();
        ImmutableManufacturerState initialState = ImmutableManufacturerState.builder()
            .currentInventory(irpData.manufacturer().startingInventory())
            .currentTime(LongSimTime.create(0))
            .schedule(new ImmutableSchedule<>(new TreeMap<>()))
            .build();
        ManufacturerImpl manufacturerImpl = new ManufacturerImpl(initialState, properties);
        return manufacturerImpl.getDevsSimulatorProvider();
    }

    @Override
    public void handleAcceptorInput(LongSimTime elapsedTime, ManufacturerAcceptoState acceptorState,
            List<PortValue<?>> portValues) {
        LongSimTime currentTime = acceptorState.getCurrentTime().plus(elapsedTime);
        acceptorState.setCurrentTime(currentTime);                
        int day = (int) TimeUtils.simTimeToDuration(acceptorState.getCurrentTime()).toDaysPart() + 1;
        for (PortValue<?> portValue: portValues) {
            if (portValue.getPortName().equals(TestAcceptor.fromPostDeliveryRoute.getPortName())) {
                ImmutableDeliveryRoute deliveryRoute = TestAcceptor.fromPostDeliveryRoute.getValue(portValue);
                // Expect one deliveries on day 1 and day 2
                acceptorState.deliveriesRoutesReceived.get(day).add(deliveryRoute);
            } else if (portValue.getPortName().equals(TestAcceptor.fromDailyInventoryCost.getPortName())) {
                ImmutableInventoryCost inventoryCost = TestAcceptor.fromDailyInventoryCost.getValue(portValue);
                if (day == 1) {
                    acceptorState.day1CostReceived = true;
                    assertEquals(21.3, inventoryCost.getCost(), 0.01);
                } else if (day == 2) {
                    assert acceptorState.day1CostReceived == true;
                    // Check for two delivery routes posted each day
                    assert acceptorState.deliveriesRoutesReceived.get(1).size() == 1;
                    assert acceptorState.deliveriesRoutesReceived.get(2).size() == 1;
                    assertEquals(24.63, inventoryCost.getCost(), 0.01);
                    System.out.println("ManufacturerImplTest succeeded!");
                } else {
                    fail("Received input at " + currentTime + "with unexpected day " + day);
                }
            } else {
                throw new IllegalArgumentException("Acceptor did not expect port value with name "
                    + portValue.getPortName());
            }
        }
    }

  @Test
  @DisplayName("Test ManufacturerImpl")
  protected void testManufacturerImpl() throws InterruptedException {
    executeExperimentalFrame(LongSimTime.create(0),
        TimeUtils.durationToSimTime(Duration.ofDays(2)), "ManufacturerTest");
  }

}
