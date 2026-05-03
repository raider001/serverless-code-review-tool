package com.kalynx.serverlessreviewtool.theme;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages application-wide loading state for long-running operations.
 * Tracks active operations and notifies listeners when loading state changes.
 */
public class LoadingStateManager {

    private static LoadingStateManager instance;
    private final Set<String> activeOperations = new HashSet<>();
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
        activeOperations.add(operationId);
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
        activeOperations.remove(operationId);
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

