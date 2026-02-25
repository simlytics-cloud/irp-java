


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
      
    
public class ImmutableRetailerProperties extends ImmutableFacilityProperties implements Immutable, IRetailerProperties {

    @NonNull
    protected final Integer retailerId;
    @NonNull
    protected final Double minInventory;
    @NonNull
    protected final Double maxInventory;
    @NonNull
    protected final Double dailyConsumption;

@JsonCreator
public ImmutableRetailerProperties(@JsonProperty("facilityProperties") ImmutableFacilityProps facilityProperties, @JsonProperty("retailerId") Integer retailerId, @JsonProperty("minInventory") Double minInventory, @JsonProperty("maxInventory") Double maxInventory, @JsonProperty("dailyConsumption") Double dailyConsumption) {
  super(facilityProperties);
  this.retailerId = retailerId;
  this.minInventory = minInventory;
  this.maxInventory = maxInventory;
  this.dailyConsumption = dailyConsumption;
}
    
}
