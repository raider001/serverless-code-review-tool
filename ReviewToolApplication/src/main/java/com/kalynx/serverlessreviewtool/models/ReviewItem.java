package com.kalynx.serverlessreviewtool.models;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReviewItem {
    private final String       reviewId;
    private final String       title;
    private final String       author;
    private final String       primaryRepository;
    private final List<String> repositories;
    private final ReviewStatus status;
    private final long         lastUpdate;
    private final List<String> reviewers;

    public ReviewItem(String reviewId, String title, String author, String primaryRepository, List<String> repositories, ReviewStatus status, long lastUpdate, List<String> reviewers) {
        this.reviewId          = reviewId;
        this.title             = title;
        this.author            = author;
        this.primaryRepository = primaryRepository;
        this.repositories      = repositories != null ? new ArrayList<>(repositories) : new ArrayList<>();
        this.status            = status;
        this.lastUpdate        = lastUpdate;
        this.reviewers         = reviewers != null ? new ArrayList<>(reviewers) : new ArrayList<>();
    }

    public ReviewItem(ReviewItem reviewItem) {
        this(reviewItem.reviewId, reviewItem.title, reviewItem.author, reviewItem.primaryRepository, reviewItem.repositories, reviewItem.status, reviewItem.lastUpdate, reviewItem.reviewers);
    }

    public String       getReviewId()          { return reviewId; }
    public String       getTitle()             { return title; }
    public String       getAuthor()            { return author; }
    public String       getPrimaryRepository() { return primaryRepository; }
    public List<String> getRepositories()      { return new ArrayList<>(repositories); }
    public ReviewStatus getStatus()            { return status; }
    public long         getLastUpdate()        { return lastUpdate; }
    public List<String> getReviewers()         { return new ArrayList<>(reviewers); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReviewItem that = (ReviewItem) o;
        return Objects.equals(reviewId, that.reviewId) &&
               Objects.equals(title, that.title) &&
               Objects.equals(author, that.author) &&
               Objects.equals(primaryRepository, that.primaryRepository) &&
               Objects.equals(repositories, that.repositories) &&
               status == that.status &&
               Objects.equals(lastUpdate, that.lastUpdate) &&
               Objects.equals(reviewers, that.reviewers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reviewId, title, author, primaryRepository, repositories, status, lastUpdate, reviewers);
    }
}
