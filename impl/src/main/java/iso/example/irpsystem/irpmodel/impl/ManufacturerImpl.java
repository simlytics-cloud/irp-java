package iso.example.irpsystem.irpmodel.impl;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import devs.iso.PortValue;
import devs.iso.time.LongSimTime;
import iso.example.irpsystem.irpdomain.DeliveryRoute;
import iso.example.irpsystem.irpdomain.ImmutableDelivery;
import iso.example.irpsystem.irpdomain.ImmutableDeliveryRoute;
import iso.example.irpsystem.irpdomain.ImmutableDeliverySchedule;
import iso.example.irpsystem.irpdomain.ImmutableInventoryCost;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableManufacturerProperties;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableManufacturerState;
import iso.example.irpsystem.irpmodel.InventoryRouting.Manufacturer;
import iso.example.irpsystem.irpmodel.InventoryRouting.Retailer;
import iso.example.irpsystem.irpmodel.algorithms.TimeUtils;

public class ManufacturerImpl extends Manufacturer {

    protected record UpdateInventory() {}
    protected record PostDelveryRoute(ImmutableDeliveryRoute immutableDeliveryRoute) {};

    public ManufacturerImpl(ImmutableManufacturerState initialState,
            ImmutableManufacturerProperties properties) {
        super(initialState, Manufacturer.modelIdentifier, properties);
        modelState.setCurrentInventory(properties.getFacilityProperties().getStartingInventory());
        modelState.getSchedule().scheduleInternalEvent(TimeUtils.durationToSimTime(
            TimeUtils.MANUFACTURER_REPORT_DURATION), new UpdateInventory());
    }

    @Override
    public void internalStateTransitionFunction() {
        LongSimTime currentTime = modelState.getCurrentTime().plus(timeAdvanceFunction());
        modelState.setCurrentTime(currentTime);
        modelState.getSchedule().removeCurrentScheduledOutput(currentTime);
        for (Object event: modelState.getSchedule().removeCurrentScheduledEvents(currentTime)) {
            if (event instanceof UpdateInventory) {
                // Increase inventory by production amount
                modelState.setCurrentInventory(modelState.getCurrentInventory() 
                    + properties.getDailyProduction());
                // Verify inventory is over min amount
                if (modelState.getCurrentInventory() < 0) {
                    simulator.getContext().getLog().error("Manufacturer "
                        + " with inventory " + modelState.getCurrentInventory() 
                        + " if less than 0");
                }                
                
                // Report inventory costs
                double cost = modelState.getCurrentInventory() 
                    * properties.getFacilityProperties().getInventoryCost();
                int day = (int) TimeUtils.simTimeToDuration(currentTime).toDaysPart() + 1;
                ImmutableInventoryCost immutableInventoryCost = ImmutableInventoryCost.builder()
                    .retailerId(0)
                    .cost(cost)
                    .day(day)
                    .build();
                modelState.getSchedule().scheduleOutput(currentTime, Retailer.dailyInventoryCost, immutableInventoryCost);

                // Schedule the next update
                LongSimTime nextUpdateTime = currentTime.plus(TimeUtils.durationToSimTime(Duration.ofDays(1)));
                modelState.getSchedule().scheduleInternalEvent(nextUpdateTime, new UpdateInventory());
            } else if (event instanceof PostDelveryRoute postDeliveryRoute) {
                double productLoaded = postDeliveryRoute.immutableDeliveryRoute().getDeliveries().stream()
                    .mapToDouble(ImmutableDelivery::getProductAmount)
                    .sum();
                modelState.setCurrentInventory(modelState.getCurrentInventory() - productLoaded);
                modelState.getSchedule().scheduleOutput(currentTime, Manufacturer.postDeliveryRoute, 
                    postDeliveryRoute.immutableDeliveryRoute());
            } else {
                throw new IllegalArgumentException("Event of type " + event.getClass().getCanonicalName() 
                    + " is not expected by RetailerImpl");
            }
        }       
    }

    @Override
    public void confluentStateTransitionFunction(List<PortValue<?>> inputs) {
        internalStateTransitionFunction();
        externalStateTransitionFunction(LongSimTime.create(0), inputs);
    }

    @Override
    protected void handleAcceptDeliverySchedule(ImmutableDeliverySchedule immutableDeliverySchedule,
            LongSimTime elapsedTime) {
        LongSimTime openTime = TimeUtils.durationToSimTime(TimeUtils.OPENING_DURATION);
        
        for (int day: immutableDeliverySchedule.getDeliveriesByDayByVehicle().keySet()) {
            Map<Integer, ImmutableDeliveryRoute> routesByVehicle =  
                immutableDeliverySchedule.getDeliveriesByDayByVehicle().get(day);
            day = day - 1;  // Day zero has index 1 on the list
            LongSimTime loadVehiclesTime = openTime.plus(TimeUtils.durationToSimTime(Duration.ofDays(day)));
            for (int vehicle: routesByVehicle.keySet()) {
                ImmutableDeliveryRoute deliveryRoute = routesByVehicle.get(vehicle);
                modelState.getSchedule().scheduleInternalEvent(loadVehiclesTime, new PostDelveryRoute(deliveryRoute));
            }
        }
    }

    @Override
    protected void handleAcceptDelivery(ImmutableDelivery immutableDelivery, LongSimTime elapsedTime) {
        modelState.setCurrentInventory(modelState.getCurrentInventory() + immutableDelivery.getProductAmount());
    }

}
