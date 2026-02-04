

package iso.example.irpsystem.irpmodel.ExperimentalFrame;

import devs.Port;
import devs.iso.PortValue;
import devs.iso.time.LongSimTime;
import devs.ScheduledDevsModel;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import devs.msg.mutability.*;
import iso.example.irpsystem.irpdomain.*;

public abstract class DeliveryScheduleGenerator
  extends ScheduledDevsModel<LongSimTime, DeliveryScheduleGeneratorState> {

public static String modelIdentifier = "deliveryScheduleGenerator";
    public static final ImmutablePort<ImmutableDeliverySchedule> postDeliverySchedule = new ImmutablePort<>("postDeliverySchedule", ImmutableDeliverySchedule.class);


    protected ImmutableDeliveryScheduleGeneratorProperties properties;

    public DeliveryScheduleGenerator(ImmutableDeliveryScheduleGeneratorState initialState, String identifier, ImmutableDeliveryScheduleGeneratorProperties properties) {
        super(initialState.toMutable(), identifier);
        this.properties = properties;
    }




    @Override
    public DeliveryScheduleGeneratorState getModelState() {
        return modelState;
    }

    public ImmutableDeliveryScheduleGeneratorState getImmutableState() {
        return modelState.toImmutable();
    }

  @Override
  public void externalStateTransitionFunction(LongSimTime elapsedTime, List<PortValue<?>> inputs) {
    LongSimTime currentTime = modelState.getCurrentTime().plus(elapsedTime);
    modelState.setCurrentTime(currentTime);
    // No input ports for DeliveryScheduleGenerator
  }



}
