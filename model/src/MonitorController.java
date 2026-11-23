import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Keeps track of the drones, runs anomaly checks on them and saves
 * new anomalies to the database.
 */
public class MonitorController {
    private final Map<Integer, Drone> myDrones = new HashMap<>();
    private final AnomalyDetector myDetector = new AnomalyDetector();
    private final List<AnomalyRecord> myAnomalies = new ArrayList<>();
    private final AnomalyService myService;
    private File myCSVFile;

    public MonitorController() {
        final DatabaseManager database = new DatabaseManager();
        database.initializeDatabase();
        myService = new AnomalyService(myDetector, new AnomalyRepository(database));
    }

    /**
     * Adds a drone, replacing any drone with the same ID.
     *
     * @param theDrone the drone to add, not null
     * @throws IllegalArgumentException if theDrone is null
     */
    public void addDrone(final Drone theDrone) {
        if (theDrone == null) {
            throw new IllegalArgumentException("Drone cannot be null.");
        }
        myDrones.put(theDrone.getDroneID(), theDrone);
    }

    /**
     * Returns the drone with the given ID, or null if there is none.
     *
     * @param theID the drone ID
     * @return the drone, or null
     */
    public Drone getDrone(final int theID) {
        return myDrones.get(theID);
    }

    public int getDroneCount() {
        return myDrones.size();
    }

    /**
     * Runs the anomaly checks on every drone and saves new anomalies to the database.
     *
     * @return the new anomalies found
     */
    public List<AnomalyRecord> update() {
        final List<AnomalyRecord> found =
                myService.processUpdate(new ArrayList<>(myDrones.values()));
        myAnomalies.addAll(found);
        return found;
    }

    /**
     * Every anomaly found so far, oldest first.
     *
     * @return a read-only view of the anomaly list
     */
    public List<AnomalyRecord> getAnomalies() {
        return Collections.unmodifiableList(myAnomalies);
    }

    /**
     * TCS-18: searches saved anomalies by type.
     *
     * @param theType the anomaly type to search for, not null
     * @return matching saved anomalies, newest first
     * @throws IllegalArgumentException if theType is null
     */
    public List<AnomalyRecord> searchByType(final AnomalyType theType) {
        if (theType == null) {
            throw new IllegalArgumentException("Type cannot be null.");
        }
        return myService.getByType(theType);
    }

    /**
     * TCS-26: every anomaly saved in the database, newest first.
     *
     * @return the saved anomaly history
     * @throws IllegalStateException if the database cannot be read
     */
    public List<AnomalyRecord> getHistory() {
        return myService.getHistory();
    }

    /**
     * TCS-28: saved anomalies between two times (both included), newest first.
     * The date search runs in the database.
     *
     * @param theStart the earliest time to include, not null
     * @param theEnd the latest time to include, not null
     * @return matching saved anomalies
     * @throws IllegalArgumentException if a time is null or theStart is after theEnd
     * @throws IllegalStateException if the database cannot be read
     */
    public List<AnomalyRecord> searchByDateRange(final Instant theStart, final Instant theEnd) {
        return myService.getByDateRange(theStart, theEnd);
    }

    /**
     * Conditions that are happening right now.
     *
     * @return the active conditions
     */
    public Set<String> getActiveErrors() {
        return myDetector.getActiveConditions();
    }

    /**
     * Sets the CSV file to read telemetry from.
     *
     * @param theFile the CSV file, not null
     * @throws IllegalArgumentException if theFile is null
     */
    public void setCSVFile(final File theFile) {
        if (theFile == null) {
            throw new IllegalArgumentException("CSV file cannot be null.");
        }
        myCSVFile = theFile;
    }

    public File getCSVFile() {
        return myCSVFile;
    }

    /**
     * Loads drones from a CSV file, replacing the current drones. Nothing
     * changes if the file has an error. The detector keeps each drone's
     * previous reading, so loading a later snapshot of the same drones
     * lets the next update find drops, turns and GPS jumps.
     *
     * @param theFile the CSV file, not null
     * @return the number of drones loaded
     * @throws IllegalArgumentException if theFile is null or has an invalid line
     * @throws IOException if the file cannot be read
     */
    public int loadCSV(final File theFile) throws IOException {
        final List<Drone> drones = DroneCSVReader.read(theFile);
        myDrones.clear();
        for (final Drone drone : drones) {
            addDrone(drone);
        }
        setCSVFile(theFile);
        return drones.size();
    }
}
