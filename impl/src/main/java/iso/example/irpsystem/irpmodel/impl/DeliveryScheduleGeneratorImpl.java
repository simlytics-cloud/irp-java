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

public class DeliveryScheduleGeneratorImpl extends DeliveryScheduleGenerator
    <ImmutableDeliveryScheduleGeneratorProperties, DeliveryScheduleGeneratorState, ImmutableDeliveryScheduleGeneratorState> {

    public DeliveryScheduleGeneratorImpl(ImmutableDeliveryScheduleGeneratorState initialState, String modelIdentifier) {
        super(initialState, modelIdentifier, ImmutableDeliveryScheduleGeneratorProperties.builder().build());
        modelState.getSchedule().scheduleOutput(LongSimTime.create(0), 
            DeliveryScheduleGenerator.postDeliverySchedule, initialState.getDeliverySchedule());
    }


    @Override
    public void handleScheduledEvents(List<Object> events) {
    }

    @Override
    public void confluentStateTransitionFunction(List<PortValue<?>> inputs) {
        internalStateTransitionFunction();
        externalStateTransitionFunction(LongSimTime.create(0), inputs);
    }

}
