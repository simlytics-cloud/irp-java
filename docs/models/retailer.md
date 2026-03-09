# Retailer Model

The Retailer model is an atomic DEVS model representing a delivery destination in the supply chain. Its primary function is to manage local inventory, consume products daily, and report its holding costs.

## Core Logic and Responsibilities

Each retailer is characterized by its daily consumption rate, `dailyConsumption`, and its storage capacity, defined by `maxInventory` and `minInventory`. Retailers start with an initial `startingInventory`.

The retailer receives deliveries from vehicles throughout the business day, which increase its current inventory. At the end of each day (at the specified closing time), the retailer subtracts its daily consumption from its current inventory and reports the resulting holding cost.

## State and Events

The internal state of the retailer tracks its current inventory level and coordinates. The model schedules a recurring internal event:
- **UpdateInventoryEvent**: Occurs at the end of each day to process consumption and report costs.

During this daily update, the retailer also performs sanity checks. If the current inventory exceeds the `maxInventory` or falls below the `minInventory`, a simulation error is logged. This monitoring helps in evaluating the effectiveness of the delivery schedule.

## Ports and Communication

The retailer communicates with the rest of the simulation through the following ports:
- **receiveDelivery** (Input): Receives product deliveries from vehicles.
- **dailyInventoryCost** (Output): Reports the holding cost to the system's coordinator.

This model serves as an important endpoint in the supply chain, as its inventory stability is one of the key indicators of a successful routing and inventory strategy.
