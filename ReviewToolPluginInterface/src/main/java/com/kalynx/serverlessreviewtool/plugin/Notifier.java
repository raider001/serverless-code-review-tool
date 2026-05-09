package com.kalynx.serverlessreviewtool.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Notifier<V, T extends Enum<?>> {

    private final Map<T, List<Consumer<V[]>>> listeners = new HashMap<>();

    @SafeVarargs
    protected final void notifyListeners(T type, V... values) {
        assert type != null : "Notification type cannot be null";
        assert values != null : "Notification values cannot be null";
        listeners.getOrDefault(type, List.of()).forEach(listener -> listener.accept(values));
    }

    public void addListener(T type, Consumer<V[]> listener) {
        listeners.computeIfAbsent(type, _ -> List.of()).add(listener);
    }

    public void removeListener(T type, Consumer<V[]> listener) {
        listeners.computeIfAbsent(type, _ -> List.of()).remove(listener);
    }
}
