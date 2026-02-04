package iso.example.irpsystem.irpmodel.impl;

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

public class TransducerImpl extends Transducer {

    protected static record ComputeFinalCosts(){};

    public TransducerImpl(ImmutableTransducerState initialState, int lastDay) {
        super(initialState, Transducer.modelIdentifier, new ImmutableTransducerProperties());
        modelState.getSchedule().scheduleInternalEvent(TimeUtils.durationToSimTime(Duration.ofDays(lastDay)), new ComputeFinalCosts());
    }

    @Override
    public void internalStateTransitionFunction() {
        LongSimTime currentTime = modelState.getCurrentTime().plus(timeAdvanceFunction());
        modelState.setCurrentTime(currentTime);
        modelState.getSchedule().removeCurrentScheduledOutput(currentTime);
        for (Object event: modelState.getSchedule().removeCurrentScheduledEvents(currentTime)) {
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

    @Override
    public void confluentStateTransitionFunction(List<PortValue<?>> inputs) {
        internalStateTransitionFunction();
        externalStateTransitionFunction(LongSimTime.create(0), inputs);
    }

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
