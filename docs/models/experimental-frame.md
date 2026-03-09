# Experimental Frame

The Experimental Frame in the Inventory Routing Problem (IRP) simulation consists of auxiliary models that support the core simulation but are not part of the physical supply chain. These components are responsible for driving the simulation with inputs and collecting performance data for analysis.

## Delivery Schedule Generator

The **DeliveryScheduleGenerator** is an atomic DEVS model that provides the initial plan for the simulation. At time zero, it generates a complete delivery schedule and sends it to the manufacturer. This schedule contains the daily routes for each vehicle throughout the simulation period.

In the reference implementation, the generator uses a greedy algorithm to fill vehicles based on retailer consumption. However, this model is designed to be easily replaceable, allowing researchers to test different optimization and scheduling algorithms within the same simulation environment.

## Transducer

The **Transducer** is an atomic DEVS model responsible for monitoring the simulation and aggregating results. It does not have any direct effect on the states of the manufacturer, retailers, or vehicles. Instead, it listens to cost reports on the dedicated reporting ports.

Throughout the simulation, the transducer collects:
- **Inventory Holding Costs**: Daily reports from the manufacturer and retailers.
- **Transportation Costs**: Daily reports from each vehicle based on distance traveled.

At the end of the simulation, the transducer triggers an internal event to process all collected data. It calculates the total costs for each component and for the entire system, printing a final summary to the console. This summary serves as the primary output for evaluating the performance of the simulation scenario.

## Factory and Coupling

The **BasicExperimentalFrameFactory** is responsible for instantiating the generator, transducer, and the coupled models for the IRP system. It also sets up the necessary couplings, ensuring that the cost reports from each component are correctly routed to the transducer.
