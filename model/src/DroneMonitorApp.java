import javax.management.monitor.Monitor;

public class DroneMonitorApp {
    private MonitorController myController;
    private MonitorDashboard myDashboard;

    DroneMonitorApp(MonitorDashboard theDashboard,  MonitorController theController) {
       myDashboard = theDashboard;
       myController = theController;
    }
    public static void main(String[] args) {
    DroneMonitorApp theApp = new DroneMonitorApp(new MonitorDashboard(), new MonitorController());
    }
    public void initDatabase(){

    }
    public void buildComponents(){

    }
}
