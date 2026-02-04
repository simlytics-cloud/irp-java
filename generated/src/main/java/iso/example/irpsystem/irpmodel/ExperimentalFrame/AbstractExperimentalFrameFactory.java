

package iso.example.irpsystem.irpmodel.ExperimentalFrame;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import devs.*;
import devs.iso.DevsMessage;
import devs.iso.time.LongSimTime;
import devs.utils.ModelUtils;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import iso.example.irpsystem.irpmodel.InventoryRouting.*;



public abstract class AbstractExperimentalFrameFactory {

    protected List<InputCouplingHandler> buildInputCouplings() {
        return Collections.singletonList(new ExperimentalFrameInputCouplingHandler());
    }
    protected List<OutputCouplingHandler> buildOutputCouplings() {
        return Collections.singletonList(new ExperimentalFrameOutputCouplingHandler());
    }
    protected abstract List<DeliveryScheduleGenerator> buildDeliveryScheduleGenerators();

    protected abstract List<Transducer> buildTransducers();

    protected abstract AbstractInventoryRoutingFactory buildInventoryRoutingFactory();


    public Map<String, Behavior<DevsMessage>> create(String parentIdentifier) {
        LongSimTime t0 = LongSimTime.builder().t(0L).build();
        Map<String, ActorRef<DevsMessage>> modelSimulators = new HashMap<>();
        PDevsCouplings couplings = new PDevsCouplings(
                    buildInputCouplings(), buildOutputCouplings());
        return Collections.singletonMap(ExperimentalFrame.modelIdentifier, Behaviors.setup(context -> {
                buildDeliveryScheduleGenerators().stream().forEach(devsModel -> {
                    ActorRef<DevsMessage> atomicModelRef = context.spawn(PDevsSimulator.create(
                        devsModel, t0), ModelUtils.toLegalActorName(devsModel.getModelIdentifier()));
                   context.watch(atomicModelRef);
                   modelSimulators.put(devsModel.getModelIdentifier(), atomicModelRef);
                });
                buildTransducers().stream().forEach(devsModel -> {
                    ActorRef<DevsMessage> atomicModelRef = context.spawn(PDevsSimulator.create(
                        devsModel, t0), ModelUtils.toLegalActorName(devsModel.getModelIdentifier()));
                   context.watch(atomicModelRef);
                   modelSimulators.put(devsModel.getModelIdentifier(), atomicModelRef);
                });
                buildInventoryRoutingFactory().create(ExperimentalFrame.modelIdentifier).entrySet().stream().forEach(entry -> {
                    ActorRef<DevsMessage> coupledModelRef = context.spawn(entry.getValue(), ModelUtils.toLegalActorName(entry.getKey()));
                    context.watch(coupledModelRef);
                    modelSimulators.put(entry.getKey(), coupledModelRef);
                });
            return new ExperimentalFrame(ExperimentalFrame.modelIdentifier, modelSimulators, couplings, context);
        }));
    }


}
