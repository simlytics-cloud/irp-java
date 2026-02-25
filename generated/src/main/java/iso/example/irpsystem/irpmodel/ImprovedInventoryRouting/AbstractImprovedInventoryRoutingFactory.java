

package iso.example.irpsystem.irpmodel.ImprovedInventoryRouting;

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
import iso.example.irpsystem.irpmodel.InventoryRouting.Manufacturer.*;
import iso.example.irpsystem.irpmodel.InventoryRouting.Vehicle.*;
import iso.example.irpsystem.irpmodel.InventoryRouting.Retailer.*;
import iso.example.irpsystem.irpmodel.InventoryRouting.*;


public abstract class AbstractImprovedInventoryRoutingFactory {
    public static final String improvedManufacturerIdentifier = "improvedManufacturer";
    public static final String improvedVehiclesIdentifier = "improvedVehicles";
    public static final String improvedRetailersIdentifier = "improvedRetailers";
    public static final String improvedInventoryRoutingIdentifier = "improvedInventoryRouting";

  protected PDevsCouplings buildCouplings() {
    PDevsCouplings couplings = PDevsCouplings.builder("ImprovedInventoryRouting")
    .addConnection("improvedInventoryRouting", InventoryRouting.receiveDeliverySchedule.getPortName(),
        "improvedManufacturer", Manufacturer.acceptDeliverySchedule.getPortName())
    .addConnection("improvedManufacturer", Manufacturer.postDeliveryRoute.getPortName(),
        "improvedVehicles", Vehicle.acceptDeliveryRoute.getPortName())
    .addConnection("improvedVehicles", Vehicle.dropDelivery.getPortName(),
        "improvedRetailers", Retailer.receiveDelivery.getPortName())
    .addConnection("improvedVehicles", Vehicle.dropDelivery.getPortName(),
        "improvedManufacturer", Manufacturer.acceptDelivery.getPortName())
    .addConnection("improvedManufacturer", Retailer.dailyInventoryCost.getPortName(),
        "improvedInventoryRouting", InventoryRouting.reportInventoryCost.getPortName())
    .addConnection("improvedRetailers", Manufacturer.dailyInventoryCost.getPortName(),
        "improvedInventoryRouting", InventoryRouting.reportInventoryCost.getPortName())
    .addConnection("improvedVehicles", Vehicle.dailyDeliveryCost.getPortName(),
        "improvedInventoryRouting", InventoryRouting.reportVehicleCost.getPortName())
        .build();
    return couplings;
  }


    protected abstract List<Manufacturer> buildManufacturers();

    protected abstract List<ImprovedVehicle> buildImprovedVehicles();

    protected abstract List<Retailer> buildRetailers();


    public Map<String, Behavior<DevsMessage>> create(String parentIdentifier) {
        LongSimTime t0 = LongSimTime.builder().t(0L).build();
        Map<String, ActorRef<DevsMessage>> modelSimulators = new HashMap<>();
        PDevsCouplings couplings = buildCouplings();
        return Collections.singletonMap(improvedInventoryRoutingIdentifier, Behaviors.setup(context -> {
                buildManufacturers().stream().forEach(devsModel -> {
                    ActorRef<DevsMessage> atomicModelRef = context.spawn(PDevsSimulator.create(
                        devsModel, t0), ModelUtils.toLegalActorName(devsModel.getModelIdentifier()));
                   context.watch(atomicModelRef);
                   modelSimulators.put(devsModel.getModelIdentifier(), atomicModelRef);
                });
                buildImprovedVehicles().stream().forEach(devsModel -> {
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

            return new ImprovedInventoryRouting(improvedInventoryRoutingIdentifier, modelSimulators, couplings, context);
        }));
    }


}
