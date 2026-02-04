


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
      
    
public class Delivery  implements Mutable, IDelivery {

    @NonNull
    protected Integer retailerId;
    @NonNull
    protected Coordinate retailerLocation;
    @NonNull
    protected Double productAmount;

@JsonCreator
public Delivery(@JsonProperty("retailerId") Integer retailerId, @JsonProperty("retailerLocation") Coordinate retailerLocation, @JsonProperty("productAmount") Double productAmount) {
  super();
  this.retailerId = retailerId;
  this.retailerLocation = retailerLocation;
  this.productAmount = productAmount;
}
    
}
