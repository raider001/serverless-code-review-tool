package com.kalynx.serverlessreviewtool.managers;

import com.kalynx.serverlessreviewtool.git.Git;
import com.kalynx.serverlessreviewtool.models.Commit;
import com.kalynx.serverlessreviewtool.models.FileChangeType;
import com.kalynx.serverlessreviewtool.models.ReviewFile;
import com.kalynx.serverlessreviewtool.ui.models.mainpanels.reviewpanel.CodeViewerModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * FileDiffManager manages loading file diffs and content for code review.
 * Handles async operations for loading commits, changed files, and diff content.
 * Updates CodeViewerModel which triggers UI updates.
 */
public class FileDiffManager {
    private static final Logger logger = LoggerFactory.getLogger(FileDiffManager.class);

    private final Git git;
    private final CodeViewerModel codeViewerModel;

    public FileDiffManager(Git git, CodeViewerModel codeViewerModel) {
        this.git = git;
        this.codeViewerModel = codeViewerModel;
    }

    /**
     * Loads commits for a review from the specified repository and branch.
     * Updates the model with available commits and sets initial start/end commit range.
     *
     * @param repositoryName name of the repository
     * @param branch branch name to load commits from
     * @param maxCommits maximum number of commits to load
     * @return future that completes when commits are loaded
     */
    public CompletableFuture<Void> loadCommitsForReview(String repositoryName, String branch, int maxCommits) {
        logger.info("Loading commits for repository: {}, branch: {}, max: {}", repositoryName, branch, maxCommits);

        return git.listCommits(repositoryName, branch, maxCommits)
            .thenApply(this::parseCommits)
            .thenAccept(commits -> {
                logger.info("Loaded {} commits", commits.size());
                codeViewerModel.setAvailableCommits(commits);

                if (!commits.isEmpty()) {
                    int startIndex = Math.min(1, commits.size() - 1);
                    Commit startCommit = commits.get(startIndex);
                    Commit endCommit = commits.get(0);

                    logger.info("Setting commit range: start={}, end={}",
                        startCommit.getShortHash(), endCommit.getShortHash());

                    codeViewerModel.setStartCommit(startCommit);
                    codeViewerModel.setEndCommit(endCommit);
                } else {
                    logger.warn("No commits found for repository: {}, branch: {}", repositoryName, branch);
                }
            })
            .exceptionally(error -> {
                logger.error("Failed to load commits: {}", error.getMessage(), error);
                codeViewerModel.setAvailableCommits(new ArrayList<>());
                return null;
            });
    }

    /**
     * Loads the list of files changed between two commits.
     * Updates the model with available files.
     *
     * @param repositoryName name of the repository
     * @param startCommit starting commit for comparison
     * @param endCommit ending commit for comparison
     * @return future that completes when files are loaded
     */
    public CompletableFuture<Void> loadChangedFiles(String repositoryName, Commit startCommit, Commit endCommit) {
        if (startCommit == null || endCommit == null) {
            codeViewerModel.setAvailableFiles(new ArrayList<>());
            return CompletableFuture.completedFuture(null);
        }

        logger.info("Loading changed files from {} to {}", startCommit.getShortHash(), endCommit.getShortHash());

        return git.listChangedFiles(repositoryName, startCommit.getHash(), endCommit.getHash())
            .thenApply(changedFiles -> {
                logger.debug("Got {} changed file lines from git", changedFiles.size());
                if (!changedFiles.isEmpty()) {
                    logger.debug("First few lines: {}", changedFiles.subList(0, Math.min(3, changedFiles.size())));
                }
                return parseChangedFiles(changedFiles, repositoryName);
            })
            .thenAccept(codeViewerModel::setAvailableFiles)
            .exceptionally(error -> {
                String errorMessage = error.getMessage();
                if (errorMessage != null && errorMessage.contains("bad object")) {
                    logger.error("Failed to load changed files - commit not found in local repository. " +
                                "Repository may need to be synced. Start: {}, End: {}",
                                startCommit.getShortHash(), endCommit.getShortHash(), error);
                } else {
                    logger.error("Failed to load changed files: {}", errorMessage, error);
                }
                codeViewerModel.setAvailableFiles(new ArrayList<>());
                return null;
            });
    }

