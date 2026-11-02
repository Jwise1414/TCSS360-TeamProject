package model;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Checks each telemetry update against the detection rules in SRS section 3.2
 * and returns any anomalies found.
 *
 * <p>The detector remembers each drone's previous reading so it can compare
 * changes between intervals, and remembers which conditions are still active
 * so a continuing condition is only recorded once (Detect.Duplicates).</p>
 *
 * @author Khalil Ahmednoor
 */
public class AnomalyDetector {

    /** Battery percentage below which LOW_BATTERY is raised (Detect.Battery). */
    public static final double LOW_BATTERY_PERCENT = 15.0;

    /** Altitude decrease in one interval, in meters, that counts as a sudden drop. */
    public static final double ALTITUDE_DROP_METERS = 30.0;

    /** Heading change in one interval, in degrees, that counts as a sharp turn. */
    public static final double SHARP_TURN_DEGREES = 60.0;

    /** Position change in one interval, in meters, that counts as a GPS jump. */
    public static final double GPS_JUMP_METERS = 1000.0;

    /** Mean radius of the Earth in meters, used for distance calculations. */
    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    /** Last reading seen for each drone, keyed by drone ID. */
    private final Map<String, TelemetryReading> previousReadings = new HashMap<>();

    /** Conditions currently active for each drone, keyed by drone ID. */
    private final Map<String, Set<AnomalyType>> activeConditions = new HashMap<>();

    /** Clock used for timestamps; can be replaced in tests. */
    private final Clock clock;

    /** Creates a detector that timestamps anomalies in UTC. */
    public AnomalyDetector() {
        this(Clock.systemUTC());
    }

    /**
     * Creates a detector with a specific clock (useful for tests).
     *
     * @param clock the clock to use for anomaly timestamps
     */
    public AnomalyDetector(final Clock clock) {
        this.clock = clock;
    }

    /**
     * Checks one new reading for a drone and returns any new anomalies.
     * The first reading for a drone can only trigger LOW_BATTERY, because the
     * other rules need a previous reading to compare against.
     *
     * @param current the newest reading for one drone
     * @return new anomalies found (empty list if none)
     */
    public List<AnomalyRecord> check(final TelemetryReading current) {
        final String id = current.getDroneId();
        final TelemetryReading previous = previousReadings.get(id);
        final Set<AnomalyType> active =
                activeConditions.computeIfAbsent(id, k -> EnumSet.noneOf(AnomalyType.class));
        final Instant now = clock.instant();
        final List<AnomalyRecord> found = new ArrayList<>();

        // Detect.Battery
        update(found, active, AnomalyType.LOW_BATTERY,
                current.getBattery() < LOW_BATTERY_PERCENT, id, now,
                String.format("Battery at %.1f%% (threshold %.0f%%)",
                        current.getBattery(), LOW_BATTERY_PERCENT));

        if (previous != null) {
            // Detect.Altitude
            final double drop = previous.getAltitude() - current.getAltitude();
            update(found, active, AnomalyType.SUDDEN_ALTITUDE_DROP,
                    drop > ALTITUDE_DROP_METERS, id, now,
                    String.format("Altitude fell %.1f m (%.1f m -> %.1f m)",
                            drop, previous.getAltitude(), current.getAltitude()));

            // Detect.Turn
            final double turn = headingChange(previous.getOrientation(),
                    current.getOrientation());
            update(found, active, AnomalyType.SHARP_TURN,
                    turn > SHARP_TURN_DEGREES, id, now,
                    String.format("Heading changed %.1f° (%.1f° -> %.1f°)",
                            turn, previous.getOrientation(), current.getOrientation()));

            // Detect.GPS
            final double jump = distanceMeters(
                    previous.getLatitude(), previous.getLongitude(),
                    current.getLatitude(), current.getLongitude());
            update(found, active, AnomalyType.GPS_LOCATION_JUMP,
                    jump > GPS_JUMP_METERS, id, now,
                    String.format("Position jumped %.0f m (possible GPS spoofing)",
                            jump));
        }

        previousReadings.put(id, current);
        return found;
    }

    /**
     * Records an anomaly only when its condition becomes active, and clears it
     * when the condition ends, so a continuing condition is recorded once.
     */
    private static void update(final List<AnomalyRecord> found,
                               final Set<AnomalyType> active,
                               final AnomalyType type, final boolean triggered,
                               final String droneId, final Instant now,
                               final String details) {
        if (triggered) {
            if (active.add(type)) {
                found.add(new AnomalyRecord(droneId, now, type, details));
            }
        } else {
            active.remove(type);
        }
    }

    /**
     * Returns the smallest angle between two headings, handling the 0°/360°
     * boundary. For example, 359° to 1° is a 2° change.
     *
     * @param from starting heading in degrees
     * @param to   ending heading in degrees
     * @return change in degrees, from 0 to 180
     */
    public static double headingChange(final double from, final double to) {
        final double diff = Math.abs(to - from) % 360.0;
        return Math.min(diff, 360.0 - diff);
    }

    /**
     * Returns the distance in meters between two latitude/longitude points
     * using the haversine formula.
     *
     * @param lat1 first latitude in degrees
     * @param lon1 first longitude in degrees
     * @param lat2 second latitude in degrees
     * @param lon2 second longitude in degrees
     * @return distance in meters
     */
    public static double distanceMeters(final double lat1, final double lon1,
                                        final double lat2, final double lon2) {
        final double dLat = Math.toRadians(lat2 - lat1);
        final double dLon = Math.toRadians(lon2 - lon1);
        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_METERS * Math.asin(Math.sqrt(a));
    }
}
