


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
      
    
public class InventoryCost  implements Mutable, IInventoryCost {

    @NonNull
    protected Integer day;
    @NonNull
    protected Integer retailerId;
    @NonNull
    protected Double cost;

@JsonCreator
public InventoryCost(@JsonProperty("day") Integer day, @JsonProperty("retailerId") Integer retailerId, @JsonProperty("cost") Double cost) {
  super();
  this.day = day;
  this.retailerId = retailerId;
  this.cost = cost;
}
    
}
