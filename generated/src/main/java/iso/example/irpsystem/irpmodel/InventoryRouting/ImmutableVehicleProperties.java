


package iso.example.irpsystem.irpmodel.InventoryRouting;

import iso.example.irpsystem.irpdomain.*;
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
@EqualsAndHashCode
@SuperBuilder(toBuilder = true)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
      
    
public class ImmutableVehicleProperties  implements Immutable, IVehicleProperties {

    @NonNull
    protected final Integer vehicleId;
    @NonNull
    protected final Double capacity;
    @NonNull
    protected final ImmutableCoordinate manufacturerLocation;
    @NonNull
    protected final Double costPerKm;
    @NonNull
    protected final Double speedKmHr;

@JsonCreator
public ImmutableVehicleProperties(@JsonProperty("vehicleId") Integer vehicleId, @JsonProperty("capacity") Double capacity, @JsonProperty("manufacturerLocation") ImmutableCoordinate manufacturerLocation, @JsonProperty("costPerKm") Double costPerKm, @JsonProperty("speedKmHr") Double speedKmHr) {
  super();
  this.vehicleId = vehicleId;
  this.capacity = capacity;
  this.manufacturerLocation = manufacturerLocation;
  this.costPerKm = costPerKm;
  this.speedKmHr = speedKmHr;
}
    
}
