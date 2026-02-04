

package iso.example.irpsystem.irpmodel.ExperimentalFrame;

import devs.InputCouplingHandler;
import devs.iso.PortValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import iso.example.irpsystem.irpdomain.*;
import iso.example.irpsystem.irpmodel.InventoryRouting.*;


public class ExperimentalFrameInputCouplingHandler extends InputCouplingHandler {

    public ExperimentalFrameInputCouplingHandler() {
      super(Optional.empty());
    }

    @Override
    public void handlePortValue(PortValue<?> portValue, Map<String, List<PortValue<?>>> receiverMap) {

        
    }

    protected String[] determineTargetModels(PortValue<?> fromPortValue) {
        return switch (fromPortValue.getPortName()) {
            case "postDeliverySchedule" -> new String[] {InventoryRouting.modelIdentifier};
            case "reportVehicleCost" -> new String[] {Transducer.modelIdentifier};
            case "reportInventoryCost" -> new String[] {Transducer.modelIdentifier};
            default -> throw new IllegalArgumentException(
                    "Could not identify target model from PortValue with identifier " +
                            fromPortValue.getPortName());
        };
    }

}
