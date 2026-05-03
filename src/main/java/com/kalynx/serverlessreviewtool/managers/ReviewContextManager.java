package com.kalynx.serverlessreviewtool.managers;

import com.kalynx.serverlessreviewtool.git.Git;
import com.kalynx.serverlessreviewtool.git.GitReviewNotesManager;
import com.kalynx.serverlessreviewtool.models.ReviewContext;
import com.kalynx.serverlessreviewtool.models.ReviewStatus;
import com.kalynx.serverlessreviewtool.models.ReviewerInfo;
import com.kalynx.serverlessreviewtool.models.Repository;
import com.kalynx.serverlessreviewtool.models.review.StreamEntry;
import com.kalynx.serverlessreviewtool.ui.models.mainpanels.reviewpanel.ReviewPanelModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ReviewContextManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewContextManager.class);

    private ReviewContext currentReviewContext;
    private ReviewPanelModel reviewPanelModel;
    private FileDiffManager fileDiffManager;
    private final Git git;
    private final RepositoryManager repositoryManager;

    private final Set<Consumer<ReviewContext>> listeners = new HashSet<>();

    public ReviewContextManager(Git git, RepositoryManager repositoryManager) {
        this.git = git;
        this.repositoryManager = repositoryManager;
    }

    public void setReviewPanelModel(ReviewPanelModel model) {
        this.reviewPanelModel = model;
    }

    public void setFileDiffManager(FileDiffManager fileDiffManager) {
        this.fileDiffManager = fileDiffManager;
    }

    public CompletableFuture<Void> loadReview(String reviewId, String repositoryName) {
        if (reviewId == null || reviewId.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        if (reviewPanelModel != null) {
            reviewPanelModel.setCurrentReview(reviewId);
        }

        GitReviewNotesManager notesManager = new GitReviewNotesManager(git, repositoryName);

        CompletableFuture<String> titleFuture = notesManager.readTitles(reviewId)
            .thenApply(entries -> {
                System.out.println("DEBUG - readTitles returned " + (entries == null ? "null" : entries.size() + " entries"));
                String value = getLatestValue(entries);
                System.out.println("DEBUG - title getLatestValue returned: '" + value + "'");
                return value;
            })
            .exceptionally(ex -> "Untitled Review");

        CompletableFuture<String> descriptionFuture = notesManager.readDescriptions(reviewId)
            .thenApply(entries -> {
                System.out.println("DEBUG - readDescriptions returned " + (entries == null ? "null" : entries.size() + " entries"));
                if (entries != null && !entries.isEmpty()) {
                    for (int i = 0; i < entries.size(); i++) {
                        System.out.println("  Entry " + i + ": " + entries.get(i));
                    }
                }
                String value = getLatestValue(entries);
                System.out.println("DEBUG - getLatestValue returned: '" + value + "'");
                return value != null ? value : "";
            })
            .exceptionally(ex -> {
                System.err.println("DEBUG - readDescriptions failed: " + ex.getMessage());
                ex.printStackTrace();
                return "";
            });

        CompletableFuture<String> authorFuture = notesManager.readAuthors(reviewId)
            .thenApply(this::getLatestValue)
            .exceptionally(ex -> "Unknown");

        CompletableFuture<String> statusFuture = notesManager.readStatuses(reviewId)
            .thenApply(this::getLatestValue)
            .exceptionally(ex -> "OPEN");

        CompletableFuture<List<String>> reviewersFuture = notesManager.readReviewers(reviewId)
            .thenApply(entries -> entries.stream()
                .map(StreamEntry::editor)
                .distinct()
                .toList())
            .exceptionally(ex -> new ArrayList<>());

        return CompletableFuture.allOf(titleFuture, descriptionFuture, authorFuture, statusFuture, reviewersFuture)
            .thenAccept(ignored -> {
                String title = titleFuture.join();
                String description = descriptionFuture.join();
                String author = authorFuture.join();
                String statusStr = statusFuture.join();
                List<String> reviewerNames = reviewersFuture.join();

                System.out.println("DEBUG - ReviewContextManager.loadReview:");
                System.out.println("  reviewId: " + reviewId);
                System.out.println("  title: " + title);
                System.out.println("  description: " + description);
                System.out.println("  author: " + author);
                System.out.println("  status: " + statusStr);
                System.out.println("  reviewers: " + reviewerNames);

                ReviewStatus status = parseStatus(statusStr);
                List<ReviewerInfo> reviewers = reviewerNames.stream()
                    .map(ReviewerInfo::new)
                    .toList();

                List<Repository> repositories = repositoryManager.getRepositories();

                ReviewContext reviewContext = new ReviewContext(
                    reviewId,
                    title,
                    description,
                    author,
                    status,
                    reviewers,
                    repositories,
                    new ArrayList<>()
                );

                System.out.println("DEBUG - Created ReviewContext:");
                System.out.println("  summary field: " + reviewContext.summary);

                setReviewContext(reviewContext);

                if (fileDiffManager != null && !repositories.isEmpty()) {
                    git.getDefaultBranch(repositoryName)
                        .thenAccept(defaultBranch -> {
                            fileDiffManager.loadCommitsForReview(repositoryName, defaultBranch, 50);
                        })
                        .exceptionally(error -> {
                            System.err.println("Failed to get default branch, trying 'main': " + error.getMessage());
                            fileDiffManager.loadCommitsForReview(repositoryName, "main", 50);
                            return null;
                        });
                }
            })
            .exceptionally(ex -> {
                if (reviewPanelModel != null) {
                    reviewPanelModel.setError("Failed to load review: " + ex.getMessage());
                }
                System.err.println("Failed to load review " + reviewId + ": " + ex.getMessage());
                return null;
            });
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
            return ReviewStatus.OPEN;
        }
    }

    public void setReviewContext(ReviewContext reviewContext) {
        this.currentReviewContext = reviewContext;
        notifyListeners();
    }

    public ReviewContext getReviewContext() {
        return currentReviewContext;
    }

    public void addListener(Consumer<ReviewContext> listener) {
        listeners.add(listener);
        listener.accept(currentReviewContext);
    }

    public void removeListener(Consumer<ReviewContext> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        LOGGER.info("Notifying listeners of review context update: {}", currentReviewContext);
        listeners.forEach(listener -> listener.accept(currentReviewContext));
    }
}


