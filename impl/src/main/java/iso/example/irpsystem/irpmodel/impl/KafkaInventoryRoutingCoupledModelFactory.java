package iso.example.irpsystem.irpmodel.impl;

import java.util.List;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.javadsl.ActorContext;

import com.typesafe.config.Config;

import devs.CoupledModelFactory;
import devs.PDevsCouplings;
import devs.SimulatorProvider;
import devs.iso.DevsMessage;
import devs.iso.time.LongSimTime;
import devs.proxy.KafkaReceiver;

/**
 * A CoupledModelFactory that supports distributed simulation using Kafka.
 * This factory creates a coupled model where some components may be remote,
 * and messages are received via a Kafka topic.
 */
public class KafkaInventoryRoutingCoupledModelFactory extends CoupledModelFactory<LongSimTime> {

    protected Config kafkaConsumerConfig = null;
    protected String kafkaTopic = "";

    /**
     * Constructs a KafkaInventoryRoutingCoupledModelFactory.
     *
     * @param modelIdentifier    The unique identifier for the coupled model.
     * @param simulatorProviders Providers for the child simulators.
     * @param couplings          The couplings between child models.
     */
    public KafkaInventoryRoutingCoupledModelFactory(String modelIdentifier,
            List<SimulatorProvider<LongSimTime>> simulatorProviders, PDevsCouplings couplings) {
        super(modelIdentifier, simulatorProviders, couplings);
    }

    /**
     * Constructs a KafkaInventoryRoutingCoupledModelFactory with Kafka configuration.
     *
     * @param modelIdentifier     The unique identifier for the coupled model.
     * @param simulatorProviders  Providers for the child simulators.
     * @param couplings           The couplings between child models.
     * @param kafkaConsumerConfig Configuration for the Kafka consumer.
     * @param kafkaTopic          The Kafka topic to listen on for remote messages.
     */
    public KafkaInventoryRoutingCoupledModelFactory(String modelIdentifier,
            List<SimulatorProvider<LongSimTime>> simulatorProviders, PDevsCouplings couplings,
            Config kafkaConsumerConfig,
            String kafkaTopic) {
        super(modelIdentifier, simulatorProviders, couplings);
        this.kafkaConsumerConfig = kafkaConsumerConfig;
        this.kafkaTopic = kafkaTopic;
    }

    /**
     * Constructs a KafkaInventoryRoutingCoupledModelFactory with logging options.
     *
     * @param modelIdentifier    The unique identifier for the coupled model.
     * @param simulatorProviders Providers for the child simulators.
     * @param couplings          The couplings between child models.
     * @param loggingModels      List of identifiers for child models to enable logging for.
     */
    public KafkaInventoryRoutingCoupledModelFactory(String modelIdentifier,
            List<SimulatorProvider<LongSimTime>> simulatorProviders, PDevsCouplings couplings,
            List<String> loggingModels) {
        super(modelIdentifier, simulatorProviders, couplings, loggingModels);
    }

    /**
     * Spawns the simulator actor and, if Kafka configuration is provided,
     * also spawns a KafkaReceiver to handle incoming messages from remote components.
     *
     * @param context     The actor context.
     * @param initialTime The initial simulation time.
     * @return The ActorRef for the created simulator (coordinator).
     */
    @Override
    public ActorRef<DevsMessage> provideSimulator(ActorContext<DevsMessage> context, LongSimTime initialTime) {
        // TODO Auto-generated method stub
        ActorRef<DevsMessage> coordinator = super.provideSimulator(context, initialTime);
        if (kafkaConsumerConfig != null) {
            context.spawn(KafkaReceiver.create(coordinator, null, modelIdentifier, kafkaConsumerConfig, 
                kafkaTopic), modelIdentifier + "Proxy");
        }
        return coordinator;
    }

    

}
