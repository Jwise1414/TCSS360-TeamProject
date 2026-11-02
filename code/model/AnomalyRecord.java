package model;

import java.time.Instant;

/**
 * One detected anomaly: which drone, when, what type, and the telemetry
 * values involved (SRS Detect.Details and Appendix A "anomaly record").
 *
 * @author Khalil Ahmednoor
 */
public final class AnomalyRecord {

    /** Database ID; null until the record is saved to SQLite. */
    private Long id;
    private final String droneId;
    private final Instant timestamp;
    private final AnomalyType type;
    private final String details;

    /**
     * Creates an anomaly record that has not been saved yet.
     *
     * @param droneId   the affected drone
     * @param timestamp when it was detected (UTC)
     * @param type      the anomaly type
     * @param details   readable description with the relevant telemetry values
     */
    public AnomalyRecord(final String droneId, final Instant timestamp,
                         final AnomalyType type, final String details) {
        this.droneId = droneId;
        this.timestamp = timestamp;
        this.type = type;
        this.details = details;
    }

    public Long getId() {
        return id;
    }

    /**
     * Sets the ID assigned by the database after the record is inserted.
     *
     * @param id the database ID
     */
    public void setId(final Long id) {
        this.id = id;
    }

    public String getDroneId() {
        return droneId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public AnomalyType getType() {
        return type;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + droneId + " " + type + ": " + details;
    }
}
