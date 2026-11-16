
/**
 * Main starting point for the Drone Monitor application.
 * This class brings together the dashboard and controller.
 */
public class DroneMonitorApp {

    // These are the main parts of the application.
    private final MonitorController myController;
    private final MonitorDashboard myDashboard;

    /**
     * Sets up the main application using the dashboard and controller.
     */
    public DroneMonitorApp(
            MonitorDashboard theDashboard,
            MonitorController theController) {

        myDashboard = theDashboard;
        myController = theController;
    }

    /**
     * This is where the program starts running.
     */
    public static void main(String[] args) {

        // Set up the database before starting the rest of the application.
        DatabaseManager databaseManager = new DatabaseManager();

        try {
            databaseManager.initializeDatabase();
        } catch (IllegalStateException e) {
            // Stop startup if the database could not be initialized.
            System.err.println("Could not start the application: "
                    + e.getMessage());
            return;
        }

        // Create the dashboard and controller, then put the app together.
        DroneMonitorApp theApp = new DroneMonitorApp(
                new MonitorDashboard(),
                new MonitorController()
        );

        theApp.buildComponents();
    }

    /**
     * Prepares the database for the application.
     */
    public void initDatabase() {
        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.initializeDatabase();
    }

    /**
     * Sets up the parts of the application.
     * More setup will go here as the dashboard and controller are connected.
     */
    public void buildComponents() {
        // TODO: Connect the dashboard and controller once their setup methods
        // are confirmed.
    }
}
