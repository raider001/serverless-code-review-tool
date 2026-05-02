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
}