    /**
     * Loads diff content for a specific file between two commits.
     * Loads both side-by-side content (left/right) and unified diff format.
     * Updates the model with file content.
     * Handles ADDED and DELETED files gracefully with appropriate placeholder messages.
     *
     * @param repositoryName name of the repository
     * @param file file to load diff for
     * @param startCommit starting commit for comparison
     * @param endCommit ending commit for comparison
     * @return future that completes when diff is loaded
     */
    public CompletableFuture<Void> loadDiffForFile(String repositoryName, ReviewFile file,
                                                     Commit startCommit, Commit endCommit) {
        if (file == null || startCommit == null || endCommit == null) {
            return CompletableFuture.completedFuture(null);
        }

        // For ADDED files, left side doesn't exist
        CompletableFuture<String> leftContentFuture;
        if (file.getChangeType() == FileChangeType.ADDED) {
            leftContentFuture = CompletableFuture.completedFuture(
                "// File does not exist in commit " + startCommit.getShortHash() + "\n" +
                "// This file was added in commit " + endCommit.getShortHash());
        } else {
            leftContentFuture = loadFileContent(repositoryName, file.getPath(), startCommit.getHash(), file);
        }

        // For DELETED files, right side doesn't exist
        CompletableFuture<String> rightContentFuture;
        if (file.getChangeType() == FileChangeType.DELETED) {
            rightContentFuture = CompletableFuture.completedFuture(
                "// File was deleted in commit " + endCommit.getShortHash() + "\n" +
                "// This file existed in commit " + startCommit.getShortHash());
        } else {
            rightContentFuture = loadFileContent(repositoryName, file.getPath(), endCommit.getHash(), file);
        }

        CompletableFuture<String> unifiedDiffFuture = loadUnifiedDiff(repositoryName,
            file.getPath(), startCommit.getHash(), endCommit.getHash());

        return CompletableFuture.allOf(leftContentFuture, rightContentFuture, unifiedDiffFuture)
            .thenAccept(ignored -> {
                String leftContent = leftContentFuture.join();
                String rightContent = rightContentFuture.join();
                String unifiedDiff = unifiedDiffFuture.join();

                codeViewerModel.setLeftContent(leftContent);
                codeViewerModel.setRightContent(rightContent);
                codeViewerModel.setUnifiedDiffContent(unifiedDiff);
            })
            .exceptionally(error -> {
                logger.error("Failed to load diff for file {}: {}", file.getPath(), error.getMessage());
                codeViewerModel.setLeftContent("// Error loading content: " + error.getMessage());
                codeViewerModel.setRightContent("// Error loading content: " + error.getMessage());
                codeViewerModel.setUnifiedDiffContent("// Error loading diff: " + error.getMessage());
                return null;
            });
    }

    /**
     * Refreshes the current view by reloading changed files and the currently selected file's diff.
     * Used when user clicks the refresh button.
     *
     * @param repositoryName name of the repository
     * @return future that completes when refresh is done
     */
    public CompletableFuture<Void> refreshCurrentView(String repositoryName) {
        Commit startCommit = codeViewerModel.startCommit.getValue();
        Commit endCommit = codeViewerModel.endCommit.getValue();
        ReviewFile currentFile = codeViewerModel.selectedFile.getValue();

        if (startCommit == null || endCommit == null) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> filesRefresh = loadChangedFiles(repositoryName, startCommit, endCommit);

        if (currentFile != null) {
            CompletableFuture<Void> diffRefresh = loadDiffForFile(repositoryName, currentFile,
                startCommit, endCommit);
            return CompletableFuture.allOf(filesRefresh, diffRefresh);
        }

        return filesRefresh;
    }

