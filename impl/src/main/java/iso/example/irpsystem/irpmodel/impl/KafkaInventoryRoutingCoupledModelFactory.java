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

public class KafkaInventoryRoutingCoupledModelFactory extends CoupledModelFactory<LongSimTime> {

    protected Config kafkaConsumerConfig = null;
    protected String kafkaTopic = "";

    public KafkaInventoryRoutingCoupledModelFactory(String modelIdentifier,
            List<SimulatorProvider<LongSimTime>> simulatorProviders, PDevsCouplings couplings) {
        super(modelIdentifier, simulatorProviders, couplings);
    }

    public KafkaInventoryRoutingCoupledModelFactory(String modelIdentifier,
            List<SimulatorProvider<LongSimTime>> simulatorProviders, PDevsCouplings couplings,
            Config kafkaConsumerConfig,
            String kafkaTopic) {
        super(modelIdentifier, simulatorProviders, couplings);
        this.kafkaConsumerConfig = kafkaConsumerConfig;
    }

    public KafkaInventoryRoutingCoupledModelFactory(String modelIdentifier,
            List<SimulatorProvider<LongSimTime>> simulatorProviders, PDevsCouplings couplings,
            List<String> loggingModels) {
        super(modelIdentifier, simulatorProviders, couplings, loggingModels);
    }

    @Override
    public ActorRef<DevsMessage> provideSimulator(ActorContext<DevsMessage> context, LongSimTime initialTime) {
        // TODO Auto-generated method stub
        ActorRef<DevsMessage> coordinator = super.provideSimulator(context, initialTime);
        if (kafkaConsumerConfig != null) {
            context.spawn(KafkaReceiver.create(coordinator, null, modelIdentifier, kafkaConsumerConfig, 
                "irp-system"), "irpsystemproxy");
        }
        return coordinator;
    }

    

}
