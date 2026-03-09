# Local Kafka Setup for IRP Simulation

This guide provides instructions for setting up a local Apache Kafka environment and its corresponding management UI. This is essential for testing remote models and participating in collaborative simulations.

## Prerequisites

Before proceeding, ensure you have the following installed on your machine:
*   **Docker Desktop** (or Docker with the Docker Compose plugin)

## Running Kafka Locally

The project includes a `compose.yml` file in the `kafka/` directory that defines a single-node Kafka broker and a Kafka-UI instance.

1.  Navigate to the `kafka` directory:
    ```bash
    cd kafka
    ```
2.  Start the services:
    ```bash
    docker compose up -d
    ```

Once started, the following services will be available:
*   **Kafka Broker**: Accessible at `localhost:29092` for simulation traffic.
*   **Kafka UI**: Accessible at `http://localhost:8080` via your web browser.

## Creating a Topic via Kafka UI

While the simulation is configured to auto-create topics, it is often useful to create them manually for monitoring:

1.  Open your web browser and go to `http://localhost:8080`.
2.  In the sidebar, click on **Topics**.
3.  Click the **Add a Topic** button in the top right.
4.  Enter the topic name (e.g., `irp-system`, as used in the default configurations).
5.  Click **Create** at the bottom of the form.

## Viewing Messages from an IRP Run

The Kafka UI provides a real-time view of messages being exchanged during a simulation.

1.  From the **Topics** list, click on the name of the topic you wish to monitor (e.g., `irp-system`).
2.  Click on the **Messages** tab.
3.  As the simulation runs, you will see `DevsMessage` objects appear in the list.
4.  Click on a message to expand its details and see the serialized state transitions and port values.

## Stopping Kafka

To stop the Kafka environment and remove the containers, run:

```bash
docker compose down
```

If you also want to remove the volumes used by Kafka, add the `-v` flag:

```bash
docker compose down -v
```

This will delete the data stored in Kafka, so use with caution.


