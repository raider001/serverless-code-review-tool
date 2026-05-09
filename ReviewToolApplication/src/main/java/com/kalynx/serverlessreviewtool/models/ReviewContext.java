package com.kalynx.serverlessreviewtool.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReviewContext {

    public final String reviewId;
    public String title;
    public String summary;
    public String author;
    public ReviewStatus status;
    public final List<ReviewerInfo> reviewers;
    public final List<Repository> repositories;
    public final List<ReviewComment> comments;

    public ReviewContext(String reviewId, String title, String summary, String author, ReviewStatus status,
                         List<ReviewerInfo> reviewers, List<Repository> repositories, List<ReviewComment> comments) {
        this.reviewId = reviewId;
        this.title = title;
        this.summary = summary;
        this.author = author;
        this.status = status;
        this.reviewers = new ArrayList<>(reviewers);
        this.repositories = new ArrayList<>(repositories);
        this.comments = new ArrayList<>(comments);
    }

    public ReviewContext(ReviewContext reviewContext) {
        this(reviewContext.reviewId, reviewContext.title, reviewContext.summary, reviewContext.author, reviewContext.status,
             reviewContext.reviewers, reviewContext.repositories, reviewContext.comments);
    }

    public String getReviewId() {
        return reviewId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public ReviewStatus getReviewStatus() {
        return status;
    }

    public void setReviewStatus(ReviewStatus status) {
        this.status = status;
    }

    public List<ReviewerInfo> getReviewers() {
        return new ArrayList<>(reviewers);
    }

    public void addReviewer(String name) {
        reviewers.add(new ReviewerInfo(name));
    }

    public void addReviewer(ReviewerInfo reviewer) {
        reviewers.add(reviewer);
    }

    public List<Repository> getRepositories() {
        return new ArrayList<>(repositories);
    }

    public void addRepository(Repository repository) {
        repositories.add(repository);
    }

    public Repository getRepository(String name) {
        return repositories.stream()
            .filter(r -> r.getName().equals(name))
            .findFirst().orElse(null);
    }

    public List<ReviewComment> getComments() {
        return new ArrayList<>(comments);
    }

    public void addComment(ReviewComment comment) {
        comments.add(comment);
    }

    public List<ReviewComment> getCommentsForFile(String filePath) {
        return comments.stream()
            .filter(c -> c.getFilePath().equals(filePath))
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReviewContext that = (ReviewContext) o;
        return Objects.equals(reviewId, that.reviewId) &&
               Objects.equals(title, that.title) &&
               Objects.equals(summary, that.summary) &&
               Objects.equals(author, that.author) &&
               status == that.status &&
               Objects.equals(reviewers, that.reviewers) &&
               Objects.equals(repositories, that.repositories) &&
               Objects.equals(comments, that.comments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reviewId, title, summary, author, status, reviewers, repositories, comments);
    }
}
