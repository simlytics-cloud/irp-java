

package iso.example.irpsystem.irpmodel.impl;

import devs.OutputCouplingHandler;
import devs.iso.PortValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import iso.example.irpsystem.irpdomain.*;
import iso.example.irpsystem.irpmodel.InventoryRouting.InventoryRouting;
import iso.example.irpsystem.irpmodel.InventoryRouting.InventoryRoutingOutputCouplingHandler;
import iso.example.irpsystem.irpmodel.InventoryRouting.Manufacturer;
import iso.example.irpsystem.irpmodel.InventoryRouting.Retailer;
import iso.example.irpsystem.irpmodel.InventoryRouting.Vehicle;


public class MultipleVehicleOutputCouplingHandler extends InventoryRoutingOutputCouplingHandler {

    public MultipleVehicleOutputCouplingHandler() {
        super();
    }

static String eval(Object o) {
    return switch (o) {
        case String s when s.isEmpty() -> "empty";
        case String s -> switch (s.charAt(0)) {
            case 'a', 'e', 'i', 'o', 'u' -> "starts with vowel";
            default -> "starts with consonant";
        };
        default -> "other";
    };
}

    
    protected String[] determineTargetModels(String sender, PortValue<?> fromPortValue) {
        return switch (sender) {
            case "manufacturer" -> switch (fromPortValue.getPortName()) {
                case "postDeliveryRoute" -> new String[] {Vehicle.modelIdentifier 
                + Manufacturer.postDeliveryRoute.getValue(fromPortValue).getVehicleId()};
                case "dailyDeliveryCost" -> new String[] {InventoryRouting.modelIdentifier};                
                case null, default -> throw new IllegalArgumentException(
                    "Could not identify target model from PortValue with sender " + sender
                        + " and identifier " + fromPortValue.getPortName());
            };
            case String s when s.startsWith("vehicle") -> switch (fromPortValue.getPortName()) {
                case "dropDelivery" -> {
                    int retailerId = Vehicle.dropDelivery.getValue(fromPortValue).getRetailerId();
                    if (retailerId == 0) {
                        yield new String[]{Manufacturer.modelIdentifier};
                    } else {
                    yield new String[] {Retailer.modelIdentifier
                        + Vehicle.dropDelivery.getValue(fromPortValue).getRetailerId()};
                    }
                }
                case "dailyDeliveryCost" -> new String[] {InventoryRouting.modelIdentifier};
                case null, default -> throw new IllegalArgumentException(
                    "Could not identify target model from PortValue with sender " + sender
                        + " and identifier " + fromPortValue.getPortName());
            };
            case String s when s.startsWith("retailer") -> switch (fromPortValue.getPortName()) {
                case "dailyInventoryCost" -> new String[] {InventoryRouting.modelIdentifier};
                case null, default -> throw new IllegalArgumentException(
                    "Could not identify target model from PortValue with sender " + sender
                        + " and identifier " + fromPortValue.getPortName());
            };
            default -> throw new IllegalArgumentException(
                    "Could not identify target model from PortValue with sender " + sender
                        + " and identifier " + fromPortValue.getPortName());
        };
    }

}
