package iso.example.irpsystem.irpmodel.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public record IrpData(
    int numNodes,
    int numTimePeriods,
    double vehicleCapacity,
    double vehicleCostPerKm,
    double vehicleSpeedKmHr,
    int numVehicles,
    String coordinatorServer,
    String coordinatorTopic,
    List<String> participants,
    Map<String, String> vehicleHosts,
    ManufacturerData manufacturer,
    List<RetailerData> retailers
) {
    public static IrpData read(String resourceName) {
        try (InputStream is = IrpData.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourceName);
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(is, IrpData.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

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
        double inventoryCost,
        String host
    ) {}

    public record RetailerData(
        int id,
        double x,
        double y,
        double startingInventory,
        double maxInventory,
        double minInventory,
        double dailyConsumption,
        double inventoryCost,
        String host
    ) {}
}
