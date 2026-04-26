package com.kalynx.serverlessreviewtool.ui.review.model;

import java.util.ArrayList;
import java.util.List;

/**
 * ReviewContext - Contains all data for a code review session
 */
public class ReviewContext {
    private final String reviewId;
    private String title;
    private String summary = "";
    private String author  = "";
    private ReviewStatus reviewStatus = ReviewStatus.OPEN;
    private final List<ReviewerInfo> reviewers = new ArrayList<>();
    private final List<Repository> repositories = new ArrayList<>();
    private final List<ReviewComment> comments = new ArrayList<>();

    public ReviewContext(String reviewId, String title) {
        this.reviewId = reviewId;
        this.title = title;
    }

    // ── title / summary ──────────────────────────────────────────────────────
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary()                 { return summary; }
    public void   setSummary(String summary)   { this.summary = summary; }
    public String getAuthor()              { return author; }
    public void   setAuthor(String author) { this.author = author; }
    public ReviewStatus getReviewStatus()                    { return reviewStatus; }
    public void         setReviewStatus(ReviewStatus status) { this.reviewStatus = status; }

    // ── reviewers ─────────────────────────────────────────────────────────────
    public void addReviewer(String name) { reviewers.add(new ReviewerInfo(name)); }
    public void addReviewer(ReviewerInfo reviewer) { reviewers.add(reviewer); }
    public List<ReviewerInfo> getReviewers() { return new ArrayList<>(reviewers); }

    // ── repositories ─────────────────────────────────────────────────────────
    public void addRepository(Repository repository) { repositories.add(repository); }
    public List<Repository> getRepositories() { return new ArrayList<>(repositories); }

    public Repository getRepository(String name) {
        return repositories.stream()
            .filter(r -> r.getName().equals(name))
            .findFirst().orElse(null);
    }

    // ── comments ─────────────────────────────────────────────────────────────
    public void addComment(ReviewComment comment) { comments.add(comment); }
    public List<ReviewComment> getComments() { return new ArrayList<>(comments); }

    public List<ReviewComment> getCommentsForFile(String filePath) {
        return comments.stream()
            .filter(c -> c.getFilePath().equals(filePath))
            .collect(java.util.stream.Collectors.toList());
    }

    public String getReviewId() { return reviewId; }

    public List<ReviewFile> getAllFiles() {
        List<ReviewFile> all = new ArrayList<>();
        for (Repository repo : repositories) all.addAll(repo.getFiles());
        return all;
    }
}

