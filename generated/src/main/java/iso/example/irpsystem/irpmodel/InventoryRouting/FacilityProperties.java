


package iso.example.irpsystem.irpmodel.InventoryRouting;


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
      
    
public abstract class FacilityProperties  implements Mutable, IFacilityProperties {

    @NonNull
    protected FacilityProps facilityProperties;

@JsonCreator
public FacilityProperties(@JsonProperty("facilityProperties") FacilityProps facilityProperties) {
  super();
  this.facilityProperties = facilityProperties;
}
    
}
