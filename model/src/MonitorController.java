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
    private File myCSVFile;

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
     * Runs the anomaly checks on every drone
     */
    public List<AnomalyRecord> update() {
        List<AnomalyRecord> found = new ArrayList<>();
        for (Drone d : myDrones.values()) {
            found.addAll(myDetector.check(d));
        }
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
     * Conditions that are happening
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
