# Vehicle Model

The Vehicle model is an atomic DEVS model responsible for the transportation of products from the manufacturer to the retailers. It models the travel time, delivery process, and transportation costs in the simulation.

## Core Logic and Responsibilities

Each vehicle has a fixed capacity, `vehicleCapacity`, and a travel speed, `vehicleSpeedKmHr`. It also incurs a cost per kilometer traveled, specified as `vehicleCostPerKm`.

When a vehicle receives a delivery route from the manufacturer, it calculates the travel time to the first retailer on the list. Upon arrival, it performs a delivery, waits for a set duration, and then moves on to the next retailer. If the vehicle is still away from the manufacturer when the business day closes, or if it completes its route, it returns to the manufacturer location.

## State and Events

The internal state of the vehicle tracks its current coordinates, its assigned delivery route, and the total distance traveled during the current day. Key internal events include:
- **DeliveryEvent**: Occurs when the vehicle reaches a retailer's location.
- **ReturnToManufacturerEvent**: Occurs when the vehicle returns to the manufacturer.

The vehicle model is responsible for calculating the travel time between coordinates. If a route exceeds the vehicle's capacity, the excess load is immediately returned to the manufacturer.

## Ports and Communication

The vehicle communicates with other models through the following ports:
- **acceptDeliveryRoute** (Input): Receives the route to be followed for the day.
- **dropDelivery** (Output): Sends a delivery message to a retailer.
- **dailyDeliveryCost** (Output): Reports the transportation cost to the transducer at the end of each day.

This model is critical for evaluating the efficiency of different routing strategies, as it directly translates route choices into time and monetary costs.
