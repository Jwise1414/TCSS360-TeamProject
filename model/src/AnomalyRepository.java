
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving anomaly records and getting them back from the database.
 */
public class AnomalyRepository {

    // We use DatabaseManager whenever we need to connect to the database.
    private final DatabaseManager databaseManager;

    public AnomalyRepository(DatabaseManager theDatabaseManager) {
        if (theDatabaseManager == null) {
            throw new IllegalArgumentException(
                    "DatabaseManager cannot be null."
            );
        }

        databaseManager = theDatabaseManager;
    }

    /**
     * Saves one anomaly record into the database.
     */
    public void save(AnomalyRecord theRecord) {

        if (theRecord == null) {
            throw new IllegalArgumentException(
                    "Anomaly record cannot be null."
            );
        }

        // The question marks are placeholders for the values we want to save.
        // This helps keep the SQL query safe and prevents SQL injection.
        String sql = "INSERT INTO anomaly_records "
                + "(drone_id, timestamp, type, details) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            // Fill in each placeholder with information from the anomaly.
            statement.setInt(1, theRecord.getDroneID());
            statement.setString(2, theRecord.getTimestamp().toString());
            statement.setString(3, theRecord.getType());
            statement.setString(4, theRecord.getDetails());

            // Actually save the new record.
            statement.executeUpdate();

        } catch (SQLException e) {
            // Let the calling class know if saving failed.
            throw new IllegalStateException(
                    "Error saving anomaly record.", e
            );
        }
    }

    /**
     * Gets every anomaly record, with the newest records first.
     */
    public List<AnomalyRecord> getAll() {

        List<AnomalyRecord> records = new ArrayList<>();

        // Sort by timestamp so the most recent anomalies appear first.
        String sql = "SELECT id, drone_id, timestamp, type, details "
                + "FROM anomaly_records ORDER BY timestamp DESC";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            // Go through the results and turn each database row into an object.
            while (results.next()) {
                records.add(createRecord(results));
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Error retrieving anomaly records.", e
            );
        }

        return records;
    }

    /**
     * Gets all anomaly records for one specific drone.
     */
    public List<AnomalyRecord> getByDroneID(int theDroneID) {

        List<AnomalyRecord> records = new ArrayList<>();

        // Only return records that match the drone ID we provide.
        String sql = "SELECT id, drone_id, timestamp, type, details "
                + "FROM anomaly_records "
                + "WHERE drone_id = ? ORDER BY timestamp DESC";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            // Set the drone ID in the query.
            statement.setInt(1, theDroneID);

            try (ResultSet results = statement.executeQuery()) {

                // Add each matching database row to our list.
                while (results.next()) {
                    records.add(createRecord(results));
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Error retrieving anomaly records.", e
            );
        }

        return records;
    }

    /**
     * Gets all anomaly records that match a specific anomaly type.
     */
    public List<AnomalyRecord> getByType(String theType) {

        if (theType == null || theType.isBlank()) {
            throw new IllegalArgumentException(
                    "Anomaly type cannot be empty."
            );
        }

        List<AnomalyRecord> records = new ArrayList<>();

        // Only return records with the anomaly type we are looking for.
        String sql = "SELECT id, drone_id, timestamp, type, details "
                + "FROM anomaly_records "
                + "WHERE type = ? ORDER BY timestamp DESC";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, theType);

            try (ResultSet results = statement.executeQuery()) {

                // Convert each matching row into an AnomalyRecord.
                while (results.next()) {
                    records.add(createRecord(results));
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Error retrieving anomaly records.", e
            );
        }

        return records;
    }

    /**
     * Gets anomaly records between two timestamps.
     * The start and end times are both included in the search.
     */
    public List<AnomalyRecord> getByDateRange(
            Instant theStart,
            Instant theEnd) {

        if (theStart == null || theEnd == null) {
            throw new IllegalArgumentException(
                    "Start and end dates cannot be null."
            );
        }

        if (theStart.isAfter(theEnd)) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date."
            );
        }

        List<AnomalyRecord> records = new ArrayList<>();

        // BETWEEN includes records that fall exactly on either endpoint.
        String sql = "SELECT id, drone_id, timestamp, type, details "
                + "FROM anomaly_records "
                + "WHERE timestamp BETWEEN ? AND ? "
                + "ORDER BY timestamp DESC";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            // Convert the timestamps to strings before searching the database.
            statement.setString(1, theStart.toString());
            statement.setString(2, theEnd.toString());

            try (ResultSet results = statement.executeQuery()) {

                // Add every record that falls within the date range.
                while (results.next()) {
                    records.add(createRecord(results));
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Error retrieving anomaly records.", e
            );
        }

        return records;
    }

    /**
     * Turns one row from the database into an AnomalyRecord object.
     */
    private AnomalyRecord createRecord(ResultSet theResults)
            throws SQLException {

        // Read the values from the current database row and use them
        // to create an object that the rest of our program can work with.
        return new AnomalyRecord(
                theResults.getInt("id"),
                theResults.getInt("drone_id"),
                Instant.parse(theResults.getString("timestamp")),
                theResults.getString("type"),
                theResults.getString("details")
        );
    }
}
