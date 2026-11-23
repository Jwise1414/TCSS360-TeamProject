import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * TCS-26: Runs combined searches over saved anomalies for the Anomaly Query window.
 * Any filter can be left empty (null) to mean "match everything".
 *
 * @author Khalil Ahmednoor
 */
public class AnomalyQueryController {

    /** Smallest allowed drone ID. */
    private static final int MIN_DRONE_ID = 0;

    private final Supplier<List<AnomalyRecord>> mySource;
    private final ZoneId myZone;

    /**
     * Creates a query controller that searches the given anomaly history.
     *
     * @param theSource supplies every saved anomaly, not null
     * @param theZone the time zone used to turn dates into start/end of day, not null
     * @throws NullPointerException if an argument is null
     */
    public AnomalyQueryController(final Supplier<List<AnomalyRecord>> theSource,
                                  final ZoneId theZone) {
        mySource = Objects.requireNonNull(theSource, "Source cannot be null.");
        myZone = Objects.requireNonNull(theZone, "Zone cannot be null.");
    }

    /**
     * Creates a query controller that uses the system time zone.
     *
     * @param theSource supplies every saved anomaly, not null
     */
    public AnomalyQueryController(final Supplier<List<AnomalyRecord>> theSource) {
        this(theSource, ZoneId.systemDefault());
    }

    /**
     * Finds saved anomalies that match every filter that was given.
     *
     * @param theType the anomaly type, or null for any type
     * @param theDroneID the drone ID, or null for any drone
     * @param theFrom the first day to include, or null for no start limit
     * @param theTo the last day to include, or null for no end limit
     * @return matching anomalies, newest first (empty if none)
     * @throws IllegalArgumentException if the drone ID is negative or theFrom is after theTo
     */
    public List<AnomalyRecord> search(final AnomalyType theType, final Integer theDroneID,
                                      final LocalDate theFrom, final LocalDate theTo) {
        if (theDroneID != null && theDroneID < MIN_DRONE_ID) {
            throw new IllegalArgumentException("Drone ID cannot be negative: " + theDroneID);
        }
        if (theFrom != null && theTo != null && theFrom.isAfter(theTo)) {
            throw new IllegalArgumentException("From date cannot be after To date.");
        }
        final Instant start = theFrom == null ? null : theFrom.atStartOfDay(myZone).toInstant();
        final Instant endExclusive =
                theTo == null ? null : theTo.plusDays(1).atStartOfDay(myZone).toInstant();

        final List<AnomalyRecord> results = new ArrayList<>();
        for (final AnomalyRecord r : mySource.get()) {
            if (theType != null && !r.getType().equals(theType.name())) {
                continue;
            }
            if (theDroneID != null && r.getDroneID() != theDroneID) {
                continue;
            }
            if (start != null && r.getTimestamp().isBefore(start)) {
                continue;
            }
            if (endExclusive != null && !r.getTimestamp().isBefore(endExclusive)) {
                continue;
            }
            results.add(r);
        }
        results.sort(Comparator.comparing(AnomalyRecord::getTimestamp).reversed());
        return results;
    }

    /**
     * Turns the text from the drone ID box into a drone ID.
     *
     * @param theText the text the user typed, may be null or blank
     * @return the drone ID, or null if the box was empty
     * @throws IllegalArgumentException if the text is not a whole number
     */
    public static Integer parseDroneID(final String theText) {
        if (theText == null || theText.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(theText.trim());
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("\"" + theText.trim()
                    + "\" is not a valid drone ID.", e);
        }
    }

    /**
     * Turns the text from a date box (yyyy-MM-dd) into a date.
     *
     * @param theText the text the user typed, may be null or blank
     * @return the date, or null if the box was empty
     * @throws IllegalArgumentException if the text is not a yyyy-MM-dd date
     */
    public static LocalDate parseDate(final String theText) {
        if (theText == null || theText.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(theText.trim());
        } catch (final java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("\"" + theText.trim()
                    + "\" is not a date. Use yyyy-MM-dd.", e);
        }
    }
}
