/*
 * Copyright (C) 2025 Shivaji Patil, College of the North Atlantic
 * All rights reserved.
 *
 * Aircraft Simulation Project
 */

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Direction control system for an aircraft axis. Manages a current value and a
 * target value, and adjusts the current value over time toward the target
 * using a physics-based movement (inertia, dampening, tolerance, max step).
 */
public class DirectionControl {
    private String name;
    private double currentValue;
    private double targetValue;
    private double velocity;
    private double min;
    private double max;
    private double inertia;
    private double dampening;
    private double tolerance;
    private double maxStep;

    // Statistics tracking
    private double totalDeviation = 0;
    private double maxDeviation = 0;
    private int sampleCount = 0;
    private boolean trackStatistics = true;

    // Listener pattern: thread-safe collection for observers
    // CopyOnWriteArrayList is the simplest correct choice for scenarios with:
    // - Many reads (every update())
    // - Few writes (add/remove listeners)
    // No synchronization overhead on iteration, which is critical on the simulation thread
    private final CopyOnWriteArrayList<DirectionControlListener> listeners = new CopyOnWriteArrayList<>();

    // Volatile field to store and expose current value with visibility guarantees
    // Notifications happen on simulation thread; EDT reads this volatile field
    // Ensures changes are safely visible across threads without explicit synchronization
    //
    // SAFE PUBLICATION GUARANTEE:
    // ===========================
    // 1. Simulation thread writes: volatileCurrentValue = currentValue (volatile write)
    // 2. Listener callback stores volatile read into roll/pitch/yaw fields
    // 3. EDT reads roll/pitch/yaw in Swing Timer (no synchronization needed)
    //
    // The volatile write-in step 1 happens-before any volatile read in step 2.
    // This Java Memory Model guarantee ensures all changes visible to the simulation
    // thread are safely visible to EDT without locks. EDT always sees the most recent
    // value because volatile reads/writes bypass CPU caches.
    private volatile double volatileCurrentValue;

    // Getters for correction mechanism display
    public String getName() { return name; }
    public double getInertia() { return inertia; }
    public double getDampening() { return dampening; }
    public double getTolerance() { return tolerance; }
    public double getVelocity() { return velocity; }

    // Protected setters so subclasses can override physics parameters
    protected void setInertia(double inertia) { this.inertia = inertia; }
    protected void setDampening(double dampening) { this.dampening = dampening; }
    protected void setTolerance(double tolerance) { this.tolerance = tolerance; }

    /**
     * Registers a listener to be notified when this control's value changes.
     * Uses CopyOnWriteArrayList for thread-safe notifications without blocking.
     *
     * @param listener The listener to register
     */
    public void addListener(DirectionControlListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Unregisters a listener from value change notifications.
     *
     * @param listener The listener to unregister
     */
    public void removeListener(DirectionControlListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners that this control's value has changed.
     * This method is called from the simulation thread during update().
     * Listeners should not call any Swing methods; they should only store values.
     */
    private void notifyListeners() {
        for (DirectionControlListener listener : listeners) {
            listener.onDirectionChanged(this);
        }
    }

    public DirectionControl(String name, double min, double max, ConfigLoader config) {
        this.name = name;
        this.min = min;
        this.max = max;

        // Read configuration values with defaults if not specified
        this.inertia = config.getDouble(name.toLowerCase() + ".inertia", 1.0);
        this.dampening = config.getDouble(name.toLowerCase() + ".dampening", 0.95);
        this.tolerance = config.getDouble(name.toLowerCase() + ".tolerance", 2.0);
        this.maxStep = config.getDouble(name.toLowerCase() + ".maxStep", 3.0);

        this.currentValue = 0;
        this.targetValue = 0;
        this.velocity = 0;
        this.volatileCurrentValue = 0;
    }

    /**
     * Update the current value based on the physics model and target.
     * Notifies all registered listeners when the value changes.
     */
    public synchronized void update() {
        double deviation = targetValue - currentValue;

        if (trackStatistics) {
            totalDeviation += Math.abs(deviation);
            maxDeviation = Math.max(maxDeviation, Math.abs(deviation));
            sampleCount++;
        }

        Main.logToCSV(name, targetValue, currentValue, velocity);

        // Skip adjustment if we're already close enough.
        if (Math.abs(deviation) < tolerance && Math.abs(velocity) < 0.1) {
            velocity = 0;
            return;
        }

        velocity += deviation / inertia;
        velocity *= dampening;

        if (velocity > maxStep) velocity = maxStep;
        if (velocity < -maxStep) velocity = -maxStep;

        currentValue += velocity;

        if (currentValue < min) {
            currentValue = min;
            velocity = 0;
        } else if (currentValue > max) {
            currentValue = max;
            velocity = 0;
        }

        // Update volatile field for EDT visibility and notify listeners
        this.volatileCurrentValue = currentValue;
        notifyListeners();
    }

    public Map<String, Double> getStatistics() {
        Map<String, Double> stats = new HashMap<>();
        stats.put("sampleCount", (double) sampleCount);
        stats.put("averageDeviation", sampleCount > 0 ? totalDeviation / sampleCount : 0);
        stats.put("maxDeviation", maxDeviation);
        return stats;
    }

    public synchronized double getCurrentValue() { return currentValue; }

    /**
     * Returns the current value with volatile visibility guarantees.
     * This is safe for EDT to read without synchronization.
     *
     * @return The most recent current value
     */
    public double getVolatileCurrentValue() {
        return volatileCurrentValue;
    }

    public synchronized void setCurrentValue(double value) {
        this.currentValue = value;
        this.volatileCurrentValue = value;
        notifyListeners();
    }

    public synchronized double getTargetValue() { return targetValue; }
    public synchronized void setTargetValue(double value) {
        if (value < min) value = min;
        if (value > max) value = max;
        this.targetValue = value;
    }
}
