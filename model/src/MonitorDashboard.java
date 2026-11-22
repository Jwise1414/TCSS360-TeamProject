import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

/**
 * The main window: shows the header, status line and the menus.
 */
public class MonitorDashboard {
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private static final Color BACKGROUND = new Color(0x1E2228);
    private static final Color TEXT = new Color(0xE6E8EB);
    private static final Color MUTED = new Color(0x9AA3AD);

    private static final String FONT_NAME = "Segoe UI";
    private static final int TITLE_FONT_SIZE = 26;
    private static final int SUBTITLE_FONT_SIZE = 14;
    private static final int STATUS_FONT_SIZE = 13;
    private static final int LOG_FONT_SIZE = 12;

    private static final int FRAME_WIDTH = 600;
    private static final int FRAME_HEIGHT = 400;
    private static final int PADDING_TOP = 24;
    private static final int PADDING_SIDE = 28;
    private static final int PADDING_BOTTOM = 20;
    private static final int LOG_WIDTH = 450;
    private static final int LOG_HEIGHT = 250;

    private static final String FRAME_TITLE = "Monitor Dashboard";
    private static final String UPDATE_TITLE = "Update";
    private static final String LOG_TITLE = "Anomaly Log";
    private static final String ERRORS_TITLE = "Active Errors";
    private static final String CSV_TITLE = "CSV";
    private static final String FILTER_ID_TITLE = "Filter by ID";
    private static final String SEARCH_TYPE_TITLE = "Search By Type";
    private static final String CSV_EXTENSION = "csv";
    private static final String COLUMN_GAP = "  ";

    private final JFrame myFrame = new JFrame(FRAME_TITLE);
    private final JPanel myPanel = new JPanel();
    private final JLabel myStatusLabel = new JLabel("Open a CSV file from the Menu to begin.");
    private MonitorController myController;

