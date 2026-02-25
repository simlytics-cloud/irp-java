


package iso.example.irpsystem.irpmodel.InventoryRouting;

import iso.example.irpsystem.irpmodel.InventoryRouting.*;
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
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = lombok.AccessLevel.PUBLIC, force = true)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
      
    
public class RetailerProperties extends FacilityProperties implements Mutable, IRetailerProperties {

    @NonNull
    protected Integer retailerId;
    @NonNull
    protected Double minInventory;
    @NonNull
    protected Double maxInventory;
    @NonNull
    protected Double dailyConsumption;

@JsonCreator
public RetailerProperties(@JsonProperty("facilityProperties") FacilityProps facilityProperties, @JsonProperty("retailerId") Integer retailerId, @JsonProperty("minInventory") Double minInventory, @JsonProperty("maxInventory") Double maxInventory, @JsonProperty("dailyConsumption") Double dailyConsumption) {
  super(facilityProperties);
  this.retailerId = retailerId;
  this.minInventory = minInventory;
  this.maxInventory = maxInventory;
  this.dailyConsumption = dailyConsumption;
}
    
}
