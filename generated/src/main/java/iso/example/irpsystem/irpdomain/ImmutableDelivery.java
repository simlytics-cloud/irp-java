


package iso.example.irpsystem.irpdomain;


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
      
    
public class ImmutableDelivery  implements Immutable, IDelivery {

    @NonNull
    protected final Integer retailerId;
    @NonNull
    protected final ImmutableCoordinate retailerLocation;
    @NonNull
    protected final Double productAmount;

@JsonCreator
public ImmutableDelivery(@JsonProperty("retailerId") Integer retailerId, @JsonProperty("retailerLocation") ImmutableCoordinate retailerLocation, @JsonProperty("productAmount") Double productAmount) {
  super();
  this.retailerId = retailerId;
  this.retailerLocation = retailerLocation;
  this.productAmount = productAmount;
}
    
}
