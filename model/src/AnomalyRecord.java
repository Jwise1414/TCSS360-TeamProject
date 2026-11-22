import java.time.Instant;

/**
 * A single anomaly found for a drone at a point in time.
 */
public class AnomalyRecord {
    /** Smallest allowed anomaly ID (0 means not saved yet). */
    private static final int MIN_ANOMALY_ID = 0;

    /** Smallest allowed drone ID. */
    private static final int MIN_DRONE_ID = 0;

    private final int myAnomalyID;
    private final int myDroneID;
    private final Instant myTimestamp;
    private final String myType;
    private final String myDetails;

    /**
     * Creates an anomaly record.
     *
     * @param theAnomalyID the anomaly ID, 0 or greater
     * @param theDroneID the ID of the drone, 0 or greater
     * @param theTimestamp when the anomaly happened, not null
     * @param theType the anomaly type name, not null or blank
     * @param theDetails a description of the anomaly, not null
     * @throws IllegalArgumentException if any argument is invalid
     */
    public AnomalyRecord(final int theAnomalyID, final int theDroneID,
                         final Instant theTimestamp, final String theType,
                         final String theDetails) {
        if (theAnomalyID < MIN_ANOMALY_ID) {
            throw new IllegalArgumentException("Anomaly ID cannot be negative: " + theAnomalyID);
        }
        if (theDroneID < MIN_DRONE_ID) {
            throw new IllegalArgumentException("Drone ID cannot be negative: " + theDroneID);
        }
        if (theTimestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null.");
        }
        if (theType == null || theType.isBlank()) {
            throw new IllegalArgumentException("Type cannot be null or blank.");
        }
        if (theDetails == null) {
            throw new IllegalArgumentException("Details cannot be null.");
        }
        myAnomalyID = theAnomalyID;
        myDroneID = theDroneID;
        myTimestamp = theTimestamp;
        myType = theType;
        myDetails = theDetails;
    }

    public int getAnomalyID() {
        return myAnomalyID;
    }

    public int getDroneID() {
        return myDroneID;
    }

    public Instant getTimestamp() {
        return myTimestamp;
    }

    public String getType() {
        return myType;
    }

    public String getDetails() {
        return myDetails;
    }

    public String toCSVRow() {
        return "";
    }
}
