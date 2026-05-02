package com.kalynx.serverlessreviewtool.managers;

import com.kalynx.serverlessreviewtool.configuration.AppSettings;
import com.kalynx.serverlessreviewtool.git.RepositoryLoader;
import com.kalynx.serverlessreviewtool.models.Repository;
import com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog.ReviewFormModels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * RepositoryManager - Manages repository data
 * Manages the list of repositories and notifies listeners of changes
 */
public class RepositoryManager {

    private List<Repository> repositories = new ArrayList<>();
    private final Set<Consumer<List<Repository>>> listeners = new HashSet<>();
    private final ReviewFormModels reviewFormModels;
    private final RepositoryLoader repositoryLoader;

    public RepositoryManager(ReviewFormModels reviewFormModels, RepositoryLoader repositoryLoader) {
        this.reviewFormModels = reviewFormModels;
        this.repositoryLoader = repositoryLoader;
    }

    /**
     * Update repositories from configuration.
     * Asynchronously loads repository data via RepositoryLoader.
     *
     * @param configs repository configurations from settings
     */
    public void updateRepositories(List<AppSettings.RepositoryConfig> configs) {
        repositoryLoader.loadRepositories(configs)
            .thenAccept(loadedRepos -> {
                this.repositories = loadedRepos;
                updateReviewFormModels();
                notifyListeners();
            })
            .exceptionally(ex -> {
                System.err.println("Failed to load repositories: " + ex.getMessage());
                return null;
            });
    }

    public List<Repository> getRepositories() {
        return List.copyOf(repositories);
    }

    public void addListener(Consumer<List<Repository>> listener) {
        listeners.add(listener);
        listener.accept(List.copyOf(repositories));
    }

    public void removeListener(Consumer<List<Repository>> listener) {
        listeners.remove(listener);
    }

    private void updateReviewFormModels() {
        List<String> repoNames = repositories.stream()
            .map(Repository::getName)
            .collect(Collectors.toList());
        reviewFormModels.availableRepositories.setValue(repoNames);
    }

    public void notifyListeners() {
        listeners.forEach(listener -> listener.accept(List.copyOf(repositories)));
    }
}

