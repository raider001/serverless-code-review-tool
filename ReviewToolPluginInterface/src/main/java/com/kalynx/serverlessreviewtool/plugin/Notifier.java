package com.kalynx.serverlessreviewtool.plugin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Provides listener registration and notification dispatch for plugin events.
 *
 * @param <V> notification payload item type
 * @param <T> notification type enum
 */
public class Notifier<V, T extends Enum<?>> {

    private final Map<T, List<Consumer<V[]>>> listeners = new ConcurrentHashMap<>();

    /**
     * Notifies all listeners registered for the supplied notification type.
     *
     * @param type notification type
     * @param values notification payload values
     */
    @SafeVarargs
    protected final void notifyListeners(T type, V... values) {
        assert type != null : "Notification type cannot be null";
        assert values != null : "Notification values cannot be null";
        listeners.getOrDefault(type, List.of()).forEach(listener -> listener.accept(values));
    }

    /**
     * Registers a listener for the supplied notification type.
     *
     * @param type notification type
     * @param listener listener to register
     */
    public void addListener(T type, Consumer<V[]> listener) {
        listeners.computeIfAbsent(type, ignored -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * Removes a listener for the supplied notification type.
     *
     * @param type notification type
     * @param listener listener to remove
     */
    public void removeListener(T type, Consumer<V[]> listener) {
        List<Consumer<V[]>> registeredListeners = listeners.get(type);
        if (registeredListeners == null) {
            return;
        }
        registeredListeners.remove(listener);
        if (registeredListeners.isEmpty()) {
            listeners.remove(type, registeredListeners);
        }
    }
}
