
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the SQLite database connection and initialization.
 */
public class DatabaseManager {

    // Location of the local SQLite database file.
    private static final String DATABASE_URL =
            "jdbc:sqlite:data/drone-monitor.db";

    /**
     * Opens a connection to the SQLite database.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    /**
     * Creates the database directory and anomaly table if needed.
     */
    public void initializeDatabase() {

        // Make sure the folder exists before opening the database.
        File dataDirectory = new File("data");

        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs();
        }

        // Create the table only if it does not already exist.
        String createTableSQL =
                "CREATE TABLE IF NOT EXISTS anomaly_records ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "drone_id INTEGER NOT NULL, "
                        + "timestamp TEXT NOT NULL, "
                        + "type TEXT NOT NULL, "
                        + "details TEXT"
                        + ")";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(createTableSQL);

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: "
                    + e.getMessage());
        }
    }
}
