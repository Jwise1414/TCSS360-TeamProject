package model;

/**
 * One snapshot of a drone's telemetry at a single update interval.
 * Field names and ranges follow the SRS data dictionary (Appendix A).
 *
 * <p>The detector works on readings instead of the Drone class directly,
 * so it can be tested without the GUI or the generator.</p>
 *
 * @author Khalil Ahmednoor
 */
public final class TelemetryReading {

    private final String droneId;
    private final double latitude;
    private final double longitude;
    private final double altitude;
    private final double battery;
    private final double orientation;
    private final double velocity;

    /**
     * Creates a telemetry reading.
     *
     * @param droneId     unique drone ID, e.g. "DRONE-01"
     * @param latitude    decimal degrees, -90 to 90
     * @param longitude   decimal degrees, -180 to 180
     * @param altitude    meters above ground, 0 or greater
     * @param battery     battery percentage, 0 to 100
     * @param orientation heading in degrees, 0 to 359
     * @param velocity    speed in meters per second, 0 or greater
     * @throws IllegalArgumentException if any value is out of range (Sim.Validate)
     */
    public TelemetryReading(final String droneId, final double latitude,
                            final double longitude, final double altitude,
                            final double battery, final double orientation,
                            final double velocity) {
        if (droneId == null || droneId.isBlank()) {
            throw new IllegalArgumentException("Drone ID is required.");
        }
        checkRange("latitude", latitude, -90, 90);
        checkRange("longitude", longitude, -180, 180);
        checkRange("altitude", altitude, 0, Double.MAX_VALUE);
        checkRange("battery", battery, 0, 100);
        checkRange("orientation", orientation, 0, 360);
        checkRange("velocity", velocity, 0, Double.MAX_VALUE);

        this.droneId = droneId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.battery = battery;
        this.orientation = orientation;
        this.velocity = velocity;
    }

    private static void checkRange(final String name, final double value,
                                   final double min, final double max) {
        if (Double.isNaN(value) || value < min || value > max) {
            throw new IllegalArgumentException(
                    name + " out of range: " + value);
        }
    }

    public String getDroneId() {
        return droneId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getBattery() {
        return battery;
    }

    public double getOrientation() {
        return orientation;
    }

    public double getVelocity() {
        return velocity;
    }
}
