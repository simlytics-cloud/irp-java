

package iso.example.irpsystem.irpmodel.BasicExperimentalFrame;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import devs.PDevsCoordinator;
import devs.PDevsCouplings;
import devs.msg.mutability.ImmutablePort;
import devs.iso.DevsMessage;
import devs.iso.time.LongSimTime;
import java.time.Duration;
import java.util.Map;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.DeliveryScheduleGenerator.*;
import iso.example.irpsystem.irpmodel.InventoryRouting.*;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.Transducer.*;
import iso.example.irpsystem.irpmodel.BasicInventoryRouting.*;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.*;

public class BasicExperimentalFrame extends PDevsCoordinator<LongSimTime> {



    public BasicExperimentalFrame(
            String modelIdentifier,
            Map<String, ActorRef<DevsMessage>> modelsSimulators,
            PDevsCouplings couplings,
            ActorContext<DevsMessage> context) {
        super(modelIdentifier, modelsSimulators, couplings, context);
    }
}
