/*
 * Copyright (C) 2025 Shivaji Patil, College of the North Atlantic
 * All rights reserved.
 *
 * Aircraft Simulation Project
 */

/**
 * Observer interface for receiving altitude change notifications.
 *
 * Implements the Observer pattern to enable event-driven updates
 * of altitude data from the simulation to UI components.
 */
public interface AltitudeObserver {
    /**
     * Called when the aircraft altitude changes
     * @param altitude The new altitude in feet
     */
    void onAltitudeChanged(double altitude);
}

