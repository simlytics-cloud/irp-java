package iso.example.irpsystem.irpmodel.impl;

import java.util.List;

import devs.iso.PortValue;
import devs.iso.time.LongSimTime;
import iso.example.irpsystem.irpdomain.ImmutableDelivery;
import iso.example.irpsystem.irpdomain.ImmutableInventoryCost;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableRetailerProperties;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableRetailerState;
import iso.example.irpsystem.irpmodel.InventoryRouting.Retailer;

/**
 * Implementation of a Retailer in inventory routing problem
 */
public class RetailerImpl extends Retailer {

    static record CloseEvent() {}
    static record OpenEvent() {}

    /**
     * Create and intialize the retailer implementation.  Sets the initial inventory to the
     * starting inventory in the properties.  Schedules the opening event at 6AM the
     * first day.
     * @param initialState The initial state
     * @param modelIdentifier The unique identifier of the retailer
     * @param properties The immutable properties of this retailer
     */
    public RetailerImpl(ImmutableRetailerState initialState, String modelIdentifier,
            ImmutableRetailerProperties properties) {
        super(initialState, modelIdentifier, properties);
        modelState.setCurrentInventory(properties.getFacilityProperties().getStartingInventory());
        modelState.getSchedule().scheduleInternalEvent(LongSimTime.create(60 * 6), new OpenEvent());
    }

    /**
     * Handle the arrival of a delivery on the Retailer.receiveDeliver port.
     */
    @Override
    protected void handleReceiveDelivery(ImmutableDelivery immutableDelivery, LongSimTime elapsedTime) {
        modelState.setCurrentInventory(modelState.getCurrentInventory() + immutableDelivery.getProductAmount());
    }

    /**
     * Handles internally scheduled OpenEvent or CloseEvent
     */
    @Override
    public void handleScheduledEvents(List<Object> events) {
        for (Object event: events) {
            if (event instanceof CloseEvent) {
                // Reduce inventor by the daily consumption
                modelState.setCurrentInventory(modelState.getCurrentInventory() 
                    - properties.getDailyConsumption());
                // Verify inventory is under max amount
                if (modelState.getCurrentInventory() > properties.getMaxInventory()) {
                    simulator.getContext().getLog().error
                    ("Retailer " + properties.getRetailerId() 
                        + " with inventory " + modelState.getCurrentInventory() 
                        + " exceeded max inventory of " + properties.getMaxInventory());
                }
                // Verify inventory is under min amount
                if (modelState.getCurrentInventory() < properties.getMinInventory()) {
                    simulator.getContext().getLog().error("Retailer " + properties.getRetailerId() 
                        + " with inventory " + modelState.getCurrentInventory() 
                        + " has less than min inventory of " + properties.getMinInventory());
                }                
                
                // Report inventory costs
                double cost = modelState.getCurrentInventory() 
                    * properties.getFacilityProperties().getInventoryCost();
                int day = (modelState.getCurrentTime().getT().intValue()) / (60 * 24) + 1;
                ImmutableInventoryCost immutableInventoryCost = ImmutableInventoryCost.builder()
                    .retailerId(properties.getRetailerId())
                    .cost(cost)
                    .day(day)
                    .build();
                //modelState.getSchedule().scheduleOutput(modelState.getCurrentTime(), Retailer.dailyInventoryCost, immutableInventoryCost);

                // Schedule the opening
                LongSimTime nextUpdateTime = LongSimTime.create(modelState.getCurrentTime().getT() + (60 * 14));  // Open at 6 AM
                modelState.getSchedule().scheduleInternalEvent(nextUpdateTime, new OpenEvent());
            } else if (event instanceof OpenEvent) {
                LongSimTime nextUpdateTime = LongSimTime.create(modelState.getCurrentTime().getT() + (60 * 10));  // Close at 4pm
                modelState.getSchedule().scheduleInternalEvent(nextUpdateTime, new CloseEvent());                
            } else {
                throw new IllegalArgumentException("Event of type " + event.getClass().getCanonicalName() 
                    + " is not expected by RetailerImpl");
            }
        }
    }

    /**
     * Implements confluent state transition by call internal state transition first
     * with zero time elapsed, then internal
     */
    @Override
    public void confluentStateTransitionFunction(List<PortValue<?>> inputs) {
        externalStateTransitionFunction(LongSimTime.create(0), inputs);
        internalStateTransitionFunction();
    }

}
