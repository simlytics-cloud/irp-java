

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

public abstract class AbstractManufacturerTest<A> extends DevsModelTest<LongSimTime> {


    protected class TestGenerator extends Generator<LongSimTime> {

        public static String modelIdentifier = "testGenerator";

    public static final ImmutablePort<ImmutableDeliverySchedule> toAcceptDeliverySchedule = new ImmutablePort<>("toAcceptDeliverySchedule", ImmutableDeliverySchedule.class);
    public static final ImmutablePort<ImmutableDelivery> toAcceptDelivery = new ImmutablePort<>("toAcceptDelivery", ImmutableDelivery.class);

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
    public static final ImmutablePort<ImmutableDeliveryRoute> fromPostDeliveryRoute = new ImmutablePort<>("fromPostDeliveryRoute", ImmutableDeliveryRoute.class);
    public static final ImmutablePort<ImmutableInventoryCost> fromDailyInventoryCost = new ImmutablePort<>("fromDailyInventoryCost", ImmutableInventoryCost.class);
         

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

        if (pvOut.getPortName().equals(TestGenerator.toAcceptDeliverySchedule.getPortName())) {
          ImmutableDeliverySchedule inputValue = TestGenerator.toAcceptDeliverySchedule.getValue(pvOut);
          PortValue<?> pvIn = Manufacturer.acceptDeliverySchedule.createPortValue(inputValue);
          addInputPortValue(pvIn, Manufacturer.modelIdentifier, receiverMap);      
      } else if (pvOut.getPortName().equals(TestGenerator.toAcceptDelivery.getPortName())) {
          ImmutableDelivery inputValue = TestGenerator.toAcceptDelivery.getValue(pvOut);
          PortValue<?> pvIn = Manufacturer.acceptDelivery.createPortValue(inputValue);
          addInputPortValue(pvIn, Manufacturer.modelIdentifier, receiverMap);      
      } 
        else {
          throw new IllegalArgumentException(
            "Did not expect an input from TestGenerator with port name "
           + pvOut.getPortName());
        }

      }
 else 
      if (sender.equals(Manufacturer.modelIdentifier)) {

        if (pvOut.getPortName().equals(Manufacturer.postDeliveryRoute.getPortName())) {
          ImmutableDeliveryRoute inputValue = Manufacturer.postDeliveryRoute.getValue(pvOut);
          PortValue<?> pvIn = TestAcceptor.fromPostDeliveryRoute.createPortValue(inputValue);
          addInputPortValue(pvIn, TestAcceptor.modelIdentifier, receiverMap);      
      } else if (pvOut.getPortName().equals(Manufacturer.dailyInventoryCost.getPortName())) {
          ImmutableInventoryCost inputValue = Manufacturer.dailyInventoryCost.getValue(pvOut);
          PortValue<?> pvIn = TestAcceptor.fromDailyInventoryCost.createPortValue(inputValue);
          addInputPortValue(pvIn, TestAcceptor.modelIdentifier, receiverMap);      
      }
        else {
          throw new IllegalArgumentException(
            "Did not expect an input from Manufacturer with port name "
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
