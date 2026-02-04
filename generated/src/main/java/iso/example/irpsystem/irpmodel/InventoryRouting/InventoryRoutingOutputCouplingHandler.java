

package iso.example.irpsystem.irpmodel.InventoryRouting;

import devs.OutputCouplingHandler;
import devs.iso.PortValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import iso.example.irpsystem.irpdomain.*;


public class InventoryRoutingOutputCouplingHandler extends OutputCouplingHandler {

    public InventoryRoutingOutputCouplingHandler() {
        super(Optional.empty(), Optional.empty(), Optional.empty());
    }

    @Override
    public void handlePortValue(String sender, PortValue<?> portValue,
                                Map<String, List<PortValue<?>>> receiverMap,
                                List<PortValue<?>> outputMessages) {

        if (portValue.getPortName().equals(Manufacturer.postDeliveryRoute.getPortName())) {
            PortValue<ImmutableDeliveryRoute> flowPortValue = Vehicle.acceptDeliveryRoute.createPortValue(
                Manufacturer.postDeliveryRoute.getValue(portValue));
            
            String[] targetModels = determineTargetModels(sender, portValue);
            for (String targetModel: targetModels) {
                addInputPortValue(flowPortValue, targetModel, receiverMap);
            }

        } else if (portValue.getPortName().equals(Vehicle.dropDelivery.getPortName())) {
            PortValue<ImmutableDelivery> flowPortValue = Retailer.receiveDelivery.createPortValue(
                Vehicle.dropDelivery.getValue(portValue));
            
            String[] targetModels = determineTargetModels(sender, portValue);
            for (String targetModel: targetModels) {
                addInputPortValue(flowPortValue, targetModel, receiverMap);
            }

        } else if (portValue.getPortName().equals(Retailer.dailyInventoryCost.getPortName())) {
            PortValue<ImmutableInventoryCost> flowPortValue = InventoryRouting.reportInventoryCost.createPortValue(
                Retailer.dailyInventoryCost.getValue(portValue));
            outputMessages.add(flowPortValue);
        } else if (portValue.getPortName().equals(Manufacturer.dailyInventoryCost.getPortName())) {
            PortValue<ImmutableInventoryCost> flowPortValue = InventoryRouting.reportInventoryCost.createPortValue(
                Manufacturer.dailyInventoryCost.getValue(portValue));
            outputMessages.add(flowPortValue);
        } else if (portValue.getPortName().equals(Vehicle.dailyDeliveryCost.getPortName())) {
            PortValue<ImmutableVehicleCost> flowPortValue = InventoryRouting.reportVehicleCost.createPortValue(
                Vehicle.dailyDeliveryCost.getValue(portValue));
            outputMessages.add(flowPortValue);
        }
        else {
            throw new IllegalArgumentException("Could not handle PortValue with identifier " + portValue.getPortName());
        }

    }

    protected String[] determineTargetModels(String sender, PortValue<?> fromPortValue) {
        String routingString = sender + "--" + fromPortValue.getPortName();
        return switch (routingString) {
            case "manufacturer--postDeliveryRoute" -> new String[] {Vehicle.modelIdentifier};
            case "vehicle--dropDelivery" -> new String[] {Retailer.modelIdentifier};
            case "retailer--dailyInventoryCost" -> new String[] {InventoryRouting.modelIdentifier};
            case "manufacturer--dailyInventoryCost" -> new String[] {InventoryRouting.modelIdentifier};
            case "vehicle--dailyDeliveryCost" -> new String[] {InventoryRouting.modelIdentifier};
            default -> throw new IllegalArgumentException(
                    "Could not identify target model from PortValue with sender " + sender
                        + " and identifier " + fromPortValue.getPortName());
        };
    }

}
