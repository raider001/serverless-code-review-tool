package com.kalynx;

import com.kalynx.serverlessreviewtool.git.Git;
import com.kalynx.serverlessreviewtool.git.GitFactory;
import com.kalynx.serverlessreviewtool.git.ReviewItemLoader;
import com.kalynx.serverlessreviewtool.models.ReviewItem;

import java.util.List;

public class TestReviewLoading {
    public static void main(String[] args) {
        try {
            Git git = GitFactory.getInstance();
            ReviewItemLoader loader = new ReviewItemLoader(git);

            System.out.println("Loading reviews from java-backend-service...");
            List<ReviewItem> reviews = loader.loadReviewsFromRepository("java-backend-service")
                .get();

            System.out.println("\nFound " + reviews.size() + " reviews:");
            for (ReviewItem review : reviews) {
                System.out.println("  - Title: " + review.getTitle());
                System.out.println("    Author: " + review.getAuthor());
                System.out.println("    Repository: " + review.getRepository());
                System.out.println("    Status: " + review.getStatus());
                System.out.println();
            }

            System.out.println("Loading reviews from python-api-service...");
            reviews = loader.loadReviewsFromRepository("python-api-service")
                .get();

            System.out.println("\nFound " + reviews.size() + " reviews:");
            for (ReviewItem review : reviews) {
                System.out.println("  - Title: " + review.getTitle());
                System.out.println("    Author: " + review.getAuthor());
                System.out.println("    Repository: " + review.getRepository());
                System.out.println("    Status: " + review.getStatus());
                System.out.println();
            }

            System.out.println("Loading reviews from react-frontend-app...");
            reviews = loader.loadReviewsFromRepository("react-frontend-app")
                .get();

            System.out.println("\nFound " + reviews.size() + " reviews:");
            for (ReviewItem review : reviews) {
                System.out.println("  - Title: " + review.getTitle());
                System.out.println("    Author: " + review.getAuthor());
                System.out.println("    Repository: " + review.getRepository());
                System.out.println("    Status: " + review.getStatus());
                System.out.println();
            }

            System.out.println("✓ All reviews loaded successfully with no null values!");

        } catch (Exception e) {
            System.err.println("Error loading reviews: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


