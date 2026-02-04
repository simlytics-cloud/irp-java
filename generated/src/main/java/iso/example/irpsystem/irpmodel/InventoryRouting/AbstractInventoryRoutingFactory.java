

package iso.example.irpsystem.irpmodel.InventoryRouting;

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

import iso.example.irpsystem.irpdomain.*;


public abstract class AbstractInventoryRoutingFactory {

    protected List<InputCouplingHandler> buildInputCouplings() {
        return Collections.singletonList(new InventoryRoutingInputCouplingHandler());
    }
    protected List<OutputCouplingHandler> buildOutputCouplings() {
        return Collections.singletonList(new InventoryRoutingOutputCouplingHandler());
    }
    protected abstract List<Manufacturer> buildManufacturers();

    protected abstract List<Vehicle> buildVehicles();

    protected abstract List<Retailer> buildRetailers();


    public Map<String, Behavior<DevsMessage>> create(String parentIdentifier) {
        LongSimTime t0 = LongSimTime.builder().t(0L).build();
        Map<String, ActorRef<DevsMessage>> modelSimulators = new HashMap<>();
        PDevsCouplings couplings = new PDevsCouplings(
                    buildInputCouplings(), buildOutputCouplings());
        return Collections.singletonMap(InventoryRouting.modelIdentifier, Behaviors.setup(context -> {
                buildManufacturers().stream().forEach(devsModel -> {
                    ActorRef<DevsMessage> atomicModelRef = context.spawn(PDevsSimulator.create(
                        devsModel, t0), ModelUtils.toLegalActorName(devsModel.getModelIdentifier()));
                   context.watch(atomicModelRef);
                   modelSimulators.put(devsModel.getModelIdentifier(), atomicModelRef);
                });
                buildVehicles().stream().forEach(devsModel -> {
                    ActorRef<DevsMessage> atomicModelRef = context.spawn(PDevsSimulator.create(
                        devsModel, t0), ModelUtils.toLegalActorName(devsModel.getModelIdentifier()));
                   context.watch(atomicModelRef);
                   modelSimulators.put(devsModel.getModelIdentifier(), atomicModelRef);
                });
                buildRetailers().stream().forEach(devsModel -> {
                    ActorRef<DevsMessage> atomicModelRef = context.spawn(PDevsSimulator.create(
                        devsModel, t0), ModelUtils.toLegalActorName(devsModel.getModelIdentifier()));
                   context.watch(atomicModelRef);
                   modelSimulators.put(devsModel.getModelIdentifier(), atomicModelRef);
                });

            return new InventoryRouting(InventoryRouting.modelIdentifier, modelSimulators, couplings, context);
        }));
    }


}
