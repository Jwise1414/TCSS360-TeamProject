import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads drones from a CSV file with this header:
 * droneID,latitude,longitude,altitude,battery,orientation,velocity
 */
public final class DroneCSVReader {
    /** The header the first line of the file must match. */
    public static final String HEADER =
            "droneID,latitude,longitude,altitude,battery,orientation,velocity";

    private static final String DELIMITER = ",";
    private static final int COLUMN_COUNT = 7;
    private static final int ID_COLUMN = 0;
    private static final int LATITUDE_COLUMN = 1;
    private static final int LONGITUDE_COLUMN = 2;
    private static final int ALTITUDE_COLUMN = 3;
    private static final int BATTERY_COLUMN = 4;
    private static final int ORIENTATION_COLUMN = 5;
    private static final int VELOCITY_COLUMN = 6;
    private static final int HEADER_LINE_NUMBER = 1;

    private DroneCSVReader() {
    }

    /**
     * Reads every drone in the file. Blank lines are skipped.
     *
     * @param theFile the CSV file, not null
     * @return the drones in the order they appear in the file
     * @throws IllegalArgumentException if theFile is null, or a line is not valid
     *         (the message includes the line number)
     * @throws IOException if the file cannot be read
     */
    public static List<Drone> read(final File theFile) throws IOException {
        if (theFile == null) {
            throw new IllegalArgumentException("CSV file cannot be null.");
        }
        final List<Drone> drones = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(theFile.toPath(),
                StandardCharsets.UTF_8)) {
            final String header = reader.readLine();
            if (header == null || !HEADER.equals(header.trim())) {
                throw new IllegalArgumentException("Line " + HEADER_LINE_NUMBER
                        + ": expected header \"" + HEADER + "\"");
            }
            int lineNumber = HEADER_LINE_NUMBER;
            String line = reader.readLine();
            while (line != null) {
                lineNumber++;
                if (!line.isBlank()) {
                    drones.add(parseLine(line, lineNumber));
                }
                line = reader.readLine();
            }
        }
        return drones;
    }

    private static Drone parseLine(final String theLine, final int theLineNumber) {
        final String[] parts = theLine.split(DELIMITER, -1);
        if (parts.length != COLUMN_COUNT) {
            throw new IllegalArgumentException("Line " + theLineNumber + ": expected "
                    + COLUMN_COUNT + " values but found " + parts.length);
        }
        try {
            return new Drone(
                    Double.parseDouble(parts[LATITUDE_COLUMN].trim()),
                    Double.parseDouble(parts[LONGITUDE_COLUMN].trim()),
                    Double.parseDouble(parts[ALTITUDE_COLUMN].trim()),
                    Integer.parseInt(parts[ID_COLUMN].trim()),
                    Double.parseDouble(parts[BATTERY_COLUMN].trim()),
                    Double.parseDouble(parts[ORIENTATION_COLUMN].trim()),
                    Double.parseDouble(parts[VELOCITY_COLUMN].trim()));
        } catch (final IllegalArgumentException e) {
            // NumberFormatException is an IllegalArgumentException too
            throw new IllegalArgumentException("Line " + theLineNumber + ": "
                    + e.getMessage(), e);
        }
    }
}
