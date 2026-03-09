# Collaborative Simulation

The Inventory Routing Problem (IRP) library is a reference implementation of a **Collaborative Modeling and Simulation Environment (CMSE)**, as described in the **ISO 21175** draft standard. This standard provides a framework for integrating diverse, distributed simulation models into a unified execution environment.

## Distributed Simulation Architecture

The simulation can run in a distributed mode where different participants, or host systems, contribute individual models to the overall simulation. For example, the manufacturer and several retailers might run locally, while another organization provides a remote vehicle model.

This distribution is made possible through the use of **Apache Kafka**, a high-performance messaging middleware. Kafka acts as the communication bridge, handling the exchange of events and synchronization of simulation time between local and remote components.

## Proxies and Coordinators

The system uses a proxy-based architecture to make the distribution transparent to the DEVS models. If a component is defined as "remote" in the configuration, the system creates a **Kafka Proxy** instead of a local model instance.

This proxy acts as a representative for the remote model within the local DEVS coordinator's context. When the coordinator sends a message to the remote model, the proxy:
1.  **Receives the message** locally as if it were the model itself.
2.  **Serializes** the message into a format suitable for transmission.
3.  **Sends the message via Kafka** to the remote system.

Conversely, when the remote model sends a response or state update, the proxy:
1.  **Receives the message from Kafka**.
2.  **Deserializes it** back into a DEVS-compatible format.
3.  **Passes the response** to the local DEVS coordinator.

This mechanism ensures that the simulation logic remains consistent, regardless of whether the individual models are physically running locally or on a different host.

## ISO 21175 Compliance

The project aligns with the core principles of the ISO 21175 standard:
- **Standardized Interfaces**: Using the DEVS formalism to ensure interoperable communication between models.
- **Middleware Interoperability**: Demonstrating how messaging systems like Kafka can facilitate simulation synchronization.
- **Configuration-Driven Composition**: Allowing for "Plug-and-Play" simulation where models can be swapped between local and remote hosts through simple JSON configuration updates.
- **Verification and Validation**: Providing a framework to test models in isolation and as part of a distributed system.

By following this standard, the IRP library provides a robust foundation for building collaborative simulations where different organizations can contribute their specialized models to a unified supply chain simulation federation.
