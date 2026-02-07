package iso.example.irpsystem.irpmodel.impl;

import devs.utils.Schedule.ScheduledEvent;
import java.time.Duration;
import java.util.List;

import devs.iso.PortValue;
import devs.iso.time.LongSimTime;
import iso.example.irpsystem.irpdomain.ImmutableDelivery;
import iso.example.irpsystem.irpdomain.ImmutableInventoryCost;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableRetailerProperties;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableRetailerState;
import iso.example.irpsystem.irpmodel.InventoryRouting.Retailer;

/**
 * 
 * Implementation of a Retailer for the Inventory Routing Problem.  Before coding, take a look at 
 * the generated Retailer class in the generated module, its parent class, Facility, and finally
 * its parent, the ScheduledDevsModel in the DEVS Streaming Framework. 
 * 
 * The ScheduledDevsModel has an internal state that extends ScheduleState.  This means
 * that its state includes both a Schedule and the current time.  This allows it to
 * provide implementations of the time advance and output functions.  Time advance 
 * returns the interval between the current time and the first item on the schedule.
 * The output function returns a bag of all PortValues on the schedule for the current time.
 * It also implemenents an internal state transition function that updates the
 * current time, removes the published outpts from the schedule, and retrieves a 
 * list of event Objects that are the currently scheduled events, passing them
 * to an abstract event handler.  So instead of implementing an internal state 
 * transition function, the model developer implements
 *   public void handleScheduledEvents(List<Object> events);
 * 
 * The schedule allows a developer to create inner classes for their internal events
 * then add them to the schedule them as follows, as in the constructor to schedule
 * the first day's opening.
 *     modelState.getSchedule().scheduleInternalEvent(LongSimTime.create(60 * 6), 
 *       new OpenEvent());
 * Similarly, outputs can be added as follows, to generate an inventory cost output
 * on the dailyInventoryCost port.
 *     modelState.getSchedule().scheduleOutput(currentTime, Retailer.dailyInventoryCost, 
 *       immutableInventoryCost);
 * 
 * The Facility class is a simple placeholder parent that shares common state value
 * and properties between the Retailer and the Manufacturer, which both extend
 * Facility.
 * 
 * Finally, the Retailer has an external state transition that first properly increment
 * current time.  Then it handles the incoming PortVales by port name and type,  
 * Sending the resultant inputs to 
 *     handleReceiveDelivery(ImmutableDelivery immutableDelivery, LongSimTime elapsedTime);
 * So the required remainig tasks are to fully implement the handling of a delivery,
 * the internal state transition, and the confluent state transition. The pulling of internal
 * events from the schedule and looping over them is also provided for you in the internal
 * state transition.
 * 
 * Finally, time is of the type LongSimTime.  Use the getT() method to get a time
 * value rperesenting minutes since simulation start.
 * 
 * For general reference, take a look at the VehicleImpl and ManufacturerImple
 * classes that have implementations already completed.  Additional examples are in 
 * the DEVS Streaming Framework example and test implementations of DEVS models.
 */
public class RetailerImpl extends Retailer {

    static record CloseEvent() {}
    static record OpenEvent() {}

    public RetailerImpl(ImmutableRetailerState initialState, String modelIdentifier,
            ImmutableRetailerProperties properties) {
        super(initialState, modelIdentifier, properties);
        modelState.setCurrentInventory(properties.getFacilityProperties().getStartingInventory());
        modelState.getSchedule().scheduleInternalEvent(LongSimTime.create(60 * 6), new OpenEvent());
    }

    /**
     * Implement the curect state updates here for receiving a delivery.  Remember, current time
     * has already been updated in the Retailer parent class.
     */
    @Override
    protected void handleReceiveDelivery(ImmutableDelivery immutableDelivery, LongSimTime elapsedTime) {
        modelState.setCurrentInventory(modelState.getCurrentInventory() + immutableDelivery.getProductAmount());
    }

    /**
     * Implement the correct behavior here for the internal state transition.  
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
                modelState.getSchedule().scheduleOutput(modelState.getCurrentTime(), Retailer.dailyInventoryCost, immutableInventoryCost);

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
     * Implement the correct behavior for the confluent state transition, where external inputs
     * arrive at the same time as a scheduled internal transition.
     */
    @Override
    public void confluentStateTransitionFunction(List<PortValue<?>> inputs) {
        internalStateTransitionFunction();
        externalStateTransitionFunction(LongSimTime.create(0), inputs);
    }

}
