package com.kalynx.serverlessreviewtool.managers;

import com.kalynx.serverlessreviewtool.models.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * RepositoryManager - Singleton manager for repository data
 * Manages the list of repositories and notifies listeners of changes
 */
public class RepositoryManager {

    private static RepositoryManager INSTANCE;

    private List<Repository> repositories = new ArrayList<>();

    private final Set<Consumer<List<Repository>>> listeners = new HashSet<>();

    public static synchronized RepositoryManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RepositoryManager();
        }
        return INSTANCE;
    }

    public void updateRepositories(List<Repository> repositories) {
        this.repositories = repositories;
        notifyListeners();
    }

    public List<Repository> getRepositories() {
        return List.copyOf(repositories);
    }

    public void addListener(Consumer<List<Repository>> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<List<Repository>> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        listeners.forEach(listener -> listener.accept(List.copyOf(repositories)));
    }
}

