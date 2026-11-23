import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * TCS-26: Window for searching saved anomalies by type, drone ID and date range
 * at the same time. Results are shown in a table, newest first.
 *
 * @author Khalil Ahmednoor
 */
public class AnomalyQueryView {

    private static final String TITLE = "Anomaly Query";
    private static final String ANY_TYPE = "Any type";
    private static final String[] COLUMNS = {"Time", "Drone", "Type", "Details"};
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private static final int ID_COLUMNS = 5;
    private static final int DATE_COLUMNS = 9;
    private static final int PADDING = 10;
    private static final int WIDTH = 780;
    private static final int HEIGHT = 420;
    /** Starting widths for the Time, Drone, Type and Details columns. */
    private static final int[] COLUMN_WIDTHS = {150, 55, 185, 360};

    private final JFrame myOwner;
    private final AnomalyQueryController myController;
    private final JDialog myDialog;
    private final JComboBox<Object> myTypeBox = new JComboBox<>();
    private final JTextField myIDField = new JTextField(ID_COLUMNS);
    private final JTextField myFromField = new JTextField(DATE_COLUMNS);
    private final JTextField myToField = new JTextField(DATE_COLUMNS);
    private final JLabel myCountLabel = new JLabel(" ");
    private final DefaultTableModel myTableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(final int theRow, final int theColumn) {
            return false;
        }
    };

    /**
     * Builds the query window. Call {@link #show()} to open it.
     *
     * @param theOwner the main window, not null
     * @param theController the controller that runs searches, not null
     */
    public AnomalyQueryView(final JFrame theOwner, final AnomalyQueryController theController) {
        myOwner = Objects.requireNonNull(theOwner, "Owner cannot be null.");
        myController = Objects.requireNonNull(theController, "Controller cannot be null.");
        myDialog = new JDialog(theOwner, TITLE, true);
        buildLayout();
    }

    /** Opens the window and shows every saved anomaly to start with. */
    public void show() {
        runSearch();
        myDialog.setLocationRelativeTo(myOwner);
        myDialog.setVisible(true);
    }

    private void buildLayout() {
        myTypeBox.addItem(ANY_TYPE);
        for (final AnomalyType type : AnomalyType.values()) {
            myTypeBox.addItem(type);
        }
        myFromField.setToolTipText("yyyy-MM-dd (leave blank for no limit)");
        myToField.setToolTipText("yyyy-MM-dd (leave blank for no limit)");
        myIDField.setToolTipText("Leave blank for every drone");

        final JButton search = new JButton("Search");
        search.addActionListener(e -> runSearch());
        final JButton clear = new JButton("Clear");
        clear.addActionListener(e -> clearFilters());
        myDialog.getRootPane().setDefaultButton(search);

        final JPanel firstRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        firstRow.add(new JLabel("Type:"));
        firstRow.add(myTypeBox);
        firstRow.add(new JLabel("Drone ID:"));
        firstRow.add(myIDField);
        final JPanel secondRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        secondRow.add(new JLabel("From:"));
        secondRow.add(myFromField);
        secondRow.add(new JLabel("To:"));
        secondRow.add(myToField);
        secondRow.add(new JLabel("(yyyy-MM-dd)"));
        secondRow.add(search);
        secondRow.add(clear);
        final JPanel filters = new JPanel(new GridLayout(2, 1));
        filters.add(firstRow);
        filters.add(secondRow);

        final JTable table = new JTable(myTableModel);
        table.setAutoCreateRowSorter(true);
        for (int i = 0; i < COLUMN_WIDTHS.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(COLUMN_WIDTHS[i]);
        }

        final JPanel content = new JPanel(new BorderLayout(PADDING, PADDING));
        content.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        content.add(filters, BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(myCountLabel, BorderLayout.SOUTH);

        myDialog.setContentPane(content);
        myDialog.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        myDialog.pack();
    }

    private void clearFilters() {
        myTypeBox.setSelectedIndex(0);
        myIDField.setText("");
        myFromField.setText("");
        myToField.setText("");
        runSearch();
    }

    // reads the filter boxes, runs the search and fills the table
    private void runSearch() {
        final Object selected = myTypeBox.getSelectedItem();
        final AnomalyType type = selected instanceof AnomalyType ? (AnomalyType) selected : null;
        final List<AnomalyRecord> results;
        try {
            final Integer droneID = AnomalyQueryController.parseDroneID(myIDField.getText());
            final LocalDate from = AnomalyQueryController.parseDate(myFromField.getText());
            final LocalDate to = AnomalyQueryController.parseDate(myToField.getText());
            results = myController.search(type, droneID, from, to);
        } catch (final IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(myDialog, ex.getMessage(), TITLE,
                    JOptionPane.WARNING_MESSAGE);
            return;
        } catch (final IllegalStateException ex) {
            JOptionPane.showMessageDialog(myDialog, "Database error: " + ex.getMessage(), TITLE,
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        myTableModel.setRowCount(0);
        for (final AnomalyRecord r : results) {
            myTableModel.addRow(new Object[] {TIME_FORMAT.format(r.getTimestamp()),
                r.getDroneID(), r.getType(), r.getDetails()});
        }
        myCountLabel.setText(results.size() + (results.size() == 1 ? " anomaly" : " anomalies")
                + " found.");
    }
}
