


package iso.example.irpsystem.irpmodel.InventoryRouting;

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
      
    
public class ImmutableManufacturerProperties extends ImmutableFacilityProperties implements Immutable, IManufacturerProperties {

    @NonNull
    protected final Double dailyProduction;

@JsonCreator
public ImmutableManufacturerProperties(@JsonProperty("facilityProperties") ImmutableFacilityProps facilityProperties, @JsonProperty("dailyProduction") Double dailyProduction) {
  super(facilityProperties);
  this.dailyProduction = dailyProduction;
}
    
}
