

package iso.example.irpsystem.irpmodel.ExperimentalFrame;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import devs.PDevsCoordinator;
import devs.PDevsCouplings;
import devs.msg.mutability.ImmutablePort;
import devs.iso.DevsMessage;
import devs.iso.time.LongSimTime;
import java.time.Duration;
import java.util.Map;



public class ExperimentalFrame extends PDevsCoordinator<LongSimTime> {



    public ExperimentalFrame(
            String modelIdentifier,
            Map<String, ActorRef<DevsMessage>> modelsSimulators,
            PDevsCouplings couplings,
            ActorContext<DevsMessage> context) {
        super(modelIdentifier, modelsSimulators, couplings, context);
    }
}
