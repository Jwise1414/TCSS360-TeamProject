/**
 * Starts the drone monitor and connects the dashboard to the controller.
 */
public class DroneMonitorApp {
    private final MonitorController myController;
    private final MonitorDashboard myDashboard;

    /**
     * Connects the dashboard to the controller.
     *
     * @param theDashboard the dashboard window, not null
     * @param theController the controller, not null
     * @throws IllegalArgumentException if either argument is null
     */
    DroneMonitorApp(final MonitorDashboard theDashboard, final MonitorController theController) {
        if (theDashboard == null) {
            throw new IllegalArgumentException("Dashboard cannot be null.");
        }
        if (theController == null) {
            throw new IllegalArgumentException("Controller cannot be null.");
        }
        myDashboard = theDashboard;
        myController = theController;
        myDashboard.setController(myController);
    }

    public static void main(final String[] theArgs) {
        new DroneMonitorApp(new MonitorDashboard(), new MonitorController());
    }

    public void initDatabase() {

    }

    public void buildComponents() {

    }
}
