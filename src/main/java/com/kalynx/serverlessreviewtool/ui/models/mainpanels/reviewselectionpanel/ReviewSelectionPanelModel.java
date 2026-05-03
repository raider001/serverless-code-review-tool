package com.kalynx.serverlessreviewtool.ui.models.mainpanels.reviewselectionpanel;

import com.kalynx.serverlessreviewtool.models.ReviewItem;
import com.kalynx.serverlessreviewtool.swingextensions.ComponentModel;

import java.util.ArrayList;
import java.util.List;

public class ReviewSelectionPanelModel {

    public final ComponentModel<List<ReviewItem>> allReviews = new ComponentModel<>();
    public final ComponentModel<List<ReviewItem>> myReviews = new ComponentModel<>();
    public final ComponentModel<List<ReviewItem>> openReviews = new ComponentModel<>();
    public final ComponentModel<List<ReviewItem>> completedReviews = new ComponentModel<>();

    public final ComponentModel<String> titleFilter = new ComponentModel<>();
    public final ComponentModel<String> authorFilter = new ComponentModel<>();
    public final ComponentModel<List<String>> repositoryFilter = new ComponentModel<>();

    public final ComponentModel<ReviewItem> selectedReview = new ComponentModel<>();

    public final ComponentModel<Integer> selectedTabIndex = new ComponentModel<>();

    public final ComponentModel<Boolean> isLoading = new ComponentModel<>();
    public final ComponentModel<String> errorMessage = new ComponentModel<>();

    public ReviewSelectionPanelModel() {
        initializeDefaults();
    }

    private void initializeDefaults() {
        allReviews.setValue(new ArrayList<>());
        myReviews.setValue(new ArrayList<>());
        openReviews.setValue(new ArrayList<>());
        completedReviews.setValue(new ArrayList<>());
        titleFilter.setValue("");
        authorFilter.setValue("");
        repositoryFilter.setValue(new ArrayList<>());
        selectedReview.setValue(null);
        selectedTabIndex.setValue(0);
        isLoading.setValue(false);
        errorMessage.setValue("");
    }

    public void clear() {
        initializeDefaults();
    }

    public void setAllReviews(List<ReviewItem> reviews) {
        allReviews.setValue(new ArrayList<>(reviews));
        applyFiltersToAllLists(reviews);
    }

    public void setFilters(String title, String author, List<String> repositories) {
        titleFilter.setValue(title != null ? title : "");
        authorFilter.setValue(author != null ? author : "");
        repositoryFilter.setValue(repositories != null ? new ArrayList<>(repositories) : new ArrayList<>());
        applyFiltersToAllLists(allReviews.getValue());
    }

    private void applyFiltersToAllLists(List<ReviewItem> reviews) {
        String title = titleFilter.getValue();
        String author = authorFilter.getValue();
        List<String> repos = repositoryFilter.getValue();

        List<ReviewItem> filtered = reviews.stream()
            .filter(r -> matchesFilters(r, title, author, repos))
            .toList();

        myReviews.setValue(filterMyReviews(filtered));
        openReviews.setValue(filterOpenReviews(filtered));
        completedReviews.setValue(filterCompletedReviews(filtered));
    }

    private boolean matchesFilters(ReviewItem review, String title, String author, List<String> repos) {
        boolean titleMatch = title.isEmpty() ||
            review.getTitle().toLowerCase().contains(title.toLowerCase());

        boolean authorMatch = author.isEmpty() ||
            review.getAuthor().toLowerCase().contains(author.toLowerCase());

        boolean repoMatch = repos == null || repos.isEmpty() ||
            repos.contains(review.getRepository());

        return titleMatch && authorMatch && repoMatch;
    }

    private List<ReviewItem> filterMyReviews(List<ReviewItem> reviews) {
        return reviews.stream()
            .filter(r -> isMyReview(r) && !isCompleted(r))
            .toList();
    }

    private List<ReviewItem> filterOpenReviews(List<ReviewItem> reviews) {
        return reviews.stream()
            .filter(r -> !isMyReview(r) && !isCompleted(r))
            .toList();
    }

    private List<ReviewItem> filterCompletedReviews(List<ReviewItem> reviews) {
        return reviews.stream()
            .filter(this::isCompleted)
            .toList();
    }

    private boolean isMyReview(ReviewItem review) {
        return "You".equals(review.getAuthor());
    }

    private boolean isCompleted(ReviewItem review) {
        return review.getStatus() == com.kalynx.serverlessreviewtool.models.ReviewStatus.COMPLETED;
    }

    public void selectReview(ReviewItem review) {
        selectedReview.setValue(review);
    }

    public void setLoadingState(boolean loading) {
        isLoading.setValue(loading);
        if (loading) {
            errorMessage.setValue("");
        }
    }

    public void setError(String error) {
        errorMessage.setValue(error);
        isLoading.setValue(false);
    }

    public boolean hasReviews() {
        return !allReviews.getValue().isEmpty();
    }

    public int getMyReviewsCount() {
        return myReviews.getValue().size();
    }

    public int getOpenReviewsCount() {
        return openReviews.getValue().size();
    }

    public int getCompletedReviewsCount() {
        return completedReviews.getValue().size();
    }

    public void setSelectedTab(int index) {
        selectedTabIndex.setValue(index);
    }

    public List<ReviewItem> getCurrentTabReviews() {
        return switch (selectedTabIndex.getValue()) {
            case 0 -> myReviews.getValue();
            case 1 -> openReviews.getValue();
            case 2 -> completedReviews.getValue();
            default -> new ArrayList<>();
        };
    }
}
