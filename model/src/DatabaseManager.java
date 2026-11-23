import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles connecting to the SQLite database and preparing it for use.
 * This class is responsible for establishing database connections
 * and creating the necessary database structure when the program runs
 * for the first time.
 */
public class DatabaseManager {

    // Stores the JDBC connection URL used to locate the SQLite database.
    // Keeping this value constant ensures that every database connection
    // uses the same database file instead of creating different locations.
    private static final String DATABASE_URL =
            "jdbc:sqlite:data/drone-monitor.db";

    /**
     * Opens a connection to the database.
     * Other classes use this connection to retrieve or save information.
     *
     * @return an active connection to the SQLite database
     * @throws SQLException if a connection cannot be established
     */
    public Connection getConnection() throws SQLException {

        // DriverManager uses the database URL to establish a connection
        // to the SQLite database file. If the database does not exist,
        // SQLite can create it when the connection is opened.
        return DriverManager.getConnection(DATABASE_URL);
    }

    /**
     * Sets up the database directory and creates the anomaly table
     * if it does not already exist.
     */
    public void initializeDatabase() {

        // Define the directory where the SQLite database file will be stored.
        // Keeping the database inside its own folder helps organize
        // application data separately from the source code.
        File dataDirectory = new File("data");

        // Check whether the database directory already exists.
        // If it does not, attempt to create it before opening the database.
        // If directory creation fails, stop initialization because the
        // database file cannot be stored in the required location.
        if (!dataDirectory.exists() && !dataDirectory.mkdirs()) {
            throw new IllegalStateException(
                    "Could not create the database directory."
            );
        }

        // Confirm that the path is actually a directory.
        // This prevents the application from attempting to store the
        // database inside a regular file with the same name.
        if (!dataDirectory.isDirectory()) {
            throw new IllegalStateException(
                    "The database path exists but is not a directory."
            );
        }

        // Define the SQL statement responsible for creating the anomaly table.
        // IF NOT EXISTS ensures that an existing table is preserved when
        // initialization runs again, preventing previously stored records
        // from being overwritten or deleted.
        String createTableSQL =
                "CREATE TABLE IF NOT EXISTS anomaly_records ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "drone_id INTEGER NOT NULL, "
                        + "timestamp TEXT NOT NULL, "
                        + "type TEXT NOT NULL, "
                        + "details TEXT"
                        + ")";

        // Use try-with-resources to automatically close both the database
        // connection and SQL statement after initialization completes.
        // This prevents database resources from remaining open unnecessarily.
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            // Execute the table creation statement.
            // If the table already exists, IF NOT EXISTS allows this
            // operation to complete without changing the existing table.
            statement.execute(createTableSQL);

            // Confirm that the database setup completed successfully.
            System.out.println("Database initialized successfully.");

        } catch (SQLException theException) {

            // Stop initialization and report the underlying database error.
            // Wrapping the SQLException in an IllegalStateException allows
            // the calling class to recognize that the application could
            // not establish the required database structure.
            throw new IllegalStateException(
                    "Database initialization failed.", theException
            );
        }
    }
}