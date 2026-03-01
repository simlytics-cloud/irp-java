package iso.example.irpsystem.irpmodel.app;

import java.time.Duration;

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

    public InventoryRoutingApp(IrpData irpData, String localSystemName, String localProxyName) {
        if (kafkaClusterConfig != null && kafkaConsumerConfig != null) {
            this.experimentalFrameFactory = new BasicExperimentalFrameFactory(irpData, localSystemName,
                kafkaConsumerConfig, kafkaClusterConfig);
        } else {
            this.experimentalFrameFactory = new BasicExperimentalFrameFactory(irpData, localSystemName);
        }
    }

    protected void executeExperimentalFrame(LongSimTime startTime, LongSimTime endTime, String localSystemName, String localProxyName, IrpData irpData) {
        ActorSystem.create(InventoryRoutingAppMain.create(startTime, endTime, 
            experimentalFrameFactory.buiCoupledModelFactory(), localSystemName, localProxyName,
            kafkaClusterConfig, kafkaConsumerConfig, irpData),
            "InventoryRoutingSystem", config);
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            String configName = args[0];
            config = ConfigFactory.load(configName);
        } else {
            config = ConfigFactory.load();
        }
        Config irpSystemConfig = config.getConfig("irp-routing-app");
        IrpData irpData = IrpData.read(irpSystemConfig.getString("irp-data-file"));
        String localSystemName = irpSystemConfig.getString("local-system-name");
        String localProxyName = irpSystemConfig.getString("local-proxy-name");

        if (config.hasPath("kafka-cluster") && config.hasPath("kafka-readall-consumer")) {
            kafkaClusterConfig = config.getConfig("kafka-cluster");
            kafkaConsumerConfig = config.getConfig("kafka-readall-consumer");
        }

        InventoryRoutingApp inventoryRoutingApp = new InventoryRoutingApp(irpData, localSystemName, localProxyName);
        LongSimTime endTime = TimeUtils.durationToSimTime(Duration.ofDays(irpData.numTimePeriods() + 1));
        inventoryRoutingApp.executeExperimentalFrame(LongSimTime.create(0), endTime, localSystemName, localProxyName, irpData);
    }

}
