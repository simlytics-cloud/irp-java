package iso.example.irpsystem.irpmodel.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record IrpData(
    int numNodes,
    int numTimePeriods,
    double vehicleCapacity,
    double vehicleCostPerKm,
    double vehicleSpeekKmHr,
    int numVehicles,
    ManufacturerData manufacturer,
    List<RetailerData> retailers
) {
    public static IrpData read(Path filePath) {
        try {
            String json = Files.readString(filePath);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, IrpData.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public record ManufacturerData(
        int id,
        double x,
        double y,
        double startingInventory,
        double dailyProduction,
        double inventoryCost
    ) {}

    public record RetailerData(
        int id,
        double x,
        double y,
        double startingInventory,
        double maxInventory,
        double minInventory,
        double dailyConsumption,
        double inventoryCost
    ) {}
}
