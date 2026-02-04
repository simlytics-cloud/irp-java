

package iso.example.irpsystem.irpmodel.InventoryRouting;

import devs.Port;
import devs.iso.PortValue;
import devs.iso.time.LongSimTime;
import devs.ScheduledDevsModel;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import devs.msg.mutability.*;


public abstract class Facility<P extends IFacilityProperties, S extends FacilityState>
  extends ScheduledDevsModel<LongSimTime, S> {

public static String modelIdentifier = "facility";



    protected P properties;

    public Facility(S initialState, String identifier, P properties) {
        super(initialState, identifier);
        this.properties = properties;
    }



}
