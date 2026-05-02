package com.kalynx.serverlessreviewtool.managers;

import com.kalynx.serverlessreviewtool.models.ReviewContext;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * ReviewContextManager - Manages the current review context
 * Manages the active review context and notifies listeners of changes
 */
public class ReviewContextManager {

    private ReviewContext currentReviewContext;

    private final Set<Consumer<ReviewContext>> listeners = new HashSet<>();

    public ReviewContextManager() {
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
        listeners.forEach(listener -> listener.accept(currentReviewContext));
    }
}
