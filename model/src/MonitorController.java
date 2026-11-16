import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MonitorController {
    private final Map<Integer, Drone> myDrones = new HashMap<>();
    private final AnomalyDetector myDetector = new AnomalyDetector();
    private final List<AnomalyRecord> myAnomalies = new ArrayList<>();
    private final AnomalyService myService;
    private File myCSVFile;

    public MonitorController() {
        DatabaseManager database = new DatabaseManager();
        database.initializeDatabase();
        myService = new AnomalyService(myDetector, new AnomalyRepository(database));
    }

    public void addDrone(Drone theDrone) {
        myDrones.put(theDrone.getDroneID(), theDrone);
    }

    public Drone getDrone(int theID) {
        return myDrones.get(theID);
    }

    public int getDroneCount() {
        return myDrones.size();
    }

    /**
     * Runs the anomaly checks on every drone and saves new anomalies to the database.
     */
    public List<AnomalyRecord> update() {
        List<AnomalyRecord> found = myService.processUpdate(new ArrayList<>(myDrones.values()));
        myAnomalies.addAll(found);
        return found;
    }

    /**
     * Every anomaly found so far, oldest first.
     */
    public List<AnomalyRecord> getAnomalies() {
        return myAnomalies;
    }

    /**
     * TCS-18: searches saved anomalies by type.
     */
    public List<AnomalyRecord> searchByType(AnomalyType theType) {
        return myService.getByType(theType);
    }

    /**
     * Conditions that are happening right now.
     */
    public Set<String> getActiveErrors() {
        return myDetector.getActiveConditions();
    }

    public void setCSVFile(File theFile) {
        myCSVFile = theFile;
    }

    public File getCSVFile() {
        return myCSVFile;
    }
}