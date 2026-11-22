/**
 * Main starting point for the Drone Monitor application.
 * This class brings together the dashboard and controller.
 */
public class DroneMonitorApp {

    // These are the main parts of the application.
    private final MonitorController myController;
    private final MonitorDashboard myDashboard;

    /**
     * Sets up the main application and connects the dashboard to the controller.
     *
     * @param theDashboard the dashboard window, not null
     * @param theController the controller, not null
     * @throws IllegalArgumentException if either argument is null
     */
    public DroneMonitorApp(final MonitorDashboard theDashboard,
                           final MonitorController theController) {
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

    /**
     * This is where the program starts running.
     *
     * @param theArgs command line arguments (not used)
     */
    public static void main(final String[] theArgs) {

        // Set up the database before starting the rest of the application.
        final DatabaseManager databaseManager = new DatabaseManager();

        try {
            databaseManager.initializeDatabase();
        } catch (final IllegalStateException e) {
            // Stop startup if the database could not be initialized.
            System.err.println("Could not start the application: "
                    + e.getMessage());
            return;
        }

        // Create the dashboard and controller, then put the app together.
        final DroneMonitorApp app = new DroneMonitorApp(
                new MonitorDashboard(),
                new MonitorController()
        );

        app.buildComponents();
    }

    /**
     * Prepares the database for the application.
     */
    public void initDatabase() {
        final DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.initializeDatabase();
    }

    /**
     * Sets up the parts of the application.
     * The dashboard and controller are connected in the constructor;
     * more setup will go here as other components are added.
     */
    public void buildComponents() {
    }
}
