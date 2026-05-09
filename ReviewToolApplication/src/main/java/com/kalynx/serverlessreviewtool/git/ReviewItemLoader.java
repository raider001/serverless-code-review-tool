package com.kalynx.serverlessreviewtool.git;

import com.kalynx.serverlessreviewtool.models.ReviewItem;
import com.kalynx.serverlessreviewtool.models.ReviewStatus;
import com.kalynx.serverlessreviewtool.models.review.StreamEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
            .toList();

        return CompletableFuture.allOf(reviewFutures.toArray(new CompletableFuture[0]))
            .thenApply(ignored -> reviewFutures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    private CompletableFuture<ReviewItem> loadReviewItem(String repositoryName, String reviewId) {
        GitReviewNotesManager notesManager = new GitReviewNotesManager(git, repositoryName);

        CompletableFuture<List<StreamEntry<String>>> titleFuture = notesManager.readTitles(reviewId)
            .exceptionally(ex -> new ArrayList<>());

        CompletableFuture<List<StreamEntry<String>>> authorFuture = notesManager.readAuthors(reviewId)
            .exceptionally(ex -> new ArrayList<>());

        CompletableFuture<List<StreamEntry<String>>> primaryRepoFuture = notesManager.readPrimaryRepository(reviewId)
            .exceptionally(ex -> new ArrayList<>());

        CompletableFuture<List<StreamEntry<String>>> statusFuture = notesManager.readStatuses(reviewId)
            .exceptionally(ex -> new ArrayList<>());

        CompletableFuture<List<StreamEntry<com.kalynx.serverlessreviewtool.models.review.ReviewerData>>> reviewersFuture = notesManager.readReviewers(reviewId)
            .exceptionally(ex -> new ArrayList<>());

        return CompletableFuture.allOf(titleFuture, authorFuture, primaryRepoFuture, statusFuture, reviewersFuture)
            .thenApply(ignored -> {
                List<StreamEntry<String>> titleEntries = titleFuture.join();
                List<StreamEntry<String>> authorEntries = authorFuture.join();
                List<StreamEntry<String>> statusEntries = statusFuture.join();
                List<StreamEntry<com.kalynx.serverlessreviewtool.models.review.ReviewerData>> reviewerEntries = reviewersFuture.join();

                String title = getLatestValue(titleEntries);
                if (title == null) title = "Untitled Review";

                String author = getLatestValue(authorEntries);
                if (author == null) author = "Unknown";

                String primaryRepo = getLatestValue(primaryRepoFuture.join());
                if (primaryRepo == null) primaryRepo = repositoryName;

                String statusStr = getLatestValue(statusEntries);
                if (statusStr == null) statusStr = "OPEN";

                List<String> reviewers = reviewerEntries.stream()
                    .map(StreamEntry::editor)
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());

                ReviewStatus status = parseStatus(statusStr);

                long lastUpdate = getMostRecentTimestamp(titleEntries, authorEntries, statusEntries, reviewerEntries);

                return new ReviewItem(reviewId, title, author, primaryRepo, List.of(repositoryName), status, lastUpdate, reviewers);
            })
            .exceptionally(ex -> {
                System.err.println("Failed to load review " + reviewId + " from " + repositoryName + ": " + ex.getMessage());
                return null;
            });
    }

    private long getMostRecentTimestamp(List<StreamEntry<String>> titleEntries,
                                        List<StreamEntry<String>> authorEntries,
                                        List<StreamEntry<String>> statusEntries,
                                        List<StreamEntry<com.kalynx.serverlessreviewtool.models.review.ReviewerData>> reviewerEntries) {
        long mostRecent = 0;

        mostRecent = Math.max(mostRecent, getLatestTimestamp(titleEntries));
        mostRecent = Math.max(mostRecent, getLatestTimestamp(authorEntries));
        mostRecent = Math.max(mostRecent, getLatestTimestamp(statusEntries));
        mostRecent = Math.max(mostRecent, getLatestTimestamp(reviewerEntries));

        return mostRecent > 0 ? mostRecent : System.currentTimeMillis();
    }

    private <T> long getLatestTimestamp(List<StreamEntry<T>> entries) {
        if (entries == null || entries.isEmpty()) {
            return 0;
        }
        return entries.stream()
            .filter(entry -> entry != null && entry.timestamp() != null)
            .mapToLong(entry -> entry.timestamp().toEpochMilli())
            .max()
            .orElse(0);
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
            return ReviewStatus.OPEN;
        }
    }
}

