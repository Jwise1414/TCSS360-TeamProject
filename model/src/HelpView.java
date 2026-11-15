import javax.swing.*;
import java.awt.*;

/**
 * makes window to help explain what the app does and how its works
 */
public class HelpView {

    private static final String ABOUT_TEXT =
            "Drone Monitor\n"
            + "\n"
            + "Temp text (change later)";

    private static final String INSTRUCTIONS_TEXT =
            "How to use the Drone Monitor\n"
            + "\n"
            + "Temp Text (change later)";

    private final JFrame myFrame;

    public HelpView(JFrame theFrame) {
        myFrame = theFrame;
    }

    public void showAbout() {
        display("About", ABOUT_TEXT);
    }

    public void showInstructions() {
        display("How to Use", INSTRUCTIONS_TEXT);
    }

    private void display(String theTitle, String theMessage) {
        JTextArea area = new JTextArea(theMessage);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(420, 260));

        JOptionPane.showMessageDialog(myFrame, scroll, theTitle, JOptionPane.INFORMATION_MESSAGE);
    }
}
