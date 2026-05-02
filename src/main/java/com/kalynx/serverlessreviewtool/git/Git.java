package com.kalynx.serverlessreviewtool.git;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Git provides asynchronous operations for managing review notes stored in git repositories.
 *
 * <p>All review data is persisted as NDJSON streams under {@code refs/notes/reviews/}
 * following the structure defined in the data model.
 *
 * <p>All operations are non-blocking and return {@link CompletableFuture} for composability.
 */
public interface Git {

    /**
     * Initializes a local repository for notes-only operations.
     *
     * <p>Creates the directory if needed, runs {@code git init}, configures the remote,
     * and fetches all review notes refs using union merge strategy.
     *
     * @param remoteUrl git remote URL
     * @return future that completes when initialization finishes
     */
    CompletableFuture<Void> cloneRepository(String remoteUrl);

    /**
     * Removes a local repository directory.
     *
     * @param repository local repository path to delete
     * @return future that completes when deletion finishes
     */
    CompletableFuture<Void> removeRepository(String repository);

    CompletableFuture<Void> fetch(String repository);

    CompletableFuture<Void> pull(String repository);

    CompletableFuture<Void> appendToNotes(String repository, String note, String data);

    /**
     *
     * @param repository The repository the notes are in.
     * @param notes The notes to push.
     * @return future that completes when push finishes
     */
    CompletableFuture<Void> pushNotes(String repository, List<String> notes);

    /**
     * List all branches in the repository.
     *
     * @param repository local repository name
     * @return future containing list of branch names
     */
    CompletableFuture<List<String>> listBranches(String repository);

    /**
     * Get the default branch name for the repository.
     * Typically "main" or "master".
     *
     * @param repository local repository name
     * @return future containing the default branch name
     */
    CompletableFuture<String> getDefaultBranch(String repository);

    /**
     * List commits for a specific branch or ref.
     * Returns commits in format: "hash|author|date|message"
     *
     * @param repository local repository name
     * @param ref branch name or commit reference (e.g., "main", "HEAD~10")
     * @param maxCount maximum number of commits to return
     * @return future containing list of commit info strings
     */
    CompletableFuture<List<String>> listCommits(String repository, String ref, int maxCount);

    /**
     * List files changed between two commits.
     * Returns files in format: "status path" (e.g., "M src/Main.java", "A newfile.txt")
     *
     * @param repository local repository name
     * @param fromCommit starting commit hash or ref
     * @param toCommit ending commit hash or ref
     * @return future containing list of changed files with status
     */
    CompletableFuture<List<String>> listChangedFiles(String repository, String fromCommit, String toCommit);
}


