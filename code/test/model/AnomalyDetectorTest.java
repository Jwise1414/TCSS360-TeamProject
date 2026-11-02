package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for AnomalyDetector, covering SRS acceptance tests TC-03 to TC-06
 * plus duplicate handling and input validation.
 *
 * @author Khalil Ahmednoor
 */
class AnomalyDetectorTest {

    private static final String ID = "DRONE-01";

    private AnomalyDetector detector;

    @BeforeEach
    void setUp() {
        detector = new AnomalyDetector();
    }

    /** Helper: a normal reading with the given values. */
    private static TelemetryReading reading(final double lat, final double lon,
                                            final double alt, final double battery,
                                            final double heading) {
        return new TelemetryReading(ID, lat, lon, alt, battery, heading, 10.0);
    }

    /** Helper: a normal reading that should not trigger anything. */
    private static TelemetryReading normal() {
        return reading(47.0, -122.0, 100.0, 80.0, 90.0);
    }

    @Test
    void normalReadingsProduceNoAnomalies() {
        assertTrue(detector.check(normal()).isEmpty());
        assertTrue(detector.check(normal()).isEmpty());
    }

    // ---- TC-03: LOW_BATTERY ----

    @Test
    void batteryBelow15RaisesLowBattery() {
        final List<AnomalyRecord> result =
                detector.check(reading(47.0, -122.0, 100.0, 14.9, 90.0));
        assertEquals(1, result.size());
        assertEquals(AnomalyType.LOW_BATTERY, result.get(0).getType());
        assertEquals(ID, result.get(0).getDroneId());
    }

    @Test
    void batteryAt15DoesNotRaiseLowBattery() {
        assertTrue(detector.check(reading(47.0, -122.0, 100.0, 15.0, 90.0)).isEmpty());
    }

    // ---- TC-04: SUDDEN_ALTITUDE_DROP ----

    @Test
    void altitudeDropOver30RaisesSuddenDrop() {
        detector.check(reading(47.0, -122.0, 100.0, 80.0, 90.0));
        final List<AnomalyRecord> result =
                detector.check(reading(47.0, -122.0, 69.0, 80.0, 90.0));
        assertEquals(1, result.size());
        assertEquals(AnomalyType.SUDDEN_ALTITUDE_DROP, result.get(0).getType());
    }

    @Test
    void altitudeDropOf30DoesNotRaise() {
        detector.check(reading(47.0, -122.0, 100.0, 80.0, 90.0));
        assertTrue(detector.check(reading(47.0, -122.0, 70.0, 80.0, 90.0)).isEmpty());
    }

    @Test
    void altitudeClimbDoesNotRaise() {
        detector.check(reading(47.0, -122.0, 100.0, 80.0, 90.0));
        assertTrue(detector.check(reading(47.0, -122.0, 200.0, 80.0, 90.0)).isEmpty());
    }

    // ---- TC-05: SHARP_TURN ----

    @Test
    void turnOver60RaisesSharpTurn() {
        detector.check(reading(47.0, -122.0, 100.0, 80.0, 0.0));
        final List<AnomalyRecord> result =
                detector.check(reading(47.0, -122.0, 100.0, 80.0, 61.0));
        assertEquals(1, result.size());
        assertEquals(AnomalyType.SHARP_TURN, result.get(0).getType());
    }

    @Test
    void turnAcross360BoundaryIsSmallChange() {
        // 359° -> 1° is a 2° change, not 358°
        assertEquals(2.0, AnomalyDetector.headingChange(359.0, 1.0), 1e-9);
        detector.check(reading(47.0, -122.0, 100.0, 80.0, 359.0));
        assertTrue(detector.check(reading(47.0, -122.0, 100.0, 80.0, 1.0)).isEmpty());
    }

    // ---- TC-06: GPS_LOCATION_JUMP ----

    @Test
    void positionJumpOver1000mRaisesGpsJump() {
        detector.check(reading(47.0, -122.0, 100.0, 80.0, 90.0));
        // 0.02° latitude is about 2,224 m
        final List<AnomalyRecord> result =
                detector.check(reading(47.02, -122.0, 100.0, 80.0, 90.0));
        assertEquals(1, result.size());
        assertEquals(AnomalyType.GPS_LOCATION_JUMP, result.get(0).getType());
    }

    @Test
    void smallPositionChangeDoesNotRaise() {
        detector.check(reading(47.0, -122.0, 100.0, 80.0, 90.0));
        // 0.005° latitude is about 556 m
        assertTrue(detector.check(reading(47.005, -122.0, 100.0, 80.0, 90.0)).isEmpty());
    }

    @Test
    void distanceCalculationIsReasonable() {
        // One degree of latitude is about 111 km
        final double d = AnomalyDetector.distanceMeters(47.0, -122.0, 48.0, -122.0);
        assertEquals(111_195, d, 100);
    }

    // ---- Detect.Multiple and Detect.Duplicates ----

    @Test
    void multipleAnomaliesForSameDrone() {
        detector.check(reading(47.0, -122.0, 100.0, 80.0, 0.0));
        final List<AnomalyRecord> result =
                detector.check(reading(47.02, -122.0, 50.0, 10.0, 90.0));
        assertEquals(4, result.size());
    }

    @Test
    void continuingConditionIsRecordedOnce() {
        assertEquals(1, detector.check(reading(47.0, -122.0, 100.0, 10.0, 90.0)).size());
        assertTrue(detector.check(reading(47.0, -122.0, 100.0, 9.0, 90.0)).isEmpty());
        assertTrue(detector.check(reading(47.0, -122.0, 100.0, 8.0, 90.0)).isEmpty());
    }

    @Test
    void conditionRecordedAgainAfterItClears() {
        detector.check(reading(47.0, -122.0, 100.0, 10.0, 90.0)); // low
        detector.check(reading(47.0, -122.0, 100.0, 50.0, 90.0)); // recharged
        final List<AnomalyRecord> result =
                detector.check(reading(47.0, -122.0, 100.0, 10.0, 90.0)); // low again
        assertEquals(1, result.size());
        assertEquals(AnomalyType.LOW_BATTERY, result.get(0).getType());
    }

    @Test
    void dronesAreTrackedSeparately() {
        detector.check(new TelemetryReading("DRONE-01", 47.0, -122.0, 100.0, 80.0, 90.0, 10.0));
        // DRONE-02's first reading is far away, but it has no previous reading to compare
        assertTrue(detector.check(
                new TelemetryReading("DRONE-02", 10.0, 10.0, 100.0, 80.0, 90.0, 10.0)).isEmpty());
    }

    // ---- Sim.Validate ----

    @Test
    void invalidTelemetryIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new TelemetryReading(ID, 47.0, -122.0, 100.0, 150.0, 90.0, 10.0));
        assertThrows(IllegalArgumentException.class,
                () -> new TelemetryReading(ID, 95.0, -122.0, 100.0, 80.0, 90.0, 10.0));
        assertThrows(IllegalArgumentException.class,
                () -> new TelemetryReading("", 47.0, -122.0, 100.0, 80.0, 90.0, 10.0));
    }
}
