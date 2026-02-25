

package iso.example.irpsystem.irpdomain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import devs.iso.time.LongSimTime;
import devs.msg.state.IScheduleState;



@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)

public interface IVehicleCost  {
}
    