package iso.example.irpsystem.irpmodel.app;

import devs.utils.ConfigUtils;
import devs.utils.KafkaUtils;
import iso.example.irpsystem.irpmodel.impl.RemoteModel;
import java.nio.file.Paths;
import java.time.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.pekko.actor.typed.ActorSystem;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import devs.iso.time.LongSimTime;
import iso.example.irpsystem.irpmodel.algorithms.TimeUtils;
import iso.example.irpsystem.irpmodel.impl.BasicExperimentalFrameFactory;
import iso.example.irpsystem.irpmodel.impl.IrpData;

public class InventoryRoutingApp {

    static private Config config;
    static private Config kafkaClusterConfig;
    static private Config kafkaConsumerConfig;

    protected final BasicExperimentalFrameFactory experimentalFrameFactory;

    public InventoryRoutingApp(IrpData irpData, Map<String, RemoteModel> remoteModels) {
        if (kafkaClusterConfig != null && kafkaConsumerConfig != null
            && remoteModels != null && remoteModels.size() > 0) {
            this.experimentalFrameFactory = new BasicExperimentalFrameFactory(irpData, remoteModels,
                kafkaConsumerConfig, kafkaClusterConfig);
        } else {
            this.experimentalFrameFactory = new BasicExperimentalFrameFactory(irpData);
        }
    }

    protected void executeExperimentalFrame(LongSimTime startTime, LongSimTime endTime, Map<String,
        RemoteModel> remoteModels, IrpData irpData) {
        ActorSystem.create(InventoryRoutingAppMain.create(startTime, endTime, 
            experimentalFrameFactory.buiCoupledModelFactory(), remoteModels,
            kafkaClusterConfig, kafkaConsumerConfig, irpData),
            "InventoryRoutingSystem", config);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        if (args.length > 0) {
            String configName = args[0];
            config = ConfigFactory.load(configName);
        } else {
            config = ConfigFactory.load();
        }
        Config irpSystemConfig = config.getConfig("irp-routing-app");
        Map<String, RemoteModel> remoteModels = new HashMap<>();
        if (irpSystemConfig.hasPath("remote-models")) {
            Config remoteModelsConfig = irpSystemConfig.getConfig("remote-models");
            for (String key : remoteModelsConfig.root().keySet()) {
                Config remoteConfig = remoteModelsConfig.getConfig(key);
                RemoteModel remoteModel = new RemoteModel(key, remoteConfig.getBoolean("run-java"),
                    remoteConfig.getString("topic"));
                remoteModels.put(key, remoteModel);
            }
        }
        IrpData irpData = IrpData.read(Paths.get(irpSystemConfig.getString("irp-data-file")));
        if (config.hasPath("kafka-cluster") && config.hasPath("kafka-readall-consumer")) {
            kafkaClusterConfig = config.getConfig("kafka-cluster");
            kafkaConsumerConfig = config.getConfig("kafka-readall-consumer");
        }
        if(kafkaClusterConfig != null && kafkaConsumerConfig != null && remoteModels.size() > 0) {
            Properties kafkaClusterProperties = ConfigUtils.toProperties(kafkaClusterConfig);
            AdminClient adminClient = KafkaUtils.createAdminClient(kafkaClusterProperties);
            List<String> topics = remoteModels.values().stream()
                .map(RemoteModel::topic)
                .distinct()
                .toList();
            //KafkaUtils.createTopics(topics, adminClient, Optional.of(1), Optional.empty());
            //Thread.sleep(5000);
        }

        InventoryRoutingApp inventoryRoutingApp = new InventoryRoutingApp(irpData, remoteModels);
        LongSimTime endTime = TimeUtils.durationToSimTime(Duration.ofDays(irpData.numTimePeriods() + 1));
        inventoryRoutingApp.executeExperimentalFrame(LongSimTime.create(0), endTime, remoteModels, irpData);
    }

}
