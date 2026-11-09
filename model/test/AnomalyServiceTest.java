import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for TCS-16 (AnomalyDetector on Drone) and TCS-17 (AnomalyService
 * saving through AnomalyRepository). Uses an in-memory repository so the
 * tests don't need the SQLite file.
 *
 * @author Khalil Ahmednoor
 */
class AnomalyServiceTest {

    /** Repository that keeps records in a list instead of SQLite. */
    private static final class FakeRepository extends AnomalyRepository {
        private final List<AnomalyRecord> mySaved = new ArrayList<>();

        FakeRepository() {
            super(new DatabaseManager());
        }

        @Override
        public void save(final AnomalyRecord theRecord) {
            mySaved.add(theRecord);
        }

        @Override
        public List<AnomalyRecord> getAll() {
            return new ArrayList<>(mySaved);
        }
    }

    private FakeRepository myRepository;
    private AnomalyService myService;

    @BeforeEach
    void setUp() {
        myRepository = new FakeRepository();
        myService = new AnomalyService(new AnomalyDetector(), myRepository);
    }

    private static Drone drone(final double theLat, final double theLon,
                               final double theAlt, final double theBattery,
                               final double theHeading) {
        return new Drone(theLat, theLon, theAlt, 1, theBattery, theHeading, 10);
    }

    @Test
    void normalReadingSavesNothing() {
        assertTrue(myService.processUpdate(List.of(drone(47.6, -122.3, 100, 90, 90))).isEmpty());
        assertTrue(myService.getHistory().isEmpty());
    }

    @Test
    void lowBatteryIsDetectedAndSaved() {
        final List<AnomalyRecord> found =
                myService.processUpdate(List.of(drone(47.6, -122.3, 100, 10, 90)));
        assertEquals(1, found.size());
        assertEquals("LOW_BATTERY", found.get(0).getType());
        assertEquals(1, myService.getHistory().size());
    }

    @Test
    void continuingConditionIsSavedOnce() {
        final Drone d = drone(47.6, -122.3, 100, 10, 90);
        myService.processUpdate(List.of(d));
        d.updateTelemetry(47.6, -122.3, 100, 9, 90, 10);
        myService.processUpdate(List.of(d));
        assertEquals(1, myService.getHistory().size());
    }

    @Test
    void altitudeDropTurnAndGpsJumpAreDetected() {
        final Drone d = drone(47.60, -122.33, 100, 90, 10);
        myService.processUpdate(List.of(d));
        // Drops 50 m, turns 90 degrees, and moves about 11 km in one interval.
        d.updateTelemetry(47.70, -122.33, 50, 90, 100, 10);
        final List<AnomalyRecord> found = myService.processUpdate(List.of(d));
        assertEquals(3, found.size());
        assertEquals(3, myService.getHistory().size());
    }

    @Test
    void headingChangeWrapsAround360() {
        assertEquals(2.0, AnomalyDetector.headingChange(359, 1), 1e-9);
    }
}
