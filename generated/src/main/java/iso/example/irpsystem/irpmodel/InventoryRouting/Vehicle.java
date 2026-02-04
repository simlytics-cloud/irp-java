

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

public abstract class Vehicle
  extends ScheduledDevsModel<LongSimTime, VehicleState> {

public static String modelIdentifier = "vehicle";
    public static final ImmutablePort<ImmutableDeliveryRoute> acceptDeliveryRoute = 
      new ImmutablePort<>("acceptDeliveryRoute", ImmutableDeliveryRoute.class);
    public static final ImmutablePort<ImmutableDelivery> dropDelivery = 
      new ImmutablePort<>("dropDelivery", ImmutableDelivery.class);
    public static final ImmutablePort<ImmutableVehicleCost> dailyDeliveryCost = 
      new ImmutablePort<>("dailyDeliveryCost", ImmutableVehicleCost.class);


    protected ImmutableVehicleProperties properties;

    public Vehicle(ImmutableVehicleState initialState, String identifier, ImmutableVehicleProperties properties) {
        super(initialState.toMutable(), identifier);
        this.properties = properties;
    }




    @Override
    public VehicleState getModelState() {
        return modelState;
    }

    public ImmutableVehicleState getImmutableState() {
        return modelState.toImmutable();
    }

  @Override
  public void externalStateTransitionFunction(LongSimTime elapsedTime, List<PortValue<?>> inputs) {
    LongSimTime currentTime = modelState.getCurrentTime().plus(elapsedTime);
    modelState.setCurrentTime(currentTime);
    for (PortValue<?> pv : inputs) {
      if (pv.getPortName().equals(Vehicle.acceptDeliveryRoute.getPortName())) {
        ImmutableDeliveryRoute immutableDeliveryRoute = Vehicle.acceptDeliveryRoute.getValue(pv);
        handleAcceptDeliveryRoute(immutableDeliveryRoute, elapsedTime);
      } else {
        throw new IllegalArgumentException(
            "Did not expect an input from port with idenfifier " + pv.getPortName());
      }
    }

  }

    /** Handles input to acceptDeliveryRoute port.
     * @param immutableDeliveryRoute - the ImmutableDeliveryRoute value arriving at the port
     * @param elapsedTime - the time since the last state transition
     */
    protected abstract void handleAcceptDeliveryRoute(ImmutableDeliveryRoute immutableDeliveryRoute, 
      LongSimTime elapsedTime);


}
