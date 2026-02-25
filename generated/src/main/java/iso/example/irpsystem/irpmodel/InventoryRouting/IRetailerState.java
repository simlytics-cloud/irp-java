

package iso.example.irpsystem.irpmodel.InventoryRouting;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import devs.iso.time.LongSimTime;
import devs.msg.state.IScheduleState;

import devs.iso.time.*;
import devs.msg.state.*;
import devs.utils.*;
import iso.example.irpsystem.irpmodel.InventoryRouting.*;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)

public interface IRetailerState extends IFacilityState {
}
    