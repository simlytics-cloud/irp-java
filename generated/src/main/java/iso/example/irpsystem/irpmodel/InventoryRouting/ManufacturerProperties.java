


package iso.example.irpsystem.irpmodel.InventoryRouting;

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
      
    
public class ManufacturerProperties extends FacilityProperties implements Mutable, IManufacturerProperties {

    @NonNull
    protected Double dailyProduction;

@JsonCreator
public ManufacturerProperties(@JsonProperty("facilityProperties") FacilityProps facilityProperties, @JsonProperty("dailyProduction") Double dailyProduction) {
  super(facilityProperties);
  this.dailyProduction = dailyProduction;
}
    
}
