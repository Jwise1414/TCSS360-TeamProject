import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for TCS-16 (AnomalyDetector), TCS-17 (saving through AnomalyRepository)
 * and TCS-18 (search by type). Uses an in-memory repository so the tests
 * don't need the SQLite file.
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

        @Override
        public List<AnomalyRecord> getByType(final String theType) {
            final List<AnomalyRecord> result = new ArrayList<>();
            for (final AnomalyRecord r : mySaved) {
                if (r.getType().equals(theType)) {
                    result.add(r);
                }
            }
            return result;
        }

        @Override
        public List<AnomalyRecord> getByDateRange(final Instant theStart, final Instant theEnd) {
            final List<AnomalyRecord> result = new ArrayList<>();
            for (final AnomalyRecord r : mySaved) {
                if (!r.getTimestamp().isBefore(theStart) && !r.getTimestamp().isAfter(theEnd)) {
                    result.add(r);
                }
            }
            return result;
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

    @Test
    void searchByTypeReturnsOnlyMatchingType() {
        final Drone d = drone(47.60, -122.33, 100, 10, 10);   // low battery
        myService.processUpdate(List.of(d));
        d.updateTelemetry(47.70, -122.33, 50, 9, 100, 10);    // drop, turn, GPS jump
        myService.processUpdate(List.of(d));

        final List<AnomalyRecord> lowBattery = myService.getByType(AnomalyType.LOW_BATTERY);
        assertEquals(1, lowBattery.size());
        assertEquals("LOW_BATTERY", lowBattery.get(0).getType());
        assertEquals(1, myService.getByType(AnomalyType.SHARP_TURN).size());
    }

    @Test
    void searchByTypeWithNoMatchesIsEmpty() {
        myService.processUpdate(List.of(drone(47.6, -122.3, 100, 10, 90)));
        assertTrue(myService.getByType(AnomalyType.GPS_LOCATION_JUMP).isEmpty());
    }

    @Test
    void searchByNullTypeThrows() {
        assertThrows(NullPointerException.class, () -> myService.getByType(null));
    }

    @Test
    void searchByDateRangeReturnsOnlyAnomaliesInRange() {
        myService.processUpdate(List.of(drone(47.6, -122.3, 100, 10, 90)));
        final Instant now = Instant.now();
        assertEquals(1, myService.getByDateRange(now.minusSeconds(60), now.plusSeconds(60))
                .size());
        assertTrue(myService.getByDateRange(now.minusSeconds(7200), now.minusSeconds(3600))
                .isEmpty());
    }
}
