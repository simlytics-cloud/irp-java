

package iso.example.irpsystem.irpmodel.ImprovedExperimentalFrame;

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
import iso.example.irpsystem.irpmodel.ImprovedInventoryRouting.*;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.DeliveryScheduleGenerator.*;
import iso.example.irpsystem.irpmodel.InventoryRouting.*;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.Transducer.*;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.*;


public abstract class AbstractImprovedExperimentalFrameFactory {
    public static final String generatorIdentifier = "generator";
    public static final String transducerIdentifier = "transducer";
    public static final String improvedInventoryRoutingIdentifier = "improvedInventoryRouting";
    public static final String improvedExperimentalFrameIdentifier = "improvedExperimentalFrame";

  protected PDevsCouplings buildCouplings() {
    PDevsCouplings couplings = PDevsCouplings.builder("ImprovedExperimentalFrame")
    .addConnection("generator", DeliveryScheduleGenerator.postDeliverySchedule.getPortName(),
        "improvedInventoryRouting", InventoryRouting.receiveDeliverySchedule.getPortName())
    .addConnection("improvedInventoryRouting", InventoryRouting.reportVehicleCost.getPortName(),
        "transducer", Transducer.aggregateVehicleCost.getPortName())
    .addConnection("improvedInventoryRouting", InventoryRouting.reportInventoryCost.getPortName(),
        "transducer", Transducer.aggregateInventoryCost.getPortName())
        .build();
    return couplings;
  }


    protected abstract List<DeliveryScheduleGenerator> buildDeliveryScheduleGenerators();

    protected abstract List<Transducer> buildTransducers();

    protected abstract AbstractImprovedInventoryRoutingFactory buildImprovedInventoryRoutingFactory();


    public Map<String, Behavior<DevsMessage>> create(String parentIdentifier) {
        LongSimTime t0 = LongSimTime.builder().t(0L).build();
        Map<String, ActorRef<DevsMessage>> modelSimulators = new HashMap<>();
        PDevsCouplings couplings = buildCouplings();
        return Collections.singletonMap(improvedExperimentalFrameIdentifier, Behaviors.setup(context -> {
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
                buildImprovedInventoryRoutingFactory().create(improvedInventoryRoutingIdentifier).entrySet().stream().forEach(entry -> {
                    ActorRef<DevsMessage> coupledModelRef = context.spawn(entry.getValue(), ModelUtils.toLegalActorName(entry.getKey()));
                    context.watch(coupledModelRef);
                    modelSimulators.put(entry.getKey(), coupledModelRef);
                });
            return new ImprovedExperimentalFrame(improvedExperimentalFrameIdentifier, modelSimulators, couplings, context);
        }));
    }


}
