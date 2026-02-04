


package iso.example.irpsystem.irpdomain;


import java.util.*;



import devs.msg.mutability.*;

import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor(access = lombok.AccessLevel.PUBLIC, force = true)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
      
    
public class ImmutableInventoryCost  implements Immutable, IInventoryCost {

    @NonNull
    protected final Integer day;
    @NonNull
    protected final Integer retailerId;
    @NonNull
    protected final Double cost;

@JsonCreator
public ImmutableInventoryCost(@JsonProperty("day") Integer day, @JsonProperty("retailerId") Integer retailerId, @JsonProperty("cost") Double cost) {
  super();
  this.day = day;
  this.retailerId = retailerId;
  this.cost = cost;
}
    
}
