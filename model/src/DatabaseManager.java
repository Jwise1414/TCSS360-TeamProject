
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles connecting to our SQLite database and setting it up
 * the first time the program runs.
 */
public class DatabaseManager {

    // This is where our database file will be stored.
    private static final String DATABASE_URL =
            "jdbc:sqlite:data/drone-monitor.db";

    /**
     * Opens a connection to the database.
     * Other classes can use this connection to read or save data.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    /**
     * Sets up the database folder and creates our anomaly table
     * if it doesn't already exist.
     */
    public void initializeDatabase() {

        // Make sure the folder for our database exists.
        File dataDirectory = new File("data");

        // Create the folder if it hasn't been made yet.
        // If this fails, we shouldn't try to open the database.
        if (!dataDirectory.exists() && !dataDirectory.mkdirs()) {
            throw new IllegalStateException(
                    "Could not create the database directory."
            );
        }

        // Also make sure "data" is actually a folder, not a file.
        if (!dataDirectory.isDirectory()) {
            throw new IllegalStateException(
                    "The database path exists but is not a directory."
            );
        }

        // This SQL creates the table that stores detected anomalies.
        // IF NOT EXISTS prevents us from replacing the table every time
        // the program starts, so previously saved records stay there.
        String createTableSQL =
                "CREATE TABLE IF NOT EXISTS anomaly_records ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "drone_id INTEGER NOT NULL, "
                        + "timestamp TEXT NOT NULL, "
                        + "type TEXT NOT NULL, "
                        + "details TEXT"
                        + ")";

        // try-with-resources automatically closes the connection
        // and statement when we're finished with them.
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            // Run the SQL command to create the table.
            statement.execute(createTableSQL);

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {

            // Don't let the app silently continue if the database
            // could not be set up. The caller can handle this error.
            throw new IllegalStateException(
                    "Database initialization failed.", e
            );
        }
    }
}
