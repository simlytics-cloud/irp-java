


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
      
    
public class ImmutableFacilityProps  implements Immutable, IFacilityProps {

    @NonNull
    protected final ImmutableCoordinate coordinate;
    @NonNull
    protected final Double startingInventory;
    @NonNull
    protected final Double inventoryCost;

@JsonCreator
public ImmutableFacilityProps(@JsonProperty("coordinate") ImmutableCoordinate coordinate, @JsonProperty("startingInventory") Double startingInventory, @JsonProperty("inventoryCost") Double inventoryCost) {
  super();
  this.coordinate = coordinate;
  this.startingInventory = startingInventory;
  this.inventoryCost = inventoryCost;
}
    
}
