package iso.example.irpsystem.irpmodel.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import devs.msg.state.ScheduleState;
import devs.msg.state.TimeState;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import devs.SimulatorProvider;
import devs.iso.PortValue;
import devs.iso.time.LongSimTime;
import devs.utils.ImmutableSchedule;
import devs.utils.Schedule;
import iso.example.irpsystem.irpdomain.ImmutableCoordinate;
import iso.example.irpsystem.irpdomain.ImmutableDelivery;
import iso.example.irpsystem.irpdomain.ImmutableDeliveryRoute;
import iso.example.irpsystem.irpdomain.ImmutableDeliverySchedule;
import iso.example.irpsystem.irpdomain.ImmutableInventoryCost;
import iso.example.irpsystem.irpmodel.InventoryRouting.AbstractRetailerTest;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableFacilityProps;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableRetailerProperties;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableRetailerState;
import iso.example.irpsystem.irpmodel.InventoryRouting.Retailer;
import iso.example.irpsystem.irpmodel.algorithms.IrpReaderTest;
import iso.example.irpsystem.irpmodel.algorithms.TimeUtils;
import iso.example.irpsystem.irpmodel.impl.IrpData;
import iso.example.irpsystem.irpmodel.impl.RetailerImpl;
import iso.example.irpsystem.irpmodel.impl.IrpData.RetailerData;

public class RetailerImplTest extends AbstractRetailerTest<RetailerImplTest.RetailerAcceptorState> {

    static IrpData irpData = IrpData.read(IrpReaderTest.path);

    @Override
    protected ScheduleState<LongSimTime> buildGeneratorState() {
        ImmutableDeliverySchedule deliverySchedule = buildDeliverySchedule();
        ImmutableDelivery dayOneDelivery = deliverySchedule.getDeliveriesByDayByVehicle()
            .get(1).get(1).getDeliveries().getFirst();
        ImmutableDelivery dayTwoDelivery = deliverySchedule.getDeliveriesByDayByVehicle()
            .get(2).get(1).getDeliveries().getFirst(); 
            
        Schedule<LongSimTime> schedule = new Schedule<>();
        Duration day1Start = TimeUtils.OPENING_DURATION;
        schedule.scheduleOutput(TimeUtils.durationToSimTime(day1Start), 
            AbstractRetailerTest.TestGenerator.toReceiveDelivery, dayOneDelivery);
        Duration day2Start = day1Start.plus(Duration.ofDays(1));
        schedule.scheduleOutput(TimeUtils.durationToSimTime(day2Start), 
            AbstractRetailerTest.TestGenerator.toReceiveDelivery, dayTwoDelivery);
        return new ScheduleState<>(LongSimTime.create(0), schedule);
    }


    protected ImmutableDeliverySchedule buildDeliverySchedule() {
        RetailerData retailerData = irpData.retailers().get(1);
        // Schedue delivery of daily consumption for first day and half that size for second day
        ImmutableDelivery delivery = ImmutableDelivery.builder()
            .retailerId(retailerData.id())
            .retailerLocation(ImmutableCoordinate.builder().x(retailerData.x()).y(retailerData.y()).build())
            .productAmount(retailerData.dailyConsumption())
            .build();
        ImmutableDeliveryRoute deliveryRoute1 = ImmutableDeliveryRoute.builder()
            .vehicleId(1)
            .deliveries(List.of(delivery))
            .build();
        Map<Integer, ImmutableDeliveryRoute> dayOneDeliveries = Map.of(1, deliveryRoute1);

        delivery = ImmutableDelivery.builder()
            .retailerId(retailerData.id())
            .retailerLocation(ImmutableCoordinate.builder().x(retailerData.x()).y(retailerData.y()).build())
            .productAmount(retailerData.dailyConsumption() / 2.0)
            .build();
        retailerData = irpData.retailers().get(3);
        ImmutableDeliveryRoute deliveryRoute2 = ImmutableDeliveryRoute.builder()
            .vehicleId(1)
            .deliveries(List.of(delivery, delivery))
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

    static class RetailerAcceptorState extends TimeState<LongSimTime> {

        public RetailerAcceptorState(LongSimTime currentTime) {
            super(currentTime);
        }

        protected boolean completedDay1 = false;
        
    }

    @Override
    protected RetailerAcceptorState buildAcceptorState() {
        return new RetailerAcceptorState(LongSimTime.create(0));
    }

    @Override
    public void handleAcceptorInput(LongSimTime elapsedTime, RetailerAcceptorState acceptorState, List<PortValue<?>> inputs) {
        LongSimTime currentTime = acceptorState.getCurrentTime().plus(elapsedTime);
        acceptorState.setCurrentTime(currentTime);
        for (PortValue<?> portValue: inputs) {
            if (portValue.getPortName().equals(AbstractRetailerTest.TestAcceptor.fromDailyInventoryCost.getPortName())) {
                ImmutableInventoryCost immutableInventoryCost 
                    = AbstractRetailerTest.TestAcceptor.fromDailyInventoryCost.getValue(portValue);
                if (TimeUtils.simTimeToDuration(currentTime).toDaysPart() == 0) {
                    assert immutableInventoryCost.getDay() == 1;
                    assertEquals(2.1, immutableInventoryCost.getCost(), 0.01);
                    acceptorState.completedDay1 = true;
                } else {
                    assert immutableInventoryCost.getDay() == 2;
                    assertEquals(1.575, immutableInventoryCost.getCost(), 0.01);
                    if (!acceptorState.completedDay1) {
                        fail("Did not complete Day 1 cost accounting");
                    }
                    System.out.println("Retailer test completed successfully");
                }
            } else {
                throw new IllegalArgumentException("AbstractRetailerTest.TestAcceptpr dif not expect port value with name " 
                    + portValue.getPortName());
            }
        }

    }

    @Override
    protected SimulatorProvider buildDevsModelProvider() {
        RetailerData retailerData = irpData.retailers().get(1);
        ImmutableRetailerState retailerState = ImmutableRetailerState.builder()
            .currentInventory(retailerData.startingInventory())
            .currentTime(LongSimTime.create(0))
            .schedule(new ImmutableSchedule(new TreeMap<>()))
            .build();
        ImmutableRetailerProperties properties = ImmutableRetailerProperties.builder()
            .retailerId(retailerData.id())
            .dailyConsumption(retailerData.dailyConsumption())
            .facilityProperties(ImmutableFacilityProps.builder()
                .coordinate(new ImmutableCoordinate(retailerData.x(), retailerData.y()))
                .inventoryCost(retailerData.inventoryCost())
                .startingInventory(retailerData.startingInventory())
                .build())
            .minInventory(retailerData.minInventory())
            .maxInventory(retailerData.maxInventory())
            .build();
        RetailerImpl retailerImpl = new RetailerImpl(retailerState, Retailer.modelIdentifier, properties);
        return retailerImpl.getDevsSimulatorProvider();
    }



  @Test
  @DisplayName("Test RetailerImpl")
  protected void testRetailerImpl() throws InterruptedException {
    executeExperimentalFrame(LongSimTime.create(0),
        TimeUtils.durationToSimTime(Duration.ofDays(2)), "RetailerTest");
  }

}
