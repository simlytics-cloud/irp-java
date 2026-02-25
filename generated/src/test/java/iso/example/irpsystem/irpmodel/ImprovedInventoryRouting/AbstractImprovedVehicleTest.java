

package iso.example.irpsystem.irpmodel.ImprovedInventoryRouting;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import devs.PDevsCouplings;
import devs.experimentalframe.Acceptor;
import devs.experimentalframe.Generator;
import devs.iso.PortValue;
import devs.iso.time.LongSimTime;
import devs.msg.mutability.ImmutablePort;
import devs.msg.state.ScheduleState;
import iso.example.irpsystem.irpmodel.DevsModelTest;
import iso.example.irpsystem.irpdomain.*;
import iso.example.irpsystem.irpmodel.InventoryRouting.*;

public abstract class AbstractImprovedVehicleTest<A> extends DevsModelTest<LongSimTime> {

  protected static final String testGeneratorIdentifier = "testGenerator";
  protected static final String testAcceptorIdentifier = "testAcceptor";
  protected static final String modelIdentifier = "improvedVehicle";
  protected static final String coupledModelName = "improvedVehicleCoupledModelTest";


    protected class TestGenerator extends Generator<LongSimTime> {

    public static final ImmutablePort<ImmutableDeliveryRoute> toAcceptDeliveryRoute = new ImmutablePort<>("toAcceptDeliveryRoute", ImmutableDeliveryRoute.class);

        protected TestGenerator() {
          super("testGenerator", buildGeneratorState());
        }

        @Override
        public void handleScheduledEvents(List<Object> events) {

        }
    }  
    @Override
    protected Generator<LongSimTime> buildGenerator() {
        return new TestGenerator();      
    }

    protected abstract ScheduleState<LongSimTime> buildGeneratorState();



    protected class TestAcceptor extends Acceptor<LongSimTime, A> {
    
    public static final ImmutablePort<ImmutableDelivery> fromDropDelivery = new ImmutablePort<>("fromDropDelivery", ImmutableDelivery.class);
    public static final ImmutablePort<ImmutableVehicleCost> fromDailyDeliveryCost = new ImmutablePort<>("fromDailyDeliveryCost", ImmutableVehicleCost.class);
         

        private final AtomicReference<Throwable> failureRef;
        
        public TestAcceptor(AtomicReference<Throwable> failureRef) {
          super(buildAcceptorState(), "testAcceptor");
          this.failureRef = failureRef;
        }

        @Override
        public void internalStateTransitionFunction() {

        }

        @Override
        public void externalStateTransitionFunction(LongSimTime elapsedTime, List<PortValue<?>> inputs) {
          try {
            handleAcceptorInput(elapsedTime, modelState, inputs);
          } catch (Throwable t) {
            // Record first failure so the test thread can fail deterministically.
            failureRef.compareAndSet(null, t);
          }
        }

        @Override
        public LongSimTime timeAdvanceFunction() {
            return LongSimTime.buildMaxValue();
        }

    }    
    @Override
    protected Acceptor<LongSimTime, ?> buldAcceptor(AtomicReference<Throwable> failureRef) {
        return new TestAcceptor(failureRef);
    }

    protected abstract A buildAcceptorState();

    public abstract void handleAcceptorInput(LongSimTime elapsedTime, A acceptorState, List<PortValue<?>> portValue);


  @Override
  protected PDevsCouplings buildCouplings() {
    PDevsCouplings couplings = PDevsCouplings.builder(coupledModelName)
        .addConnection(testGeneratorIdentifier, "toAcceptDeliveryRoute",
            modelIdentifier, "acceptDeliveryRoute")
        .addConnection(modelIdentifier, "dropDelivery",
            testAcceptorIdentifier, "fromDropDelivery")
        .addConnection(modelIdentifier, "dailyDeliveryCost",
            testAcceptorIdentifier, "fromDailyDeliveryCost")
        .build();
    return couplings;
  }

}
