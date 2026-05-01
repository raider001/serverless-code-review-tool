package com.kalynx.serverlessreviewtool.git;

import java.nio.file.Path;
import java.util.List;

/**
 * Git provides operations for managing review notes stored in git repositories.
 *
 * <p>All review data is persisted as NDJSON streams under {@code refs/notes/reviews/}
 * following the structure defined in the data model.
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
     * @throws GitException if initialization or initial fetch fails
     */
    void initNotesRepository(Path repoPath, String remote, String remoteUrl) throws GitException;

    /**
     * Removes a local repository directory.
     *
     * @param repoPath local repository path to delete
     * @throws GitException if deletion fails
     */
    void removeRepository(Path repoPath) throws GitException;

    /**
     * Appends one NDJSON entry to a stream ref.
     *
     * <p>Creates the stream if it doesn't exist. Entry should be a single-line JSON object.
     *
     * @param repoPath  local repository path
     * @param streamRef fully-qualified ref path (e.g. {@code refs/notes/reviews/{id}/metadata/title})
     * @param entryJson single-line JSON entry to append
     * @throws GitException if append fails
     */
    void appendToStream(Path repoPath, String streamRef, String entryJson) throws GitException;

    /**
     * Reads all NDJSON entries from a stream ref.
     *
     * @param repoPath  local repository path
     * @param streamRef fully-qualified ref path
     * @return list of NDJSON entries in order; empty if stream doesn't exist
     * @throws GitException if read fails
     */
    List<String> readStream(Path repoPath, String streamRef) throws GitException;

    /**
     * Fetches all review notes refs from the remote.
     *
     * <p>Uses union merge strategy to safely merge concurrent updates:
     * {@code git -c notes.mergeStrategy=union fetch <remote> +refs/notes/reviews/*:refs/notes/reviews/*}
     *
     * @param repoPath local repository path
     * @param remote   remote name
     * @throws GitException if fetch fails
     */
    void fetchNotes(Path repoPath, String remote) throws GitException;

    /**
     * Pushes all local review notes refs to the remote.
     *
     * @param repoPath local repository path
     * @param remote   remote name
     * @throws GitException if push fails
     */
    void pushNotes(Path repoPath, String remote) throws GitException;

    /**
     * Checks if a ref exists in the repository.
     *
     * @param repoPath local repository path
     * @param ref      ref path to check
     * @return true if ref exists
     * @throws GitException if check fails
     */
    boolean refExists(Path repoPath, String ref) throws GitException;

    /**
     * Lists all review IDs in the repository.
     *
     * <p>Scans {@code refs/notes/reviews/} and extracts unique review identifiers.
     *
     * @param repoPath local repository path
     * @return list of review IDs
     * @throws GitException if scan fails
     */
    List<String> listReviews(Path repoPath) throws GitException;
}
