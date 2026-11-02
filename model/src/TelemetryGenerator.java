
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TelemetryGenerator {

    private List<Drone> drones; // holds our drones
    private Random random;
    private int updateInterval = 1000; // milleseconds
    private ScheduledExecutorService scheduler; // scheduler to update telemetry without manual prompting

    // Initialize the fleet and the resources used for telemetry generation.
    public TelemetryGenerator() {
        drones = new ArrayList<>();
        random = new Random();

        initializeDrones();
    }

    // Create the initial fleet with unique IDs and starting telemetry values.
    private void initializeDrones() {
        drones.add(new Drone(47.60, -122.33, 100, 1, 100, 90, 10));
        drones.add(new Drone(47.61, -122.34, 120, 2, 85, 180, 12));
        drones.add(new Drone(47.62, -122.32, 90, 3, 95, 270, 8));
    }

    // Generate new telemetry for each drone using its previous values.
    private void updateDrones() {
        for (Drone drone : drones) {

            double newLatitude = Math.max(-90, Math.min(90, drone.getLatitude() + randomChange(0.0001)));
            double newLongitude = Math.max(-180, Math.min(180, drone.getLongitude() + randomChange(0.0001)));
            double newAltitude = Math.max(0, drone.getAltitude() + randomChange(5));
            double newBattery = Math.max(0, drone.getBattery() - random.nextDouble() * 0.05);
            double newOrientation = (drone.getOrientation() + randomChange(10) + 360) % 360;
            double newVelocity = Math.max(0, drone.getVelocity() + randomChange(1));

            drone.updateTelemetry(
                    newLatitude,
                    newLongitude,
                    newAltitude,
                    newBattery,
                    newOrientation,
                    newVelocity
            );
        }
    }

    // Generate a random change between the negative and positive limit to mimic possibile changes per sec
    private double randomChange(double limit) {
        return random.nextDouble(-limit, limit);
    }

    // Automatically update the fleet at the configured interval.
    public void startSimulation() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(
                this::updateDrones,
                0,
                updateInterval,
                TimeUnit.MILLISECONDS
        );
    }

    // Stop scheduled updates when the simulation is no longer needed.
    public void stopSimulation() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    // Return the fleet so other application components can access the drones.
    public List<Drone> getDrones() {
        return drones;
    }
}
