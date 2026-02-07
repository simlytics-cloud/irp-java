

package iso.example.irpsystem.irpmodel.InventoryRouting;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import devs.OutputCouplingHandler;
import devs.experimentalframe.Acceptor;
import devs.experimentalframe.Generator;
import devs.iso.PortValue;
import devs.iso.time.LongSimTime;
import devs.msg.mutability.ImmutablePort;
import devs.msg.state.ScheduleState;
import iso.example.irpsystem.irpmodel.DevsModelTest;
import iso.example.irpsystem.irpdomain.*;

public abstract class AbstractVehicleTest<A> extends DevsModelTest<LongSimTime> {


    protected class TestGenerator extends Generator<LongSimTime> {

        public static String modelIdentifier = "testGenerator";

    public static final ImmutablePort<ImmutableDeliveryRoute> toAcceptDeliveryRoute = new ImmutablePort<>("toAcceptDeliveryRoute", ImmutableDeliveryRoute.class);

        protected TestGenerator() {
          super(modelIdentifier, buildGeneratorState());
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
        static String modelIdentifier = "testAcceptor";
    public static final ImmutablePort<ImmutableDelivery> fromDropDelivery = new ImmutablePort<>("fromDropDelivery", ImmutableDelivery.class);
    public static final ImmutablePort<ImmutableVehicleCost> fromDailyDeliveryCost = new ImmutablePort<>("fromDailyDeliveryCost", ImmutableVehicleCost.class);
         

        private final AtomicReference<Throwable> failureRef;
        
        public TestAcceptor(AtomicReference<Throwable> failureRef) {
          super(buildAcceptorState(), modelIdentifier);
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
  protected OutputCouplingHandler buildTestOutputCouplings() {
    final class TestOutputCouplingHandler extends OutputCouplingHandler {

    public TestOutputCouplingHandler() {
      super(Optional.empty(), Optional.empty(), Optional.empty());
    }

    @Override
    public void handlePortValue(String sender, PortValue<?> pvOut,
      Map<String, List<PortValue<?>>> receiverMap, List<PortValue<?>> outputMessages) {
      
      if (sender.equals(TestGenerator.modelIdentifier)) {

        if (pvOut.getPortName().equals(TestGenerator.toAcceptDeliveryRoute.getPortName())) {
          ImmutableDeliveryRoute inputValue = TestGenerator.toAcceptDeliveryRoute.getValue(pvOut);
          PortValue<?> pvIn = Vehicle.acceptDeliveryRoute.createPortValue(inputValue);
          addInputPortValue(pvIn, Vehicle.modelIdentifier, receiverMap);      
      } 
        else {
          throw new IllegalArgumentException(
            "Did not expect an input from TestGenerator with port name "
           + pvOut.getPortName());
        }

      }
 else 
      if (sender.equals(Vehicle.modelIdentifier)) {

        if (pvOut.getPortName().equals(Vehicle.dropDelivery.getPortName())) {
          ImmutableDelivery inputValue = Vehicle.dropDelivery.getValue(pvOut);
          PortValue<?> pvIn = TestAcceptor.fromDropDelivery.createPortValue(inputValue);
          addInputPortValue(pvIn, TestAcceptor.modelIdentifier, receiverMap);      
      } else if (pvOut.getPortName().equals(Vehicle.dailyDeliveryCost.getPortName())) {
          ImmutableVehicleCost inputValue = Vehicle.dailyDeliveryCost.getValue(pvOut);
          PortValue<?> pvIn = TestAcceptor.fromDailyDeliveryCost.createPortValue(inputValue);
          addInputPortValue(pvIn, TestAcceptor.modelIdentifier, receiverMap);      
      }
        else {
          throw new IllegalArgumentException(
            "Did not expect an input from Vehicle with port name "
           + pvOut.getPortName());
        }

      }
 else {
        throw new IllegalArgumentException(
          "Did not expect an input from model with identifier " + sender);
        }
       }
    }
      return new TestOutputCouplingHandler();
    }

}
