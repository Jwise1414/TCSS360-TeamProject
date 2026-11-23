import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Creates and manages the drone fleet used in the simulation.
 * This class generates initial drone data and continuously updates
 * each drone's telemetry while the simulation is running.
 */
public class TelemetryGenerator {

    // Stores all drones currently participating in the simulation.
    // Keeping this list private prevents other classes from directly
    // modifying the fleet managed by this generator.
    private final List<Drone> myDrones;

    // Generates random values used to simulate realistic changes
    // in drone location, altitude, battery, orientation, and velocity.
    private final Random myRandom;

    // Defines how frequently telemetry updates occur.
    // The interval is measured in milliseconds, with 1000 milliseconds
    // representing one second.
    private static final int UPDATE_INTERVAL = 1000;

    // Manages the background task responsible for automatically updating
    // drone telemetry at the configured interval.
    private ScheduledExecutorService myScheduler;

    public TelemetryGenerator() {

        myDrones = new ArrayList<>();
        myRandom = new Random();

        // Populate the fleet with the initial drones required by the simulation.
        // This ensures that drone data is available before updates begin.
        initializeDrones();
    }

    /**
     * Creates the starting drones with different IDs and telemetry values.
     */
    private void initializeDrones() {

        // Initialize three drones with different starting coordinates,
        // altitudes, IDs, battery levels, orientations, and velocities.
        // These initial values provide a starting point for the simulation.
        myDrones.add(new Drone(47.60, -122.33, 100, 1, 100, 90, 10));
        myDrones.add(new Drone(47.61, -122.34, 120, 2, 85, 180, 12));
        myDrones.add(new Drone(47.62, -122.32, 90, 3, 95, 270, 8));
    }

    /**
     * Updates the telemetry for every drone using its previous values.
     */
    private void updateDrones() {

        // Process every drone currently stored in the fleet.
        // Each drone receives new telemetry values based on its previous
        // state, creating continuous movement throughout the simulation.
        for (Drone drone : myDrones) {

            // Generate small random changes to the drone's latitude.
            // Math.min and Math.max prevent the resulting coordinate
            // from exceeding the valid geographic latitude range.
            double newLatitude = Math.max(-90, Math.min(90,
                    drone.getLatitude() + randomChange(0.0001)));

            // Update longitude using a small random adjustment.
            // The result is restricted to the valid longitude range
            // to prevent the simulation from producing invalid coordinates.
            double newLongitude = Math.max(-180, Math.min(180,
                    drone.getLongitude() + randomChange(0.0001)));

            // Simulate changes in altitude by adding a random adjustment.
            // Altitude cannot fall below zero because a negative altitude
            // would not be valid for this simulation.
            double newAltitude = Math.max(0,
                    drone.getAltitude() + randomChange(5));

            // Simulate gradual battery consumption.
            // Unlike other telemetry values, battery decreases over time
            // rather than randomly increasing or decreasing.
            // Math.max ensures the battery level never drops below zero.
            double newBattery = Math.max(0,
                    drone.getBattery() - myRandom.nextDouble() * 0.05);

            // Simulate changes in the drone's orientation.
            // Adding 360 before applying modulo keeps the result within
            // the 0-359 degree range, even when the random adjustment
            // produces a negative orientation change.
            double newOrientation = (drone.getOrientation()
                    + randomChange(10) + 360) % 360;

            // Simulate changes in drone velocity.
            // Velocity can increase or decrease, but it cannot become negative.
            double newVelocity = Math.max(0,
                    drone.getVelocity() + randomChange(1));

            // Pass all newly calculated telemetry values to the Drone object.
            // The Drone class is responsible for storing the updated values,
            // keeping the drone's current state synchronized with the simulation.
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
     * Returns a random change between a negative and positive limit.
     *
     * @param theLimit maximum magnitude of the random change
     * @return a random value between negative and positive limit
     */
    private double randomChange(final double theLimit) {

        // Generate a random value within the supplied range.
        // For example, a limit of 5 produces a value between -5 and 5.
        // This allows different telemetry properties to change by
        // different amounts depending on the value passed into this method.
        return myRandom.nextDouble(-theLimit, theLimit);
    }

    /**
     * Starts automatically updating drone telemetry at the configured interval.
     */
    public synchronized void startSimulation() {

        // Prevent multiple background schedulers from running simultaneously.
        // If a scheduler already exists and has not been shut down,
        // the simulation is already running, so no additional scheduler is needed.
        if (myScheduler != null && !myScheduler.isShutdown()) {
            return;
        }

        // Create a single-threaded scheduler to handle telemetry updates.
        // Using a dedicated background thread allows the simulation to update
        // without blocking the main application thread.
        myScheduler = Executors.newSingleThreadScheduledExecutor();

        // Schedule the telemetry update task to run repeatedly.
        // The initial delay is zero, so the first update happens immediately.
        // Subsequent updates occur at the configured interval.
        myScheduler.scheduleAtFixedRate(
                this::updateDrones,
                0,
                UPDATE_INTERVAL,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * Stops the automatic telemetry updates when the simulation is finished.
     */
    public synchronized void stopSimulation() {

        // Check whether a scheduler currently exists before attempting
        // to shut it down.
        if (myScheduler != null) {

            // Interrupt pending scheduled tasks and stop the background
            // scheduler so that no additional telemetry updates are performed.
            myScheduler.shutdownNow();

            // Reset the scheduler reference to indicate that the simulation
            // is no longer running and allow it to be started again later.
            myScheduler = null;
        }
    }

    /**
     * Provides access to the drones currently participating in the simulation.
     *
     * @return an unmodifiable copy of the current drone list
     */
    public List<Drone> getDrones() {

        // Return a separate copy of the fleet wrapped in an unmodifiable list.
        // This prevents calling classes from adding or removing drones directly
        // from the generator's internal collection.
        return Collections.unmodifiableList(new ArrayList<>(myDrones));
    }
}