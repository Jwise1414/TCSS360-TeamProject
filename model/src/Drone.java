public class Drone {
    private double myLatitude;
    private double myLongitude;
    private double myAltitude;
    private int myDroneID;
    private double myBattery;
    private double myOrientation;
    private double myVelocity;
    private boolean mySimState;

    /**
     * drone object creation
     * @param theLatitude
     * @param theLongitude
     * @param theAltitude
     * @param theDroneId
     * @param theBattery
     * @param theOrientation
     * @param theVelocity
     */
    public Drone(double theLatitude, double theLongitude, double theAltitude, int theDroneId, double theBattery,
                 double theOrientation, double theVelocity) {
        this.myLatitude = theLatitude;
        this.myLongitude = theLongitude;
        this.myAltitude = theAltitude;
        this.myDroneID = theDroneId;
        this.myBattery = theBattery;
        this.myOrientation = theOrientation;
        this.myVelocity = theVelocity;
        this.mySimState = true;
    }

    /**
     * decided to keep separate rather than all put together so we can update one at a time
     * Different from the SRS
     */
    private void UpdateLatitude(double theLatitude) {
        this.myLatitude = theLatitude;
    }
    private void UpdateLongitude(double theLongitude) {
        this.myLongitude = theLongitude;
    }
    private void UpdateAltitude(double theAltitude) {
        this.myAltitude = theAltitude;
    }
    private void UpdateBattery(double theBattery) {
        this.myBattery = theBattery;
    }
    private void UpdateOrientation(double theOrientation) {
        this.myOrientation = theOrientation;
    }
    private void UpdateVelocity(double theVelocity) {
        this.myVelocity = theVelocity;
    }
    private void UpdateSimState(boolean theSimState) {
        this.mySimState = theSimState;
    }

    /**
     * Updates all telemetry values at once (called by TelemetryGenerator
     * every update interval).
     */
    public void updateTelemetry(double theLatitude, double theLongitude, double theAltitude,
                                double theBattery, double theOrientation, double theVelocity) {
        UpdateLatitude(theLatitude);
        UpdateLongitude(theLongitude);
        UpdateAltitude(theAltitude);
        UpdateBattery(theBattery);
        UpdateOrientation(theOrientation);
        UpdateVelocity(theVelocity);
    }

    /**
     * Getter methods
     */
    public double getLatitude() {
        return this.myLatitude;
    }
    public double getLongitude() {
        return this.myLongitude;
    }
    public double getAltitude() {
        return this.myAltitude;
    }
    public int getDroneID() {
        return this.myDroneID;
    }
    public double getBattery() {
        return this.myBattery;
    }
    public double getOrientation() {
        return this.myOrientation;
    }
    public double getVelocity() {
        return this.myVelocity;
    }
    public boolean getSimState() {
        return this.mySimState;
    }
}
