

package iso.example.irpsystem.irpmodel.BasicExperimentalFrame;

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
import iso.example.irpsystem.irpmodel.ExperimentalFrame.DeliveryScheduleGenerator.*;
import iso.example.irpsystem.irpmodel.InventoryRouting.*;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.Transducer.*;
import iso.example.irpsystem.irpmodel.BasicInventoryRouting.*;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.*;


public abstract class AbstractBasicExperimentalFrameFactory {
    public static final String generatorIdentifier = "generator";
    public static final String transducerIdentifier = "transducer";
    public static final String basicInventoryRoutingIdentifier = "basicInventoryRouting";
    public static final String basicExperimentalFrameIdentifier = "basicExperimentalFrame";

  protected PDevsCouplings buildCouplings() {
    PDevsCouplings couplings = PDevsCouplings.builder("BasicExperimentalFrame")
    .addConnection("generator", DeliveryScheduleGenerator.postDeliverySchedule.getPortName(),
        "basicInventoryRouting", InventoryRouting.receiveDeliverySchedule.getPortName())
    .addConnection("basicInventoryRouting", InventoryRouting.reportVehicleCost.getPortName(),
        "transducer", Transducer.aggregateVehicleCost.getPortName())
    .addConnection("basicInventoryRouting", InventoryRouting.reportInventoryCost.getPortName(),
        "transducer", Transducer.aggregateInventoryCost.getPortName())
        .build();
    return couplings;
  }


    protected abstract List<DeliveryScheduleGenerator> buildDeliveryScheduleGenerators();

    protected abstract List<Transducer> buildTransducers();

    protected abstract AbstractBasicInventoryRoutingFactory buildBasicInventoryRoutingFactory();


    public Map<String, Behavior<DevsMessage>> create(String parentIdentifier) {
        LongSimTime t0 = LongSimTime.builder().t(0L).build();
        Map<String, ActorRef<DevsMessage>> modelSimulators = new HashMap<>();
        PDevsCouplings couplings = buildCouplings();
        return Collections.singletonMap(basicExperimentalFrameIdentifier, Behaviors.setup(context -> {
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
                buildBasicInventoryRoutingFactory().create(basicInventoryRoutingIdentifier).entrySet().stream().forEach(entry -> {
                    ActorRef<DevsMessage> coupledModelRef = context.spawn(entry.getValue(), ModelUtils.toLegalActorName(entry.getKey()));
                    context.watch(coupledModelRef);
                    modelSimulators.put(entry.getKey(), coupledModelRef);
                });
            return new BasicExperimentalFrame(basicExperimentalFrameIdentifier, modelSimulators, couplings, context);
        }));
    }


}
