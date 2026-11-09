import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * TCS-16 / TCS-17: Connects anomaly detection to the SQLite database.
 *
 * <p>Each update interval, the service runs the AnomalyDetector on every
 * drone and saves each new anomaly through AnomalyRepository so it is kept
 * in drone-monitor.db. The returned list can be shown as alerts in the GUI.</p>
 *
 * @author Khalil Ahmednoor
 */
public class AnomalyService {

    private final AnomalyDetector myDetector;
    private final AnomalyRepository myRepository;

    /**
     * Creates the service.
     *
     * @param theDetector   the detector to run on each drone
     * @param theRepository where detected anomalies are saved
     */
    public AnomalyService(final AnomalyDetector theDetector,
                          final AnomalyRepository theRepository) {
        myDetector = Objects.requireNonNull(theDetector);
        myRepository = Objects.requireNonNull(theRepository);
    }

    /**
     * Checks every drone, saves any new anomalies, and returns them.
     *
     * @param theDrones the drones to check this interval
     * @return new anomalies found (empty list if none)
     */
    public List<AnomalyRecord> processUpdate(final List<Drone> theDrones) {
        final List<AnomalyRecord> found = new ArrayList<>();
        for (final Drone drone : theDrones) {
            for (final AnomalyRecord anomaly : myDetector.check(drone)) {
                myRepository.save(anomaly);
                found.add(anomaly);
            }
        }
        return found;
    }

    /**
     * Returns every saved anomaly, newest first.
     *
     * @return all anomaly records in the database
     */
    public List<AnomalyRecord> getHistory() {
        return myRepository.getAll();
    }
}
