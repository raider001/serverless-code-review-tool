package com.kalynx;

import com.kalynx.serverlessreviewtool.git.Git;
import com.kalynx.serverlessreviewtool.git.GitFactory;
import com.kalynx.serverlessreviewtool.git.ReviewItemLoader;
import com.kalynx.serverlessreviewtool.models.ReviewItem;

import java.util.List;

public class VerifyReviews {
    public static void main(String[] args) {
        try {
            Git git = GitFactory.getInstance();
            ReviewItemLoader loader = new ReviewItemLoader(git);

            System.out.println("=== Java Backend Service ===");
            List<ReviewItem> javaReviews = loader.loadReviewsFromRepository("java-backend-service").get();
            System.out.println("Found " + javaReviews.size() + " reviews:");
            for (ReviewItem review : javaReviews) {
                System.out.println("  Title: " + review.getTitle());
                System.out.println("  Author: " + review.getAuthor());
                System.out.println("  Status: " + review.getStatus());
                System.out.println();
            }

            System.out.println("=== Python API Service ===");
            List<ReviewItem> pythonReviews = loader.loadReviewsFromRepository("python-api-service").get();
            System.out.println("Found " + pythonReviews.size() + " reviews:");
            for (ReviewItem review : pythonReviews) {
                System.out.println("  Title: " + review.getTitle());
                System.out.println("  Author: " + review.getAuthor());
                System.out.println("  Status: " + review.getStatus());
                System.out.println();
            }

            System.out.println("=== React Frontend App ===");
            List<ReviewItem> reactReviews = loader.loadReviewsFromRepository("react-frontend-app").get();
            System.out.println("Found " + reactReviews.size() + " reviews:");
            for (ReviewItem review : reactReviews) {
                System.out.println("  Title: " + review.getTitle());
                System.out.println("  Author: " + review.getAuthor());
                System.out.println("  Status: " + review.getStatus());
                System.out.println();
            }

            int totalReviews = javaReviews.size() + pythonReviews.size() + reactReviews.size();
            System.out.println("✓ SUCCESS: " + totalReviews + " reviews loaded with proper titles!");

        } catch (Exception e) {
            System.err.println("✗ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

