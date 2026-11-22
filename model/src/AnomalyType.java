/**
 * The four kinds of anomaly the system can detect (SRS section 3.2).
 * Stored in the database by name, e.g. "LOW_BATTERY".
 *
 * @author Khalil Ahmednoor
 */
public enum AnomalyType {
    /** Battery fell below the low-battery threshold. */
    LOW_BATTERY,
    /** Altitude dropped more than the threshold in one interval (unsafe maneuver). */
    SUDDEN_ALTITUDE_DROP,
    /** Heading changed more than the threshold in one interval (unsafe maneuver). */
    SHARP_TURN,
    /** Position jumped more than the threshold in one interval (possible GPS spoofing). */
    GPS_LOCATION_JUMP
}
