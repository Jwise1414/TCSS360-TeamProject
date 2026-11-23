
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class AnomalyQueryController {

    // The controller needs the view to read filters and display results.
    // It also needs the repository to retrieve saved anomaly records.
    private final AnomalyQueryView queryView;
    private final AnomalyRepository anomalyRepository;

    public AnomalyQueryController(
            AnomalyQueryView theView,
            AnomalyRepository theRepository) {

        if (theView == null || theRepository == null) {
            throw new IllegalArgumentException(
                    "View and repository cannot be null."
            );
        }

        queryView = theView;
        anomalyRepository = theRepository;

        // Connect the Search button to the search method below.
        queryView.setSearchAction(this::search);

        // Show the search window after setting up its button.
        queryView.showWindow();
    }

    // Runs whenever the user clicks the Search button.
    private void search() {

        try {
            // Read the filters entered in the search window.
            String droneText = queryView.getDroneID();
            String startText = queryView.getStartDate();
            String endText = queryView.getEndDate();
            String selectedType = queryView.getSelectedType();

            Integer droneID = null;

            // Convert the drone ID to a number if the user entered one.
            if (!droneText.isEmpty()) {
                droneID = Integer.parseInt(droneText);

                if (droneID < 0) {
                    queryView.showMessage(
                            "Drone ID cannot be negative."
                    );
                    return;
                }
            }

            // A date range needs both a start date and an end date.
            if (startText.isEmpty() != endText.isEmpty()) {
                queryView.showMessage(
                        "Please enter both a start date and an end date."
                );
                return;
            }

            Instant start = null;
            Instant end = null;

            // Convert the entered dates into Instants for comparison.
            if (!startText.isEmpty()) {

                LocalDate startDate = LocalDate.parse(startText);
                LocalDate endDate = LocalDate.parse(endText);

                if (startDate.isAfter(endDate)) {
                    queryView.showMessage(
                            "Start date cannot be after end date."
                    );
                    return;
                }

                // Start at midnight on the selected start date.
                start = startDate.atStartOfDay(ZoneOffset.UTC)
                        .toInstant();

                // Include the entire end date, not just midnight.
                end = endDate.plusDays(1)
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant()
                        .minusNanos(1);
            }

            // Get the saved records from the existing repository.
            // We filter them here because the current repository
            // does not have a combined-filter method.
            List<AnomalyRecord> allRecords =
                    anomalyRepository.getAll();

            List<AnomalyRecord> matchingRecords = new ArrayList<>();

            // Check each record against the filters the user selected.
            for (AnomalyRecord record : allRecords) {

                // Skip records that do not match the drone ID.
                if (droneID != null &&
                        record.getDroneID() != droneID) {
                    continue;
                }

                // Skip records outside the selected date range.
                if (start != null &&
                        (record.getTimestamp().isBefore(start) ||
                                record.getTimestamp().isAfter(end))) {
                    continue;
                }

                // Skip records that do not match the selected type.
                if (!"All types".equals(selectedType) &&
                        !selectedType.equals(record.getType())) {
                    continue;
                }

                // If the record passes every selected filter, keep it.
                matchingRecords.add(record);
            }

            // Send the matching records back to the view for display.
            queryView.displayResults(matchingRecords);

        } catch (NumberFormatException e) {
            queryView.showMessage(
                    "Drone ID must be a valid number."
            );

        } catch (DateTimeParseException e) {
            queryView.showMessage(
                    "Please enter dates in yyyy-MM-dd format."
            );

        } catch (IllegalStateException e) {
            // The repository uses this exception when database access fails.
            queryView.showMessage(
                    "Could not retrieve anomaly records."
            );
        }
    }
}
