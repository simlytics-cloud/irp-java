

package iso.example.irpsystem.irpmodel.BasicInventoryRouting;

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


public abstract class AbstractBasicInventoryRoutingFactory {
    public static final String manufacturerIdentifier = "manufacturer";
    public static final String vehiclesIdentifier = "vehicles";
    public static final String retailersIdentifier = "retailers";
    public static final String basicInventoryRoutingIdentifier = "basicInventoryRouting";

  protected PDevsCouplings buildCouplings() {
    PDevsCouplings couplings = PDevsCouplings.builder("BasicInventoryRouting")
    .addConnection("basicInventoryRouting", InventoryRouting.receiveDeliverySchedule.getPortName(),
        "manufacturer", Manufacturer.acceptDeliverySchedule.getPortName())
    .addConnection("manufacturer", Manufacturer.postDeliveryRoute.getPortName(),
        "vehicles", Vehicle.acceptDeliveryRoute.getPortName())
    .addConnection("vehicles", Vehicle.dropDelivery.getPortName(),
        "retailers", Retailer.receiveDelivery.getPortName())
    .addConnection("manufacturer", Retailer.dailyInventoryCost.getPortName(),
        "basicInventoryRouting", InventoryRouting.reportInventoryCost.getPortName())
    .addConnection("retailers", Manufacturer.dailyInventoryCost.getPortName(),
        "basicInventoryRouting", InventoryRouting.reportInventoryCost.getPortName())
    .addConnection("vehicles", Vehicle.dailyDeliveryCost.getPortName(),
        "basicInventoryRouting", InventoryRouting.reportVehicleCost.getPortName())
        .build();
    return couplings;
  }


    protected abstract List<Manufacturer> buildManufacturers();

    protected abstract List<Vehicle> buildVehicles();

    protected abstract List<Retailer> buildRetailers();


    public Map<String, Behavior<DevsMessage>> create(String parentIdentifier) {
        LongSimTime t0 = LongSimTime.builder().t(0L).build();
        Map<String, ActorRef<DevsMessage>> modelSimulators = new HashMap<>();
        PDevsCouplings couplings = buildCouplings();
        return Collections.singletonMap(basicInventoryRoutingIdentifier, Behaviors.setup(context -> {
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

            return new BasicInventoryRouting(basicInventoryRoutingIdentifier, modelSimulators, couplings, context);
        }));
    }


}
