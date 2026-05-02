package com.kalynx.serverlessreviewtool.managers;

import com.kalynx.serverlessreviewtool.models.ReviewItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ReviewItemManager {

    private List<ReviewItem> reviewItems = new ArrayList<>();

    private final Set<Consumer<List<ReviewItem>>> listeners = new HashSet<>();

    public ReviewItemManager() {
    }

    public void updateReviewItems(List<ReviewItem> reviewItems) {
        this.reviewItems = reviewItems;
        notifyListeners();
    }

    public void addListener(Consumer<List<ReviewItem>> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<List<ReviewItem>> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        listeners.forEach(listener -> listener.accept(List.copyOf(reviewItems)));
    }

}
