package com.kalynx.serverlessreviewtool.managers;

import com.kalynx.serverlessreviewtool.git.ReviewItemLoader;
import com.kalynx.serverlessreviewtool.models.Repository;
import com.kalynx.serverlessreviewtool.models.ReviewItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        List<Repository> repositories = repositoryManager.getRepositories();

        List<CompletableFuture<List<ReviewItem>>> futures = repositories.stream()
            .map(repo -> reviewItemLoader.loadReviewsFromRepository(repo.getName()))
            .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(ignored -> {
                List<ReviewItem> allReviews = new ArrayList<>();
                for (CompletableFuture<List<ReviewItem>> future : futures) {
                    allReviews.addAll(future.join());
                }
                return allReviews;
            })
            .thenAccept(this::updateReviewItems);
    }

    public void updateReviewItems(List<ReviewItem> reviewItems) {
        this.reviewItems = reviewItems;
        notifyListeners();
    }

    public List<ReviewItem> getReviewItems() {
        return List.copyOf(reviewItems);
    }

    public void addListener(Consumer<List<ReviewItem>> listener) {
        listeners.add(listener);
        listener.accept(List.copyOf(reviewItems));
    }

    public void removeListener(Consumer<List<ReviewItem>> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        LOGGER.info("Notifying listeners of review item list update: {} items", reviewItems.size());
        listeners.forEach(listener -> listener.accept(List.copyOf(reviewItems)));
    }

}
