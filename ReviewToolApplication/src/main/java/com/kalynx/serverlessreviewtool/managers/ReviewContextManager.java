package com.kalynx.serverlessreviewtool.managers;

import com.kalynx.serverlessreviewtool.git.Git;
import com.kalynx.serverlessreviewtool.git.GitReviewNotesManager;
import com.kalynx.serverlessreviewtool.models.FileChangeType;
import com.kalynx.serverlessreviewtool.models.ReviewContext;
import com.kalynx.serverlessreviewtool.models.ReviewFile;
import com.kalynx.serverlessreviewtool.models.ReviewStatus;
import com.kalynx.serverlessreviewtool.models.ReviewerInfo;
import com.kalynx.serverlessreviewtool.models.ReviewerStatus;
import com.kalynx.serverlessreviewtool.models.Repository;
import com.kalynx.serverlessreviewtool.models.review.StreamEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * ReviewContextManager manages the lifecycle of a ReviewContext.
 * Responsibilities:
 * - Search across all repositories for review notes
 * - Load review metadata from git notes
 * - Create and maintain current ReviewContext
 * - Notify listeners when context changes
 * Does NOT handle:
 * - Repository fetching (caller's responsibility)
 * - Commit loading (FileDiffManager's responsibility)
 * - File loading (FileDiffManager's responsibility)
 */
public class ReviewContextManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewContextManager.class);

    private ReviewContext currentReviewContext;
    private final Git git;
    private final RepositoryManager repositoryManager;
    private final Set<Consumer<ReviewContext>> listeners = new HashSet<>();

    public ReviewContextManager(Git git, RepositoryManager repositoryManager) {
        this.git = git;
        this.repositoryManager = repositoryManager;
    }

    /**
     * Load comments for a review from git notes.
     *
     * @param reviewId the review identifier
     * @return future that completes with list of ReviewComment objects
     */
    public CompletableFuture<List<com.kalynx.serverlessreviewtool.models.ReviewComment>> loadCommentsForReview(String reviewId) {
        if (reviewId == null || reviewId.isEmpty()) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        LOGGER.info("Loading comments for review: {}", reviewId);

        List<Repository> allRepositories = repositoryManager.getRepositories();
        if (allRepositories.isEmpty()) {
            LOGGER.warn("No repositories configured, cannot load comments");
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        return findPrimaryRepositoryForReview(reviewId, allRepositories)
            .thenCompose(primaryRepo -> {
                GitReviewNotesManager notesManager = new GitReviewNotesManager(git, primaryRepo.getName());

                return notesManager.listCommentIds(reviewId)
                    .thenCompose(commentIds -> {
                        if (commentIds.isEmpty()) {
                            LOGGER.info("No comments found for review: {}", reviewId);
                            return CompletableFuture.completedFuture(new ArrayList<com.kalynx.serverlessreviewtool.models.ReviewComment>());
                        }

                        LOGGER.info("Found {} comment threads for review: {}", commentIds.size(), reviewId);

                        List<CompletableFuture<com.kalynx.serverlessreviewtool.models.ReviewComment>> commentFutures =
                            commentIds.stream()
                                .map(commentId -> loadSingleComment(notesManager, reviewId, commentId))
                                .toList();

                        return CompletableFuture.allOf(commentFutures.toArray(new CompletableFuture[0]))
                            .thenApply(ignored -> {
                                List<com.kalynx.serverlessreviewtool.models.ReviewComment> comments =
                                    commentFutures.stream()
                                        .map(CompletableFuture::join)
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList());

                                LOGGER.info("Loaded {} comments for review: {}", comments.size(), reviewId);
                                return comments;
                            });
                    });
            })
            .exceptionally(error -> {
                LOGGER.error("Failed to load comments for review: {}", reviewId, error);
                return new ArrayList<>();
            });
    }

    private CompletableFuture<Repository> findPrimaryRepositoryForReview(String reviewId, List<Repository> repositories) {
        List<CompletableFuture<Repository>> futures = repositories.stream()
            .map(repo -> {
                GitReviewNotesManager notesManager = new GitReviewNotesManager(git, repo.getName());
                return notesManager.readTitles(reviewId)
                    .thenApply(titles -> (titles != null && !titles.isEmpty()) ? repo : null)
                    .exceptionally(ignored -> null);
            })
            .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(ignored -> futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(repositories.getFirst()));
    }

    private CompletableFuture<com.kalynx.serverlessreviewtool.models.ReviewComment> loadSingleComment(
            GitReviewNotesManager notesManager, String reviewId, String commentId) {

        return notesManager.readCommentMetadata(reviewId, commentId)
            .thenCombine(notesManager.readCommentText(reviewId, commentId),
                (metadata, textEntries) -> {
                    if (metadata.isEmpty() || textEntries.isEmpty()) {
                        return null;
                    }

                    StreamEntry<GitReviewNotesManager.CommentMetadata> latestMetadata = metadata.getLast();
                    StreamEntry<GitReviewNotesManager.CommentTextData> firstText = textEntries.getFirst();

                    GitReviewNotesManager.CommentMetadata metaData = latestMetadata.data();
                    GitReviewNotesManager.CommentTextData textData = firstText.data();

                    return new Object[] { commentId, metaData.file(), metaData.line(),
                        firstText.editor(), textData.text(), firstText.timestamp().toString(),
                        textData.replyTo(), "review".equals(textData.type()) };
                })
            .thenCompose(commentData -> {
                if (commentData == null) {
                    return CompletableFuture.completedFuture(null);
                }

                return notesManager.readCommentStatus(reviewId, commentId)
                    .thenApply(statusEntries -> {
                        String id = (String) commentData[0];
                        String filePath = (String) commentData[1];
                        int lineNumber = (int) commentData[2];
                        String editor = (String) commentData[3];
                        String text = (String) commentData[4];
                        String timestamp = (String) commentData[5];
                        String replyTo = (String) commentData[6];
                        boolean needsResolution = (boolean) commentData[7];

                        com.kalynx.serverlessreviewtool.models.ReviewComment comment =
                            new com.kalynx.serverlessreviewtool.models.ReviewComment(
                                id, filePath, lineNumber, editor, text, timestamp, replyTo, needsResolution
                            );

                        // Apply the latest status from the status stream (overrides initial needsResolution from type)
                        if (!statusEntries.isEmpty()) {
                            StreamEntry<GitReviewNotesManager.CommentStatusData> latestStatus =
                                statusEntries.getLast();
                            GitReviewNotesManager.CommentStatusData statusData = latestStatus.data();

                            // Apply needsResolution from status (can override type-based initial value)
                            if (statusData.needsResolution() != null) {
                                comment.setNeedsResolution(statusData.needsResolution());
                            }

                            // Apply resolved status (handle both true and false)
                            if (statusData.resolved() != null) {
                                if (statusData.resolved()) {
                                    comment.markResolved(latestStatus.editor());
                                } else {
                                    comment.markUnresolved();
                                }
                            }
                        }

                        return comment;
                    });
            })
            .exceptionally(error -> {
                LOGGER.error("Failed to load comment {}: {}", commentId, error.getMessage());
                return null;
            });
    }

    /**
     * Save a comment to git notes.
     *
     * @param reviewId the review identifier
     * @param comment the comment to save
     * @return future that completes when comment is saved
     */
    public CompletableFuture<Void> saveComment(String reviewId, com.kalynx.serverlessreviewtool.models.ReviewComment comment) {
        if (reviewId == null || reviewId.isEmpty() || comment == null) {
            return CompletableFuture.completedFuture(null);
        }

        LOGGER.info("Saving comment for review: {} (id: {})", reviewId, comment.getId());

        ReviewContext context = currentReviewContext;
        if (context == null || context.repositories.isEmpty()) {
            LOGGER.warn("No review context or repositories, cannot save comment");
            return CompletableFuture.completedFuture(null);
        }

        Repository primaryRepo = context.repositories.getFirst();
        GitReviewNotesManager notesManager = new GitReviewNotesManager(git, primaryRepo.getName());

        String commentType = comment.needsResolution() ? "review" : "comment";

        CompletableFuture<Void> saveFuture = notesManager.writeCommentMetadata(
            reviewId, comment.getId(), comment.getAuthor(),
            comment.getFilePath(), comment.getLineNumber(), comment.getLineNumber(), null
        ).thenCompose(ignored ->
            notesManager.writeCommentText(
                reviewId, comment.getId(), comment.getAuthor(),
                comment.getText(), comment.getParentId(), commentType
            )
        );

        if (comment.needsResolution() || comment.isResolved()) {
            saveFuture = saveFuture.thenCompose(ignored ->
                notesManager.writeCommentStatus(
                    reviewId, comment.getId(), comment.getAuthor(),
                    comment.needsResolution(), comment.isResolved()
                )
            );
        }

        return saveFuture
            .thenRun(() -> LOGGER.info("Comment saved successfully for review: {} (id: {})", reviewId, comment.getId()))
            .exceptionally(error -> {
                LOGGER.error("Failed to save comment for review: {} (id: {})", reviewId, comment.getId(), error);
                return null;
            });
    }

    /**
     * Save all comments for a review to git notes.
     * This is useful when multiple comments have been updated (e.g., resolution status changes).
     * Uses a single atomic git operation to avoid race conditions.
     *
     * @param reviewId the review identifier
     * @param comments the comments to save
     * @return future that completes when all comments are saved
     */
    public CompletableFuture<Void> saveAllComments(String reviewId, List<com.kalynx.serverlessreviewtool.models.ReviewComment> comments) {
        if (reviewId == null || reviewId.isEmpty() || comments == null || comments.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        LOGGER.info("Batch saving {} comments for review: {}", comments.size(), reviewId);

        List<CompletableFuture<Void>> saveFutures = comments.stream()
            .map(comment -> saveComment(reviewId, comment))
            .toList();

        return CompletableFuture.allOf(saveFutures.toArray(new CompletableFuture[0]))
            .thenRun(() -> LOGGER.info("All {} comments batch-saved successfully for review: {}", comments.size(), reviewId))
            .exceptionally(error -> {
                LOGGER.error("Failed to batch-save comments for review: {}", reviewId, error);
                return null;
            });
    }

    /**
     * Load review metadata by searching across all known repositories.
     * Uses an optimized batch read operation to minimize Git operations.
     * Does NOT handle repository syncing or file/commit loading.
     *
     * @param reviewId the review identifier
     * @return future that completes when ReviewContext is created and set
     */
    public CompletableFuture<ReviewContext> loadReviewMetadata(String reviewId) {
        if (reviewId == null || reviewId.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        LOGGER.info("Searching for review {} across all repositories", reviewId);

        List<Repository> allRepositories = repositoryManager.getRepositories();

        if (allRepositories.isEmpty()) {
            LOGGER.warn("No repositories configured, cannot load review {}", reviewId);
            return CompletableFuture.completedFuture(null);
        }

        return findReviewInRepositories(reviewId, allRepositories)
            .thenCompose(repositoryName -> {
                if (repositoryName == null) {
                    LOGGER.warn("Review {} not found in any repository", reviewId);
                    return CompletableFuture.completedFuture(null);
                }

                LOGGER.info("Found review {} in repository: {}", reviewId, repositoryName);
                return loadReviewFromRepositories(reviewId, repositoryName, allRepositories, true);
            });
    }

    /**
     * Load review metadata without reloading comments.
     *
     * @param reviewId the review identifier
     * @return future that completes when ReviewContext metadata is refreshed
     */
    public CompletableFuture<ReviewContext> loadReviewMetadataOnly(String reviewId) {
        if (reviewId == null || reviewId.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        LOGGER.info("Searching for review {} across all repositories (metadata only)", reviewId);

        List<Repository> allRepositories = repositoryManager.getRepositories();

        if (allRepositories.isEmpty()) {
            LOGGER.warn("No repositories configured, cannot load review {}", reviewId);
            return CompletableFuture.completedFuture(null);
        }

        return findReviewInRepositories(reviewId, allRepositories)
            .thenCompose(repositoryName -> {
                if (repositoryName == null) {
                    LOGGER.warn("Review {} not found in any repository", reviewId);
                    return CompletableFuture.completedFuture(null);
                }

                LOGGER.info("Found review {} in repository: {}", reviewId, repositoryName);
                return loadReviewFromRepositories(reviewId, repositoryName, allRepositories, false);
            });
    }

    /**
     * Load review metadata from specific repositories.
     * Uses the provided repository names to only search those repositories.
     * Does NOT handle repository syncing or file/commit loading.
     *
     * @param reviewId the review identifier
     * @param repositoryNames list of repository names to search
     * @return future that completes when ReviewContext is created and set
     */
    public CompletableFuture<ReviewContext> loadReviewMetadata(String reviewId, List<String> repositoryNames) {
        if (reviewId == null || reviewId.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        if (repositoryNames == null || repositoryNames.isEmpty()) {
            LOGGER.info("No specific repositories provided, searching all repositories for review {}", reviewId);
            return loadReviewMetadata(reviewId);
        }

        LOGGER.info("Loading review {} from specified {} repositories", reviewId, repositoryNames.size());

        List<Repository> specificRepositories = new ArrayList<>();
        for (String repoName : repositoryNames) {
            Repository repo = repositoryManager.getRepositoryByName(repoName);
            if (repo != null) {
                specificRepositories.add(repo);
            } else {
                LOGGER.warn("Repository '{}' not found in RepositoryManager", repoName);
            }
        }

        if (specificRepositories.isEmpty()) {
            LOGGER.warn("None of the specified repositories found, falling back to search all repositories");
            return loadReviewMetadata(reviewId);
        }

        return findReviewInRepositories(reviewId, specificRepositories)
            .thenCompose(repositoryName -> {
                if (repositoryName == null) {
                    LOGGER.warn("Review {} not found in specified repositories", reviewId);
                    return CompletableFuture.completedFuture(null);
                }

                LOGGER.info("Found review {} in repository: {}", reviewId, repositoryName);
                return loadReviewFromRepositories(reviewId, repositoryName, specificRepositories, true);
            });
    }

    /**
     * Load review metadata from specific repositories without reloading comments.
     *
     * @param reviewId the review identifier
     * @param repositoryNames list of repository names to search
     * @return future that completes when ReviewContext metadata is refreshed
     */
    public CompletableFuture<ReviewContext> loadReviewMetadataOnly(String reviewId, List<String> repositoryNames) {
        if (reviewId == null || reviewId.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        if (repositoryNames == null || repositoryNames.isEmpty()) {
            LOGGER.info("No specific repositories provided, searching all repositories for review {} (metadata only)", reviewId);
            return loadReviewMetadataOnly(reviewId);
        }

        LOGGER.info("Loading review {} from specified {} repositories (metadata only)", reviewId, repositoryNames.size());

        List<Repository> specificRepositories = new ArrayList<>();
        for (String repoName : repositoryNames) {
            Repository repo = repositoryManager.getRepositoryByName(repoName);
            if (repo != null) {
                specificRepositories.add(repo);
            } else {
                LOGGER.warn("Repository '{}' not found in RepositoryManager", repoName);
            }
        }

        if (specificRepositories.isEmpty()) {
            LOGGER.warn("None of the specified repositories found, falling back to search all repositories");
            return loadReviewMetadataOnly(reviewId);
        }

        return findReviewInRepositories(reviewId, specificRepositories)
            .thenCompose(repositoryName -> {
                if (repositoryName == null) {
                    LOGGER.warn("Review {} not found in specified repositories", reviewId);
                    return CompletableFuture.completedFuture(null);
                }

                LOGGER.info("Found review {} in repository: {}", reviewId, repositoryName);
                return loadReviewFromRepositories(reviewId, repositoryName, specificRepositories, false);
            });
    }

    private CompletableFuture<String> findReviewInRepositories(String reviewId, List<Repository> repositories) {
        List<CompletableFuture<String>> searchFutures = repositories.stream()
            .map(repo -> checkRepositoryForReview(reviewId, repo.getName()))
            .toList();

        return CompletableFuture.allOf(searchFutures.toArray(new CompletableFuture[0]))
            .thenApply(ignored -> {
                for (CompletableFuture<String> future : searchFutures) {
                    String repoName = future.join();
                    if (repoName != null) {
                        return repoName;
                    }
                }
                return null;
            });
    }

    private CompletableFuture<String> checkRepositoryForReview(String reviewId, String repositoryName) {
        GitReviewNotesManager notesManager = new GitReviewNotesManager(git, repositoryName);

        return notesManager.readTitles(reviewId)
            .thenApply(entries -> {
                if (entries != null && !entries.isEmpty()) {
                    LOGGER.debug("Review {} found in repository: {}", reviewId, repositoryName);
                    return repositoryName;
                }
                return null;
            })
            .exceptionally(error -> {
                LOGGER.debug("Review {} not found in repository {}: {}", reviewId, repositoryName, error.getMessage());
                return null;
            });
    }

    private CompletableFuture<ReviewContext> loadReviewFromRepositories(
            String reviewId,
            String repositoryName,
            List<Repository> allRepositories,
            boolean includeComments) {
        LOGGER.info("Loading review metadata: reviewId={}, repository={}", reviewId, repositoryName);

        GitReviewNotesManager notesManager = new GitReviewNotesManager(git, repositoryName);

                return notesManager.readAllMetadata(reviewId)
            .thenCompose(metadata -> {
                String title = getLatestValue(metadata.titles());
                String description = getLatestValue(metadata.descriptions());
                String author = getLatestValue(metadata.authors());
                String statusStr = getLatestValue(metadata.statuses());
                String branch = getLatestValue(metadata.branches());
                String baseBranch = getLatestValue(metadata.baseBranches());
                boolean hasClosedHistory = metadata.statuses().stream()
                    .map(StreamEntry::data)
                    .filter(Objects::nonNull)
                    .map(this::parseStatus)
                    .anyMatch(this::isClosedStatus);

                List<StreamEntry<com.kalynx.serverlessreviewtool.models.review.ReviewerData>> latestReviewerEntries = metadata.reviewers().stream()
                    .collect(Collectors.groupingBy(
                        StreamEntry::editor,
                        Collectors.maxBy(Comparator.comparing(StreamEntry::timestamp))
                    ))
                    .values().stream()
                    .flatMap(java.util.Optional::stream)
                    .filter(entry -> !isLeftReviewerStatus(entry.data().getStatus()))
                    .toList();

                String resolvedTitle = title != null ? title : "Untitled Review";
                String resolvedDescription = description != null ? description : "";
                String resolvedAuthor = author != null ? author : "Unknown";
                String resolvedStatus = statusStr != null ? statusStr : "OPEN";

                LOGGER.info("Loaded review metadata: reviewId={}, title={}, author={}, reviewers={}",
                    reviewId, resolvedTitle, resolvedAuthor, latestReviewerEntries.size());

                ReviewStatus status = parseStatus(resolvedStatus);
                List<ReviewerInfo> reviewers = latestReviewerEntries.stream()
                    .map(entry -> {
                        ReviewerInfo reviewerInfo = new ReviewerInfo(entry.editor());
                        reviewerInfo.setStatus(parseReviewerStatus(entry.data().getStatus()));
                        return reviewerInfo;
                    })
                    .toList();

                return findRepositoriesContainingReview(reviewId, allRepositories, repositoryName)
                    .thenCompose(reviewRepositories -> {
                        LOGGER.info("Review {} found in {} repositories", reviewId, reviewRepositories.size());

                        List<com.kalynx.serverlessreviewtool.models.ReviewComment> existingComments =
                            currentReviewContext != null && reviewId.equals(currentReviewContext.reviewId)
                                ? new ArrayList<>(currentReviewContext.getComments())
                                : new ArrayList<>();

                        if (!includeComments) {
                            ReviewContext reviewContext = new ReviewContext(
                                reviewId,
                                resolvedTitle,
                                resolvedDescription,
                                resolvedAuthor,
                                status,
                                reviewers,
                                reviewRepositories,
                                existingComments,
                                branch,
                                baseBranch,
                                hasClosedHistory
                            );

                            setReviewContext(reviewContext);
                            return CompletableFuture.completedFuture(reviewContext);
                        }

                        return loadCommentsForReview(reviewId)
                            .thenApply(comments -> {
                                LOGGER.info("Loaded {} comments for review {}", comments.size(), reviewId);

                                ReviewContext reviewContext = new ReviewContext(
                                    reviewId,
                                    resolvedTitle,
                                    resolvedDescription,
                                    resolvedAuthor,
                                    status,
                                    reviewers,
                                    reviewRepositories,
                                    comments,
                                    branch,
                                    baseBranch,
                                    hasClosedHistory
                                );

                                setReviewContext(reviewContext);
                                return reviewContext;
                            });
                    });
            }).exceptionally(error -> {
                LOGGER.error("Failed to load review metadata for {}: {}", reviewId, error.getMessage(), error);
                return null;
            });
    }

    private CompletableFuture<List<Repository>> findRepositoriesContainingReview(
            String reviewId,
            List<Repository> allRepositories,
            String fallbackRepositoryName) {
        List<CompletableFuture<Repository>> futures = allRepositories.stream()
            .map(repo -> {
                GitReviewNotesManager repoNotesManager = new GitReviewNotesManager(git, repo.getName());
                return repoNotesManager.readTitles(reviewId)
                    .thenApply(titles -> {
                        if (titles != null && !titles.isEmpty()) {
                            LOGGER.info("Review {} found in repository: {}", reviewId, repo.getName());
                            return repo;
                        }
                        return null;
                    })
                    .exceptionally(ignored -> {
                        LOGGER.debug("Review {} not found in repository {}", reviewId, repo.getName());
                        return null;
                    });
            })
            .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(ignored -> {
                List<Repository> reviewRepositories = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .toList();

                if (!reviewRepositories.isEmpty()) {
                    return reviewRepositories;
                }

                List<Repository> fallback = new ArrayList<>();
                Repository fallbackRepo = repositoryManager.getRepositoryByName(fallbackRepositoryName);
                if (fallbackRepo != null) {
                    fallback.add(fallbackRepo);
                }
                LOGGER.warn("Review {} not found in any repository, using fallback: {}", reviewId, fallbackRepositoryName);
                return fallback;
            });
    }

    /**
     * Load files changed in a review by comparing branch against base branch.
     * Returns a list of ReviewFile objects representing all changed files in the review.
     *
     * @param repositoryName the repository containing the review
     * @param reviewBranch the branch being reviewed (e.g., "feature/my-feature" or "origin/feature/my-feature")
     * @param baseBranch the base branch to compare against (e.g., "main" or "origin/main")
     * @return future that completes with list of ReviewFile objects
     */
    public CompletableFuture<List<ReviewFile>> loadFilesForReview(
            String repositoryName,
            String reviewBranch,
            String baseBranch) {

        if (repositoryName == null || reviewBranch == null || baseBranch == null) {
            LOGGER.warn("Invalid parameters for loading files: repo={}, reviewBranch={}, baseBranch={}",
                repositoryName, reviewBranch, baseBranch);
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        LOGGER.info("Loading files for review in repository '{}': {} -> {}",
            repositoryName, baseBranch, reviewBranch);

        return git.listChangedFiles(repositoryName, baseBranch, reviewBranch)
            .thenApply(changedFilePaths -> {
                LOGGER.info("Git diff returned {} changed file paths for repository '{}'",
                    changedFilePaths.size(), repositoryName);

                List<ReviewFile> reviewFiles = changedFilePaths.stream()
                    .map(filePath -> parseChangedFileLine(filePath, repositoryName, baseBranch, reviewBranch))
                    .toList();

                LOGGER.info("Loaded {} files for review in repository '{}'",
                    reviewFiles.size(), repositoryName);

                return reviewFiles;
            })
            .exceptionally(error -> {
                LOGGER.error("Failed to load files for review in repository '{}': {}",
                    repositoryName, error.getMessage(), error);
                return new ArrayList<>();
            });
    }

    private ReviewFile parseChangedFileLine(String line, String repositoryName,
                                            String baseBranch, String reviewBranch) {
        String trimmed = line.trim();
        String[] parts = trimmed.split("\\s+", 2);

        if (parts.length >= 2) {
            String status = parts[0];
            String path = parts[1];
            FileChangeType changeType = parseFileChangeType(status);
            return new ReviewFile(path, repositoryName, changeType, baseBranch, reviewBranch);
        } else {
            LOGGER.warn("Malformed file line: '{}', defaulting to MODIFIED", line);
            return new ReviewFile(trimmed, repositoryName, FileChangeType.MODIFIED, baseBranch, reviewBranch);
        }
    }

    private FileChangeType parseFileChangeType(String status) {
        if (status == null || status.isEmpty()) {
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

    /**
     * Load files changed in a review by comparing the already-known branches across all given repositories.
     * Uses branch and baseBranch from the loaded ReviewContext, avoiding a redundant metadata fetch.
     *
     * @param repositories list of repositories containing the review
     * @param branch the review branch (e.g., "feature/my-feature")
     * @param baseBranch the base branch to compare against (e.g., "origin/main")
     * @return future that completes with list of ReviewFile objects across all repositories
     */
    public CompletableFuture<List<ReviewFile>> loadFilesFromReviewCommits(List<Repository> repositories, String branch, String baseBranch) {
        if (repositories == null || repositories.isEmpty()) {
            LOGGER.warn("No repositories provided for loading files");
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        if (branch == null || baseBranch == null) {
            LOGGER.warn("Missing branch or baseBranch for loading files");
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        LOGGER.info("Loading files from review branches for {} repositories", repositories.size());
        LOGGER.info("Branch: {}, BaseBranch: {}", branch, baseBranch);

        List<CompletableFuture<List<ReviewFile>>> fileFutures = repositories.stream()
            .map(repo -> {
                LOGGER.info("Loading files from repository: {}", repo.getName());
                return loadFilesForReview(repo.getName(), branch, baseBranch);
            })
            .toList();

        return CompletableFuture.allOf(fileFutures.toArray(new CompletableFuture[0]))
            .thenApply(ignored -> {
                List<ReviewFile> allFiles = new ArrayList<>();
                for (int i = 0; i < fileFutures.size(); i++) {
                    List<ReviewFile> repoFiles = fileFutures.get(i).join();
                    LOGGER.info("Repository '{}' returned {} files", repositories.get(i).getName(), repoFiles.size());
                    allFiles.addAll(repoFiles);
                }
                LOGGER.info("Total {} files loaded from review branches across all repositories", allFiles.size());
                return allFiles;
            })
            .exceptionally(error -> {
                LOGGER.error("Failed to load files from review branches: {}", error.getMessage(), error);
                return new ArrayList<>();
            });
    }

    /**
     * Load files for a review using persisted commit snapshots instead of moving branch/base refs.
     * This is used for reviews that have been closed at least once and later re-opened.
     */
    public CompletableFuture<List<ReviewFile>> loadFilesFromStoredReviewCommits(
            String reviewId,
            List<Repository> repositories,
            String branch,
            String baseBranch) {
        if (reviewId == null || reviewId.isBlank() || repositories == null || repositories.isEmpty()) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        LOGGER.info("Loading files from stored commit snapshots for review {} across {} repositories",
            reviewId, repositories.size());

        List<CompletableFuture<List<ReviewFile>>> fileFutures = repositories.stream()
            .map(repo -> loadFilesForRepositoryFromStoredCommits(reviewId, repo.getName(), branch, baseBranch))
            .toList();

        return CompletableFuture.allOf(fileFutures.toArray(new CompletableFuture[0]))
            .thenApply(ignored -> {
                List<ReviewFile> allFiles = new ArrayList<>();
                for (CompletableFuture<List<ReviewFile>> future : fileFutures) {
                    allFiles.addAll(future.join());
                }
                LOGGER.info("Loaded {} files from stored commit snapshots for review {}", allFiles.size(), reviewId);
                return allFiles;
            })
            .exceptionally(error -> {
                LOGGER.error("Failed loading files from stored commits for review {}: {}",
                    reviewId, error.getMessage(), error);
                return new ArrayList<>();
            });
    }

    public CompletableFuture<List<String>> loadLatestReviewCommits(String reviewId, String repositoryName) {
        if (reviewId == null || reviewId.isBlank() || repositoryName == null || repositoryName.isBlank()) {
            return CompletableFuture.completedFuture(List.of());
        }
        GitReviewNotesManager notesManager = new GitReviewNotesManager(git, repositoryName);
        return notesManager.readCommits(reviewId)
            .thenApply(this::getLatestValue)
            .thenApply(commits -> commits != null ? commits : List.<String>of())
            .exceptionally(error -> {
                LOGGER.warn("Unable to load stored commits for review {} in repo {}: {}",
                    reviewId, repositoryName, error.getMessage());
                return List.of();
            });
    }

    /**
     * Capture commit snapshots for each repository participating in a review.
     * Used when transitioning a review to a terminal state so reopened reviews can replay historical changes.
     */
    public CompletableFuture<Void> captureReviewCommitSnapshots(
            String reviewId,
            List<Repository> repositories,
            String reviewBranch,
            String baseBranch,
            String editor) {
        if (reviewId == null || reviewId.isBlank() || repositories == null || repositories.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        if (reviewBranch == null || reviewBranch.isBlank() || baseBranch == null || baseBranch.isBlank()) {
            LOGGER.warn("Skipping commit snapshot capture for review {} - review/base branch is empty", reviewId);
            return CompletableFuture.completedFuture(null);
        }

        List<CompletableFuture<Void>> futures = repositories.stream()
            .map(repo -> {
                GitReviewNotesManager notesManager = new GitReviewNotesManager(git, repo.getName());
                String commitRange = baseBranch + ".." + reviewBranch;
                return loadLatestReviewCommits(reviewId, repo.getName())
                    .thenCompose(existingCommits -> git.listCommits(repo.getName(), commitRange, 1000)
                        .thenApply(this::extractCommitHashes)
                        .thenCompose(commitHashes -> {
                            if (commitHashes.isEmpty()) {
                                if (existingCommits != null && !existingCommits.isEmpty()) {
                                    LOGGER.info("Scoped review range {} is empty for review {} in repo {}; keeping existing stored snapshot of {} commits",
                                        commitRange, reviewId, repo.getName(), existingCommits.size());
                                } else {
                                    LOGGER.warn("No scoped review commits resolved for review {} in repo {} using range {}",
                                        reviewId, repo.getName(), commitRange);
                                }
                                return CompletableFuture.completedFuture(null);
                            }

                            if (existingCommits != null && existingCommits.equals(commitHashes)) {
                                LOGGER.info("Stored commit snapshot for review {} in repo {} is already correct ({} commits)",
                                    reviewId, repo.getName(), commitHashes.size());
                                return CompletableFuture.completedFuture(null);
                            }

                            LOGGER.info("Capturing {} scoped review commits for review {} in repo {}",
                                commitHashes.size(), reviewId, repo.getName());
                            return notesManager.writeReviewCommits(reviewId, editor, commitHashes);
                        }))
                    .exceptionally(error -> {
                        LOGGER.warn("Failed to capture commits for review {} in repo {}: {}",
                            reviewId, repo.getName(), error.getMessage());
                        return null;
                    });
            })
            .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }


    private List<String> extractCommitHashes(List<String> commitRows) {
        if (commitRows == null || commitRows.isEmpty()) {
            return List.of();
        }
        return commitRows.stream()
            .map(row -> row == null ? "" : row)
            .map(row -> row.split("\\|", 2))
            .filter(parts -> parts.length > 0 && !parts[0].isBlank())
            .map(parts -> parts[0])
            .toList();
    }

    private CompletableFuture<List<ReviewFile>> loadFilesForRepositoryFromStoredCommits(
            String reviewId,
            String repositoryName,
            String branch,
            String baseBranch) {
        GitReviewNotesManager notesManager = new GitReviewNotesManager(git, repositoryName);
        return notesManager.readCommits(reviewId)
            .thenApply(this::getLatestValue)
            .thenCompose(commits -> {
                if (commits == null || commits.isEmpty()) {
                    LOGGER.warn("No stored commit snapshot for review {} in repo {}; falling back to branch diff",
                        reviewId, repositoryName);
                    return loadFilesForReview(repositoryName, branch, baseBranch);
                }

                List<CompletableFuture<List<String>>> perCommitFutures = commits.stream()
                    .map(commitHash -> loadChangedFilesForCommit(repositoryName, commitHash))
                    .toList();

                return CompletableFuture.allOf(perCommitFutures.toArray(new CompletableFuture[0]))
                    .thenApply(ignored -> {
                        Map<String, String> statusByPath = new LinkedHashMap<>();
                        for (CompletableFuture<List<String>> future : perCommitFutures) {
                            for (String line : future.join()) {
                                String[] parts = line.trim().split("\\s+", 2);
                                if (parts.length < 2) {
                                    continue;
                                }
                                String status = parts[0];
                                String path = parts[1];
                                statusByPath.put(path, status);
                            }
                        }

                        List<ReviewFile> files = new ArrayList<>();
                        for (Map.Entry<String, String> entry : statusByPath.entrySet()) {
                            files.add(parseChangedFileLine(entry.getValue() + " " + entry.getKey(),
                                repositoryName, baseBranch, branch));
                        }
                        return files;
                    });
            })
            .exceptionally(error -> {
                LOGGER.error("Failed loading stored commit files for review {} in repo {}: {}",
                    reviewId, repositoryName, error.getMessage(), error);
                return new ArrayList<>();
            });
    }

    private CompletableFuture<List<String>> loadChangedFilesForCommit(String repositoryName, String commitHash) {
        if (commitHash == null || commitHash.isBlank()) {
            return CompletableFuture.completedFuture(List.of());
        }
        return git.executeAsync(repositoryName,
                "show", "--name-status", "--pretty=format:", "--root", commitHash)
            .thenApply(output -> Arrays.stream(output.split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .toList())
            .exceptionally(error -> {
                LOGGER.warn("Failed to load changed files for commit {} in repo {}: {}",
                    commitHash, repositoryName, error.getMessage());
                return List.of();
            });
    }

    /**
     * Set the current ReviewContext and notify all listeners.
     */
    public void setReviewContext(ReviewContext reviewContext) {
        this.currentReviewContext = reviewContext;
        LOGGER.info("ReviewContext set: {}", reviewContext != null ? reviewContext.getReviewId() : "null");
        notifyListeners();
    }

    /**
     * Save review metadata to git notes using a parallel batch write.
     * All metadata streams (title, description, author, status, reviewers) are fetched in parallel,
     * written locally, and pushed in a single git push command — replacing the previous sequential
     * per-field fetch-write-push chain.
     *
     * @param reviewContext the updated review context to persist
     * @return future that completes when all metadata has been written and pushed
     */
    public CompletableFuture<Void> saveReviewMetadata(ReviewContext reviewContext) {
        if (reviewContext == null || reviewContext.reviewId == null || reviewContext.reviewId.isEmpty()) {
            LOGGER.warn("Cannot save review - invalid review context");
            return CompletableFuture.completedFuture(null);
        }

        LOGGER.info("Saving review metadata for review: {}", reviewContext.reviewId);

        if (reviewContext.repositories.isEmpty()) {
            LOGGER.warn("No repositories in review context, cannot save");
            return CompletableFuture.completedFuture(null);
        }

        Repository primaryRepo = reviewContext.repositories.getFirst();
        GitReviewNotesManager notesManager = new GitReviewNotesManager(git, primaryRepo.getName());
        String editor = reviewContext.author != null ? reviewContext.author : "system";

        List<Map.Entry<String, com.kalynx.serverlessreviewtool.models.review.ReviewerData>> reviewerEntries = new ArrayList<>();

        for (ReviewerInfo reviewer : reviewContext.reviewers) {
            reviewerEntries.add(Map.entry(
                reviewer.getName(),
                new com.kalynx.serverlessreviewtool.models.review.ReviewerData(
                    reviewer.getStatus().name().toLowerCase(), "")));
        }

        if (currentReviewContext != null) {
            Set<String> newReviewerNames = reviewContext.reviewers.stream()
                .map(ReviewerInfo::getName)
                .collect(Collectors.toSet());

            for (ReviewerInfo previousReviewer : currentReviewContext.reviewers) {
                if (!newReviewerNames.contains(previousReviewer.getName())) {
                    LOGGER.info("Writing LEFT status for removed reviewer {} on review {}",
                        previousReviewer.getName(), reviewContext.reviewId);
                    reviewerEntries.add(Map.entry(
                        previousReviewer.getName(),
                        new com.kalynx.serverlessreviewtool.models.review.ReviewerData("left", "")));
                }
            }
        }

        return notesManager.saveAllMetadataBatch(
                reviewContext.reviewId,
                editor,
                reviewContext.title,
                reviewContext.summary,
                reviewContext.author,
                reviewContext.status.name(),
                reviewerEntries)
            .thenRun(() -> {
                LOGGER.info("Review metadata saved successfully for review: {}", reviewContext.reviewId);
                setReviewContext(reviewContext);
            })
            .exceptionally(error -> {
                LOGGER.error("Failed to save review metadata for review: " + reviewContext.reviewId, error);
                throw new RuntimeException("Failed to save review metadata", error);
            });
    }

    /**
     * Get the current ReviewContext.
     */
    public ReviewContext getReviewContext() {
        return currentReviewContext;
    }

    /**
     * Add a listener for ReviewContext changes.
     * Listener is immediately called with current context.
     */
    public void addListener(Consumer<ReviewContext> listener) {
        listeners.add(listener);
        listener.accept(currentReviewContext);
    }

    public CompletableFuture<Void> addReviewer(String reviewId, String reviewerName, List<String> repositoryNames) {
        if (repositoryNames == null || repositoryNames.isEmpty()) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("Repository names cannot be null or empty"));
        }

        String primaryRepoName = repositoryNames.getFirst();
        GitReviewNotesManager notesManager = new GitReviewNotesManager(git, primaryRepoName);

        com.kalynx.serverlessreviewtool.models.review.ReviewerData reviewerData =
            new com.kalynx.serverlessreviewtool.models.review.ReviewerData("REVIEWING", null);

        LOGGER.info("Adding reviewer {} to review {} in repository {}", reviewerName, reviewId, primaryRepoName);

        return notesManager.writeReviewer(reviewId, reviewerName, reviewerData);
    }

    /**
     * Update a reviewer's decision status for a review.
     *
     * @param reviewId the review identifier
     * @param reviewerName reviewer/editor name
     * @param reviewerStatus target decision status
     * @param repositoryNames repositories containing the review notes
     * @return future completing when reviewer status is written
     */
    public CompletableFuture<Void> updateReviewerStatus(
            String reviewId,
            String reviewerName,
            ReviewerStatus reviewerStatus,
            List<String> repositoryNames) {
        if (repositoryNames == null || repositoryNames.isEmpty()) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("Repository names cannot be null or empty"));
        }
        if (reviewerStatus == null) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("Reviewer status cannot be null"));
        }

        String primaryRepoName = repositoryNames.getFirst();
        GitReviewNotesManager notesManager = new GitReviewNotesManager(git, primaryRepoName);
        String statusValue = mapReviewerStatus(reviewerStatus);

        com.kalynx.serverlessreviewtool.models.review.ReviewerData reviewerData =
            new com.kalynx.serverlessreviewtool.models.review.ReviewerData(statusValue, null);

        LOGGER.info("Updating reviewer {} status to {} for review {} in repository {}",
            reviewerName, reviewerStatus, reviewId, primaryRepoName);

        return notesManager.writeReviewer(reviewId, reviewerName, reviewerData);
    }

    public CompletableFuture<Void> removeReviewer(String reviewId, String reviewerName, List<String> repositoryNames) {
        if (repositoryNames == null || repositoryNames.isEmpty()) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("Repository names cannot be null or empty"));
        }

        String primaryRepoName = repositoryNames.getFirst();
        GitReviewNotesManager notesManager = new GitReviewNotesManager(git, primaryRepoName);

        com.kalynx.serverlessreviewtool.models.review.ReviewerData reviewerData =
            new com.kalynx.serverlessreviewtool.models.review.ReviewerData("LEFT", null);

        LOGGER.info("Removing reviewer {} from review {} in repository {}", reviewerName, reviewId, primaryRepoName);

        return notesManager.writeReviewer(reviewId, reviewerName, reviewerData);
    }

    /**
     * Remove a listener.
     */
    @SuppressWarnings("unused")
    public void removeListener(Consumer<ReviewContext> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        LOGGER.debug("Notifying {} listeners of ReviewContext change", listeners.size());
        listeners.forEach(listener -> listener.accept(currentReviewContext));
    }

    private <T> T getLatestValue(List<StreamEntry<T>> entries) {
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        return entries.getLast().data();
    }

    private ReviewStatus parseStatus(String statusStr) {
        if (statusStr == null) {
            return ReviewStatus.OPEN;
        }
        try {
            return ReviewStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Unknown review status: {}, defaulting to OPEN", statusStr);
            return ReviewStatus.OPEN;
        }
    }

    private boolean isClosedStatus(ReviewStatus status) {
        return status == ReviewStatus.COMPLETED || status == ReviewStatus.CANCELLED;
    }

    private boolean isLeftReviewerStatus(String status) {
        return status != null && "left".equalsIgnoreCase(status.trim());
    }

    private ReviewerStatus parseReviewerStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return ReviewerStatus.REVIEWING;
        }

        String normalized = status.trim().toLowerCase().replace(' ', '_');
        return switch (normalized) {
            case "approved" -> ReviewerStatus.APPROVED;
            case "changes_requested", "rejected" -> ReviewerStatus.CHANGES_REQUESTED;
            case "reviewing", "pending" -> ReviewerStatus.REVIEWING;
            default -> {
                LOGGER.warn("Unknown reviewer status '{}', defaulting to REVIEWING", status);
                yield ReviewerStatus.REVIEWING;
            }
        };
    }

    private String mapReviewerStatus(ReviewerStatus reviewerStatus) {
        return switch (reviewerStatus) {
            case APPROVED -> "approved";
            case CHANGES_REQUESTED -> "changes_requested";
            case REVIEWING -> "reviewing";
        };
    }
}

