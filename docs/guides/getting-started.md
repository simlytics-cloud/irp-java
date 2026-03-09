# Getting Started

This guide provides the basic instructions for building, running, and testing the Inventory Routing Problem (IRP) simulation. Whether you are running a simple local simulation or setting up a distributed collaborative environment, these steps will help you get started.

## Building the Project

The project is built using Maven and Java. To compile all source files and generate the required DEVS models, run the following command from the root directory:

```bash
mvn clean install
```

This will build the two main modules: `generated` (containing the DEVS boilerplate and model interfaces) and `impl` (containing the core simulation logic).

## Running the Simulation

The primary entry point for the simulation is the `InventoryRoutingApp` class. By default, it uses the configuration defined in `src/main/resources/reference.conf`, which specifies the active JSON scenario file and the local system name.

To run the application using the default settings:

```bash
mvn exec:java -pl impl -Dexec.mainClass="iso.example.irpsystem.irpmodel.app.InventoryRoutingApp"
```

If you are running a scenario with remote models or using a Kafka-based configuration (e.g., `S_abs1n5_2_L3_local_proxies.json`), you must ensure Kafka is running. See the [Local Kafka Setup Guide](../../kafka/running-local-kafka.md) for instructions.

You can specify a different configuration file by passing its name as a command-line argument to the main class. The output will show the progress of the simulation and, at the end, a summary of all costs reported by the transducer.

## Testing Your Models

The library provides comprehensive unit tests for each of the core DEVS models (Vehicle, Retailer, and Manufacturer) in the `src/test/java` directory. Local tests verify the models within a single JVM. To run all local tests:

```bash
mvn test
```

## Remote and Collaborative Simulation

Collaborative modeling is a core feature of this project. For detailed information on constructing, configuring, and testing remote models that participate in a distributed simulation via Kafka, please refer to the [Remote Collaboration Guide](remote-collaboration.md).

To participate in a collaborative simulation, you must ensure that your host system is correctly defined in the JSON configuration. When the application starts, it identifies which models are assigned to your `local-system-name` and automatically creates the necessary Kafka receivers and proxies to bridge your models into the larger simulation.
