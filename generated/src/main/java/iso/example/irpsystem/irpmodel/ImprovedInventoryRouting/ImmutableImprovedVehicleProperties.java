


package iso.example.irpsystem.irpmodel.ImprovedInventoryRouting;

import iso.example.irpsystem.irpdomain.*;
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
      
    
public class ImmutableImprovedVehicleProperties extends ImmutableVehicleProperties implements Immutable, IImprovedVehicleProperties {



@JsonCreator
public ImmutableImprovedVehicleProperties(@JsonProperty("vehicleId") Integer vehicleId, @JsonProperty("capacity") Double capacity, @JsonProperty("manufacturerLocation") ImmutableCoordinate manufacturerLocation, @JsonProperty("costPerKm") Double costPerKm, @JsonProperty("speedKmHr") Double speedKmHr) {
  super(vehicleId, capacity, manufacturerLocation, costPerKm, speedKmHr);

}
    
}
