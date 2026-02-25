


package iso.example.irpsystem.irpmodel.ImprovedInventoryRouting;

import iso.example.irpsystem.irpdomain.*;
import devs.iso.time.*;
import devs.msg.state.*;
import devs.utils.*;
import iso.example.irpsystem.irpmodel.InventoryRouting.*;
import java.util.*;



import devs.msg.mutability.*;

import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
      
    
public class ImmutableImprovedVehicleState extends ImmutableVehicleState implements Immutable, IImprovedVehicleState {



@JsonCreator
public ImmutableImprovedVehicleState(@JsonProperty("currentTime") devs.iso.time.LongSimTime currentTime, @JsonProperty("schedule") devs.utils.ImmutableSchedule<devs.iso.time.LongSimTime> schedule, @JsonProperty("dailyKmTraveled") Double dailyKmTraveled, @JsonProperty("deliveryRoute") ImmutableDeliveryRoute deliveryRoute, @JsonProperty("location") ImmutableCoordinate location) {
  super(currentTime, schedule, dailyKmTraveled, deliveryRoute, location);

}
    
}
