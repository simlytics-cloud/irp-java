

package iso.example.irpsystem.irpmodel.InventoryRouting;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import devs.PDevsCoordinator;
import devs.PDevsCouplings;
import devs.msg.mutability.ImmutablePort;
import devs.iso.DevsMessage;
import devs.iso.time.LongSimTime;
import java.time.Duration;
import java.util.Map;
import iso.example.irpsystem.irpdomain.*;

public class InventoryRouting extends PDevsCoordinator<LongSimTime> {

    public static final ImmutablePort<ImmutableDeliverySchedule> receiveDeliverySchedule = new ImmutablePort<>("receiveDeliverySchedule", ImmutableDeliverySchedule.class);
    public static final ImmutablePort<ImmutableVehicleCost> reportVehicleCost = new ImmutablePort<>("reportVehicleCost", ImmutableVehicleCost.class);
    public static final ImmutablePort<ImmutableInventoryCost> reportInventoryCost = new ImmutablePort<>("reportInventoryCost", ImmutableInventoryCost.class);

    public InventoryRouting(
            String modelIdentifier,
            Map<String, ActorRef<DevsMessage>> modelsSimulators,
            PDevsCouplings couplings,
            ActorContext<DevsMessage> context) {
        super(modelIdentifier, modelsSimulators, couplings, context);
    }
}
