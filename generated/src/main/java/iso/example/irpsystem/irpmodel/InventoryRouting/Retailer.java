

package iso.example.irpsystem.irpmodel.InventoryRouting;

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

public abstract class Retailer
  extends Facility<ImmutableRetailerProperties, RetailerState> {

public static String modelIdentifier = "retailer";
    public static final ImmutablePort<ImmutableDelivery> receiveDelivery = new ImmutablePort<>("receiveDelivery", ImmutableDelivery.class);
    public static final ImmutablePort<ImmutableInventoryCost> dailyInventoryCost = new ImmutablePort<>("dailyInventoryCost", ImmutableInventoryCost.class);


    protected ImmutableRetailerProperties properties;

    public Retailer(ImmutableRetailerState initialState, String identifier, ImmutableRetailerProperties properties) {
        super(initialState.toMutable(), identifier, properties);
        this.properties = properties;
    }




    @Override
    public RetailerState getModelState() {
        return modelState;
    }

    public ImmutableRetailerState getImmutableState() {
        return modelState.toImmutable();
    }

  @Override
  public void externalStateTransitionFunction(LongSimTime elapsedTime, List<PortValue<?>> inputs) {
    LongSimTime currentTime = modelState.getCurrentTime().plus(elapsedTime);
    modelState.setCurrentTime(currentTime);
    for (PortValue<?> pv : inputs) {
      if (pv.getPortName().equals(Retailer.receiveDelivery.getPortName())) {
        ImmutableDelivery immutableDelivery = Retailer.receiveDelivery.getValue(pv);
        handleReceiveDelivery(immutableDelivery, elapsedTime);
      } else {
        throw new IllegalArgumentException(
            "Did not expect an input from port with idenfifier " + pv.getPortName());
      }
    }

  }

    /** Handles input to receiveDelivery port.
     * @param immutableDelivery - the ImmutableDelivery value arriving at the port
     * @param elapsedTime - the time since the last state transition
     */
    protected abstract void handleReceiveDelivery(ImmutableDelivery immutableDelivery, LongSimTime elapsedTime);


}
