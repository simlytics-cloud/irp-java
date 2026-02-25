


package iso.example.irpsystem.irpdomain;


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
@EqualsAndHashCode
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = lombok.AccessLevel.PUBLIC, force = true)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
      
    
public class VehicleCost  implements Mutable, IVehicleCost {

    @NonNull
    protected Integer day;
    @NonNull
    protected Integer vehicleId;
    @NonNull
    protected Double cost;

@JsonCreator
public VehicleCost(@JsonProperty("day") Integer day, @JsonProperty("vehicleId") Integer vehicleId, @JsonProperty("cost") Double cost) {
  super();
  this.day = day;
  this.vehicleId = vehicleId;
  this.cost = cost;
}
    
}
