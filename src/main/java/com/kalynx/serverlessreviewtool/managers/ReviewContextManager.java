package com.kalynx.serverlessreviewtool.managers;

import com.kalynx.serverlessreviewtool.models.ReviewContext;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * ReviewContextManager - Singleton manager for the current review context
 * Manages the active review context and notifies listeners of changes
 */
public class ReviewContextManager {

    private static ReviewContextManager INSTANCE;

    private ReviewContext currentReviewContext;

    private final Set<Consumer<ReviewContext>> listeners = new HashSet<>();

    public static synchronized ReviewContextManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ReviewContextManager();
        }
        return INSTANCE;
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
