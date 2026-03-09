package iso.example.irpsystem.irpmodel.impl;

import devs.utils.Schedule.ScheduledEvent;
import iso.example.irpsystem.irpmodel.InventoryRouting.ManufacturerState;
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

/**
 * Implementation of a Manufacturer in the Inventory Routing Problem (IRP) simulation.
 * The manufacturer produces products daily, updates inventory, reports inventory costs,
 * and schedules delivery routes to vehicles.
 */
public class ManufacturerImpl extends Manufacturer<ImmutableManufacturerProperties, ManufacturerState, ImmutableManufacturerState> {

    /**
     * Event to signal an inventory update.
     */
    protected record UpdateInventory() {
    }

    /**
     * Event to signal the posting of a delivery route to a vehicle.
     *
     * @param immutableDeliveryRoute The delivery route to be posted.
     */
    protected record PostDelveryRoute(ImmutableDeliveryRoute immutableDeliveryRoute) {
    };

    /**
     * Constructs a new ManufacturerImpl.
     *
     * @param initialState    The initial state of the manufacturer.
     * @param modelIdentifier The unique identifier for this manufacturer model.
     * @param properties      The static properties of the manufacturer (production rate, etc.).
     */
    public ManufacturerImpl(ImmutableManufacturerState initialState,
            String modelIdentifier, ImmutableManufacturerProperties properties) {
        super(initialState, modelIdentifier, properties);
        modelState.setCurrentInventory(properties.getFacilityProperties().getStartingInventory());
        modelState.getSchedule().scheduleInternalEvent(TimeUtils.durationToSimTime(
            TimeUtils.MANUFACTURER_REPORT_DURATION), new UpdateInventory());
    }

    /**
     * Processes internal scheduled events such as inventory updates and posting delivery routes.
     *
     * @param events The list of events to process.
     */
    @Override
    public void handleScheduledEvents(List<Object> events) {

        for (Object event:events) {
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
                int day = (int) TimeUtils.simTimeToDuration(modelState.getCurrentTime()).toDaysPart() + 1;
                ImmutableInventoryCost immutableInventoryCost = ImmutableInventoryCost.builder()
                    .retailerId(0)
                    .cost(cost)
                    .day(day)
                    .build();
                modelState.getSchedule().scheduleOutput(modelState.getCurrentTime(), Retailer.dailyInventoryCost, immutableInventoryCost);

                // Schedule the next update
                LongSimTime nextUpdateTime = modelState.getCurrentTime().plus(TimeUtils.durationToSimTime(Duration.ofDays(1)));
                modelState.getSchedule().scheduleInternalEvent(nextUpdateTime, new UpdateInventory());
            } else if (event instanceof PostDelveryRoute postDeliveryRoute) {
                double productLoaded = postDeliveryRoute.immutableDeliveryRoute().getDeliveries().stream()
                    .mapToDouble(ImmutableDelivery::getProductAmount)
                    .sum();
                modelState.setCurrentInventory(modelState.getCurrentInventory() - productLoaded);
                modelState.getSchedule().scheduleOutput(modelState.getCurrentTime(), Manufacturer.postDeliveryRoute,
                    postDeliveryRoute.immutableDeliveryRoute());
            } else {
                throw new IllegalArgumentException("Event of type " + event.getClass().getCanonicalName() 
                    + " is not expected by RetailerImpl");
            }
        }       
    }

    /**
     * Handles simultaneous internal and external transitions.
     * Executes the internal transition followed by the external transition.
     *
     * @param inputs The list of port values received as input.
     */
    @Override
    public void confluentStateTransitionFunction(List<PortValue<?>> inputs) {
        internalStateTransitionFunction();
        externalStateTransitionFunction(LongSimTime.create(0), inputs);
    }

    /**
     * Handles the acceptance of a new delivery schedule.
     * Schedules internal events to post delivery routes to vehicles at the start of each day.
     *
     * @param immutableDeliverySchedule The new delivery schedule to follow.
     * @param elapsedTime               The time elapsed since the last state transition.
     */
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
                modelState.getSchedule().scheduleInternalEvent(loadVehiclesTime,
                    new PostDelveryRoute(deliveryRoute));
            }
        }
    }

    /**
     * Handles the acceptance of returned products from a vehicle.
     * Increases the manufacturer's inventory by the returned amount.
     *
     * @param immutableDelivery The delivery details of the returned products.
     * @param elapsedTime       The time elapsed since the last state transition.
     */
    @Override
    protected void handleAcceptDelivery(ImmutableDelivery immutableDelivery, LongSimTime elapsedTime) {
        modelState.setCurrentInventory(modelState.getCurrentInventory() + immutableDelivery.getProductAmount());
    }

}
