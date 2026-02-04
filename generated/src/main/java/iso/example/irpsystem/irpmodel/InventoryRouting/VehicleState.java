


package iso.example.irpsystem.irpmodel.InventoryRouting;

import iso.example.irpsystem.irpdomain.*;
import devs.iso.time.*;
import devs.msg.state.*;
import devs.utils.*;
import java.util.*;



import devs.msg.mutability.*;


import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = lombok.AccessLevel.PUBLIC, force = true)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
      
    
public class VehicleState extends ScheduleState<LongSimTime> implements Mutable, IVehicleState {

    @NonNull
    protected Double dailyKmTraveled;
    @NonNull
    protected DeliveryRoute deliveryRoute;
    @NonNull
    protected Coordinate location;

@JsonCreator
public VehicleState(@JsonProperty("currentTime") devs.iso.time.LongSimTime currentTime, @JsonProperty("schedule") devs.utils.Schedule<devs.iso.time.LongSimTime> schedule, @JsonProperty("dailyKmTraveled") Double dailyKmTraveled, @JsonProperty("deliveryRoute") DeliveryRoute deliveryRoute, @JsonProperty("location") Coordinate location) {
  super(currentTime, schedule);
  this.dailyKmTraveled = dailyKmTraveled;
  this.deliveryRoute = deliveryRoute;
  this.location = location;
}
    
}
