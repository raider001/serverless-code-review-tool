package com.kalynx.serverlessreviewtool.utils;

import javax.swing.Timer;

/**
 * DebounceTimer - Utility for debouncing rapid events
 * Delays execution until a specified time has passed since the last trigger
 */
public class DebounceTimer {

    private final Timer timer;

    /**
     * Create a debounce timer with the specified delay and action
     *
     * @param delayMillis Delay in milliseconds before action fires
     * @param action The action to execute after the delay
     */
    public DebounceTimer(int delayMillis, Runnable action) {
        this.timer = new Timer(delayMillis, e -> action.run());
        this.timer.setRepeats(false);
    }

    /**
     * Trigger the debounce timer
     * If already running, resets the countdown
     * If not running, starts the countdown
     */
    public void trigger() {
        if (timer.isRunning()) {
            timer.restart();
        } else {
            timer.start();
        }
    }

    /**
     * Cancel the pending action
     */
    public void cancel() {
        if (timer.isRunning()) {
            timer.stop();
        }
    }

    /**
     * Check if the timer is currently counting down
     *
     * @return true if timer is running
     */
    public boolean isRunning() {
        return timer.isRunning();
    }

    /**
     * Change the delay time
     *
     * @param delayMillis New delay in milliseconds
     */
    public void setDelay(int delayMillis) {
        timer.setDelay(delayMillis);
    }

    /**
     * Get the current delay time
     *
     * @return Delay in milliseconds
     */
    public int getDelay() {
        return timer.getDelay();
    }
}

