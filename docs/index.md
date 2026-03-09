# Inventory Routing Problem Simulation Wiki

Welcome to the documentation for the Inventory Routing Problem (IRP) simulation. This project provides a full-running implementation of a collaborative supply chain simulation based on the **ISO 21175** draft standard for Collaborative Modeling and Simulation Environment (CMSE).

The library is designed not only as a complete simulation app but also as a framework for collaborators to build and test their own remote instances of supply chain components like Vehicles, Retailers, or Manufacturers.

### Documentation Sections

- **[Architecture Overview](architecture/overview.md)**: A high-level view of the system design and the goals of the Inventory Routing Problem.
- **[IRP Definition](architecture/irp-definition.md)**: A complete definition of the Inventory Routing Problem, including the rules for the manufacturer, retailers, and vehicles.
- **[DEVS Framework](architecture/devs-framework.md)**: Detailed explanation of the underlying Parallel DEVS (PDevs) implementation.
- **[Collaborative Simulation](architecture/collaborative-simulation.md)**: How the system uses Apache Kafka to enable distributed, multi-participant simulations in compliance with emerging ISO 21175 standard.
- **Models**: Detailed logic for the core simulation components:
    - **[Manufacturer](models/manufacturer.md)**
    - **[Retailer](models/retailer.md)**
    - **[Vehicle](models/vehicle.md)**
    - **[Experimental Frame](models/experimental-frame.md)** (Generators and Transducers)
- **[Data Structures](data-structures/irp-data.md)**: Documentation for the JSON configuration files and the IrpData record.
- **[Getting Started](guides/getting-started.md)**: Instructions on how to build, run, and test the simulation.
- **[Remote Model Construction & Testing](guides/remote-collaboration.md)**: A detailed guide for collaborators on building and testing their own remote simulation components.

This wiki is maintained as a set of Markdown files within the repository, ensuring that documentation stays in sync with the source code.
