import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AnomalyDetector {
    private static final double LOW_BATTER_PERCENT = 15.0;
    private static final double MAX_ALTITUDE = 30.0;
    private static final double MAX_TURN_DEGREES = 60.0;
    private static final double MAX_GPS_JUMP_M = 1000.0;
    private Map<Integer, Drone> previousStates;
    private Set<String> activeConditions;

    public AnomalyDetector(){
        previousStates = new HashMap<>();
        activeConditions = new HashSet<>();
    }
}
