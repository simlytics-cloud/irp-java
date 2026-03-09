# Constructing and Testing Remote Models

Collaborative modeling and simulation is the central focus of this project, following the ISO 21175 draft standard. This guide explains how to build, configure, and test remote instances of Vehicles, Retailers, or Manufacturers that can participate in a distributed Inventory Routing Problem (IRP) simulation.

## Overview of Remote Collaboration

The system uses Apache Kafka as the messaging middleware to synchronize state and exchange events between distributed components. In a collaborative environment, different organizations can manage and test their own pieces of the supply chain model simultaneously. A "remote" model is one that runs in its own process or on a different host, communicating with the central simulation via Kafka proxies.

## Configuration for Remote Instances

To participate in a collaborative simulation, your model must be correctly defined in the scenario's JSON configuration file (e.g., `S_abs1n5_2_L3_local_proxies.json`).

### Defining Hosts and Participants

In the JSON configuration, the `participants` list should include the names of all systems involved. Each model component (Manufacturer, Retailer, or Vehicle) is assigned a `host`. 

*   **Local Components**: If a component's host matches the `local-system-name` in your `reference.conf`, it will be run as a local Java object.
*   **Remote Components**: If the host does not match, the application automatically creates a `KafkaDevsStreamProxyProvider`. This proxy acts as a representative for the remote model within the local simulation. It receives messages from the local coordinator, serializes them for transmission over Kafka to the remote host, and then deserializes any incoming Kafka messages from that remote model to pass them back into the local DEVS engine.

### Kafka Settings

The configuration must specify the `coordinatorServer` (the address of the Kafka cluster) and the `coordinatorTopic` (the topic used for message passing). All participants must use the same topic and have access to the same Kafka server to communicate.

## Importance of Model Identifiers

In a collaborative simulation, all messages for all components are published to the same `coordinatorTopic`. This means that every remote model instance—whether it’s a Vehicle, Retailer, or Manufacturer—receives every message sent across that topic. 

To ensure correct behavior, each model **must** filter incoming messages based on the `receiverId` field. A model should only process messages where the `receiverId` matches its own assigned identifier.

### Configuring Identifiers via IRP Data

Model identifiers are derived from the JSON configuration of the IRP problem instance. It is crucial that these names are consistent across all collaborators:

*   **Manufacturer**: The default identifier is typically `manufacturer`.
*   **Retailers**: These are identified as `retailer` followed by their `id` (e.g., `retailer0`, `retailer1`).
*   **Vehicles**: These are identified as `vehicle` followed by their index (e.g., `vehicle0`, `vehicle1`).

These IDs are used by the `KafkaReceiver` and the local proxies to determine which messages to route to which DEVS model. When implementing a remote model, you must ensure it is listening for and responding to the correct ID as defined in the scenario's `participants` and `vehicleHosts` mappings.

## Building a Remote Model

Collaborators can use the existing `impl` classes (`VehicleImpl`, `RetailerImpl`, `ManufacturerImpl`) as a reference. To build a remote model:
1.  Implement the required DEVS logic for your component.
2.  Ensure your model can serialize and deserialize messages in the format expected by the IRP domain (e.g., `DeliveryRoute`, `Delivery`, `VehicleCost`).
3.  Set up a Kafka consumer to listen for messages on the simulation topic and a Kafka producer to send state updates or events back to the coordinator.

## Testing Remote Models

The project includes dedicated test methods to verify the ability of models to interact over a network. Each core DEVS model has a corresponding test class in `src/test/java`:
*   `VehicleImplTest.java`
*   `RetailerImplTest.java`
*   `ManufacturerImplTest.java`

### Using KafkaLocalProxy

Each of these test classes contains a method (e.g., `testRemoteVehicle`) specifically designed for remote verification. These tests use a `KafkaLocalProxy` to act as a proxy for a remote model.

In a collaborative simulation, the `KafkaLocalProxy` sits within the local coupled model and represents a component that is running on a different system. Its role is to:
1.  **Receive messages** from the local DEVS coordinator intended for the remote model.
2.  **Serialize** these messages and send them via Kafka to the actual remote model.
3.  **Listen for responses** from the remote model on the Kafka topic.
4.  **Deserialize** those responses and pass them back to the local DEVS coordinator.

By using this proxy in tests, you can verify that your local simulation correctly communicates with a remote implementation over the network.

To run these tests:
1.  **Start Kafka**: Use the provided Docker Compose configuration to start a local Kafka cluster and management UI. For detailed instructions, see the [Local Kafka Setup Guide](../../kafka/running-local-kafka.md).
2.  **Enable the Test**: These tests are marked with `@Disabled("Requires Kafka Connection")` by default. Remove the `@Disabled` annotation to enable them.
3.  **Execute the Test**: Run the specific test method using your IDE or Maven.

The test will attempt to connect to Kafka, subscribe to the simulation topic, and verify that messages are correctly passed between the local simulator and the (simulated) remote proxy. This confirms that your model's network interface and DEVS logic are compatible with the collaborative environment.
