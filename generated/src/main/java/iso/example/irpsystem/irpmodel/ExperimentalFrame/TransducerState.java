


package iso.example.irpsystem.irpmodel.ExperimentalFrame;

import devs.iso.time.*;
import devs.msg.state.*;
import devs.utils.*;
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
      
    
public class TransducerState extends ScheduleState<LongSimTime> implements Mutable, ITransducerState {

    @Builder.Default
    @NonNull
    protected Map<Integer, Map<Integer, Double>> vehicleCostByDayByVehicle = new HashMap<>();
    @Builder.Default
    @NonNull
    protected Map<Integer, Map<Integer, Double>> inventoryCostByDayByRetailer = new HashMap<>();

@JsonCreator
public TransducerState(@JsonProperty("currentTime") devs.iso.time.LongSimTime currentTime, @JsonProperty("schedule") devs.utils.Schedule<devs.iso.time.LongSimTime> schedule, @JsonProperty("vehicleCostByDayByVehicle") Map<Integer, Map<Integer, Double>> vehicleCostByDayByVehicle, @JsonProperty("inventoryCostByDayByRetailer") Map<Integer, Map<Integer, Double>> inventoryCostByDayByRetailer) {
  super(currentTime, schedule);
  this.vehicleCostByDayByVehicle = (vehicleCostByDayByVehicle != null) ? Map.copyOf(vehicleCostByDayByVehicle) : Map.of();
  this.inventoryCostByDayByRetailer = (inventoryCostByDayByRetailer != null) ? Map.copyOf(inventoryCostByDayByRetailer) : Map.of();
}
    
}
