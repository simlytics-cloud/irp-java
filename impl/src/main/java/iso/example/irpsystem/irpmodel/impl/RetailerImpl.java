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
        // TODO(tutorial-1 step-1) Implement handling of delivery
    }

    /**
     * Handles internally scheduled OpenEvent or CloseEvent
     */
    @Override
    public void handleScheduledEvents(List<Object> events) {
        for (Object event: events) {
            if (event instanceof CloseEvent) {
                // TODO(tutorial-1 step-1) Implement handling of CloseEvent
            } else if (event instanceof OpenEvent) {
                // TODO(tutorial-1 step-1) Implement handling of OpenEvent
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
        // TODO(tutorial-1 step-1) Implement confluent state transition
    }

}
