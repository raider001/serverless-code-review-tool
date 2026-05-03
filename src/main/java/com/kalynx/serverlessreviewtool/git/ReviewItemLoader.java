package com.kalynx.serverlessreviewtool.git;

import com.kalynx.serverlessreviewtool.models.ReviewItem;
import com.kalynx.serverlessreviewtool.models.ReviewStatus;
import com.kalynx.serverlessreviewtool.models.review.StreamEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ReviewItemLoader {

    private final Git git;
    private static final String NOTES_REF_PREFIX = "refs/notes/reviews/";
    private static final Pattern REVIEW_ID_PATTERN = Pattern.compile("^" + Pattern.quote(NOTES_REF_PREFIX) + "([^/]+)/");

    public ReviewItemLoader(Git git) {
        this.git = git;
    }

    public CompletableFuture<List<ReviewItem>> loadReviewsFromRepository(String repositoryName) {
        return listReviewIds(repositoryName)
            .thenCompose(reviewIds -> loadReviewItems(repositoryName, reviewIds));
    }

    private CompletableFuture<List<String>> listReviewIds(String repositoryName) {
        return git.executeAsync(repositoryName, "show-ref")
            .thenApply(output -> {
                List<String> reviewIds = new ArrayList<>();
                String[] lines = output.split("\n");

                for (String line : lines) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 2) {
                        String ref = parts[1];
                        if (ref.startsWith(NOTES_REF_PREFIX)) {
                            Matcher matcher = REVIEW_ID_PATTERN.matcher(ref);
                            if (matcher.find()) {
                                String reviewId = matcher.group(1);
                                if (!reviewIds.contains(reviewId)) {
                                    reviewIds.add(reviewId);
                                }
                            }
                        }
                    }
                }

                return reviewIds;
            })
            .exceptionally(ex -> {
                return new ArrayList<>();
            });
    }

    private CompletableFuture<List<ReviewItem>> loadReviewItems(String repositoryName, List<String> reviewIds) {
        List<CompletableFuture<ReviewItem>> reviewFutures = reviewIds.stream()
            .map(reviewId -> loadReviewItem(repositoryName, reviewId))
            .collect(Collectors.toList());

        return CompletableFuture.allOf(reviewFutures.toArray(new CompletableFuture[0]))
            .thenApply(ignored -> reviewFutures.stream()
                .map(CompletableFuture::join)
                .filter(item -> item != null)
                .collect(Collectors.toList()));
    }

    private CompletableFuture<ReviewItem> loadReviewItem(String repositoryName, String reviewId) {
        GitReviewNotesManager notesManager = new GitReviewNotesManager(git, repositoryName);

        CompletableFuture<String> titleFuture = notesManager.readTitles(reviewId)
            .thenApply(entries -> {
                String value = getLatestValue(entries);
                return value != null ? value : "Untitled Review";
            })
            .exceptionally(ex -> "Untitled Review");

        CompletableFuture<String> authorFuture = notesManager.readAuthors(reviewId)
            .thenApply(entries -> {
                String value = getLatestValue(entries);
                return value != null ? value : "Unknown";
            })
            .exceptionally(ex -> "Unknown");

        CompletableFuture<String> statusFuture = notesManager.readStatuses(reviewId)
            .thenApply(entries -> {
                String value = getLatestValue(entries);
                return value != null ? value : "OPEN";
            })
            .exceptionally(ex -> "OPEN");

        return CompletableFuture.allOf(titleFuture, authorFuture, statusFuture)
            .thenApply(ignored -> {
                String title = titleFuture.join();
                String author = authorFuture.join();
                String statusStr = statusFuture.join();
                ReviewStatus status = parseStatus(statusStr);
                long lastUpdate = System.currentTimeMillis();

                return new ReviewItem(title, author, repositoryName, status, lastUpdate);
            })
            .exceptionally(ex -> {
                System.err.println("Failed to load review " + reviewId + " from " + repositoryName + ": " + ex.getMessage());
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
}

