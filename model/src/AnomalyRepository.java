
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and retrieving anomaly records from the database.
 */
public class AnomalyRepository {

    private final DatabaseManager databaseManager;

    public AnomalyRepository(DatabaseManager theDatabaseManager) {
        databaseManager = theDatabaseManager;
    }

    /**
     * Saves an anomaly record to the database.
     */
    public void save(AnomalyRecord theRecord) {

        String sql = "INSERT INTO anomaly_records "
                + "(drone_id, timestamp, type, details) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            // Fill in the values for the new anomaly record.
            statement.setInt(1, theRecord.getDroneID());
            statement.setString(2, theRecord.getTimestamp().toString());
            statement.setString(3, theRecord.getType());
            statement.setString(4, theRecord.getDetails());

            statement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error saving anomaly: " + e.getMessage());
        }
    }

    /**
     * Retrieves all anomaly records, with the newest records first.
     */
    public List<AnomalyRecord> getAll() {

        List<AnomalyRecord> records = new ArrayList<>();

        String sql = "SELECT * FROM anomaly_records "
                + "ORDER BY timestamp DESC";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {
                records.add(createRecord(results));
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving anomalies: "
                    + e.getMessage());
        }

        return records;
    }

    /**
     * Retrieves anomaly records belonging to a specific drone.
     */
    public List<AnomalyRecord> getByDroneID(int theDroneID) {

        List<AnomalyRecord> records = new ArrayList<>();

        String sql = "SELECT * FROM anomaly_records "
                + "WHERE drone_id = ? ORDER BY timestamp DESC";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, theDroneID);

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    records.add(createRecord(results));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving anomalies: "
                    + e.getMessage());
        }

        return records;
    }

    /**
     * Retrieves anomaly records matching a specific anomaly type.
     */
    public List<AnomalyRecord> getByType(String theType) {

        List<AnomalyRecord> records = new ArrayList<>();

        String sql = "SELECT * FROM anomaly_records "
                + "WHERE type = ? ORDER BY timestamp DESC";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, theType);

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    records.add(createRecord(results));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving anomalies: "
                    + e.getMessage());
        }

        return records;
    }

    /**
     * Retrieves anomaly records between two timestamps.
     * Both endpoints are included in the search.
     */
    public List<AnomalyRecord> getByDateRange(
            Instant theStart,
            Instant theEnd) {

        List<AnomalyRecord> records = new ArrayList<>();

        String sql = "SELECT * FROM anomaly_records "
                + "WHERE timestamp BETWEEN ? AND ? "
                + "ORDER BY timestamp DESC";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, theStart.toString());
            statement.setString(2, theEnd.toString());

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    records.add(createRecord(results));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving anomalies: "
                    + e.getMessage());
        }

        return records;
    }

    /**
     * Converts a database row into an AnomalyRecord object.
     */
    private AnomalyRecord createRecord(ResultSet theResults)
            throws SQLException {

        return new AnomalyRecord(
                theResults.getInt("id"),
                theResults.getInt("drone_id"),
                Instant.parse(theResults.getString("timestamp")),
                theResults.getString("type"),
                theResults.getString("details")
        );
    }
}
