package iso.example.irpsystem.irpmodel.app;

import java.nio.file.Paths;
import java.time.Duration;

import org.apache.pekko.actor.typed.ActorSystem;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import devs.iso.time.LongSimTime;
import iso.example.irpsystem.irpmodel.algorithms.TimeUtils;
import iso.example.irpsystem.irpmodel.impl.ExperimentalFrameFactory;
import iso.example.irpsystem.irpmodel.impl.IrpData;

public class InventoryRoutingApp {

    static private Config config;

    protected final ExperimentalFrameFactory experimentalFrameFactory;

    public InventoryRoutingApp(IrpData irpData) {
        this.experimentalFrameFactory = new ExperimentalFrameFactory(irpData);
    }

    protected void executeExperimentalFrame(LongSimTime startTime, LongSimTime endTime) {
        ActorSystem.create(InventoryRoutingAppMain.create(startTime, endTime, 
            experimentalFrameFactory.buiCoupledModelFactory()),
            "InventoryRoutingSystem", config);
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            String configName = args[0];
            config = ConfigFactory.load(configName);
        } else {
            config = ConfigFactory.load();
        }
        IrpData irpData = IrpData.read(Paths.get(args[0]));
        InventoryRoutingApp inventoryRoutingApp = new InventoryRoutingApp(irpData);
        LongSimTime endTime = TimeUtils.durationToSimTime(Duration.ofDays(irpData.numTimePeriods() + 1));
        inventoryRoutingApp.executeExperimentalFrame(LongSimTime.create(0), endTime);
    }

}
