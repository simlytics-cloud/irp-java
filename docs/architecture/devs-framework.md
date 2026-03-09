# DEVS Framework

The Inventory Routing Problem (IRP) simulation is built on top of the **[DEVS Streaming Framework](https://github.com/simlytics-cloud/devs-streaming)**, a robust implementation of the **Discrete Event System Specification (DEVS)** and its parallel version, **Parallel DEVS (PDevs)**. This framework provides a mathematically rigorous way to define how models transition between states and communicate via discrete events.

## DEVS/PDevs Implementation

In this system, models are classified into two categories: **Atomic Models** and **Coupled Models**. Atomic models define the behavior and state transitions of a single component, while coupled models define how these atomic models are interconnected and how they communicate.

Each model in the simulation transitions through several functions:
- **Internal State Transition Function**: Updates the model's state based on internal events.
- **External State Transition Function**: Updates the state when the model receives external inputs.
- **Confluent State Transition Function**: Handles the case where internal and external transitions occur simultaneously.
- **Output Function**: Generates output events before an internal transition takes place.
- **Time Advance Function**: Determines the time interval until the next internal event.

The DEVS models implemented in this project use a specialized [ScheduleDevsModel](https://github.com/simlytics-cloud/devs-streaming/blob/main/src/main/java/devs/ScheduledDevsModel.java) that has an internal event schedule and current time in its state.

## Simulation Execution via Pekko Actors

The simulation's engine is built using **Apache Pekko**, an actor-based concurrency framework. Each DEVS model is wrapped by a simulator actor, and coupled models are managed by coordinator actors. This design ensures that simulation time remains synchronized across all models, even when they are executing on different threads or distributed across a network.

The simulator actors follow a well-defined lifecycle, receiving and processing messages like `SimulationInit`, `CollectOutputs`, and `InternalTransition`. This lifecycle ensures that the simulation proceeds in discrete steps, maintaining the logical order of events.

## Core Framework Features

The DEVS Streaming Framework provides the foundational infrastructure for the IRP simulation:

- **Core DEVS Engine**: Implements the simulators and coordinators required for Parallel DEVS, ensuring synchronization and correct event ordering.
- **Port-Based Communication**: Facilitates decoupled model design where components communicate via standardized ports rather than direct references.
- **Streaming and Distribution**: Built-in support for Apache Kafka, allowing simulation events to be streamed across distributed systems via the emerging ISO 21175 [collaborative simulation](./collaborative-simulation.md) standard
- **Proxy Mechanisms**: Includes `KafkaLocalProxy` and `KafkaDevsStreamProxyProvider`, which  bridge local simulators with remote components running on different hosts.

## Port-Based Communication

Communication between models is handled through a port-based system. Each model defines a set of input and output ports. When an atomic model generates an output message on a specific port, the coupled model's coordinator resolves the couplings and forwards the message to the appropriate input ports of other models.

This port-based approach provides a clean separation of concerns, as atomic models do not need to know which other models they are connected to. The coupling logic is handled externally by the factory and coordinator, facilitating modularity and reuse.
