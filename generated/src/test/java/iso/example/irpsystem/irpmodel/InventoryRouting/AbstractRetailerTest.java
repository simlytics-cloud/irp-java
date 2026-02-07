

package iso.example.irpsystem.irpmodel.InventoryRouting;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import devs.OutputCouplingHandler;
import devs.experimentalframe.Acceptor;
import devs.experimentalframe.Generator;
import devs.iso.PortValue;
import devs.iso.time.LongSimTime;
import devs.msg.mutability.ImmutablePort;
import devs.msg.state.ScheduleState;
import iso.example.irpsystem.irpmodel.DevsModelTest;
import iso.example.irpsystem.irpdomain.*;

public abstract class AbstractRetailerTest<A> extends DevsModelTest<LongSimTime> {


    protected class TestGenerator extends Generator<LongSimTime> {

        public static String modelIdentifier = "testGenerator";

    public static final ImmutablePort<ImmutableDelivery> toReceiveDelivery = new ImmutablePort<>("toReceiveDelivery", ImmutableDelivery.class);

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
    public static final ImmutablePort<ImmutableInventoryCost> fromDailyInventoryCost = new ImmutablePort<>("fromDailyInventoryCost", ImmutableInventoryCost.class);
         

        public TestAcceptor() {
            super(buildAcceptorState(), modelIdentifier);
        }

        @Override
        public void internalStateTransitionFunction() {

        }

        @Override
        public void externalStateTransitionFunction(LongSimTime elapsedTime, List<PortValue<?>> inputs) {
            handleAcceptorInput(elapsedTime, modelState, inputs);
            
        }

        @Override
        public LongSimTime timeAdvanceFunction() {
            return LongSimTime.buildMaxValue();
        }

    }    
    @Override
    protected Acceptor<LongSimTime, ?> buldAcceptor() {
        return new TestAcceptor();
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

        if (pvOut.getPortName().equals(TestGenerator.toReceiveDelivery.getPortName())) {
          ImmutableDelivery inputValue = TestGenerator.toReceiveDelivery.getValue(pvOut);
          PortValue<?> pvIn = Retailer.receiveDelivery.createPortValue(inputValue);
          addInputPortValue(pvIn, Retailer.modelIdentifier, receiverMap);      
      } 
        else {
          throw new IllegalArgumentException(
            "Did not expect an input from TestGenerator with port name "
           + pvOut.getPortName());
        }

      }
 else 
      if (sender.equals(Retailer.modelIdentifier)) {

        if (pvOut.getPortName().equals(Retailer.dailyInventoryCost.getPortName())) {
          ImmutableInventoryCost inputValue = Retailer.dailyInventoryCost.getValue(pvOut);
          PortValue<?> pvIn = TestAcceptor.fromDailyInventoryCost.createPortValue(inputValue);
          addInputPortValue(pvIn, TestAcceptor.modelIdentifier, receiverMap);      
      }
        else {
          throw new IllegalArgumentException(
            "Did not expect an input from Retailer with port name "
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
