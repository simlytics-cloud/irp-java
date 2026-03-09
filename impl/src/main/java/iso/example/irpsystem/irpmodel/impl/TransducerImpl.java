package iso.example.irpsystem.irpmodel.impl;

import devs.utils.Schedule.ScheduledEvent;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.TransducerState;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import devs.iso.PortValue;
import devs.iso.time.LongSimTime;
import iso.example.irpsystem.irpdomain.ImmutableInventoryCost;
import iso.example.irpsystem.irpdomain.ImmutableVehicleCost;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.ImmutableTransducerProperties;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.ImmutableTransducerState;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.Transducer;
import iso.example.irpsystem.irpmodel.algorithms.TimeUtils;

/**
 * Implementation of a Transducer in the Inventory Routing Problem (IRP) simulation.
 * The transducer aggregates inventory and vehicle costs throughout the simulation
 * and computes the final total costs at the end of the simulation period.
 */
public class TransducerImpl extends Transducer<ImmutableTransducerProperties, TransducerState, ImmutableTransducerState> {

    /**
     * Event to signal the computation of final costs at the end of the simulation.
     */
    protected static record ComputeFinalCosts(){
    };

    /**
     * Constructs a new TransducerImpl.
     * Schedules a final cost computation event at the end of the specified last day.
     *
     * @param initialState    The initial state of the transducer.
     * @param modelIdentifier The unique identifier for this transducer model.
     * @param lastDay         The last day of the simulation, used to schedule final cost computation.
     */
    public TransducerImpl(ImmutableTransducerState initialState, String modelIdentifier, int lastDay) {
        super(initialState, modelIdentifier, ImmutableTransducerProperties.builder().build());
        modelState.getSchedule().scheduleInternalEvent(TimeUtils.durationToSimTime(Duration.ofDays(lastDay)),
            new ComputeFinalCosts());
    }

    /**
     * Processes internal scheduled events, specifically the computation and printing of final costs.
     *
     * @param events The list of events to process.
     */
    @Override
    public void handleScheduledEvents(List<Object> events) {
        for (Object event: events) {
            if (event instanceof ComputeFinalCosts) {
                double totalVehicleCost = 0.0;
                double totalInventoryCost = 0.0;
                for (int day: modelState.getVehicleCostByDayByVehicle().keySet()) {
                    System.out.println("Day " + day + "costs:");
                    Map<Integer, Double> costByBehicle = modelState.getVehicleCostByDayByVehicle().get(day);
                    for (int vehcile: costByBehicle.keySet()) {
                        System.out.println(String.format("Vehicle %d: %f", vehcile, costByBehicle.get(vehcile)));
                        totalVehicleCost += costByBehicle.get(vehcile);
                    }
                    Map<Integer, Double> costByRetailer = modelState.getInventoryCostByDayByRetailer().get(day);
                    for (int retailerId: costByRetailer.keySet()) {
                        System.out.println(String.format("Retailer %d: %f", retailerId, costByRetailer.get(retailerId)));
                        totalInventoryCost += costByRetailer.get(retailerId);
                    }
                }
                System.out.println(String.format("Vehicle costs %f + retailer costs %f = %f total",
                    totalVehicleCost, totalInventoryCost, totalVehicleCost + totalInventoryCost));
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
     * Handles the aggregation of inventory costs reported by retailers and the manufacturer.
     * Stores the cost in the state, indexed by day and facility ID.
     *
     * @param immutableInventoryCost The reported inventory cost.
     * @param elapsedTime           The time elapsed since the last state transition.
     */
    @Override
    protected void handleAggregateInventoryCost(ImmutableInventoryCost immutableInventoryCost,
            LongSimTime elapsedTime) {
        int day = (int) TimeUtils.simTimeToDuration(modelState.getCurrentTime()).toDaysPart() + 1;
        if (!modelState.getInventoryCostByDayByRetailer().containsKey(day)) {
            modelState.getInventoryCostByDayByRetailer().put(day, new HashMap<>());
        }
        Map<Integer, Double> costByRetailer = modelState.getInventoryCostByDayByRetailer().get(day);
        double retailerTotalCost = immutableInventoryCost.getCost();
        costByRetailer.put(immutableInventoryCost.getRetailerId(), retailerTotalCost);
    }

    /**
     * Handles the aggregation of vehicle costs reported by vehicles.
     * Stores the cost in the state, indexed by day and vehicle ID.
     *
     * @param immutableVehicleCost The reported vehicle cost.
     * @param elapsedTime          The time elapsed since the last state transition.
     */
    @Override
    protected void handleAggregateVehicleCost(ImmutableVehicleCost immutableVehicleCost, LongSimTime elapsedTime) {
        int day = (int) TimeUtils.simTimeToDuration(modelState.getCurrentTime()).toDaysPart() + 1;
        if (!modelState.getVehicleCostByDayByVehicle().containsKey(day)) {
            modelState.getVehicleCostByDayByVehicle().put(day, new HashMap<>());
        }        
        Map<Integer, Double> costByVehicle = modelState.getVehicleCostByDayByVehicle().get(day);
        double vehicleTotalCost = immutableVehicleCost.getCost();
        costByVehicle.put(immutableVehicleCost.getVehicleId(), vehicleTotalCost);
    }

}
