package iso.example.irpsystem.irpmodel.app;

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

    public InventoryRoutingAppMain(ActorContext<DevsMessage> context,
        LongSimTime startTime,
        LongSimTime endTime,
        CoupledModelFactory<LongSimTime> experimentalFrameFactory
    ) {
        super(context);
        actorSystem = context.getSystem();
        CompletionStage<Done> shutdowCompletionStage = actorSystem.getWhenTerminated();
        shutdowCompletionStage.toCompletableFuture().whenComplete((done, e) -> {
            if (e == null) {
                System.exit(0);
            } else {
                e.printStackTrace();
                System.exit(1);
            }
        });
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
        CoupledModelFactory<LongSimTime> experimentalFrameFactory) {
        return Behaviors.setup(context -> 
            new InventoryRoutingAppMain(context, startTime, endTime, experimentalFrameFactory));

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
