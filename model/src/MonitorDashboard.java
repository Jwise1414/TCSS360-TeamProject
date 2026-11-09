import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

public class MonitorDashboard {
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private static final Color BACKGROUND = new Color(0x1E2228);
    private static final Color TEXT = new Color(0xE6E8EB);
    private static final Color MUTED = new Color(0x9AA3AD);

    JFrame frame = new JFrame("Monitor Dashboard");
    JPanel panel = new JPanel();
    private final JLabel myStatusLabel = new JLabel("Open a CSV file from the Menu to begin.");
    private MonitorController myController;

    MonitorDashboard() {
        // use the normal Windows/Mac look for menus, dialogs and the file chooser
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {

        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        panel.setLayout(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));

        JLabel title = new JLabel("Drone Monitor");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Telemetry and anomaly alerts");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);

        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        header.add(title);
        header.add(subtitle);
        panel.add(header, BorderLayout.NORTH);

        myStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        myStatusLabel.setForeground(MUTED);
        panel.add(myStatusLabel, BorderLayout.SOUTH);

        frame.add(panel);
        buildMenu();
        frame.setVisible(true);
    }

    public void setController(MonitorController theController) {
        myController = theController;
    }

    public void buildMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        //update logic
        JMenuItem update = new JMenuItem("Update");
        update.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
        menu.add(update);
        update.addActionListener(e -> {
            List<AnomalyRecord> found = myController.update();
            JOptionPane.showMessageDialog(frame,
                    "Checked " + myController.getDroneCount() + " drones.\n"
                            + found.size() + " new anomalies found.",
                    "Update", JOptionPane.INFORMATION_MESSAGE);
            myStatusLabel.setText("Checked " + myController.getDroneCount() + " drones, "
                    + myController.getActiveErrors().size() + " active errors.");
        });

        //log logic
        JMenuItem logOut = new JMenuItem("Anomaly Log");
        logOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
        menu.add(logOut);
        logOut.addActionListener(e -> {
            List<AnomalyRecord> anomalies = myController.getAnomalies();
            if (anomalies.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No anomalies recorded.",
                        "Anomaly Log", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            StringBuilder text = new StringBuilder();
            for (AnomalyRecord r : anomalies) {
                text.append(TIME_FORMAT.format(r.getTimestamp()))
                        .append("  Drone #").append(r.getDroneID())
                        .append("  ").append(r.getType())
                        .append("  ").append(r.getDetails())
                        .append("\n");
            }
            showScrollingMessage("Anomaly Log", text.toString(), JOptionPane.INFORMATION_MESSAGE);
        });

        //error logic
        JMenuItem error = new JMenuItem("Active Errors");
        error.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
        menu.add(error);
        error.addActionListener(e -> {
            Set<String> errors = myController.getActiveErrors();
            if (errors.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No active errors.",
                        "Errors", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            showScrollingMessage("Active Errors", String.join("\n", errors), JOptionPane.WARNING_MESSAGE);
        });

        //chooseCSVfile logic
        JMenuItem CSV = new JMenuItem("Open CSV...");
        CSV.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        menu.addSeparator();
        menu.add(CSV);
        CSV.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                myController.setCSVFile(file);
                myStatusLabel.setText("File: " + file.getName());
                JOptionPane.showMessageDialog(frame, "Selected " + file.getName(),
                        "CSV", JOptionPane.INFORMATION_MESSAGE);
            }
        });


        menuBar.add(menu);

        frame.setJMenuBar(menuBar);
        frame.revalidate();
        frame.repaint();

    }

    // shows long text in a dialog that scrolls instead of growing off the screen
    private void showScrollingMessage(String theTitle, String theText, int theType) {
        JTextArea area = new JTextArea(theText);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(450, 250));
        JOptionPane.showMessageDialog(frame, scroll, theTitle, theType);
    }

}
