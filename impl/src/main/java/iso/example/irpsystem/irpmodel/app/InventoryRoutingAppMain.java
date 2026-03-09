package iso.example.irpsystem.irpmodel.app;

import com.typesafe.config.Config;
import devs.PDEVSModel;
import devs.PDevsSimulator;
import devs.proxy.KafkaDevsStreamProxy;
import devs.proxy.KafkaReceiver;
import iso.example.irpsystem.irpmodel.impl.BasicExperimentalFrameFactory;
import iso.example.irpsystem.irpmodel.impl.BasicInventoryRoutingFactory;
import iso.example.irpsystem.irpmodel.impl.IrpData;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.apache.pekko.Done;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.Terminated;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.apache.pekko.actor.typed.javadsl.ReceiveBuilder;

import devs.CoupledModelFactory;
import devs.RootCoordinator;
import devs.iso.DevsMessage;
import devs.iso.SimulationInit;
import devs.iso.time.LongSimTime;

public class InventoryRoutingAppMain extends AbstractBehavior <DevsMessage> {

    private ActorRef<DevsMessage> rootCoordinator;
    private ActorSystem<Void> actorSystem;
    private final String localSystemName;
    private final String localProxyName;


    public InventoryRoutingAppMain(ActorContext<DevsMessage> context,
        LongSimTime startTime,
        LongSimTime endTime,
        CoupledModelFactory<LongSimTime> experimentalFrameFactory,
        String localSystemName,
        String localProxyName,
        Config kafkaClusterConfig,
        Config kafkaConsumerConfig,
        IrpData irpData
    ) {
        super(context);
        actorSystem = context.getSystem();
      this.localSystemName = localSystemName;
      this.localProxyName = localProxyName;
      CompletionStage<Done> shutdowCompletionStage = actorSystem.getWhenTerminated();
        shutdowCompletionStage.toCompletableFuture().whenComplete((done, e) -> {
            if (e == null) {
                System.exit(0);
            } else {
                e.printStackTrace();
                System.exit(1);
            }
        });
        runLocalModels(irpData, kafkaClusterConfig, kafkaConsumerConfig);
        ActorRef<DevsMessage> experimentalFrame = context.spawn(experimentalFrameFactory.create(startTime), "inventoryRoutingApp");
        rootCoordinator = context.spawn(RootCoordinator.create(endTime, experimentalFrame,
            BasicExperimentalFrameFactory.basicExperimentalFrameIdentifier), "root");
        context.watch(rootCoordinator);
        rootCoordinator.tell(SimulationInit.builder()
            .eventTime(startTime)
            .simulationId("InventoryRoutingApp")
            .messageId(java.util.UUID.randomUUID().toString())
            .senderId("InventoryRoutingApp")
            .receiverId("root")
            .build());
    }

    public static Behavior<DevsMessage> create(LongSimTime startTime,
        LongSimTime endTime,
        CoupledModelFactory<LongSimTime> experimentalFrameFactory,
        String localSystemName,
        String localProxyName,
        Config kafkaClusterConfig,
        Config kafkaConsumerConfig,
        IrpData irpData) {
        return Behaviors.setup(context -> 
            new InventoryRoutingAppMain(
                context,
                startTime,
                endTime,
                experimentalFrameFactory,
                localSystemName,
                localProxyName,
                kafkaClusterConfig,
                kafkaConsumerConfig,
                irpData));

    }

    protected void runLocalModels(IrpData irpData
        , Config kafkaClusterConfig, Config kafkaConsumerConfig) {

        Map<String, ActorRef<DevsMessage>> coordinatorProxies = new HashMap<>();

        for (IrpData.RetailerData retailerData : irpData.retailers()) {
            String modelId = "retailer" + retailerData.id();
            if (localProxyName.equals(retailerData.host())) {
                createLocalProxy(modelId, BasicInventoryRoutingFactory.buildRetailer(retailerData),
                    kafkaClusterConfig, kafkaConsumerConfig, coordinatorProxies);
            }
        }

        if (localProxyName.equals(irpData.manufacturer().host())) {
            String modelId = BasicInventoryRoutingFactory.manufacturerIdentifier;
            createLocalProxy(modelId, BasicInventoryRoutingFactory.buildManufacturer(irpData),
                kafkaClusterConfig, kafkaConsumerConfig, coordinatorProxies);
        }
        
        for (String vehicleId : irpData.vehicleHosts().keySet()) {
            if (localProxyName.equals(irpData.vehicleHosts().get(vehicleId))) {
                int id = Integer.parseInt(vehicleId.replace("vehicle", ""));
                createLocalProxy(vehicleId, BasicInventoryRoutingFactory.buildVehicle(id, irpData),
                    kafkaClusterConfig, kafkaConsumerConfig, coordinatorProxies);
            }
        }
    }

    private void createLocalProxy(String modelId, PDEVSModel<LongSimTime, ?> retailer, Config kafkaClusterConfig,
        Config kafkaConsumerConfig, Map<String, ActorRef<DevsMessage>> coordinatorProxies) {
        
        String topic = "irp-system"; // Default topic
        
        // Create a proxy for the coordinator if it doesn't exist
        if (!coordinatorProxies.containsKey(topic)) {
            ActorRef<DevsMessage> coordinatorProxy =
                getContext().spawn(
                    KafkaDevsStreamProxy.create(BasicInventoryRoutingFactory.basicInventoryRoutingIdentifier,
                        topic,
                        kafkaClusterConfig), modelId + "InventoryRoutingCoordinatorProxy");
            coordinatorProxies.put(topic, coordinatorProxy);
        }

        ActorRef<DevsMessage> retailerSimulator = getContext().spawn(
            PDevsSimulator.create(retailer, LongSimTime.create(0)), modelId + "Simulator");


        getContext().spawn(
            KafkaReceiver.create(retailerSimulator, coordinatorProxies.get(topic), modelId, kafkaConsumerConfig,
                topic), modelId + "Receiver");
    }


    @Override
    public Receive<DevsMessage> createReceive() {
        ReceiveBuilder<DevsMessage> receiveBuilder = newReceiveBuilder();
        receiveBuilder.onSignal(Terminated.class, this::onTerminated);
        return receiveBuilder.build();
    }

    private Behavior<DevsMessage> onTerminated(Terminated terminated) {
        return Behaviors.stopped();
    }
    

}
