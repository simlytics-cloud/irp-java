package iso.example.irpsystem.irpmodel.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Data structure representing an Inventory Routing Problem (IRP) instance.
 * Contains configuration for nodes, time periods, vehicles, manufacturer, and retailers.
 * Also includes methods to read the data from JSON resources or files.
 *
 * @param numNodes          Total number of nodes (including manufacturer and retailers).
 * @param numTimePeriods    Number of days in the simulation.
 * @param vehicleCapacity   Maximum capacity of each vehicle.
 * @param vehicleCostPerKm  Cost per kilometer for vehicle travel.
 * @param vehicleSpeedKmHr  Speed of vehicles in km/hr.
 * @param numVehicles       Total number of vehicles available.
 * @param coordinatorServer Host/port for the Kafka coordinator server.
 * @param coordinatorTopic  Kafka topic for simulation coordination.
 * @param participants      List of participant identifiers.
 * @param vehicleHosts      Mapping from vehicle identifiers to their host systems.
 * @param manufacturer      Data for the manufacturer facility.
 * @param retailers         List of data for retailer facilities.
 */
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
    /**
     * Reads IrpData from a resource in the classpath.
     *
     * @param resourceName The name of the resource (e.g., a JSON file).
     * @return The parsed IrpData instance.
     * @throws UncheckedIOException If an I/O error occurs or the resource is not found.
     */
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

    /**
     * Reads IrpData from a file path.
     *
     * @param filePath The path to the JSON file.
     * @return The parsed IrpData instance.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    public static IrpData read(Path filePath) {
        try {
            String json = Files.readString(filePath);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, IrpData.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Data structure for manufacturer properties.
     *
     * @param id                Unique identifier for the manufacturer.
     * @param x                 X-coordinate of the manufacturer.
     * @param y                 Y-coordinate of the manufacturer.
     * @param startingInventory Initial inventory level.
     * @param dailyProduction   Amount produced each day.
     * @param inventoryCost     Daily cost per unit of inventory.
     * @param host              The host system where the manufacturer model runs.
     */
    public record ManufacturerData(
        int id,
        double x,
        double y,
        double startingInventory,
        double dailyProduction,
        double inventoryCost,
        String host
    ) {}

    /**
     * Data structure for retailer properties.
     *
     * @param id                Unique identifier for the retailer.
     * @param x                 X-coordinate of the retailer.
     * @param y                 Y-coordinate of the retailer.
     * @param startingInventory Initial inventory level.
     * @param maxInventory      Maximum inventory capacity.
     * @param minInventory      Minimum inventory level to maintain.
     * @param dailyConsumption  Amount consumed each day.
     * @param inventoryCost     Daily cost per unit of inventory.
     * @param host              The host system where the retailer model runs.
     */
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