    private CompletableFuture<String> loadFileContent(String repositoryName, String filePath,
                                                       String commitHash, ReviewFile file) {
        return git.executeAsync(repositoryName, "show", commitHash + ":" + filePath)
            .exceptionally(error -> {
                String errorMsg = error.getMessage();

                // Handle cases where file doesn't exist in this commit
                if (errorMsg != null && (errorMsg.contains("does not exist") ||
                                        errorMsg.contains("not in") ||
                                        errorMsg.contains("path") && errorMsg.contains("exists on disk"))) {

                    if (file.getChangeType() == FileChangeType.ADDED) {
                        return "// File does not exist in this commit\n" +
                               "// File: " + filePath + "\n" +
                               "// This file was added in a later commit";
                    } else if (file.getChangeType() == FileChangeType.DELETED) {
                        return "// File was deleted in a later commit\n" +
                               "// File: " + filePath + "\n" +
                               "// This file existed in an earlier commit";
                    } else {
                        return "// File not available in commit " + commitHash.substring(0, 8) + "\n" +
                               "// File: " + filePath;
                    }
                }

                // Other errors
                logger.warn("Failed to load file content for {} at {}: {}",
                           filePath, commitHash, errorMsg);
                return "// Error loading file content\n" +
                       "// File: " + filePath + "\n" +
                       "// Error: " + errorMsg;
            });
    }

    private CompletableFuture<String> loadUnifiedDiff(String repositoryName, String filePath,
                                                        String fromCommit, String toCommit) {
        return git.executeAsync(repositoryName, "diff", fromCommit, toCommit, "--", filePath)
            .exceptionally(error -> {
                String errorMsg = error.getMessage();
                logger.warn("Failed to generate unified diff for {}: {}", filePath, errorMsg);

                // Return a descriptive message for the diff pane
                return "# Unable to generate diff\n" +
                       "# File: " + filePath + "\n" +
                       "# Error: " + errorMsg;
            });
    }

    private List<Commit> parseCommits(List<String> commitStrings) {
        List<Commit> commits = new ArrayList<>();
        for (String commitString : commitStrings) {
            try {
                String[] parts = commitString.split("\\|", 4);
                if (parts.length >= 4) {
                    commits.add(new Commit(parts[0], parts[3], parts[1], parts[2]));
                } else {
                    logger.warn("Skipping malformed commit line: '{}' (only {} parts)", commitString, parts.length);
                }
            } catch (Exception e) {
                logger.error("Error parsing commit line '{}': {}", commitString, e.getMessage(), e);
            }
        }
        return commits;
    }

    private List<ReviewFile> parseChangedFiles(List<String> changedFileStrings, String repositoryName) {
        List<ReviewFile> files = new ArrayList<>();
        for (String fileString : changedFileStrings) {
            try {
                String trimmed = fileString.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                String[] parts = trimmed.split("\\s+", 2);
                if (parts.length >= 2) {
                    String status = parts[0];
                    String path = parts[1];
                    FileChangeType changeType = parseFileChangeType(status);
                    files.add(new ReviewFile(path, repositoryName, changeType));
                } else {
                    logger.warn("Skipping malformed changed file line: '{}' (only {} parts)", trimmed, parts.length);
                }
            } catch (Exception e) {
                logger.error("Error parsing changed file line '{}': {}", fileString, e.getMessage(), e);
            }
        }
        return files;
    }

    private FileChangeType parseFileChangeType(String status) {
        if (status == null || status.isEmpty()) {
            logger.warn("Empty or null status, defaulting to MODIFIED");
            return FileChangeType.MODIFIED;
        }
        return switch (status.toUpperCase().charAt(0)) {
            case 'A' -> FileChangeType.ADDED;
            case 'M' -> FileChangeType.MODIFIED;
            case 'D' -> FileChangeType.DELETED;
            case 'R' -> FileChangeType.RENAMED;
            default -> FileChangeType.MODIFIED;
        };
    }
}

