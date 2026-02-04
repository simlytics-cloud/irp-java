

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

public abstract class Transducer
  extends ScheduledDevsModel<LongSimTime, TransducerState> {

public static String modelIdentifier = "transducer";
    public static final ImmutablePort<ImmutableInventoryCost> aggregateInventoryCost = new ImmutablePort<>("aggregateInventoryCost", ImmutableInventoryCost.class);
    public static final ImmutablePort<ImmutableVehicleCost> aggregateVehicleCost = new ImmutablePort<>("aggregateVehicleCost", ImmutableVehicleCost.class);


    protected ImmutableTransducerProperties properties;

    public Transducer(ImmutableTransducerState initialState, String identifier, ImmutableTransducerProperties properties) {
        super(initialState.toMutable(), identifier);
        this.properties = properties;
    }




    @Override
    public TransducerState getModelState() {
        return modelState;
    }

    public ImmutableTransducerState getImmutableState() {
        return modelState.toImmutable();
    }

  @Override
  public void externalStateTransitionFunction(LongSimTime elapsedTime, List<PortValue<?>> inputs) {
    LongSimTime currentTime = modelState.getCurrentTime().plus(elapsedTime);
    modelState.setCurrentTime(currentTime);
    for (PortValue<?> pv : inputs) {
      if (pv.getPortName().equals(Transducer.aggregateInventoryCost.getPortName())) {
        ImmutableInventoryCost immutableInventoryCost = Transducer.aggregateInventoryCost.getValue(pv);
        handleAggregateInventoryCost(immutableInventoryCost, elapsedTime);
      } else if (pv.getPortName().equals(Transducer.aggregateVehicleCost.getPortName())) {
        ImmutableVehicleCost immutableVehicleCost = Transducer.aggregateVehicleCost.getValue(pv);
        handleAggregateVehicleCost(immutableVehicleCost, elapsedTime);
      } else {
        throw new IllegalArgumentException(
            "Did not expect an input from port with idenfifier " + pv.getPortName());
      }
    }

  }

    /** Handles input to aggregateInventoryCost port.
     * @param immutableInventoryCost - the ImmutableInventoryCost value arriving at the port
     * @param elapsedTime - the time since the last state transition
     */
    protected abstract void handleAggregateInventoryCost(ImmutableInventoryCost immutableInventoryCost, LongSimTime elapsedTime);

    /** Handles input to aggregateVehicleCost port.
     * @param immutableVehicleCost - the ImmutableVehicleCost value arriving at the port
     * @param elapsedTime - the time since the last state transition
     */
    protected abstract void handleAggregateVehicleCost(ImmutableVehicleCost immutableVehicleCost, LongSimTime elapsedTime);


}