    MonitorDashboard() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final ClassNotFoundException | InstantiationException
                       | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // fall back to the default Swing look and feel
            System.err.println("Could not load system look and feel: " + e.getMessage());
        }
        //GUI
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        myFrame.setLocationRelativeTo(null);
        myFrame.setResizable(false);

        myPanel.setLayout(new BorderLayout());
        myPanel.setBackground(BACKGROUND);
        myPanel.setBorder(BorderFactory.createEmptyBorder(PADDING_TOP, PADDING_SIDE,
                PADDING_BOTTOM, PADDING_SIDE));

        final JLabel title = new JLabel("Drone Monitor");
        title.setFont(new Font(FONT_NAME, Font.BOLD, TITLE_FONT_SIZE));
        title.setForeground(TEXT);
        final JLabel subtitle = new JLabel("Telemetry and anomaly alerts");
        subtitle.setFont(new Font(FONT_NAME, Font.PLAIN, SUBTITLE_FONT_SIZE));
        subtitle.setForeground(MUTED);

        final JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        header.add(title);
        header.add(subtitle);
        myPanel.add(header, BorderLayout.NORTH);

        myStatusLabel.setFont(new Font(FONT_NAME, Font.PLAIN, STATUS_FONT_SIZE));
        myStatusLabel.setForeground(MUTED);
        myPanel.add(myStatusLabel, BorderLayout.SOUTH);

        myFrame.add(myPanel);
        buildMenu();
        myFrame.setVisible(true);
    }

    /**
     * Sets the controller the menus talk to.
     *
     * @param theController the controller, not null
     * @throws IllegalArgumentException if theController is null
     */
    public void setController(final MonitorController theController) {
        if (theController == null) {
            throw new IllegalArgumentException("Controller cannot be null.");
        }
        myController = theController;
    }

    public void buildMenu() {
        final JMenuBar menuBar = new JMenuBar();
        final JMenu menu = new JMenu("Menu");
        //update logic
        final JMenuItem update = new JMenuItem(UPDATE_TITLE);
        update.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
        menu.add(update);
        update.addActionListener(e -> {
            final List<AnomalyRecord> found = myController.update();
            JOptionPane.showMessageDialog(myFrame,
                    "Checked " + myController.getDroneCount() + " drones.\n"
                            + found.size() + " new anomalies found.",
                    UPDATE_TITLE, JOptionPane.INFORMATION_MESSAGE);
            myStatusLabel.setText("Checked " + myController.getDroneCount() + " drones, "
                    + myController.getActiveErrors().size() + " active errors.");
        });

        //log logic
        final JMenuItem logOut = new JMenuItem(LOG_TITLE);
        logOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
        menu.add(logOut);
        logOut.addActionListener(e -> {
            final List<AnomalyRecord> anomalies = myController.getAnomalies();
            if (anomalies.isEmpty()) {
                JOptionPane.showMessageDialog(myFrame, "No anomalies recorded.",
                        LOG_TITLE, JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            showScrollingMessage(LOG_TITLE, formatRecords(anomalies),
                    JOptionPane.INFORMATION_MESSAGE);
        });

        //error logic
        final JMenuItem error = new JMenuItem(ERRORS_TITLE);
        error.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
        menu.add(error);
        error.addActionListener(e -> {
            final Set<String> errors = myController.getActiveErrors();
            if (errors.isEmpty()) {
                JOptionPane.showMessageDialog(myFrame, "No active errors.",
                        ERRORS_TITLE, JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            showScrollingMessage(ERRORS_TITLE, String.join("\n", errors),
                    JOptionPane.WARNING_MESSAGE);
        });

        //search by type logic (TCS-18)
        final JMenuItem byType = new JMenuItem("Search By Type...");
        byType.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
        menu.add(byType);
        byType.addActionListener(e -> {
            final AnomalyType choice = (AnomalyType) JOptionPane.showInputDialog(myFrame,
                    "Choose an anomaly type:", SEARCH_TYPE_TITLE,
                    JOptionPane.QUESTION_MESSAGE, null,
                    AnomalyType.values(), AnomalyType.values()[0]);
            if (choice == null) {
                return; // user cancelled
            }
            final List<AnomalyRecord> results = myController.searchByType(choice);
            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(myFrame, "No " + choice + " anomalies recorded.",
                        SEARCH_TYPE_TITLE, JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            showScrollingMessage(SEARCH_TYPE_TITLE + ": " + choice + " (" + results.size() + ")",
                    formatRecords(results), JOptionPane.INFORMATION_MESSAGE);
        });

        //chooseCSVfile logic
        final JMenuItem csv = new JMenuItem("Open CSV...");
        csv.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        menu.addSeparator();
        menu.add(csv);
        csv.addActionListener(e -> {
            final JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("CSV files", CSV_EXTENSION));
            if (chooser.showOpenDialog(myFrame) == JFileChooser.APPROVE_OPTION) {
                final File file = chooser.getSelectedFile();
                try {
                    final int count = myController.loadCSV(file);
                    myStatusLabel.setText("File: " + file.getName() + " (" + count
                            + " drones). Press Update to check them.");
                    JOptionPane.showMessageDialog(myFrame, "Loaded " + count + " drones from "
                            + file.getName(), CSV_TITLE, JOptionPane.INFORMATION_MESSAGE);
                } catch (final IOException | IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(myFrame, "Could not load " + file.getName()
                            + ":\n" + ex.getMessage(), CSV_TITLE, JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        menuBar.add(menu);

        //filter logic
        final JMenu filterMenu = new JMenu("Filter");
        filterMenu.add(buildFilterByIDItem());
        filterMenu.addSeparator();
        for (final AnomalyType type : AnomalyType.values()) {
            final JMenuItem item = new JMenuItem(type.name());
            item.addActionListener(e -> showAnomaliesOfType(type));
            filterMenu.add(item);
        }
        menuBar.add(filterMenu);

        //help logic
        final HelpView help = new HelpView(myFrame);
        final JMenu helpMenu = new JMenu("Help");

        final JMenuItem instructions = new JMenuItem("How to Use");
        instructions.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        instructions.addActionListener(e -> help.showInstructions());
        helpMenu.add(instructions);

        final JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> help.showAbout());
        helpMenu.add(about);

        menuBar.add(helpMenu);

        myFrame.setJMenuBar(menuBar);
        myFrame.revalidate();
        myFrame.repaint();
    }

    // Builds the menu item that asks for a drone ID and shows its anomalies
    private JMenuItem buildFilterByIDItem() {
        final JMenuItem byID = new JMenuItem("By Drone ID...");
        byID.addActionListener(e -> {
            final String input = JOptionPane.showInputDialog(myFrame, "Enter a drone ID:",
                    FILTER_ID_TITLE, JOptionPane.QUESTION_MESSAGE);
            if (input == null || input.isBlank()) {
                return;
            }
            try {
                showAnomaliesOfID(Integer.parseInt(input.trim()));
            } catch (final NumberFormatException ex) {
                JOptionPane.showMessageDialog(myFrame, "\"" + input + "\" is not a valid ID.",
                        FILTER_ID_TITLE, JOptionPane.ERROR_MESSAGE);
            }
        });
        return byID;
    }

    // Organizes the Anomalies based off type
    private void showAnomaliesOfType(final AnomalyType theType) {
        if (theType == null) {
            throw new IllegalArgumentException("Type cannot be null.");
        }
        final StringBuilder text = new StringBuilder();
        int count = 0;
        for (final AnomalyRecord r : myController.getAnomalies()) {
            if (r.getType().equals(theType.name())) {
                text.append(TIME_FORMAT.format(r.getTimestamp()))
                        .append(COLUMN_GAP).append("Drone #").append(r.getDroneID())
                        .append(COLUMN_GAP).append(r.getDetails())
                        .append("\n");
                count++;
            }
        }
        showScrollingMessage(theType.name() + " (" + count + ")", text.toString(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Organizes the Anomalies based off ID
    private void showAnomaliesOfID(final int theID) {
        final StringBuilder text = new StringBuilder();
        int count = 0;
        for (final AnomalyRecord r : myController.getAnomalies()) {
            if (r.getDroneID() == theID) {
                text.append(TIME_FORMAT.format(r.getTimestamp()))
                        .append(COLUMN_GAP).append(r.getType())
                        .append(COLUMN_GAP).append(r.getDetails())
                        .append("\n");
                count++;
            }
        }
        showScrollingMessage("Drone ID: " + theID + "  Anomaly Count: " + count, text.toString(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    // formats anomaly records one per line for the log and search dialogs
    private String formatRecords(final List<AnomalyRecord> theRecords) {
        if (theRecords == null) {
            throw new IllegalArgumentException("Records cannot be null.");
        }
        final StringBuilder text = new StringBuilder();
        for (final AnomalyRecord r : theRecords) {
            text.append(TIME_FORMAT.format(r.getTimestamp()))
                    .append(COLUMN_GAP).append("Drone #").append(r.getDroneID())
                    .append(COLUMN_GAP).append(r.getType())
                    .append(COLUMN_GAP).append(r.getDetails())
                    .append("\n");
        }
        return text.toString();
    }

    // shows long text in a dialog that scrolls instead of growing off the screen
    private void showScrollingMessage(final String theTitle, final String theText,
                                      final int theType) {
        if (theTitle == null || theText == null) {
            throw new IllegalArgumentException("Title and text cannot be null.");
        }
        final JTextArea area = new JTextArea(theText);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, LOG_FONT_SIZE));
        final JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(LOG_WIDTH, LOG_HEIGHT));
        JOptionPane.showMessageDialog(myFrame, scroll, theTitle, theType);
    }
}
