import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for TCS-26 (AnomalyQueryController). Uses a fixed list of records
 * and UTC so the tests don't need SQLite or depend on the computer's time zone.
 *
 * @author Khalil Ahmednoor
 */
class AnomalyQueryControllerTest {

    private static final AnomalyRecord BATTERY_1 = new AnomalyRecord(1, 1,
            Instant.parse("2026-11-20T10:00:00Z"), "LOW_BATTERY", "Battery 12%");
    private static final AnomalyRecord TURN_1 = new AnomalyRecord(2, 1,
            Instant.parse("2026-11-21T09:30:00Z"), "SHARP_TURN", "Heading changed 95 degrees");
    private static final AnomalyRecord BATTERY_2 = new AnomalyRecord(3, 2,
            Instant.parse("2026-11-22T23:59:59Z"), "LOW_BATTERY", "Battery 8%");
    private static final AnomalyRecord GPS_2 = new AnomalyRecord(4, 2,
            Instant.parse("2026-11-23T00:00:00Z"), "GPS_LOCATION_JUMP", "Moved 900 m");

    private AnomalyQueryController myController;

    @BeforeEach
    void setUp() {
        myController = new AnomalyQueryController(
                () -> List.of(BATTERY_1, TURN_1, BATTERY_2, GPS_2), ZoneOffset.UTC);
    }

    @Test
    void noFiltersReturnsEverythingNewestFirst() {
        assertEquals(List.of(GPS_2, BATTERY_2, TURN_1, BATTERY_1),
                myController.search(null, null, null, null));
    }

    @Test
    void filtersByType() {
        assertEquals(List.of(BATTERY_2, BATTERY_1),
                myController.search(AnomalyType.LOW_BATTERY, null, null, null));
    }

    @Test
    void filtersByDroneID() {
        assertEquals(List.of(TURN_1, BATTERY_1), myController.search(null, 1, null, null));
    }

    @Test
    void combinesTypeAndDroneID() {
        assertEquals(List.of(BATTERY_2),
                myController.search(AnomalyType.LOW_BATTERY, 2, null, null));
    }

    @Test
    void dateRangeIncludesWholeEndDay() {
        final LocalDate day = LocalDate.parse("2026-11-22");
        assertEquals(List.of(BATTERY_2), myController.search(null, null, day, day));
    }

    @Test
    void fromDateOnly() {
        assertEquals(List.of(GPS_2, BATTERY_2),
                myController.search(null, null, LocalDate.parse("2026-11-22"), null));
    }

    @Test
    void noMatchesGivesEmptyList() {
        assertTrue(myController.search(AnomalyType.SUDDEN_ALTITUDE_DROP, null, null, null)
                .isEmpty());
    }

    @Test
    void rejectsNegativeDroneID() {
        assertThrows(IllegalArgumentException.class,
                () -> myController.search(null, -1, null, null));
    }

    @Test
    void rejectsFromAfterTo() {
        assertThrows(IllegalArgumentException.class, () -> myController.search(null, null,
                LocalDate.parse("2026-11-23"), LocalDate.parse("2026-11-20")));
    }

    @Test
    void parsesInputBoxes() {
        assertNull(AnomalyQueryController.parseDroneID("  "));
        assertEquals(Integer.valueOf(7), AnomalyQueryController.parseDroneID(" 7 "));
        assertNull(AnomalyQueryController.parseDate(""));
        assertEquals(LocalDate.of(2026, 11, 23), AnomalyQueryController.parseDate("2026-11-23"));
        assertThrows(IllegalArgumentException.class,
                () -> AnomalyQueryController.parseDroneID("abc"));
        assertThrows(IllegalArgumentException.class,
                () -> AnomalyQueryController.parseDate("11/23/2026"));
    }

    @Test
    void rejectsNullSource() {
        assertThrows(NullPointerException.class, () -> new AnomalyQueryController(null));
    }
}
