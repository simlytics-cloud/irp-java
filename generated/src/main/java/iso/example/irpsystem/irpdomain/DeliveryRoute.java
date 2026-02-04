


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
      
    
public class DeliveryRoute  implements Mutable, IDeliveryRoute {

    @NonNull
    protected Integer vehicleId;
    @Builder.Default
    @NonNull
    protected List<Delivery> deliveries = new ArrayList<>();

@JsonCreator
public DeliveryRoute(@JsonProperty("vehicleId") Integer vehicleId, @JsonProperty("deliveries") List<Delivery> deliveries) {
  super();
  this.vehicleId = vehicleId;
  this.deliveries = (deliveries != null) ? List.copyOf(deliveries) : List.of();
}
    
}
