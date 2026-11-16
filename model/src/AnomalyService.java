import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * TCS-16 / TCS-17 / TCS-18: Connects anomaly detection to the SQLite database
 * and lets the GUI search saved anomalies.
 *
 * @author Khalil Ahmednoor
 */
public class AnomalyService {

    private final AnomalyDetector myDetector;
    private final AnomalyRepository myRepository;

    public AnomalyService(final AnomalyDetector theDetector,
                          final AnomalyRepository theRepository) {
        myDetector = Objects.requireNonNull(theDetector);
        myRepository = Objects.requireNonNull(theRepository);
    }

    /**
     * Checks every drone, saves any new anomalies, and returns them.
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
     */
    public List<AnomalyRecord> getHistory() {
        return myRepository.getAll();
    }

    /**
     * TCS-18: Returns saved anomalies of one type, newest first.
     *
     * @param theType the anomaly type to search for
     * @return matching anomaly records (empty list if none)
     */
    public List<AnomalyRecord> getByType(final AnomalyType theType) {
        return myRepository.getByType(Objects.requireNonNull(theType).name());
    }
}