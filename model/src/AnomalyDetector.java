import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Checks drone telemetry for anomalies and remembers which ones are active,
 * so a continuing condition is only reported once.
 */
public class AnomalyDetector {
    private static final double LOW_BATTERY_PERCENT = 15.0;
    private static final double MAX_ALTITUDE_DROP_M = 30.0;
    private static final double MAX_TURN_DEGREES = 60.0;
    private static final double MAX_GPS_JUMP_M = 1000.0;
    private static final double EARTH_RADIUS_M = 6371000.0;
    private static final double FULL_CIRCLE_DEGREES = 360.0;
    private static final double HALF_CIRCLE_DEGREES = 180.0;
    private static final int FIRST_ANOMALY_ID = 1;

    private final Map<Integer, Drone> myPreviousStates;
    private final Set<String> myActiveConditions;
    private int myNextAnomalyID;

    public AnomalyDetector() {
        myPreviousStates = new HashMap<>();
        myActiveConditions = new HashSet<>();
        myNextAnomalyID = FIRST_ANOMALY_ID;
    }

    /**
     * Checks drone for any anomalies.
     *
     * @param theDrone the drone to check, not null
     * @return the new anomalies found
     * @throws IllegalArgumentException if theDrone is null
     */
    public List<AnomalyRecord> check(final Drone theDrone) {
        if (theDrone == null) {
            throw new IllegalArgumentException("Drone cannot be null.");
        }
        final List<AnomalyRecord> found = new ArrayList<>();
        final int id = theDrone.getDroneID();

        checkCondition(found, id, AnomalyType.LOW_BATTERY,
                theDrone.getBattery() <= LOW_BATTERY_PERCENT,
                "Battery at " + theDrone.getBattery() + "%");
        final Drone previous = myPreviousStates.get(id);
        if (previous != null) {
            final double drop = previous.getAltitude() - theDrone.getAltitude();
            checkCondition(found, id, AnomalyType.SUDDEN_ALTITUDE_DROP,
                    drop > MAX_ALTITUDE_DROP_M,
                    "Altitude fell " + Math.round(drop) + " m since last update");

            double turn = Math.abs(theDrone.getOrientation() - previous.getOrientation())
                    % FULL_CIRCLE_DEGREES;
            if (turn > HALF_CIRCLE_DEGREES) {
                turn = FULL_CIRCLE_DEGREES - turn;
            }
            checkCondition(found, id, AnomalyType.SHARP_TURN, turn > MAX_TURN_DEGREES,
                    "Turned " + Math.round(turn) + " degrees since last update");

            final double jump = distanceMeters(previous, theDrone);
            checkCondition(found, id, AnomalyType.GPS_LOCATION_JUMP, jump > MAX_GPS_JUMP_M,
                    "Position jumped " + Math.round(jump) + " m since last update");
        }

        myPreviousStates.put(id, new Drone(theDrone.getLatitude(), theDrone.getLongitude(),
                theDrone.getAltitude(), id, theDrone.getBattery(),
                theDrone.getOrientation(), theDrone.getVelocity()));
        return found;
    }

    /**
     * What is currently a problem.
     *
     * @return a read-only view of the active conditions
     */
    public Set<String> getActiveConditions() {
        return Collections.unmodifiableSet(myActiveConditions);
    }

    private void checkCondition(final List<AnomalyRecord> theFound, final int theDroneID,
                                final AnomalyType theType, final boolean theIsHappening,
                                final String theDetails) {
        final String key = "Drone #" + theDroneID + ": " + theType.name();
        if (theIsHappening) {
            if (myActiveConditions.add(key)) {
                theFound.add(new AnomalyRecord(myNextAnomalyID++, theDroneID, Instant.now(),
                        theType.name(), theDetails));
            }
        } else {
            myActiveConditions.remove(key);
        }
    }

    private double distanceMeters(final Drone theA, final Drone theB) {
        final double lat1 = Math.toRadians(theA.getLatitude());
        final double lat2 = Math.toRadians(theB.getLatitude());
        final double dLat = lat2 - lat1;
        final double dLon = Math.toRadians(theB.getLongitude() - theA.getLongitude());
        final double halfDLat = Math.sin(dLat / 2);
        final double halfDLon = Math.sin(dLon / 2);
        final double a = halfDLat * halfDLat
                + Math.cos(lat1) * Math.cos(lat2) * halfDLon * halfDLon;
        return 2 * EARTH_RADIUS_M * Math.asin(Math.sqrt(a));
    }
}
