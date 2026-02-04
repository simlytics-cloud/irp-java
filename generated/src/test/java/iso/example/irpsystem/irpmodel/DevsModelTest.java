package iso.example.irpsystem.irpmodel;


import devs.CoupledModelFactory;
import devs.OutputCouplingHandler;
import devs.PDevsCouplings;
import devs.RootCoordinator;
import devs.SimulatorProvider;
import devs.experimentalframe.Acceptor;
import devs.experimentalframe.Generator;
import devs.iso.DevsMessage;
import devs.iso.SimulationInit;
import devs.iso.time.SimTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit;
import org.apache.pekko.actor.testkit.typed.javadsl.TestProbe;
import org.apache.pekko.actor.typed.ActorRef;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public abstract class DevsModelTest<T extends SimTime> {

  protected final CoupledModelFactory<T> coupledModelFactory;
  protected final Generator<T> generator;
  protected final Acceptor<T, ?> acceptor;

  protected abstract Generator<T> buildGenerator();
  protected abstract Acceptor<T, ?> buldAcceptor();
  protected abstract SimulatorProvider<T> buildDevsModelProvider();
  protected abstract OutputCouplingHandler buildTestOutputCouplings();


  public DevsModelTest() {
    generator = buildGenerator();
    acceptor = buldAcceptor();
    List<SimulatorProvider<T>> simulatorProviders = new ArrayList<>();
    simulatorProviders.add(generator.getDevsSimulatorProvider());
    simulatorProviders.add(acceptor.getDevsSimulatorProvider());
    simulatorProviders.add(buildDevsModelProvider());
    PDevsCouplings couplings = new PDevsCouplings(Collections.emptyList(),
        Collections.singletonList(buildTestOutputCouplings()));
    coupledModelFactory = new CoupledModelFactory<T>(
        "vehicleImplTest",
        simulatorProviders,
        couplings);
  }


  protected void executeExperimentalFrame(T startTime, T endTime, String simulationId)
      throws InterruptedException {
    ActorTestKit testKit = ActorTestKit.create();
    ActorRef<DevsMessage> testFrame =
        testKit.spawn(coupledModelFactory.create(startTime), "vehicleImplTest");
    ActorRef<DevsMessage> rootCoordinator =
        testKit.spawn(RootCoordinator.create(endTime, testFrame, "vehicleImplTest"), "root");
    rootCoordinator.tell(SimulationInit.<T>builder()
        .eventTime(startTime)
        .simulationId(simulationId)
        .messageId("SimulationInit")
        .senderId("TestActor")
        .receiverId("root")
        .build());
    TestProbe<DevsMessage> testProbe = testKit.createTestProbe();
    testProbe.expectTerminated(rootCoordinator, Duration.ofSeconds(1000));
    testKit.shutdownTestKit();
  }

}