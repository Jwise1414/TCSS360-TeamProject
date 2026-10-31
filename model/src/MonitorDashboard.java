import javax.swing.*;
import java.awt.*;

public class MonitorDashboard {
    JFrame frame = new JFrame("Monitor Dashboard");
    JPanel panel = new JPanel();
    MonitorDashboard() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.add(panel);
        panel.setSize(500, 500);
        panel.setLayout(null);
        panel.setBackground(Color.black);
        buildMenu();



    }
    //public void setController(controller: MonitorController){}

    public void buildMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        //update logic
        JMenuItem update = new JMenuItem("update");
        menu.add(update);
        update.addActionListener(e -> {});

        //log logic
        JMenuItem logOut = new JMenuItem("Log");
        menu.add(logOut);
        logOut.addActionListener(e -> {});

        //error logic
        JMenuItem error = new JMenuItem("Error");
        menu.add(error);
        error.addActionListener(e -> {});

        //chooseCSVfile logic
        JMenuItem CSV = new JMenuItem("CSV");
        menu.add(CSV);
        CSV.addActionListener(e -> {});


        menuBar.add(menu);

        frame.setJMenuBar(menuBar);
        frame.revalidate();
        frame.repaint();

    }

}
