# Data Structures

The Inventory Routing Problem (IRP) simulation is highly data-driven, with its topology and parameters defined through JSON configuration files. This documentation explains the structure of the simulation data and the key domain objects used throughout the simulation.

## The IrpData Record

The `IrpData` record is the primary data structure that represents an IRP problem instance. It is deserialized from JSON files at the beginning of the simulation. Key fields include:
- **Simulation Parameters**: `numNodes`, `numTimePeriods`, and vehicle-related properties like `vehicleCapacity`, `vehicleCostPerKm`, and `vehicleSpeedKmHr`.
- **Collaborative Settings**: `coordinatorServer` and `coordinatorTopic` for Kafka-based simulation, along with a list of `participants` and their corresponding hosts for each component.
- **Node Data**: Detailed properties for the `manufacturer` and a list of `retailers`.

## Node and Facility Data

Each facility in the simulation, whether a manufacturer or a retailer, has a set of core properties. These include its physical coordinates (X and Y), initial inventory, and holding cost.
- **ManufacturerData**: Includes additional fields for `dailyProduction`.
- **RetailerData**: Includes additional fields for `maxInventory`, `minInventory`, and `dailyConsumption`.
- **Host System**: Both manufacturer and retailer data structures include a `host` field, which determines if the model is run on the local Java system or on a remote runner.

## Core Domain Objects

Several immutable records are used to represent the various events and data points exchanged between models. These objects are designed to be easily serialized into JSON for communication over Kafka.
- **Coordinate**: Represents a location in a 2D plane.
- **Delivery**: Contains information about a delivery destination, location, and the product amount.
- **DeliveryRoute**: A collection of deliveries assigned to a specific vehicle for a single day.
- **DeliverySchedule**: A complete mapping of all routes for all vehicles across all days of the simulation.
- **InventoryCost and VehicleCost**: Structures used for reporting costs to the transducer.

## Configuration Files

The project includes several example JSON configuration files in the `src/main/resources` directory. These files range from small, local-only scenarios (e.g., `S_abs1n5_2_L3_all_local.json`) to larger, distributed scenarios with multiple remote proxies. Users can create new scenarios by defining their own JSON files that follow the `IrpData` structure.
