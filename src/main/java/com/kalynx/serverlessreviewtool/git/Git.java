package com.kalynx.serverlessreviewtool.git;

import java.nio.file.Path;
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
     * @param repoPath  local repository path
     * @param remote    remote name (e.g. "origin")
     * @param remoteUrl git remote URL
     * @return future that completes when initialization finishes
     */
    CompletableFuture<Void> initNotesRepository(Path repoPath, String remote, String remoteUrl);

    /**
     * Removes a local repository directory.
     *
     * @param repoPath local repository path to delete
     * @return future that completes when deletion finishes
     */
    CompletableFuture<Void> removeRepository(Path repoPath);

    /**
     * Appends one NDJSON entry to a stream ref.
     *
     * <p>Creates the stream if it doesn't exist. Entry should be a single-line JSON object.
     *
     * @param repoPath  local repository path
     * @param streamRef fully-qualified ref path (e.g. {@code refs/notes/reviews/{id}/metadata/title})
     * @param entryJson single-line JSON entry to append
     * @return future that completes when append finishes
     */
    CompletableFuture<Void> appendToStream(Path repoPath, String streamRef, String entryJson);

    /**
     * Reads all NDJSON entries from a stream ref.
     *
     * @param repoPath  local repository path
     * @param streamRef fully-qualified ref path
     * @return future that completes with list of NDJSON entries in order; empty if stream doesn't exist
     */
    CompletableFuture<List<String>> readStream(Path repoPath, String streamRef);

    /**
     * Fetches all review notes refs from the remote.
     *
     * <p>Uses union merge strategy to safely merge concurrent updates:
     * {@code git -c notes.mergeStrategy=union fetch <remote> +refs/notes/reviews/*:refs/notes/reviews/*}
     *
     * @param repoPath local repository path
     * @param remote   remote name
     * @return future that completes when fetch finishes
     */
    CompletableFuture<Void> fetchNotes(Path repoPath, String remote);

    /**
     * Pushes all local review notes refs to the remote.
     *
     * @param repoPath local repository path
     * @param remote   remote name
     * @return future that completes when push finishes
     */
    CompletableFuture<Void> pushNotes(Path repoPath, String remote);

    /**
     * Checks if a ref exists in the repository.
     *
     * @param repoPath local repository path
     * @param ref      ref path to check
     * @return future that completes with true if ref exists, false otherwise
     */
    CompletableFuture<Boolean> refExists(Path repoPath, String ref);

    /**
     * Lists all review IDs in the repository.
     *
     * <p>Scans {@code refs/notes/reviews/} and extracts unique review identifiers.
     *
     * @param repoPath local repository path
     * @return future that completes with list of review IDs
     */
    CompletableFuture<List<String>> listReviews(Path repoPath);
}
