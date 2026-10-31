import java.util.HashMap;
import java.util.Map;

public class MonitorController {
    private final Map<Integer, Drone> myDrones = new HashMap<>();

    public void addDrone(Drone theDrone) {
        myDrones.put(theDrone.getDroneID(), theDrone);
    }

    public Drone getDrone(int theID) {
        return myDrones.get(theID);
    }
}


