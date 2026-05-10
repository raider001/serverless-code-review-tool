package com.kalynx.serverlessreviewtool.managers;

import com.kalynx.serverlessreviewtool.git.ReviewItemLoader;
import com.kalynx.serverlessreviewtool.models.Repository;
import com.kalynx.serverlessreviewtool.models.ReviewItem;
import com.kalynx.serverlessreviewtool.theme.LoadingStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ReviewItemManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewItemManager.class);

    private List<ReviewItem> reviewItems = new ArrayList<>();

    private final Set<Consumer<List<ReviewItem>>> listeners = new HashSet<>();
    private final ReviewItemLoader reviewItemLoader;
    private final RepositoryManager repositoryManager;

    public ReviewItemManager(ReviewItemLoader reviewItemLoader, RepositoryManager repositoryManager) {
        this.reviewItemLoader = reviewItemLoader;
        this.repositoryManager = repositoryManager;
    }

    public CompletableFuture<Void> refresh() {
        LoadingStateManager.getInstance().startLoading("refresh-review-items");
        List<Repository> repositories = repositoryManager.getRepositories();

        List<CompletableFuture<List<ReviewItem>>> futures = repositories.stream()
            .map(repo -> reviewItemLoader.loadReviewsFromRepository(repo.getName()))
            .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(ignored -> {
                Map<String, ReviewItem> reviewMap = new HashMap<>();

                for (CompletableFuture<List<ReviewItem>> future : futures) {
                    List<ReviewItem> repoReviews = future.join();

                    for (ReviewItem review : repoReviews) {
                        String reviewId = review.getReviewId();

                        if (reviewMap.containsKey(reviewId)) {
                            ReviewItem existing = reviewMap.get(reviewId);
                            List<String> mergedRepos = new ArrayList<>(existing.getRepositories());
                            mergedRepos.addAll(review.getRepositories());

                            String primaryRepo = determinePrimaryRepository(existing, review);
                            String mergedBranch = review.getBranch() != null ? review.getBranch() : existing.getBranch();
                            String mergedBaseBranch = review.getBaseBranch() != null ? review.getBaseBranch() : existing.getBaseBranch();

                            ReviewItem merged = new ReviewItem(
                                reviewId,
                                review.getTitle(),
                                review.getAuthor(),
                                primaryRepo,
                                mergedRepos,
                                review.getStatus(),
                                Math.max(existing.getLastUpdate(), review.getLastUpdate()),
                                review.getReviewers(),
                                mergedBranch,
                                mergedBaseBranch
                            );
                            reviewMap.put(reviewId, merged);

                            LOGGER.debug("Merged review '{}' from additional repository. Primary: {}, Total repos: {}",
                                reviewId, primaryRepo, mergedRepos.size());
                        } else {
                            reviewMap.put(reviewId, review);
                        }
                    }
                }

                return new ArrayList<>(reviewMap.values());
            })
            .thenAccept(items -> {
                updateReviewItems(items);
                LoadingStateManager.getInstance().stopLoading("refresh-review-items");
            });
    }

    private String determinePrimaryRepository(ReviewItem existing, ReviewItem incoming) {
        String existingPrimary = existing.getPrimaryRepository();
        String incomingPrimary = incoming.getPrimaryRepository();

        if (existingPrimary == null && incomingPrimary == null) {
            return existing.getRepositories().isEmpty() ? null : existing.getRepositories().getFirst();
        }

        if (existingPrimary != null && incomingPrimary != null && !existingPrimary.equals(incomingPrimary)) {
            LOGGER.warn("Review '{}' has conflicting primary repositories: '{}' vs '{}'. Using '{}'.",
                existing.getReviewId(), existingPrimary, incomingPrimary, existingPrimary);
        }

        return existingPrimary != null ? existingPrimary : incomingPrimary;
    }

    public void updateReviewItems(List<ReviewItem> reviewItems) {
        this.reviewItems = reviewItems;
        notifyListeners();
    }

    public void addListener(Consumer<List<ReviewItem>> listener) {
        listeners.add(listener);
        listener.accept(List.copyOf(reviewItems));
    }

    public void removeListener(Consumer<List<ReviewItem>> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        LOGGER.debug("Notifying listeners of review item list update: {} items", reviewItems.size());
        listeners.forEach(listener -> listener.accept(List.copyOf(reviewItems)));
    }

}
