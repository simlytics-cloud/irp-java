# Architecture Overview

The Inventory Routing Problem (IRP) simulation is a discrete-event system designed to model a supply chain involving a single manufacturer, multiple retailers, and a fleet of delivery vehicles. The goal of the simulation is to manage inventory levels across all nodes while minimizing the total cost, which includes both inventory holding costs and transportation expenses.

## The Inventory Routing Problem

The simulation focuses on solving a specific instance of the **Inventory Routing Problem (IRP)**. In this scenario, a manufacturer must manage a fleet of vehicles to distribute products to multiple retailers over a multi-day period.

For a complete definition of the problem, including the governing rules for each element and the overall objective, please refer to the **[Inventory Routing Problem Definition](irp-definition.md)**.

## Key Components

The simulation consists of several atomic and coupled models. The manufacturer produces inventory and posts delivery routes to vehicles. Retailers receive these deliveries and consume products over time, reporting their holding costs. Vehicles follow the routes, calculate travel distances, and report their transportation costs.

A transducer aggregates all cost reports from the manufacturer, retailers, and vehicles. At the end of the simulation, it computes and prints the final performance metrics, allowing for a quantitative evaluation of the chosen routing and inventory strategy.

## High-Level System Design

The system is built on top of a custom DEVS framework implemented using Apache Pekko for actor-based simulation. This architecture allows the simulation to be highly modular and extensible. Components can be easily swapped or extended to explore different inventory management and routing algorithms.

The architecture is also designed for distribution. Using Kafka as a messaging middleware, components can run on different host systems while maintaining a unified simulation clock and event-passing mechanism. This satisfies the requirements for collaborative modeling as defined in the ISO 21175 draft standard.
