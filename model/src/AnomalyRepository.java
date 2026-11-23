import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving anomaly records and retrieving them from the database.
 * This repository acts as the connection between the application's
 * anomaly records and the database where those records are stored.
 */
public class AnomalyRepository {

    // Stores the DatabaseManager used to establish database connections.
    // Keeping this field private prevents other classes from directly
    // changing the database manager used by this repository.
    private final DatabaseManager myDatabaseManager;

    public AnomalyRepository(final DatabaseManager theDatabaseManager) {

        // A valid DatabaseManager is required because every database
        // operation in this class depends on it to establish a connection.
        if (theDatabaseManager == null) {
            throw new IllegalArgumentException(
                    "DatabaseManager cannot be null."
            );
        }

        myDatabaseManager = theDatabaseManager;
    }

    /**
     * Saves one anomaly record into the database.
     */
    public void save(final AnomalyRecord theRecord) {

        // Prevent saving a null record because there would be no
        // anomaly information available to insert into the database.
        if (theRecord == null) {
            throw new IllegalArgumentException(
                    "Anomaly record cannot be null."
            );
        }

        // The question marks are parameter placeholders.
        // Using PreparedStatement keeps the data separate from the SQL
        // command, helping prevent SQL injection attacks.
        String sql = "INSERT INTO anomaly_records "
                + "(drone_id, timestamp, type, details) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection connection = myDatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            // Assign each anomaly property to its corresponding SQL
            // placeholder. The parameter positions start at 1.
            statement.setInt(1, theRecord.getDroneID());
            statement.setString(2, theRecord.getTimestamp().toString());
            statement.setString(3, theRecord.getType());
            statement.setString(4, theRecord.getDetails());

            // Execute the INSERT statement to save the anomaly record
            // permanently in the database.
            statement.executeUpdate();

        } catch (SQLException theException) {

            // Convert the database exception into an unchecked exception
            // so the calling class is informed that saving failed.
            throw new IllegalStateException(
                    "Error saving anomaly record.", theException
            );
        }
    }

    /**
     * Gets every anomaly record, with the newest records first.
     */
    public List<AnomalyRecord> getAll() {

        List<AnomalyRecord> records = new ArrayList<>();

        // Retrieve all stored anomaly records and sort them by timestamp.
        // DESC places the most recent records at the beginning of the results.
        String sql = "SELECT id, drone_id, timestamp, type, details "
                + "FROM anomaly_records ORDER BY timestamp DESC";

        try (Connection connection = myDatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            // Iterate through every row returned by the database.
            // Each row is converted into an AnomalyRecord object before
            // being added to the list that will be returned to the caller.
            while (results.next()) {
                records.add(createRecord(results));
            }

        } catch (SQLException theException) {

            // Report database errors that occur while retrieving records.
            throw new IllegalStateException(
                    "Error retrieving anomaly records.", theException
            );
        }

        return records;
    }

    /**
     * Gets all anomaly records for one specific drone.
     */
    public List<AnomalyRecord> getByDroneID(final int theDroneID) {

        List<AnomalyRecord> records = new ArrayList<>();

        // The WHERE clause filters the results so only records belonging
        // to the requested drone are returned. The timestamp ordering
        // ensures the newest matching records appear first.
        String sql = "SELECT id, drone_id, timestamp, type, details "
                + "FROM anomaly_records "
                + "WHERE drone_id = ? ORDER BY timestamp DESC";

        try (Connection connection = myDatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            // Assign the requested drone ID to the query placeholder.
            statement.setInt(1, theDroneID);

            try (ResultSet results = statement.executeQuery()) {

                // Process each matching database row and convert it into
                // an AnomalyRecord before adding it to the results list.
                while (results.next()) {
                    records.add(createRecord(results));
                }
            }

        } catch (SQLException theException) {

            throw new IllegalStateException(
                    "Error retrieving anomaly records.", theException
            );
        }

        return records;
    }

    /**
     * Gets all anomaly records that match a specific anomaly type.
     */
    public List<AnomalyRecord> getByType(final String theType) {

        // Reject null or blank anomaly types because they cannot provide
        // a meaningful filter for the database query.
        if (theType == null || theType.isBlank()) {
            throw new IllegalArgumentException(
                    "Anomaly type cannot be empty."
            );
        }

        List<AnomalyRecord> records = new ArrayList<>();

        // Filter the database results by anomaly type.
        // Only records whose type matches the supplied value are returned.
        // Results are sorted with the newest anomalies appearing first.
        String sql = "SELECT id, drone_id, timestamp, type, details "
                + "FROM anomaly_records "
                + "WHERE type = ? ORDER BY timestamp DESC";

        try (Connection connection = myDatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            // Assign the requested anomaly type to the SQL placeholder.
            statement.setString(1, theType);

            try (ResultSet results = statement.executeQuery()) {

                // Convert every matching database row into an object
                // and add it to the list of matching anomaly records.
                while (results.next()) {
                    records.add(createRecord(results));
                }
            }

        } catch (SQLException theException) {

            throw new IllegalStateException(
                    "Error retrieving anomaly records.", theException
            );
        }

        return records;
    }

    /**
     * Gets anomaly records between two timestamps.
     * Both the start and end timestamps are included in the search.
     */
    public List<AnomalyRecord> getByDateRange(
            final Instant theStart,
            final Instant theEnd) {

        // Both timestamps are required to define a valid date range.
        if (theStart == null || theEnd == null) {
            throw new IllegalArgumentException(
                    "Start and end dates cannot be null."
            );
        }

        // A date range must begin before or at the same time as it ends.
        // Rejecting an invalid range prevents an unintended database search.
        if (theStart.isAfter(theEnd)) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date."
            );
        }

        List<AnomalyRecord> records = new ArrayList<>();

        // BETWEEN retrieves records whose timestamps fall within the
        // supplied range, including records exactly equal to either endpoint.
        // DESC ensures the newest matching records appear first.
        String sql = "SELECT id, drone_id, timestamp, type, details "
                + "FROM anomaly_records "
                + "WHERE timestamp BETWEEN ? AND ? "
                + "ORDER BY timestamp DESC";

        try (Connection connection = myDatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            // Convert both Instant values into strings so they can be
            // compared against the timestamp values stored in the database.
            statement.setString(1, theStart.toString());
            statement.setString(2, theEnd.toString());

            try (ResultSet results = statement.executeQuery()) {

                // Iterate through the matching rows and convert each one
                // into an AnomalyRecord before adding it to the results list.
                while (results.next()) {
                    records.add(createRecord(results));
                }
            }

        } catch (SQLException theException) {

            throw new IllegalStateException(
                    "Error retrieving anomaly records.", theException
            );
        }

        return records;
    }

    /**
     * Converts one database result row into an AnomalyRecord object.
     *
     * @param theResults the result set positioned at the current database row
     * @return an AnomalyRecord containing the values from that row
     * @throws SQLException if a database value cannot be retrieved
     */
    private AnomalyRecord createRecord(final ResultSet theResults)
            throws SQLException {

        // Read each column from the current database row and pass its value
        // into the AnomalyRecord constructor. This keeps database retrieval
        // separate from object creation and avoids repeating conversion logic
        // throughout the repository's retrieval methods.
        return new AnomalyRecord(
                theResults.getInt("id"),
                theResults.getInt("drone_id"),
                Instant.parse(theResults.getString("timestamp")),
                theResults.getString("type"),
                theResults.getString("details")
        );
    }
}