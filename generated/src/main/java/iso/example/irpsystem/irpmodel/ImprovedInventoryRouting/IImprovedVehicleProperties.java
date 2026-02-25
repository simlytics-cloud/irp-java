

package iso.example.irpsystem.irpmodel.ImprovedInventoryRouting;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import devs.iso.time.LongSimTime;
import devs.msg.state.IScheduleState;

import iso.example.irpsystem.irpdomain.*;
import iso.example.irpsystem.irpmodel.InventoryRouting.*;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)

public interface IImprovedVehicleProperties extends IVehicleProperties {
}
    