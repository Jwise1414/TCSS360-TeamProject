
/**
 * Main starting point for the Drone Monitor application.
 * This class brings together the dashboard and controller.
 */
public class DroneMonitorApp {

    // These are the main parts of the application.
    private final MonitorController myController;
    private final MonitorDashboard myDashboard;
    private final DatabaseManager myDatabaseManager;

    // Keep the query controller running while the app is open.
    private AnomalyQueryController myQueryController;

    public DroneMonitorApp(
            MonitorDashboard theDashboard,
            MonitorController theController) {

        myDashboard = theDashboard;
        myController = theController;
        myDatabaseManager = new DatabaseManager();
    }

    public static void main(String[] args) {

        // Set up the database before starting the rest of the application.
        DatabaseManager databaseManager = new DatabaseManager();

        try {
            databaseManager.initializeDatabase();
        } catch (IllegalStateException e) {
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

    public void initDatabase() {
        myDatabaseManager.initializeDatabase();
    }

    public void buildComponents() {

        // Connect the query feature to the database.
        AnomalyRepository repository =
                new AnomalyRepository(myDatabaseManager);

        // Create the anomaly history screen and its controller.
        AnomalyQueryView queryView = new AnomalyQueryView();

        myQueryController = new AnomalyQueryController(
                queryView,
                repository
        );

        // The query screen opens separately from the main dashboard.
    }
}
