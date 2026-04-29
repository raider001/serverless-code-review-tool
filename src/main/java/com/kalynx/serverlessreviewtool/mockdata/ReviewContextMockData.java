package com.kalynx.serverlessreviewtool.mockdata;

import com.kalynx.serverlessreviewtool.managers.ReviewContextManager;
import com.kalynx.serverlessreviewtool.models.ReviewContext;
import com.kalynx.serverlessreviewtool.models.Repository;
import com.kalynx.serverlessreviewtool.models.ReviewComment;
import com.kalynx.serverlessreviewtool.models.ReviewStatus;
import com.kalynx.serverlessreviewtool.models.ReviewerInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * ReviewContextMockData - Provides mock review context data for development and testing
 * Populates the ReviewContextManager with a sample review
 */
public class ReviewContextMockData {

    public static void loadMockData() {
        ReviewContext mockReview = createMockReviewContext();
        ReviewContextManager.getInstance().setReviewContext(mockReview);
    }

    private static ReviewContext createMockReviewContext() {
        String reviewId = "REVIEW-123";
        String title = "Implement OAuth2 authentication";
        String summary = "Adds full OAuth2 support across the backend API and frontend app, " +
                        "including token validation, refresh token handling, and a new login UI component.";
        String author = "John Doe";
        ReviewStatus status = ReviewStatus.OPEN;

        List<ReviewerInfo> reviewers = createMockReviewers();
        List<Repository> repositories = createMockRepositories();
        List<ReviewComment> comments = createMockComments();

        return new ReviewContext(reviewId, title, summary, author, status, reviewers, repositories, comments);
    }

    private static List<ReviewerInfo> createMockReviewers() {
        List<ReviewerInfo> reviewers = new ArrayList<>();

        reviewers.add(new ReviewerInfo("Alice Chen"));
        reviewers.add(new ReviewerInfo("Bob Martin"));
        reviewers.add(new ReviewerInfo("Carlos Rivera"));

        return reviewers;
    }

    private static List<Repository> createMockRepositories() {
        List<Repository> repositories = new ArrayList<>();

        repositories.add(new Repository("backend-api", "RESTful API server", "https://github.com/company/backend-api"));
        repositories.add(new Repository("frontend-app", "React web application", "https://github.com/company/frontend-app"));

        return repositories;
    }

    private static List<ReviewComment> createMockComments() {
        List<ReviewComment> comments = new ArrayList<>();

        comments.add(new ReviewComment(
            "comment-1",
            "src/auth/OAuthService.java",
            42,
            "Alice Chen",
            "Consider adding error handling for invalid tokens",
            "2024-04-20 10:30:00"
        ));

        comments.add(new ReviewComment(
            "comment-2",
            "src/auth/TokenValidator.java",
            87,
            "Bob Martin",
            "This validation logic looks good, but we should add unit tests",
            "2024-04-20 14:15:00"
        ));

        comments.add(new ReviewComment(
            "comment-3",
            "src/components/LoginForm.tsx",
            125,
            "Carlos Rivera",
            "Nice implementation! UI looks clean.",
            "2024-04-21 09:00:00"
        ));

        return comments;
    }

    public static void refreshMockData() {
        loadMockData();
        System.out.println("Mock review context data refreshed");
    }
}

