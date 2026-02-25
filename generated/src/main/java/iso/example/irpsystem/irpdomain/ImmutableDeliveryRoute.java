


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
      
    
public class ImmutableDeliveryRoute  implements Immutable, IDeliveryRoute {

    @NonNull
    protected final Integer vehicleId;
    @Builder.Default
    @NonNull
    protected final List<ImmutableDelivery> deliveries = List.of();

@JsonCreator
public ImmutableDeliveryRoute(@JsonProperty("vehicleId") Integer vehicleId, @JsonProperty("deliveries") List<ImmutableDelivery> deliveries) {
  super();
  this.vehicleId = vehicleId;
  this.deliveries = (deliveries != null) ? List.copyOf(deliveries) : List.of();
}
    
}
