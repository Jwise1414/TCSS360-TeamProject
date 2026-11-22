
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Creates our drone fleet and keeps updating their telemetry
 * while the simulation is running.
 */
public class TelemetryGenerator {

    // Stores all the drones being used in the simulation.
    private final List<Drone> drones;

    // Used to generate random changes in the drone telemetry.
    private final Random random;

    // How often the drones get new telemetry (in milliseconds).
    private final int updateInterval = 1000;

    // Runs the telemetry updates automatically in the background.
    private ScheduledExecutorService scheduler;

    public TelemetryGenerator() {
        drones = new ArrayList<>();
        random = new Random();

        // Start with three drones so we have a fleet to simulate.
        initializeDrones();
    }

    /**
     * Creates the starting drones with different IDs and telemetry values.
     */
    private void initializeDrones() {
        drones.add(new Drone(47.60, -122.33, 100, 1, 100, 90, 10));
        drones.add(new Drone(47.61, -122.34, 120, 2, 85, 180, 12));
        drones.add(new Drone(47.62, -122.32, 90, 3, 95, 270, 8));
    }

    /**
     * Updates the telemetry for every drone using its previous values.
     */
    private void updateDrones() {
        for (Drone drone : drones) {

            // Make small random changes to location and altitude.
            // The limits help keep the coordinates and altitude realistic.
            double newLatitude = Math.max(-90, Math.min(90,
                    drone.getLatitude() + randomChange(0.0001)));

            double newLongitude = Math.max(-180, Math.min(180,
                    drone.getLongitude() + randomChange(0.0001)));

            double newAltitude = Math.max(0,
                    drone.getAltitude() + randomChange(5));

            // Battery slowly decreases instead of randomly going up and down.
            double newBattery = Math.max(0,
                    drone.getBattery() - random.nextDouble() * 0.05);

            // Keep the orientation between 0 and 359 degrees.
            double newOrientation = (drone.getOrientation()
                    + randomChange(10) + 360) % 360;

            // Velocity can change in either direction, but cannot go below zero.
            double newVelocity = Math.max(0,
                    drone.getVelocity() + randomChange(1));

            // Send all the new values to the Drone object.
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

    /**
     * Returns a random change between negative and positive limit.
     * For example, a limit of 5 gives a value between -5 and 5.
     */
    private double randomChange(double limit) {
        return random.nextDouble(-limit, limit);
    }

    /**
     * Starts automatically updating the drones once every second.
     */
    public synchronized void startSimulation() {

        // Don't create another scheduler if the simulation is already running.
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

    /**
     * Stops the automatic updates when the simulation is finished.
     */
    public synchronized void stopSimulation() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    /**
     * Gives other parts of the application access to the drone fleet.
     */
    public List<Drone> getDrones() {

        // Return a copy so other classes can't add or remove drones
        // directly from the generator's list.
        return Collections.unmodifiableList(new ArrayList<>(drones));
    }
}
