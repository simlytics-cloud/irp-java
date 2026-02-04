


package iso.example.irpsystem.irpmodel.ExperimentalFrame;

import devs.iso.time.*;
import devs.msg.state.*;
import devs.utils.*;
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
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = lombok.AccessLevel.PUBLIC, force = true)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
      
    
public class ImmutableTransducerState extends ImmutableScheduleState<LongSimTime> implements Immutable, ITransducerState {

    @Builder.Default
    @NonNull
    protected final Map<Integer, Map<Integer, Double>> vehicleCostByDayByVehicle = Map.of();
    @Builder.Default
    @NonNull
    protected final Map<Integer, Map<Integer, Double>> inventoryCostByDayByRetailer = Map.of();

@JsonCreator
public ImmutableTransducerState(@JsonProperty("currentTime") devs.iso.time.LongSimTime currentTime, @JsonProperty("schedule") devs.utils.ImmutableSchedule<devs.iso.time.LongSimTime> schedule, @JsonProperty("vehicleCostByDayByVehicle") Map<Integer, Map<Integer, Double>> vehicleCostByDayByVehicle, @JsonProperty("inventoryCostByDayByRetailer") Map<Integer, Map<Integer, Double>> inventoryCostByDayByRetailer) {
  super(currentTime, schedule);
  this.vehicleCostByDayByVehicle = (vehicleCostByDayByVehicle != null) ? Map.copyOf(vehicleCostByDayByVehicle) : Map.of();
  this.inventoryCostByDayByRetailer = (inventoryCostByDayByRetailer != null) ? Map.copyOf(inventoryCostByDayByRetailer) : Map.of();
}
    
}
