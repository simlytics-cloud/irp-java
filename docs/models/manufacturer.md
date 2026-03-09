# Manufacturer Model

The Manufacturer model is an atomic DEVS model responsible for managing production and inventory at the source of the supply chain. It acts as the central coordinator for delivery routes, dispatching products to retailers via vehicles.

## Core Logic and Responsibilities

The manufacturer's primary role is to produce a fixed amount of product each day, specified as `dailyProduction`. This production increases the current inventory level, which starts at `startingInventory`.

Each day, the manufacturer reports its current inventory and the associated holding cost. These costs are sent to the transducer for final aggregation. The manufacturer also receives a delivery schedule at the beginning of the simulation and is responsible for posting the appropriate delivery routes to the vehicles at the start of each business day.

## State and Events

The internal state of the manufacturer tracks the current inventory level and a schedule of internal events. Key events include:
- **UpdateInventory**: Occurs daily to increase inventory by the production amount and trigger cost reporting.
- **PostDeliveryRoute**: Signals that a vehicle should be dispatched with a specific set of deliveries.

When a vehicle returns to the manufacturer, either at the end of its route or due to a capacity overflow, the manufacturer accepts the returned products and adds them back to its inventory.

## Ports and Communication

The manufacturer communicates with other components through the following ports:
- **acceptDeliverySchedule** (Input): Receives the overall simulation plan.
- **postDeliveryRoute** (Output): Sends a delivery route to a specific vehicle.
- **dailyInventoryCost** (Output): Reports the holding cost to the system's coordinator.
- **acceptDelivery** (Input): Receives products returned by a vehicle.
