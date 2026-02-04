

package iso.example.irpsystem.irpmodel.InventoryRouting;

import devs.msg.state.ScheduleState;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import devs.OutputCouplingHandler;
import devs.experimentalframe.Acceptor;
import devs.experimentalframe.Generator;
import devs.iso.PortValue;
import devs.iso.time.LongSimTime;
import devs.msg.mutability.ImmutablePort;
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
        public void internalStateTransitionFunction() {
            LongSimTime currentTime = modelState.getCurrentTime().plus(timeAdvanceFunction());
            modelState.setCurrentTime(currentTime);
            modelState.getSchedule().removeCurrentScheduledOutput(currentTime);
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
