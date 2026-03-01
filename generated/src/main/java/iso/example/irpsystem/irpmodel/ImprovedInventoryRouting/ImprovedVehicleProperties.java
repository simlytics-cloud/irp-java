


package iso.example.irpsystem.irpmodel.ImprovedInventoryRouting;

import iso.example.irpsystem.irpdomain.*;
import iso.example.irpsystem.irpmodel.InventoryRouting.*;
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
      
    
public class ImprovedVehicleProperties extends VehicleProperties implements Mutable, IImprovedVehicleProperties {



@JsonCreator
public ImprovedVehicleProperties(@JsonProperty("vehicleId") Integer vehicleId, @JsonProperty("capacity") Double capacity, @JsonProperty("manufacturerLocation") Coordinate manufacturerLocation, @JsonProperty("costPerKm") Double costPerKm, @JsonProperty("speedKmHr") Double speedKmHr) {
  super(vehicleId, capacity, manufacturerLocation, costPerKm, speedKmHr);

}
    
}
