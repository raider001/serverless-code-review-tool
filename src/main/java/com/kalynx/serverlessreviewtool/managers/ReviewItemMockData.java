package com.kalynx.serverlessreviewtool.managers;

import com.kalynx.serverlessreviewtool.models.ReviewItem;
import com.kalynx.serverlessreviewtool.ui.review.model.ReviewStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * ReviewItemMockData - Provides mock review data for development and testing
 * Populates the ReviewItemManager with sample reviews
 */
public class ReviewItemMockData {

    /**
     * Load mock review data into the ReviewItemManager
     * Call this once at application startup for testing/development
     */
    public static void loadMockData() {
        List<ReviewItem> mockReviews = createMockReviews();
        ReviewItemManager.getInstance().updateReviewItems(mockReviews);
    }

    /**
     * Create a list of mock review items with timestamps that demonstrate all time format ranges:
     * - Minutes only (< 1 hour)
     * - Hours + minutes (1-23 hours)
     * - Days only (1-7 days)
     * - Absolute date (> 7 days)
     * @return List of mock ReviewItem objects
     */
    private static List<ReviewItem> createMockReviews() {
        List<ReviewItem> reviews = new ArrayList<>();

        // Use current time as base to ensure all timestamps are in the past
        long baseTime = System.currentTimeMillis();

        // My Reviews (authored by "You")
        // Demonstrate: 3 days ago
        reviews.add(new ReviewItem(
            "Update database migration scripts", "You", "backend-api",
            ReviewStatus.OPEN, baseTime - (3L * 24 * 60 * 60 * 1000)));

        // Demonstrate: 1 hour 20 minutes ago
        reviews.add(new ReviewItem(
            "Add unit tests for user service", "You", "backend-api",
            ReviewStatus.COMPLETED, baseTime - (1L * 60 * 60 * 1000 + 20L * 60 * 1000)));

        // Demonstrate: Last updated (15 days = 2+ weeks ago)
        reviews.add(new ReviewItem(
            "Refactor authentication middleware", "You", "shared-lib",
            ReviewStatus.CHANGES_REQUESTED, baseTime - (15L * 24 * 60 * 60 * 1000)));

        // Demonstrate: 6 days ago
        reviews.add(new ReviewItem(
            "Optimize database queries", "You", "backend-api",
            ReviewStatus.OPEN, baseTime - (6L * 24 * 60 * 60 * 1000)));

        // Open Reviews (by others, needing review)
        // Demonstrate: 30 minutes ago
        reviews.add(new ReviewItem(
            "Add OAuth2 authentication flow", "John Doe", "backend-api",
            ReviewStatus.OPEN, baseTime - (30L * 60 * 1000)));

        // Demonstrate: 5 hours 45 minutes ago
        reviews.add(new ReviewItem(
            "Implement responsive navigation menu", "Jane Smith", "frontend-app",
            ReviewStatus.OPEN, baseTime - (5L * 60 * 60 * 1000 + 45L * 60 * 1000)));

        // Demonstrate: 1 day ago
        reviews.add(new ReviewItem(
            "Fix memory leak in image processing", "Bob Johnson", "mobile-app",
            ReviewStatus.OPEN, baseTime - (1L * 24 * 60 * 60 * 1000)));

        // Demonstrate: 45 minutes ago
        reviews.add(new ReviewItem(
            "Implement dark mode support", "Eve Anderson", "frontend-app",
            ReviewStatus.OPEN, baseTime - (45L * 60 * 1000)));

        // Demonstrate: 12 hours ago (no minutes, since minutes would be 0)
        reviews.add(new ReviewItem(
            "Add GraphQL API endpoints", "Michael Scott", "backend-api",
            ReviewStatus.OPEN, baseTime - (12L * 60 * 60 * 1000)));

        // Completed Reviews
        // Demonstrate: 2 days ago
        reviews.add(new ReviewItem(
            "Implement user profile editing", "Sarah Connor", "frontend-app",
            ReviewStatus.COMPLETED, baseTime - (2L * 24 * 60 * 60 * 1000)));

        // Demonstrate: 3 hours 10 minutes ago
        reviews.add(new ReviewItem(
            "Fix security vulnerability in auth", "James Bond", "backend-api",
            ReviewStatus.COMPLETED, baseTime - (3L * 60 * 60 * 1000 + 10L * 60 * 1000)));

        // Demonstrate: Last updated (30 days ago)
        reviews.add(new ReviewItem(
            "Add Redis caching layer", "Tony Stark", "backend-api",
            ReviewStatus.COMPLETED, baseTime - (30L * 24 * 60 * 60 * 1000)));

        return reviews;
    }

    /**
     * Refresh the mock data (useful for testing refresh functionality)
     */
    public static void refreshMockData() {
        loadMockData();
        System.out.println("Mock review data refreshed");
    }
}

