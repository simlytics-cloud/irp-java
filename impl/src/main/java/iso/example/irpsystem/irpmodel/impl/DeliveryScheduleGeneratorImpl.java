package iso.example.irpsystem.irpmodel.impl;

import devs.utils.Schedule.ScheduledEvent;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.DeliveryScheduleGeneratorProperties;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.DeliveryScheduleGeneratorState;
import java.util.List;
import java.util.TreeMap;

import devs.iso.PortValue;
import devs.iso.time.LongSimTime;
import devs.utils.Schedule;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.DeliveryScheduleGenerator;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.ImmutableDeliveryScheduleGeneratorProperties;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.ImmutableDeliveryScheduleGeneratorState;

/**
 * Implementation of a Delivery Schedule Generator in the Inventory Routing Problem (IRP) simulation.
 * This component is responsible for posting the initial delivery schedule at the beginning of the simulation.
 */
public class DeliveryScheduleGeneratorImpl extends DeliveryScheduleGenerator
    <ImmutableDeliveryScheduleGeneratorProperties, DeliveryScheduleGeneratorState, ImmutableDeliveryScheduleGeneratorState> {

    /**
     * Constructs a new DeliveryScheduleGeneratorImpl.
     * Schedules the output of the initial delivery schedule at simulation time 0.
     *
     * @param initialState    The initial state of the generator, containing the delivery schedule.
     * @param modelIdentifier The unique identifier for this generator model.
     */
    public DeliveryScheduleGeneratorImpl(ImmutableDeliveryScheduleGeneratorState initialState, String modelIdentifier) {
        super(initialState, modelIdentifier, ImmutableDeliveryScheduleGeneratorProperties.builder().build());
        modelState.getSchedule().scheduleOutput(LongSimTime.create(0), 
            DeliveryScheduleGenerator.postDeliverySchedule, initialState.getDeliverySchedule());
    }


    /**
     * Processes internal scheduled events. This implementation does not handle any specific events.
     *
     * @param events The list of events to process.
     */
    @Override
    public void handleScheduledEvents(List<Object> events) {
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

}
