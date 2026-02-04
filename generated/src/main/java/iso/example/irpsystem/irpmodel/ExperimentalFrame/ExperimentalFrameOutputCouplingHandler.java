

package iso.example.irpsystem.irpmodel.ExperimentalFrame;

import devs.OutputCouplingHandler;
import devs.iso.PortValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import iso.example.irpsystem.irpdomain.*;
import iso.example.irpsystem.irpmodel.InventoryRouting.*;


public class ExperimentalFrameOutputCouplingHandler extends OutputCouplingHandler {

    public ExperimentalFrameOutputCouplingHandler() {
        super(Optional.empty(), Optional.empty(), Optional.empty());
    }

    @Override
    public void handlePortValue(String sender, PortValue<?> portValue,
                                Map<String, List<PortValue<?>>> receiverMap,
                                List<PortValue<?>> outputMessages) {

        if (portValue.getPortName().equals(DeliveryScheduleGenerator.postDeliverySchedule.getPortName())) {
            PortValue<ImmutableDeliverySchedule> flowPortValue = InventoryRouting.acceptDeliverySchedule.createPortValue(
                DeliveryScheduleGenerator.postDeliverySchedule.getValue(portValue));
            
            String[] targetModels = determineTargetModels(sender, portValue);
            for (String targetModel: targetModels) {
                addInputPortValue(flowPortValue, targetModel, receiverMap);
            }

        } else if (portValue.getPortName().equals(InventoryRouting.reportVehicleCost.getPortName())) {
            PortValue<ImmutableVehicleCost> flowPortValue = Transducer.aggregateVehicleCost.createPortValue(
                InventoryRouting.reportVehicleCost.getValue(portValue));
            
            String[] targetModels = determineTargetModels(sender, portValue);
            for (String targetModel: targetModels) {
                addInputPortValue(flowPortValue, targetModel, receiverMap);
            }

        } else if (portValue.getPortName().equals(InventoryRouting.reportInventoryCost.getPortName())) {
            PortValue<ImmutableInventoryCost> flowPortValue = Transducer.aggregateInventoryCost.createPortValue(
                InventoryRouting.reportInventoryCost.getValue(portValue));
            
            String[] targetModels = determineTargetModels(sender, portValue);
            for (String targetModel: targetModels) {
                addInputPortValue(flowPortValue, targetModel, receiverMap);
            }

        }
        else {
            throw new IllegalArgumentException("Could not handle PortValue with identifier " + portValue.getPortName());
        }

    }

    protected String[] determineTargetModels(String sender, PortValue<?> fromPortValue) {
        String routingString = sender + "--" + fromPortValue.getPortName();
        return switch (routingString) {
            case "deliveryScheduleGenerator--postDeliverySchedule" -> new String[] {InventoryRouting.modelIdentifier};
            case "inventoryRouting--reportVehicleCost" -> new String[] {Transducer.modelIdentifier};
            case "inventoryRouting--reportInventoryCost" -> new String[] {Transducer.modelIdentifier};
            default -> throw new IllegalArgumentException(
                    "Could not identify target model from PortValue with sender " + sender
                        + " and identifier " + fromPortValue.getPortName());
        };
    }

}
