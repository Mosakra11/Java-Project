/*
 * Copyright (C) 2025 Shivaji Patil, College of the North Atlantic
 * All rights reserved.
 *
 * Aircraft Simulation Project
 */

/**
 * Listener interface for receiving DirectionControl change notifications.
 *
 * Implements the Observer pattern to enable event-driven updates of flight
 * control data (roll, pitch, yaw) from the simulation thread to UI components.
 *
 * Important threading notes:
 * - Notifications are fired on the simulation thread (Main.java)
 * - Listeners must NOT call any Swing methods directly
 * - Listeners should store values safely (volatile or atomic) for EDT visibility
 * - GUI will read stored values from the Event Dispatch Thread during rendering
 */
public interface DirectionControlListener {
    /**
     * Called when a DirectionControl's value changes.
     * Notifications happen on the simulation thread, not the EDT.
     * Store the value in a thread-safe field; do not update UI directly.
     *
     * @param control The DirectionControl that changed
     */
    void onDirectionChanged(DirectionControl control);
}

