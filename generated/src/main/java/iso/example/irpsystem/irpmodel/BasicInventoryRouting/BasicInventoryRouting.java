

package iso.example.irpsystem.irpmodel.BasicInventoryRouting;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import devs.PDevsCoordinator;
import devs.PDevsCouplings;
import devs.msg.mutability.ImmutablePort;
import devs.iso.DevsMessage;
import devs.iso.time.LongSimTime;
import java.time.Duration;
import java.util.Map;

import iso.example.irpsystem.irpmodel.InventoryRouting.*;
import iso.example.irpsystem.irpmodel.InventoryRouting.Manufacturer.*;
import iso.example.irpsystem.irpmodel.InventoryRouting.Vehicle.*;
import iso.example.irpsystem.irpmodel.InventoryRouting.Retailer.*;
import iso.example.irpsystem.irpmodel.InventoryRouting.*;

public class BasicInventoryRouting extends PDevsCoordinator<LongSimTime> {



    public BasicInventoryRouting(
            String modelIdentifier,
            Map<String, ActorRef<DevsMessage>> modelsSimulators,
            PDevsCouplings couplings,
            ActorContext<DevsMessage> context) {
        super(modelIdentifier, modelsSimulators, couplings, context);
    }
}
