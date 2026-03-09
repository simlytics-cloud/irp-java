package iso.example.irpsystem.irpmodel.algorithms;

import static org.junit.jupiter.api.Assertions.*;

import iso.example.irpsystem.irpmodel.impl.IrpData;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class IrpReaderTest {

public static Path path = Paths.get("src/main/resources/S_abs1n5_2_L3.json");
public static IrpData irpData = IrpData.read(path);

  @Test
    void testRead() {


        assertNotNull(irpData);
        assertEquals(6, irpData.numNodes());
        assertEquals(3, irpData.numTimePeriods());
        assertEquals(144.0, irpData.vehicleCapacity());
        assertEquals(2, irpData.numVehicles());
        assertEquals("irp-system", irpData.coordinatorTopic());
        assertEquals("LocalJava", irpData.participants().get(0));
        assertEquals("RemoteRunnersJava", irpData.participants().get(1));
        assertEquals("RemoteRunnersAdevs", irpData.participants().get(2));
        assertEquals("LocalJava",irpData.vehicleHosts().get("vehicle0"));
        assertEquals("RemoteRunnersAdevs",irpData.vehicleHosts().get("vehicle1"));

        // Manufacturer
        assertEquals(0, irpData.manufacturer().id());
        assertEquals(154.0, irpData.manufacturer().x());
        assertEquals(417.0, irpData.manufacturer().y());
        assertEquals(510.0, irpData.manufacturer().startingInventory());
        assertEquals(193.0, irpData.manufacturer().dailyProduction());
        assertEquals(0.03, irpData.manufacturer().inventoryCost());
        assertEquals("LocalJava", irpData.manufacturer().host());

        // Retailers
        assertEquals(5, irpData.retailers().size());
        
        IrpData.RetailerData r0 = irpData.retailers().get(0);
        assertEquals(0, r0.id());
        assertEquals(172.0, r0.x());
        assertEquals(334.0, r0.y());
        assertEquals(130.0, r0.startingInventory());
        assertEquals(195.0, r0.maxInventory());
        assertEquals(0.0, r0.minInventory());
        assertEquals(65.0, r0.dailyConsumption());
        assertEquals(0.02, r0.inventoryCost());
        assertEquals("LocalJava", r0.host());

        assertEquals("RemoteRunnersAdevs", irpData.retailers().get(1).host());
        assertEquals("RemoteRunnersJava", irpData.retailers().get(2).host());

        IrpData.RetailerData r5 = irpData.retailers().get(4);
        assertEquals(4, r5.id());
        assertEquals(38.0, r5.x());
        assertEquals(152.0, r5.y());
        assertEquals(11.0, r5.startingInventory());
        assertEquals(22.0, r5.maxInventory());
        assertEquals(0.0, r5.minInventory());
        assertEquals(11.0, r5.dailyConsumption());
        assertEquals(0.02, r5.inventoryCost());
        assertEquals("LocalJava", r5.host());
    }
}
