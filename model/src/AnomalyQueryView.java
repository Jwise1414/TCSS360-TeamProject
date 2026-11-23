
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Provides the graphical interface for searching and viewing
 * previously recorded drone anomalies.
 *
 * This class is the View in the MVC architecture. It handles
 * user input and displays results, while the Controller handles
 * search logic and communicates with the Repository.
 */
public class AnomalyQueryView {

    // Main window and user input components.
    private final JFrame frame;
    private final JTextField droneIDField;
    private final JTextField startDateField;
    private final JTextField endDateField;
    private final JComboBox<String> typeBox;

    // Search controls and results display components.
    private final JButton searchButton;
    private final JLabel messageLabel;
    private final DefaultTableModel tableModel;

    /**
     * Creates the anomaly history window and initializes
     * its input fields, dropdown, results table, and layout.
     */
    public AnomalyQueryView() {

        // JFrame represents the main application window.
        frame = new JFrame("Anomaly History");

        // Text fields allow users to enter optional search criteria.
        droneIDField = new JTextField(10);
        startDateField = new JTextField(10);
        endDateField = new JTextField(10);

        // Dropdown lets users search by a specific anomaly type
        // or select "All types" to include every anomaly category.
        typeBox = new JComboBox<>();
        typeBox.addItem("All types");

        // Populate the dropdown using the project's AnomalyType enum.
        // This keeps the available search options consistent with
        // the anomaly types defined elsewhere in the application.
        for (AnomalyType type : AnomalyType.values()) {
            typeBox.addItem(type.name());
        }

        // Button triggers the search action supplied by the Controller.
        searchButton = new JButton("Search");

        // Displays instructions, validation errors, and search results status.
        messageLabel = new JLabel("Enter filters and select Search.");

        // Defines the columns and initial empty state of the results table.
        tableModel = new DefaultTableModel(
                new String[]{"Drone ID", "Timestamp", "Type", "Details"}, 0
        ) {
            // Prevents users from editing historical anomaly records
            // directly through the results table.
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // JTable displays the records managed by the table model.
        JTable resultsTable = new JTable(tableModel);

        // GridLayout organizes the search labels and input components
        // into five rows and two columns, with spacing between components.
        JPanel filterPanel = new JPanel(new GridLayout(5, 2, 5, 5));

        // Adds padding around the search controls.
        filterPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        // Add the search labels and their corresponding input components.
        filterPanel.add(new JLabel("Drone ID (optional):"));
        filterPanel.add(droneIDField);

        filterPanel.add(new JLabel("Start date (yyyy-MM-dd):"));
        filterPanel.add(startDateField);

        filterPanel.add(new JLabel("End date (yyyy-MM-dd):"));
        filterPanel.add(endDateField);

        filterPanel.add(new JLabel("Anomaly type:"));
        filterPanel.add(typeBox);

        filterPanel.add(new JLabel(""));
        filterPanel.add(searchButton);

        // BorderLayout divides the main window into regions:
        // NORTH for filters, CENTER for search results, and SOUTH for messages.
        frame.setLayout(new BorderLayout(5, 5));

        frame.add(filterPanel, BorderLayout.NORTH);

        // JScrollPane allows users to scroll through results when
        // the number of anomaly records exceeds the visible table area.
        frame.add(new JScrollPane(resultsTable), BorderLayout.CENTER);

        frame.add(messageLabel, BorderLayout.SOUTH);

        // Configure the window dimensions, initial position, and closing behavior.
        frame.setSize(700, 450);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * Connects the Search button to an action provided by the Controller.
     *
     * Runnable allows the Controller to supply the search operation
     * without making the View responsible for implementing search logic.
     */
    public void setSearchAction(Runnable theAction) {
        searchButton.addActionListener(e -> theAction.run());
    }

    /**
     * Makes the anomaly history window visible to the user.
     */
    public void showWindow() {
        frame.setVisible(true);
    }

    // Getter methods allow the Controller to retrieve the user's search criteria.

    /**
     * Retrieves the entered drone ID, removing surrounding whitespace.
     */
    public String getDroneID() {
        return droneIDField.getText().trim();
    }

    /**
     * Retrieves the entered start date.
     */
    public String getStartDate() {
        return startDateField.getText().trim();
    }

    /**
     * Retrieves the entered end date.
     */
    public String getEndDate() {
        return endDateField.getText().trim();
    }

    /**
     * Retrieves the currently selected anomaly type from the dropdown.
     */
    public String getSelectedType() {
        return (String) typeBox.getSelectedItem();
    }

    /**
     * Updates the results table with anomaly records returned by the Controller.
     *
     * Existing rows are removed before the new results are displayed,
     * preventing records from previous searches from remaining in the table.
     */
    public void displayResults(List<AnomalyRecord> theRecords) {

        // Clear previous search results.
        tableModel.setRowCount(0);

        // Add each matching anomaly record as a row in the table.
        for (AnomalyRecord record : theRecords) {
            tableModel.addRow(new Object[]{
                    record.getDroneID(),
                    record.getTimestamp(),
                    record.getType(),
                    record.getDetails()
            });
        }

        // Update the status message based on the search results.
        if (theRecords.isEmpty()) {
            messageLabel.setText("No matching anomaly records found.");
        } else {
            messageLabel.setText(
                    theRecords.size() + " matching record(s) found."
            );
        }
    }

    /**
     * Displays a message provided by the Controller.
     * Used for input validation errors and database-related messages.
     */
    public void showMessage(String theMessage) {
        messageLabel.setText(theMessage);
    }
}
