package iso.example.irpsystem.irpmodel.impl;

import devs.utils.Schedule.ScheduledEvent;
import iso.example.irpsystem.irpmodel.InventoryRouting.RetailerState;
import java.time.Duration;
import java.util.List;

import devs.iso.PortValue;
import devs.iso.time.LongSimTime;
import iso.example.irpsystem.irpdomain.ImmutableDelivery;
import iso.example.irpsystem.irpdomain.ImmutableInventoryCost;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableRetailerProperties;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableRetailerState;
import iso.example.irpsystem.irpmodel.InventoryRouting.Retailer;
import iso.example.irpsystem.irpmodel.algorithms.TimeUtils;

public class RetailerImpl extends Retailer<ImmutableRetailerProperties, RetailerState, ImmutableRetailerState> {

    static record UpdateInventoryEvent() {
    }

    public RetailerImpl(ImmutableRetailerState initialState, String modelIdentifier,
            ImmutableRetailerProperties properties) {
        super(initialState, modelIdentifier, properties);
        modelState.setCurrentInventory(properties.getFacilityProperties().getStartingInventory());
        modelState.getSchedule().scheduleInternalEvent(TimeUtils.durationToSimTime(TimeUtils.CLOSING_DURATION), new UpdateInventoryEvent());
    }

    @Override
    protected void handleReceiveDelivery(ImmutableDelivery immutableDelivery, LongSimTime elapsedTime) {
        modelState.setCurrentInventory(modelState.getCurrentInventory() + immutableDelivery.getProductAmount());
    }

    @Override
    public void handleScheduledEvents(List<Object> events) {
        for (Object event: events) {
            if (event instanceof UpdateInventoryEvent) {
                // Verify inventory is under max amount
                if (modelState.getCurrentInventory() > properties.getMaxInventory()) {
                    simulator.getContext().getLog().error
                    ("Retailer " + properties.getRetailerId() 
                        + " with inventory " + modelState.getCurrentInventory() 
                        + " exceeded max inventory of " + properties.getMaxInventory());
                }
                // Reduce inventor by the daily consumption
                modelState.setCurrentInventory(modelState.getCurrentInventory() 
                    - properties.getDailyConsumption());
                // Verify inventory is under min amount
                if (modelState.getCurrentInventory() < properties.getMinInventory()) {
                    simulator.getContext().getLog().error("Retailer " + properties.getRetailerId() 
                        + " with inventory " + modelState.getCurrentInventory() 
                        + " has less than min inventory of " + properties.getMinInventory());
                }                
                
                // Report inventory costs
                double cost = modelState.getCurrentInventory() 
                    * properties.getFacilityProperties().getInventoryCost();
                int day = (int) TimeUtils.simTimeToDuration(modelState.getCurrentTime()).toDaysPart() + 1;
                ImmutableInventoryCost immutableInventoryCost = ImmutableInventoryCost.builder()
                    .retailerId(properties.getRetailerId())
                    .cost(cost)
                    .day(day)
                    .build();
                modelState.getSchedule().scheduleOutput(modelState.getCurrentTime(), Retailer.dailyInventoryCost, immutableInventoryCost);

                // Schedule the next update
                LongSimTime nextUpdateTime = modelState.getCurrentTime()
                    .plus(TimeUtils.durationToSimTime(Duration.ofDays(1)));
                modelState.getSchedule().scheduleInternalEvent(nextUpdateTime,
                    new UpdateInventoryEvent());
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

}
