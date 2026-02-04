package iso.example.irpsystem.irpmodel.algorithms;

import java.time.Duration;

import devs.iso.time.LongSimTime;

/**
 * For the simulation, LongSimTime represents minutes.  Each day starts deliveries at 6 AM.
 * All retailers close at 4pm and report their inventory
 * Vehicles start at the manufacturer and move along the route at their deisgnatted speed
 *   from location to location.  A delivery takes 15 minutes at a retailer. If a deliver
 *   is anticipated to be after 4pm, it is not delivered, and the inventory is returned
 *   to the manufacturer.
 * The manufacturer reports its inventory at 11:59 PM
 */
public class TimeUtils {

    public static int MINUTES_PER_DELIVERY = 15;
    public static int CLOSING_HOUR = 16;
    public static int OPENING_HOUR = 6;
    public static Duration CLOSING_DURATION = Duration.ofHours(CLOSING_HOUR);
    public static Duration OPENING_DURATION = Duration.ofHours(OPENING_HOUR);
    public static Duration MANUFACTURER_REPORT_DURATION = Duration.ofDays(1).minus(Duration.ofMinutes(1));
    
    public static Duration simTimeToDuration(LongSimTime simTime) {
        return Duration.ofMinutes(simTime.getT());
    }

    public static LongSimTime durationToSimTime(Duration duration) {
        return LongSimTime.create(duration.toMinutes());
    }
}
