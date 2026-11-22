import javax.swing.*;
import java.awt.*;

/**
 * Makes windows that explain what the app does and how it works.
 */
public class HelpView {

    private static final String ABOUT_TITLE = "About";
    private static final String INSTRUCTIONS_TITLE = "How to Use";

    private static final String ABOUT_TEXT =
            "Drone Monitor\n"
            + "\n"
            + "Temp text (change later)";

    private static final String INSTRUCTIONS_TEXT =
            "How to use the Drone Monitor\n"
            + "\n"
            + "Temp Text (change later)";

    private static final String FONT_NAME = "Segoe UI";
    private static final int FONT_SIZE = 13;
    private static final int TEXT_PADDING = 8;
    private static final int DIALOG_WIDTH = 420;
    private static final int DIALOG_HEIGHT = 260;

    private final JFrame myFrame;

    /**
     * Creates a help view whose dialogs are centered on the given frame.
     *
     * @param theFrame the parent frame, not null
     * @throws IllegalArgumentException if theFrame is null
     */
    public HelpView(final JFrame theFrame) {
        if (theFrame == null) {
            throw new IllegalArgumentException("Frame cannot be null.");
        }
        myFrame = theFrame;
    }

    public void showAbout() {
        display(ABOUT_TITLE, ABOUT_TEXT);
    }

    public void showInstructions() {
        display(INSTRUCTIONS_TITLE, INSTRUCTIONS_TEXT);
    }

    private void display(final String theTitle, final String theMessage) {
        final JTextArea area = new JTextArea(theMessage);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setFont(new Font(FONT_NAME, Font.PLAIN, FONT_SIZE));
        area.setBorder(BorderFactory.createEmptyBorder(TEXT_PADDING, TEXT_PADDING,
                TEXT_PADDING, TEXT_PADDING));

        final JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));

        JOptionPane.showMessageDialog(myFrame, scroll, theTitle, JOptionPane.INFORMATION_MESSAGE);
    }
}
