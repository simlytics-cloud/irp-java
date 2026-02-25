

package iso.example.irpsystem.irpmodel.InventoryRouting;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import devs.iso.time.LongSimTime;
import devs.msg.state.IScheduleState;

import iso.example.irpsystem.irpdomain.*;
import devs.iso.time.*;
import devs.msg.state.*;
import devs.utils.*;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)

public interface IVehicleState extends IScheduleState<LongSimTime> {
}
    