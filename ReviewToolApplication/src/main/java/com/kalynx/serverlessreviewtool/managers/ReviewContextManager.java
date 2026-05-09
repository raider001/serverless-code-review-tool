package com.kalynx.serverlessreviewtool.managers;

import com.kalynx.serverlessreviewtool.git.Git;
import com.kalynx.serverlessreviewtool.git.GitReviewNotesManager;
import com.kalynx.serverlessreviewtool.models.FileChangeType;
import com.kalynx.serverlessreviewtool.models.ReviewContext;
import com.kalynx.serverlessreviewtool.models.ReviewFile;
import com.kalynx.serverlessreviewtool.models.ReviewStatus;
import com.kalynx.serverlessreviewtool.models.ReviewerInfo;
import com.kalynx.serverlessreviewtool.models.Repository;
import com.kalynx.serverlessreviewtool.models.review.StreamEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
 *
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

        Repository primaryRepo = allRepositories.stream()
            .filter(repo -> {
                GitReviewNotesManager notesManager = new GitReviewNotesManager(git, repo.getName());
                try {
                    List<StreamEntry<String>> titles = notesManager.readTitles(reviewId).get();
                    return titles != null && !titles.isEmpty();
                } catch (Exception e) {
                    return false;
                }
            })
            .findFirst()
            .orElse(allRepositories.getFirst());

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
            })
            .exceptionally(error -> {
                LOGGER.error("Failed to load comments for review: {}", reviewId, error);
                return new ArrayList<com.kalynx.serverlessreviewtool.models.ReviewComment>();
            });
    }

    private CompletableFuture<com.kalynx.serverlessreviewtool.models.ReviewComment> loadSingleComment(
            GitReviewNotesManager notesManager, String reviewId, String commentId) {

        return notesManager.readCommentMetadata(reviewId, commentId)
            .thenCombine(notesManager.readCommentText(reviewId, commentId),
                (metadata, textEntries) -> {
                    if (metadata.isEmpty() || textEntries.isEmpty()) {
                        return null;
                    }

                    StreamEntry<GitReviewNotesManager.CommentMetadata> latestMetadata = metadata.get(metadata.size() - 1);
                    StreamEntry<GitReviewNotesManager.CommentTextData> firstText = textEntries.get(0);

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
                                statusEntries.get(statusEntries.size() - 1);
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
                return loadReviewFromRepositories(reviewId, repositoryName, allRepositories);
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
                return loadReviewFromRepositories(reviewId, repositoryName, specificRepositories);
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

    private CompletableFuture<ReviewContext> loadReviewFromRepositories(String reviewId, String repositoryName, List<Repository> allRepositories) {
        LOGGER.info("Loading review metadata: reviewId={}, repository={}", reviewId, repositoryName);

        GitReviewNotesManager notesManager = new GitReviewNotesManager(git, repositoryName);

        return notesManager.readAllMetadata(reviewId)
            .thenCompose(metadata -> {
                String title = getLatestValue(metadata.titles());
                String description = getLatestValue(metadata.descriptions());
                String author = getLatestValue(metadata.authors());
                String statusStr = getLatestValue(metadata.statuses());

                List<String> reviewerNames = metadata.reviewers().stream()
                    .collect(Collectors.groupingBy(
                        StreamEntry::editor,
                        Collectors.maxBy((e1, e2) -> e1.timestamp().compareTo(e2.timestamp()))
                    ))
                    .values().stream()
                    .filter(opt -> opt.isPresent() && !"LEFT".equalsIgnoreCase(opt.get().data().getStatus()))
                    .map(opt -> opt.get().editor())
                    .distinct()
                    .toList();

                if (title == null) title = "Untitled Review";
                if (description == null) description = "";
                if (author == null) author = "Unknown";
                if (statusStr == null) statusStr = "OPEN";

                LOGGER.info("Loaded review metadata: reviewId={}, title={}, author={}, reviewers={}",
                    reviewId, title, author, reviewerNames.size());

                ReviewStatus status = parseStatus(statusStr);
                List<ReviewerInfo> reviewers = reviewerNames.stream()
                    .map(ReviewerInfo::new)
                    .toList();

                List<Repository> reviewRepositories = new ArrayList<>();

                for (Repository repo : allRepositories) {
                    GitReviewNotesManager repoNotesManager = new GitReviewNotesManager(git, repo.getName());
                    try {
                        List<StreamEntry<String>> titles = repoNotesManager.readTitles(reviewId).get();
                        if (titles != null && !titles.isEmpty()) {
                            LOGGER.info("Review {} found in repository: {}", reviewId, repo.getName());
                            reviewRepositories.add(repo);
                        }
                    } catch (Exception e) {
                        LOGGER.debug("Review {} not found in repository {}", reviewId, repo.getName());
                    }
                }

                if (reviewRepositories.isEmpty()) {
                    Repository fallbackRepo = repositoryManager.getRepositoryByName(repositoryName);
                    if (fallbackRepo != null) {
                        reviewRepositories.add(fallbackRepo);
                    }
                    LOGGER.warn("Review {} not found in any repository, using fallback: {}", reviewId, repositoryName);
                }

                LOGGER.info("Review {} found in {} repositories", reviewId, reviewRepositories.size());

                String finalTitle = title;
                String finalDescription = description;
                String finalAuthor = author;

                return loadCommentsForReview(reviewId)
                    .thenApply(comments -> {
                        LOGGER.info("Loaded {} comments for review {}", comments.size(), reviewId);

                        ReviewContext reviewContext = new ReviewContext(
                            reviewId,
                            finalTitle,
                            finalDescription,
                            finalAuthor,
                            status,
                            reviewers,
                            reviewRepositories,
                            comments
                        );

                        setReviewContext(reviewContext);
                        return reviewContext;
                    });
            }).exceptionally(error -> {
                LOGGER.error("Failed to load review metadata for {}: {}", reviewId, error.getMessage(), error);
                return null;
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

    public CompletableFuture<List<ReviewFile>> loadFilesForAllRepositories(
            List<Repository> repositories,
            String reviewBranch,
            String baseBranch) {

        if (repositories == null || repositories.isEmpty()) {
            LOGGER.warn("No repositories provided for loading files");
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        LOGGER.info("Loading files from {} repositories", repositories.size());

        List<CompletableFuture<List<ReviewFile>>> fileFutures = repositories.stream()
            .map(repo -> loadFilesForReview(repo.getName(), reviewBranch, baseBranch))
            .toList();

        return CompletableFuture.allOf(fileFutures.toArray(new CompletableFuture[0]))
            .thenApply(ignored -> {
                List<ReviewFile> allFiles = new ArrayList<>();
                for (CompletableFuture<List<ReviewFile>> future : fileFutures) {
                    allFiles.addAll(future.join());
                }

                LOGGER.info("Loaded total of {} files across all repositories", allFiles.size());
                return allFiles;
            })
            .exceptionally(error -> {
                LOGGER.error("Failed to load files from all repositories: {}", error.getMessage(), error);
                return new ArrayList<>();
            });
    }

    public CompletableFuture<List<ReviewFile>> loadFilesFromReviewCommits(String reviewId, List<Repository> repositories) {
        if (repositories == null || repositories.isEmpty()) {
            LOGGER.warn("No repositories provided for loading files");
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        LOGGER.info("Loading files from review branches for {} repositories", repositories.size());

        Repository primaryRepo = repositories.getFirst();
        GitReviewNotesManager notesManager = new GitReviewNotesManager(git, primaryRepo.getName());

        return notesManager.readAllMetadata(reviewId)
            .thenCompose(metadata -> {
                String branch = getLatestValue(metadata.branches());
                String baseBranch = getLatestValue(metadata.baseBranches());

                LOGGER.info("=== FILE LOADING DEBUG ===");
                LOGGER.info("Review ID: {}", reviewId);
                LOGGER.info("Branch: {}", branch);
                LOGGER.info("BaseBranch: {}", baseBranch);
                LOGGER.info("Number of repositories: {}", repositories.size());

                if (branch == null || baseBranch == null) {
                    LOGGER.warn("Missing branch or baseBranch metadata for review {}", reviewId);
                    return CompletableFuture.completedFuture(new ArrayList<ReviewFile>());
                }

                LOGGER.info("Loading files from branch '{}' compared to '{}'", branch, baseBranch);

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
                            for (ReviewFile file : repoFiles) {
                                LOGGER.info("  - {}", file.getPath());
                            }
                            allFiles.addAll(repoFiles);
                        }

                        LOGGER.info("=== TOTAL: {} files loaded from review branches across all repositories ===", allFiles.size());
                        return allFiles;
                    });
            })
            .exceptionally(error -> {
                LOGGER.error("Failed to load files from review branches: {}", error.getMessage(), error);
                return new ArrayList<ReviewFile>();
            });
    }

    private CompletableFuture<List<ReviewFile>> loadFilesForReviewFromCommits(String reviewId, String repositoryName) {
        LOGGER.info("Loading files from review commits for repository '{}'", repositoryName);

        GitReviewNotesManager notesManager = new GitReviewNotesManager(git, repositoryName);

        return notesManager.readCommits(reviewId)
            .thenCompose(commitEntries -> {
                if (commitEntries == null || commitEntries.isEmpty()) {
                    LOGGER.warn("No commits found for review {} in repository {}", reviewId, repositoryName);
                    return CompletableFuture.completedFuture(new ArrayList<>());
                }

                List<String> commits = commitEntries.get(commitEntries.size() - 1).data();

                if (commits == null || commits.isEmpty()) {
                    LOGGER.warn("Empty commits list for review {} in repository {}", reviewId, repositoryName);
                    return CompletableFuture.completedFuture(new ArrayList<>());
                }

                LOGGER.info("Found {} commits for review {} in repository {}", commits.size(), reviewId, repositoryName);

                String firstCommit = commits.get(commits.size() - 1);
                String lastCommit = commits.get(0);

                String compareBase = firstCommit + "^";

                return git.listChangedFiles(repositoryName, compareBase, lastCommit)
                    .thenApply(changedFilePaths -> {
                        List<ReviewFile> reviewFiles = changedFilePaths.stream()
                            .map(filePath -> new ReviewFile(filePath, repositoryName, FileChangeType.MODIFIED))
                            .toList();

                        LOGGER.info("Loaded {} files from commits in repository '{}'",
                            reviewFiles.size(), repositoryName);

                        return reviewFiles;
                    })
                    .exceptionally(error -> {
                        LOGGER.error("Failed to load files from commits in repository '{}': {}",
                            repositoryName, error.getMessage(), error);
                        return new ArrayList<>();
                    });
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

    public CompletableFuture<Void> saveReviewMetadata(ReviewContext reviewContext) {
        if (reviewContext == null || reviewContext.reviewId == null || reviewContext.reviewId.isEmpty()) {
            LOGGER.warn("Cannot save review - invalid review context");
            return CompletableFuture.completedFuture(null);
        }

        LOGGER.info("Saving review metadata for review: {}", reviewContext.reviewId);

        if (reviewContext.repositories == null || reviewContext.repositories.isEmpty()) {
            LOGGER.warn("No repositories in review context, cannot save");
            return CompletableFuture.completedFuture(null);
        }

        Repository primaryRepo = reviewContext.repositories.getFirst();
        GitReviewNotesManager notesManager = new GitReviewNotesManager(git, primaryRepo.getName());
        String editor = reviewContext.author != null ? reviewContext.author : "system";

        CompletableFuture<Void> saveFuture = notesManager.writeReviewTitle(reviewContext.reviewId, editor, reviewContext.title)
            .thenCompose(ignored -> notesManager.writeReviewDescription(reviewContext.reviewId, editor, reviewContext.summary))
            .thenCompose(ignored -> notesManager.writeReviewAuthor(reviewContext.reviewId, editor, reviewContext.author))
            .thenCompose(ignored -> notesManager.writeReviewStatus(reviewContext.reviewId, editor, reviewContext.status.name()));

        for (ReviewerInfo reviewer : reviewContext.reviewers) {
            saveFuture = saveFuture.thenCompose(ignored -> {
                com.kalynx.serverlessreviewtool.models.review.ReviewerData reviewerData =
                    new com.kalynx.serverlessreviewtool.models.review.ReviewerData(
                        reviewer.getStatus().name().toLowerCase(),
                        ""
                    );
                return notesManager.writeReviewer(reviewContext.reviewId, reviewer.getName(), reviewerData);
            });
        }

        return saveFuture.thenRun(() -> {
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
     * Clear the current ReviewContext.
     */
    public void clearReviewContext() {
        this.currentReviewContext = null;
        LOGGER.info("ReviewContext cleared");
        notifyListeners();
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
        return entries.get(entries.size() - 1).data();
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
}

