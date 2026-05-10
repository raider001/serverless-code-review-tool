package com.kalynx.serverlessreviewtool.theme;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages application-wide loading state for long-running operations.
 * Tracks active operations and notifies listeners when loading state changes.
 */
public class LoadingStateManager {

    private static final Logger logger = LoggerFactory.getLogger(LoadingStateManager.class);
    private static LoadingStateManager instance;
    private final Set<String> activeOperations = new HashSet<>();
    private final Map<String, Long> operationStartTimes = new HashMap<>();
    private final List<Runnable> listeners = new ArrayList<>();

    private LoadingStateManager() {
    }

    /**
     * Gets the singleton instance of LoadingStateManager.
     *
     * @return the singleton instance
     */
    public static synchronized LoadingStateManager getInstance() {
        if (instance == null) {
            instance = new LoadingStateManager();
        }
        return instance;
    }

    /**
     * Starts a loading operation with the specified ID.
     * If this is the first active operation, notifies listeners that loading has started.
     *
     * @param operationId unique identifier for the operation
     */
    public synchronized void startLoading(String operationId) {
        boolean wasLoading = isLoading();
        boolean added = activeOperations.add(operationId);
        if (added) {
            operationStartTimes.put(operationId, System.currentTimeMillis());
        }
        if (!wasLoading && isLoading()) {
            notifyListeners();
        }
    }

    /**
     * Stops a loading operation with the specified ID.
     * If this was the last active operation, notifies listeners that loading has stopped.
     *
     * @param operationId unique identifier for the operation
     */
    public synchronized void stopLoading(String operationId) {
        boolean wasLoading = isLoading();
        boolean removed = activeOperations.remove(operationId);
        Long startedAt = operationStartTimes.remove(operationId);
        if (removed) {
            long durationMs = startedAt == null ? -1L : Math.max(0L, System.currentTimeMillis() - startedAt);
            if (durationMs >= 0L) {
                logger.info("Loading operation completed: {} ({} ms)", operationId, durationMs);
            } else {
                logger.info("Loading operation completed: {}", operationId);
            }
        }
        if (wasLoading && !isLoading()) {
            notifyListeners();
        }
    }

    /**
     * Checks if any operations are currently loading.
     *
     * @return true if at least one operation is active
     */
    public synchronized boolean isLoading() {
        return !activeOperations.isEmpty();
    }

    /**
     * Adds a listener to be notified when loading state changes.
     *
     * @param listener the listener to add
     */
    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener from notifications.
     *
     * @param listener the listener to remove
     */
    public void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }
}

