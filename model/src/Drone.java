/**
 * Holds the current telemetry values for one drone.
 */
public class Drone {
    /** Lowest valid latitude in degrees. */
    private static final double MIN_LATITUDE = -90.0;
    /** Highest valid latitude in degrees. */
    private static final double MAX_LATITUDE = 90.0;
    /** Lowest valid longitude in degrees. */
    private static final double MIN_LONGITUDE = -180.0;
    /** Highest valid longitude in degrees. */
    private static final double MAX_LONGITUDE = 180.0;
    /** Lowest valid battery percentage. */
    private static final double MIN_BATTERY = 0.0;
    /** Highest valid battery percentage. */
    private static final double MAX_BATTERY = 100.0;
    /** Lowest valid velocity. */
    private static final double MIN_VELOCITY = 0.0;
    /** Smallest allowed drone ID. */
    private static final int MIN_DRONE_ID = 0;

    private double myLatitude;
    private double myLongitude;
    private double myAltitude;
    private final int myDroneID;
    private double myBattery;
    private double myOrientation;
    private double myVelocity;
    private boolean mySimState;

    /**
     * Creates a drone with its starting telemetry.
     *
     * @param theLatitude latitude in degrees, -90 to 90
     * @param theLongitude longitude in degrees, -180 to 180
     * @param theAltitude altitude in meters
     * @param theDroneID the drone ID, 0 or greater
     * @param theBattery battery percentage, 0 to 100
     * @param theOrientation heading in degrees
     * @param theVelocity speed, 0 or greater
     * @throws IllegalArgumentException if any argument is invalid
     */
    public Drone(final double theLatitude, final double theLongitude, final double theAltitude,
                 final int theDroneID, final double theBattery,
                 final double theOrientation, final double theVelocity) {
        if (theDroneID < MIN_DRONE_ID) {
            throw new IllegalArgumentException("Drone ID cannot be negative: " + theDroneID);
        }
        myDroneID = theDroneID;
        updateLatitude(theLatitude);
        updateLongitude(theLongitude);
        updateAltitude(theAltitude);
        updateBattery(theBattery);
        updateOrientation(theOrientation);
        updateVelocity(theVelocity);
        mySimState = true;
    }

    /*
     * Decided to keep these separate rather than all put together so we can
     * update one value at a time. Different from the SRS.
     */
    private void updateLatitude(final double theLatitude) {
        if (Double.isNaN(theLatitude) || theLatitude < MIN_LATITUDE || theLatitude > MAX_LATITUDE) {
            throw new IllegalArgumentException("Invalid latitude: " + theLatitude);
        }
        myLatitude = theLatitude;
    }

    private void updateLongitude(final double theLongitude) {
        if (Double.isNaN(theLongitude) || theLongitude < MIN_LONGITUDE
                || theLongitude > MAX_LONGITUDE) {
            throw new IllegalArgumentException("Invalid longitude: " + theLongitude);
        }
        myLongitude = theLongitude;
    }

    private void updateAltitude(final double theAltitude) {
        if (!Double.isFinite(theAltitude)) {
            throw new IllegalArgumentException("Invalid altitude: " + theAltitude);
        }
        myAltitude = theAltitude;
    }

    private void updateBattery(final double theBattery) {
        if (Double.isNaN(theBattery) || theBattery < MIN_BATTERY || theBattery > MAX_BATTERY) {
            throw new IllegalArgumentException("Invalid battery percentage: " + theBattery);
        }
        myBattery = theBattery;
    }

    private void updateOrientation(final double theOrientation) {
        if (!Double.isFinite(theOrientation)) {
            throw new IllegalArgumentException("Invalid orientation: " + theOrientation);
        }
        myOrientation = theOrientation;
    }

    private void updateVelocity(final double theVelocity) {
        if (!Double.isFinite(theVelocity) || theVelocity < MIN_VELOCITY) {
            throw new IllegalArgumentException("Invalid velocity: " + theVelocity);
        }
        myVelocity = theVelocity;
    }

    private void updateSimState(final boolean theSimState) {
        mySimState = theSimState;
    }

    /**
     * Updates all telemetry values at once (called by TelemetryGenerator
     * every update interval).
     *
     * @param theLatitude latitude in degrees, -90 to 90
     * @param theLongitude longitude in degrees, -180 to 180
     * @param theAltitude altitude in meters
     * @param theBattery battery percentage, 0 to 100
     * @param theOrientation heading in degrees
     * @param theVelocity speed, 0 or greater
     * @throws IllegalArgumentException if any argument is invalid
     */
    public void updateTelemetry(final double theLatitude, final double theLongitude,
                                final double theAltitude, final double theBattery,
                                final double theOrientation, final double theVelocity) {
        updateLatitude(theLatitude);
        updateLongitude(theLongitude);
        updateAltitude(theAltitude);
        updateBattery(theBattery);
        updateOrientation(theOrientation);
        updateVelocity(theVelocity);
    }

    public double getLatitude() {
        return myLatitude;
    }

    public double getLongitude() {
        return myLongitude;
    }

    public double getAltitude() {
        return myAltitude;
    }

    public int getDroneID() {
        return myDroneID;
    }

    public double getBattery() {
        return myBattery;
    }

    public double getOrientation() {
        return myOrientation;
    }

    public double getVelocity() {
        return myVelocity;
    }

    public boolean getSimState() {
        return mySimState;
    }
}
