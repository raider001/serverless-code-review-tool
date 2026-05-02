package com.kalynx.serverlessreviewtool.git;

import com.kalynx.serverlessreviewtool.configuration.AppSettings;
import com.kalynx.serverlessreviewtool.models.Commit;
import com.kalynx.serverlessreviewtool.models.FileChangeType;
import com.kalynx.serverlessreviewtool.models.Repository;
import com.kalynx.serverlessreviewtool.models.ReviewFile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * RepositoryLoader - Utility service for loading repository information from Git
 * Orchestrates Git operations to build Repository model objects from configuration.
 */
public class RepositoryLoader {

    private final Git git;

    public RepositoryLoader(Git git) {
        this.git = git;
    }

    /**
     * Load repositories from configuration.
     * For each configured repository, attempts to clone if not present and extract basic information.
     *
     * @param configs list of repository configurations
     * @return future containing list of loaded Repository models
     */
    public CompletableFuture<List<Repository>> loadRepositories(List<AppSettings.RepositoryConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        List<CompletableFuture<Repository>> futures = configs.stream()
            .map(this::loadRepository)
            .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(_ -> futures.stream()
                .map(CompletableFuture::join)
                .filter(java.util.Objects::nonNull)
                .toList());
    }

    /**
     * Load a single repository from configuration.
     * Clones if necessary, then extracts commits and changed files.
     *
     * @param config repository configuration
     * @return future containing loaded Repository model
     */
    public CompletableFuture<Repository> loadRepository(AppSettings.RepositoryConfig config) {
        String repoName = config.getName();
        String repoUrl = config.getUrl();

        return ensureRepositoryCloned(repoName, repoUrl)
            .thenCompose(_ -> git.fetch(repoName))
            .thenCompose(_ -> loadRepositoryData(config))
            .exceptionally(ex -> {
                System.err.println("Failed to load repository " + repoName + ": " + ex.getMessage());
                return createEmptyRepository(config);
            });
    }

    private CompletableFuture<Void> ensureRepositoryCloned(String name, String url) {
        return git.cloneRepository(url)
            .exceptionally(ex -> {
                System.err.println("Failed to clone or initialize repository " + name + ": " + ex.getMessage());
                return null;
            });
    }

    private CompletableFuture<Repository> loadRepositoryData(AppSettings.RepositoryConfig config) {
        String repoName = config.getName();
        Repository repository = new Repository(repoName, "", config.getUrl());

        return loadCommits(repository, repoName)
            .thenCompose(_ -> loadChangedFiles(repository, repoName))
            .thenApply(_ -> repository);
    }

    private CompletableFuture<Void> loadCommits(Repository repository, String repoName) {
        return git.getDefaultBranch(repoName)
            .thenCompose(defaultBranch ->
                git.listCommits(repoName, "origin/" + defaultBranch, 50))
            .thenAccept(commitLines -> {
                for (String line : commitLines) {
                    Commit commit = parseCommit(line);
                    if (commit != null) {
                        repository.addCommit(commit);
                    }
                }
            })
            .exceptionally(ex -> {
                System.err.println("Failed to load commits for " + repoName + ": " + ex.getMessage());
                return null;
            });
    }

    private CompletableFuture<Void> loadChangedFiles(Repository repository, String repoName) {
        List<Commit> commits = repository.getCommits();
        if (commits.size() < 2) {
            return CompletableFuture.completedFuture(null);
        }

        String latestCommit = commits.get(0).getHash();
        String previousCommit = commits.get(1).getHash();

        return git.listChangedFiles(repoName, previousCommit, latestCommit)
            .thenAccept(fileLines -> {
                for (String line : fileLines) {
                    ReviewFile file = parseChangedFile(line, repoName);
                    if (file != null) {
                        repository.addFile(file);
                    }
                }
            })
            .exceptionally(ex -> {
                System.err.println("Failed to load changed files for " + repoName + ": " + ex.getMessage());
                return null;
            });
    }

    private Commit parseCommit(String commitLine) {
        String[] parts = commitLine.split("\\|");
        if (parts.length < 4) {
            return null;
        }
        return new Commit(parts[0], parts[3], parts[1], parts[2]);
    }

    private ReviewFile parseChangedFile(String line, String repository) {
        String[] parts = line.split("\\s+", 2);
        if (parts.length < 2) {
            return null;
        }

        String status = parts[0];
        String path = parts[1];

        FileChangeType changeType = switch (status) {
            case "A" -> FileChangeType.ADDED;
            case "D" -> FileChangeType.DELETED;
            case "M" -> FileChangeType.MODIFIED;
            case "R" -> FileChangeType.RENAMED;
            default -> FileChangeType.MODIFIED;
        };

        return new ReviewFile(path, repository, changeType);
    }

    private Repository createEmptyRepository(AppSettings.RepositoryConfig config) {
        return new Repository(config.getName(), "", config.getUrl());
    }
}

