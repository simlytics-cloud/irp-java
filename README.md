# Inventory Routing Problem (IRP) Simulation

This project provides a comprehensive, discrete-event simulation of the **Inventory Routing Problem (IRP)**. It is built using a custom Parallel DEVS (PDevs) framework on top of Apache Pekko and is designed to support **Collaborative Modeling and Simulation (C&S)** in alignment with the **ISO 21175** draft standard.

## Overview

The simulation models a supply chain where a single manufacturer distributes products to multiple retailers using a fleet of vehicles. The core objective is to minimize the combined costs of inventory holding and transportation while ensuring that retailers do not run out of stock.

![Inventory Routing Problem](docs/architecture/inventory_routing_to_scale.png){width=30%}

Key features include:
- **Discrete-Event Simulation**: Built on the mathematically rigorous DEVS formalism.
- **Collaborative Environment**: Supports distributed simulations where different organizations can implement and test their own remote models (e.g., a specific vehicle or retailer strategy).
- **Kafka Integration**: Uses Apache Kafka as messaging middleware for synchronization and event exchange between distributed participants.
- **Configuration-Driven**: Easily switch between local and remote model implementations via JSON configuration files.

## Documentation (Wiki)

Comprehensive documentation for this project is maintained in the `docs/` directory. It serves as a wiki for understanding the architecture, models, and how to participate in collaborative simulations.

**[Start here: Wiki Home Page](docs/index.md)**

### Key Documentation Sections:

- **[IRP Definition](docs/architecture/irp-definition.md)**: Detailed rules for the manufacturer, retailers, and vehicles.
- **[Architecture Overview](docs/architecture/overview.md)**: High-level system design and goals.
- **[Remote Model Construction & Testing](docs/guides/remote-collaboration.md)**: **The most important section for collaborators** wishing to build and test their own remote simulation components.
- **[Getting Started](docs/guides/getting-started.md)**: Instructions on building, running, and testing the simulation.
- **[DEVS Framework](docs/architecture/devs-framework.md)**: Deep dive into the underlying simulation engine.

## Quick Start

### Build the Project
```bash
mvn clean install
```

### Run a Local Simulation
```bash
mvn exec:java -pl impl -Dexec.mainClass="iso.example.irpsystem.irpmodel.app.InventoryRoutingApp"
```

For more detailed instructions, see the **[Getting Started Guide](docs/guides/getting-started.md)**.
