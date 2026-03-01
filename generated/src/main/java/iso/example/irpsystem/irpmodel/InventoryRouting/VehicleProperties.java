


package iso.example.irpsystem.irpmodel.InventoryRouting;

import iso.example.irpsystem.irpdomain.*;
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
      
    
public class VehicleProperties  implements Mutable, IVehicleProperties {

    @NonNull
    protected Integer vehicleId;
    @NonNull
    protected Double capacity;
    @NonNull
    protected Coordinate manufacturerLocation;
    @NonNull
    protected Double costPerKm;
    @NonNull
    protected Double speedKmHr;

@JsonCreator
public VehicleProperties(@JsonProperty("vehicleId") Integer vehicleId, @JsonProperty("capacity") Double capacity, @JsonProperty("manufacturerLocation") Coordinate manufacturerLocation, @JsonProperty("costPerKm") Double costPerKm, @JsonProperty("speedKmHr") Double speedKmHr) {
  super();
  this.vehicleId = vehicleId;
  this.capacity = capacity;
  this.manufacturerLocation = manufacturerLocation;
  this.costPerKm = costPerKm;
  this.speedKmHr = speedKmHr;
}
    
}
