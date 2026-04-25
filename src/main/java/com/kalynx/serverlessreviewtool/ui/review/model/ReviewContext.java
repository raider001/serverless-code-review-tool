package com.kalynx.serverlessreviewtool.ui.review.model;

import java.util.ArrayList;
import java.util.List;

/**
 * ReviewContext - Contains all data for a code review session
 */
public class ReviewContext {
    private final String reviewId;
    private final String title;
    private final List<Repository> repositories;
    private final List<ReviewComment> comments;

    public ReviewContext(String reviewId, String title) {
        this.reviewId = reviewId;
        this.title = title;
        this.repositories = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    public void addRepository(Repository repository) {
        repositories.add(repository);
    }

    public void addComment(ReviewComment comment) {
        comments.add(comment);
    }

    public List<Repository> getRepositories() {
        return new ArrayList<>(repositories);
    }

    public Repository getRepository(String name) {
        return repositories.stream()
            .filter(r -> r.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    public List<ReviewComment> getComments() {
        return new ArrayList<>(comments);
    }

    public List<ReviewComment> getCommentsForFile(String filePath) {
        List<ReviewComment> fileComments = new ArrayList<>();
        for (ReviewComment comment : comments) {
            if (comment.getFilePath().equals(filePath)) {
                fileComments.add(comment);
            }
        }
        return fileComments;
    }

    public String getReviewId() {
        return reviewId;
    }

    public String getTitle() {
        return title;
    }

    public List<ReviewFile> getAllFiles() {
        List<ReviewFile> allFiles = new ArrayList<>();
        for (Repository repo : repositories) {
            allFiles.addAll(repo.getFiles());
        }
        return allFiles;
    }
}

