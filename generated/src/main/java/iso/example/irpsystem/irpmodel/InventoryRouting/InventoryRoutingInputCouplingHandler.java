

package iso.example.irpsystem.irpmodel.InventoryRouting;

import devs.InputCouplingHandler;
import devs.iso.PortValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import iso.example.irpsystem.irpdomain.*;


public class InventoryRoutingInputCouplingHandler extends InputCouplingHandler {

    public InventoryRoutingInputCouplingHandler() {
      super(Optional.empty());
    }

    @Override
    public void handlePortValue(PortValue<?> portValue, Map<String, List<PortValue<?>>> receiverMap) {

        if (portValue.getPortName().equals(InventoryRouting.acceptDeliverySchedule.getPortName())) {
            PortValue<ImmutableDeliverySchedule> flowPortValue = Manufacturer.acceptDeliverySchedule.createPortValue(
                InventoryRouting.acceptDeliverySchedule.getValue(portValue));
            String[] targetModels = determineTargetModels(portValue);
            for (String targetModel: targetModels) {
                addInputPortValue(flowPortValue, targetModel, receiverMap);
            }
        }
        else {
            throw new IllegalArgumentException("Could not handle PortValue with identifier " + portValue.getPortName());
        }

    }

    protected String[] determineTargetModels(PortValue<?> fromPortValue) {
        return switch (fromPortValue.getPortName()) {
            case "acceptDeliverySchedule" -> new String[] {Manufacturer.modelIdentifier};
            case "postDeliveryRoute" -> new String[] {Vehicle.modelIdentifier};
            case "dropDelivery" -> new String[] {Retailer.modelIdentifier};
            case "dailyInventoryCost" -> new String[] {InventoryRouting.modelIdentifier};
            case "dailyDeliveryCost" -> new String[] {InventoryRouting.modelIdentifier};
            default -> throw new IllegalArgumentException(
                    "Could not identify target model from PortValue with identifier " +
                            fromPortValue.getPortName());
        };
    }

}
