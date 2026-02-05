package iso.example.irpsystem.irpmodel.app;

import com.typesafe.config.Config;
import devs.PDevsSimulator;
import devs.proxy.KafkaDevsStreamProxy;
import devs.proxy.KafkaReceiver;
import iso.example.irpsystem.irpmodel.InventoryRouting.InventoryRouting;
import iso.example.irpsystem.irpmodel.InventoryRouting.Retailer;
import iso.example.irpsystem.irpmodel.impl.InventoryRoutingFactory;
import iso.example.irpsystem.irpmodel.impl.IrpData;
import iso.example.irpsystem.irpmodel.impl.RemoteModel;
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
import iso.example.irpsystem.irpmodel.ExperimentalFrame.ExperimentalFrame;

public class InventoryRoutingAppMain extends AbstractBehavior <DevsMessage> {

    private ActorRef<DevsMessage> rootCoordinator;
    private ActorSystem<Void> actorSystem;
    private final Map<String, RemoteModel> remoteModels;
    private final Config kafkaClusterConfig;
    private final Config kafkaConsumerConfig;
    private final IrpData irpData;


    public InventoryRoutingAppMain(ActorContext<DevsMessage> context,
        LongSimTime startTime,
        LongSimTime endTime,
        CoupledModelFactory<LongSimTime> experimentalFrameFactory,
        Map<String, RemoteModel> remoteModels,
        Config kafkaClusterConfig,
        Config kafkaConsumerConfig,
        IrpData irpData
    ) {
        super(context);
        actorSystem = context.getSystem();
      this.remoteModels = remoteModels;
      this.kafkaClusterConfig = kafkaClusterConfig;
      this.kafkaConsumerConfig = kafkaConsumerConfig;
      this.irpData = irpData;
      CompletionStage<Done> shutdowCompletionStage = actorSystem.getWhenTerminated();
        shutdowCompletionStage.toCompletableFuture().whenComplete((done, e) -> {
            if (e == null) {
                System.exit(0);
            } else {
                e.printStackTrace();
                System.exit(1);
            }
        });
        if (remoteModels != null && remoteModels.size() > 0) {
            runRemoteModelsLocally(irpData, kafkaClusterConfig, kafkaConsumerConfig);
        }
        ActorRef<DevsMessage> experimentalFrame = context.spawn(experimentalFrameFactory.create(startTime), "inventoryRoutingApp");
        rootCoordinator = context.spawn(RootCoordinator.create(endTime, experimentalFrame, ExperimentalFrame.modelIdentifier), "root");
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
        Map<String, RemoteModel> remoteModels,
        Config kafkaClusterConfig,
        Config kafkaConsumerConfig,
        IrpData irpData) {
        return Behaviors.setup(context -> 
            new InventoryRoutingAppMain(
                context,
                startTime,
                endTime,
                experimentalFrameFactory,
                remoteModels,
                kafkaClusterConfig,
                kafkaConsumerConfig,
                irpData));

    }

    protected void runRemoteModelsLocally(IrpData irpData
        , Config kafkaClusterConfig, Config kafkaConsumerConfig) {

        Map<String, ActorRef<DevsMessage>> coordinatorProxies = new HashMap<>();

        for (String modelId : remoteModels.keySet()) {
            if (modelId.startsWith("retailer")) {
                RemoteModel remoteModel = remoteModels.get(modelId);

                // Create a proxy for the coordinator if it doesn't exist
                if (!coordinatorProxies.containsKey(remoteModel.topic())) {
                    ActorRef<DevsMessage> coordinatorProxy =
                        getContext().spawn(
                            KafkaDevsStreamProxy.create(InventoryRouting.modelIdentifier,
                                "irp-system",
                                kafkaClusterConfig), "inventoryRoutingCoordinatorProxy");
                    coordinatorProxies.put(remoteModel.topic(), coordinatorProxy);
                }

                // Create the retailer
                char retailerId = modelId.charAt(modelId.length() - 1);
                int retailerIndex = Integer.parseInt(String.valueOf(retailerId)) - 1;
                Retailer retailer = InventoryRoutingFactory.buildRetailer(irpData.retailers()
                    .get(retailerIndex));

                ActorRef<DevsMessage> retailerSimulator = getContext().spawn(
                    PDevsSimulator.create(retailer, LongSimTime.create(0)), modelId + "Simulator");


                ActorRef<DevsMessage> retailerReceiver = getContext().spawn(
                    KafkaReceiver.create(retailerSimulator, coordinatorProxies.get(remoteModel.topic()), modelId, kafkaConsumerConfig,
                        remoteModel.topic()), modelId + "Receiver");
            }
        }


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
