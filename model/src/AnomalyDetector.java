import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TCS-16: Checks each drone's telemetry against the detection rules in
 * SRS section 3.2 and returns any new anomalies as AnomalyRecords.
 *
 * <p>Drone objects are updated in place by TelemetryGenerator, so the
 * detector keeps its own copy of each drone's previous values to compare
 * changes between intervals. It also remembers which conditions are still
 * active so a continuing condition is only recorded once.</p>
 *
 * @author Khalil Ahmednoor
 */
public class AnomalyDetector {

    /** Battery percentage below which LOW_BATTERY is raised. */
    public static final double LOW_BATTERY_PERCENT = 15.0;

    /** Altitude decrease in one interval, in meters, that counts as a sudden drop. */
    public static final double ALTITUDE_DROP_METERS = 30.0;

    /** Heading change in one interval, in degrees, that counts as a sharp turn. */
    public static final double SHARP_TURN_DEGREES = 60.0;

    /** Position change in one interval, in meters, that counts as a GPS jump. */
    public static final double GPS_JUMP_METERS = 1000.0;

    /** Mean radius of the Earth in meters, used for distance calculations. */
    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    /** ID used for records that have not been saved to the database yet. */
    private static final int UNSAVED_ID = 0;

    /** Copy of the values from a drone's previous update. */
    private record Snapshot(double latitude, double longitude,
                            double altitude, double orientation) { }

    /** Last values seen for each drone, keyed by drone ID. */
    private final Map<Integer, Snapshot> myPreviousStates = new HashMap<>();

    /** Conditions currently active for each drone, keyed by drone ID. */
    private final Map<Integer, Set<AnomalyType>> myActiveConditions = new HashMap<>();

    /** Clock used for timestamps; can be replaced in tests. */
    private final Clock myClock;

    /** Creates a detector that timestamps anomalies in UTC. */
    public AnomalyDetector() {
        this(Clock.systemUTC());
    }

    /**
     * Creates a detector with a specific clock (useful for tests).
     *
     * @param theClock the clock to use for anomaly timestamps
     */
    public AnomalyDetector(final Clock theClock) {
        myClock = theClock;
    }

    /**
     * Checks a drone's current telemetry and returns any new anomalies.
     * The first check for a drone can only raise LOW_BATTERY, because the
     * other rules need a previous update to compare against.
     *
     * @param theDrone the drone to check
     * @return new anomalies found (empty list if none)
     */
    public List<AnomalyRecord> check(final Drone theDrone) {
        final int id = theDrone.getDroneID();
        final Snapshot previous = myPreviousStates.get(id);
        final Set<AnomalyType> active = myActiveConditions.computeIfAbsent(
                id, k -> EnumSet.noneOf(AnomalyType.class));
        final Instant now = myClock.instant();
        final List<AnomalyRecord> found = new ArrayList<>();

        // Detect.Battery
        update(found, active, AnomalyType.LOW_BATTERY,
                theDrone.getBattery() < LOW_BATTERY_PERCENT, id, now,
                String.format("Battery at %.1f%% (threshold %.0f%%)",
                        theDrone.getBattery(), LOW_BATTERY_PERCENT));

        if (previous != null) {
            // Detect.Altitude
            final double drop = previous.altitude() - theDrone.getAltitude();
            update(found, active, AnomalyType.SUDDEN_ALTITUDE_DROP,
                    drop > ALTITUDE_DROP_METERS, id, now,
                    String.format("Altitude fell %.1f m (%.1f m -> %.1f m)",
                            drop, previous.altitude(), theDrone.getAltitude()));

            // Detect.Turn
            final double turn = headingChange(previous.orientation(),
                    theDrone.getOrientation());
            update(found, active, AnomalyType.SHARP_TURN,
                    turn > SHARP_TURN_DEGREES, id, now,
                    String.format("Heading changed %.1f deg (%.1f -> %.1f)",
                            turn, previous.orientation(), theDrone.getOrientation()));

            // Detect.GPS
            final double jump = distanceMeters(
                    previous.latitude(), previous.longitude(),
                    theDrone.getLatitude(), theDrone.getLongitude());
            update(found, active, AnomalyType.GPS_LOCATION_JUMP,
                    jump > GPS_JUMP_METERS, id, now,
                    String.format("Position jumped %.0f m (possible GPS spoofing)", jump));
        }

        myPreviousStates.put(id, new Snapshot(theDrone.getLatitude(),
                theDrone.getLongitude(), theDrone.getAltitude(),
                theDrone.getOrientation()));
        return found;
    }

    /**
     * Records an anomaly only when its condition becomes active, and clears it
     * when the condition ends, so a continuing condition is recorded once.
     */
    private static void update(final List<AnomalyRecord> theFound,
                               final Set<AnomalyType> theActive,
                               final AnomalyType theType, final boolean theTriggered,
                               final int theDroneId, final Instant theTime,
                               final String theDetails) {
        if (theTriggered) {
            if (theActive.add(theType)) {
                theFound.add(new AnomalyRecord(UNSAVED_ID, theDroneId, theTime,
                        theType.name(), theDetails));
            }
        } else {
            theActive.remove(theType);
        }
    }

    /**
     * Returns the smallest angle between two headings, handling the 0/360
     * boundary. For example, 359 to 1 is a 2 degree change.
     *
     * @param theFrom starting heading in degrees
     * @param theTo   ending heading in degrees
     * @return change in degrees, from 0 to 180
     */
    public static double headingChange(final double theFrom, final double theTo) {
        final double diff = Math.abs(theTo - theFrom) % 360.0;
        return Math.min(diff, 360.0 - diff);
    }

    /**
     * Returns the distance in meters between two latitude/longitude points
     * using the haversine formula.
     */
    public static double distanceMeters(final double theLat1, final double theLon1,
                                        final double theLat2, final double theLon2) {
        final double dLat = Math.toRadians(theLat2 - theLat1);
        final double dLon = Math.toRadians(theLon2 - theLon1);
        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(theLat1)) * Math.cos(Math.toRadians(theLat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_METERS * Math.asin(Math.sqrt(a));
    }
}
