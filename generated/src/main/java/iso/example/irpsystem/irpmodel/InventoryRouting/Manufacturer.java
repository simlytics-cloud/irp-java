

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

public abstract class Manufacturer
  extends Facility<ImmutableManufacturerProperties, ManufacturerState> {

public static String modelIdentifier = "manufacturer";
    public static final ImmutablePort<ImmutableDeliverySchedule> acceptDeliverySchedule = new ImmutablePort<>("acceptDeliverySchedule", ImmutableDeliverySchedule.class);
    public static final ImmutablePort<ImmutableDelivery> acceptDelivery = new ImmutablePort<>("acceptDelivery", ImmutableDelivery.class);
    public static final ImmutablePort<ImmutableDeliveryRoute> postDeliveryRoute = new ImmutablePort<>("postDeliveryRoute", ImmutableDeliveryRoute.class);
    public static final ImmutablePort<ImmutableInventoryCost> dailyInventoryCost = new ImmutablePort<>("dailyInventoryCost", ImmutableInventoryCost.class);


    protected ImmutableManufacturerProperties properties;

    public Manufacturer(ImmutableManufacturerState initialState, String identifier, ImmutableManufacturerProperties properties) {
        super(initialState.toMutable(), identifier, properties);
        this.properties = properties;
    }




    @Override
    public ManufacturerState getModelState() {
        return modelState;
    }

    public ImmutableManufacturerState getImmutableState() {
        return modelState.toImmutable();
    }

  @Override
  public void externalStateTransitionFunction(LongSimTime elapsedTime, List<PortValue<?>> inputs) {
    LongSimTime currentTime = modelState.getCurrentTime().plus(elapsedTime);
    modelState.setCurrentTime(currentTime);
    for (PortValue<?> pv : inputs) {
      if (pv.getPortName().equals(Manufacturer.acceptDeliverySchedule.getPortName())) {
        ImmutableDeliverySchedule immutableDeliverySchedule = Manufacturer.acceptDeliverySchedule.getValue(pv);
        handleAcceptDeliverySchedule(immutableDeliverySchedule, elapsedTime);
      } else if (pv.getPortName().equals(Manufacturer.acceptDelivery.getPortName())) {
        ImmutableDelivery immutableDelivery = Manufacturer.acceptDelivery.getValue(pv);
        handleAcceptDelivery(immutableDelivery, elapsedTime);
      } else {
        throw new IllegalArgumentException(
            "Did not expect an input from port with idenfifier " + pv.getPortName());
      }
    }

  }

    /** Handles input to acceptDeliverySchedule port.
     * @param immutableDeliverySchedule - the ImmutableDeliverySchedule value arriving at the port
     * @param elapsedTime - the time since the last state transition
     */
    protected abstract void handleAcceptDeliverySchedule(ImmutableDeliverySchedule immutableDeliverySchedule, LongSimTime elapsedTime);

    /** Handles input to acceptDelivery port.
     * @param immutableDelivery - the ImmutableDelivery value arriving at the port
     * @param elapsedTime - the time since the last state transition
     */
    protected abstract void handleAcceptDelivery(ImmutableDelivery immutableDelivery, LongSimTime elapsedTime);


}
