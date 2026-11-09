import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnomalyDetector {
    private static final double LOW_BATTER_PERCENT = 15.0;
    private static final double MAX_ALTITUDE = 30.0;
    private static final double MAX_TURN_DEGREES = 60.0;
    private static final double MAX_GPS_JUMP_M = 1000.0;
    private static final double EARTH_RADIUS_M = 6371000.0;
    private Map<Integer, Drone> previousStates;
    private Set<String> activeConditions;
    private int nextAnomalyID = 1;

    public AnomalyDetector(){
        previousStates = new HashMap<>();
        activeConditions = new HashSet<>();
    }

    /**
     * Checks drone for any anomalies
     */
    public List<AnomalyRecord> check(Drone theDrone) {
        List<AnomalyRecord> found = new ArrayList<>();
        int id = theDrone.getDroneID();

        checkCondition(found, id, "LOW_BATTERY", theDrone.getBattery() <= LOW_BATTER_PERCENT,
                "Battery at " + theDrone.getBattery() + "%");
        checkCondition(found, id, "ALTITUDE", theDrone.getAltitude() > MAX_ALTITUDE,
                "Altitude " + theDrone.getAltitude() + " m is above " + MAX_ALTITUDE + " m");

        Drone previous = previousStates.get(id);
        if (previous != null) {
            double turn = Math.abs(theDrone.getOrientation() - previous.getOrientation()) % 360;
            if (turn > 180) {
                turn = 360 - turn;
            }
            checkCondition(found, id, "SHARP_TURN", turn > MAX_TURN_DEGREES,
                    "Turned " + Math.round(turn) + " degrees since last update");

            double jump = distanceMeters(previous, theDrone);
            checkCondition(found, id, "GPS_JUMP", jump > MAX_GPS_JUMP_M,
                    "Position jumped " + Math.round(jump) + " m since last update");
        }

        // store a copy so later changes to the drone don't change its previous state
        previousStates.put(id, new Drone(theDrone.getLatitude(), theDrone.getLongitude(),
                theDrone.getAltitude(), id, theDrone.getBattery(),
                theDrone.getOrientation(), theDrone.getVelocity()));
        return found;
    }

    /**
     * What is currently a problem
     */
    public Set<String> getActiveConditions() {
        return activeConditions;
    }

    private void checkCondition(List<AnomalyRecord> theFound, int theDroneID, String theType,
                                boolean theIsHappening, String theDetails) {
        String key = "Drone #" + theDroneID + ": " + theType;
        if (theIsHappening) {
            // add() returns false if the condition was already active
            if (activeConditions.add(key)) {
                theFound.add(new AnomalyRecord(nextAnomalyID++, theDroneID, Instant.now(), theType, theDetails));
            }
        } else {
            activeConditions.remove(key);
        }
    }


    private double distanceMeters(Drone theA, Drone theB) {
        double lat1 = Math.toRadians(theA.getLatitude());
        double lat2 = Math.toRadians(theB.getLatitude());
        double dLat = lat2 - lat1;
        double dLon = Math.toRadians(theB.getLongitude() - theA.getLongitude());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_M * Math.asin(Math.sqrt(a));
    }
}
