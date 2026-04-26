package com.kalynx.serverlessreviewtool.models;

import com.kalynx.serverlessreviewtool.ui.review.model.ReviewStatus;

import java.util.Objects;

public class ReviewItem {
    private final String       title;
    private final String       author;
    private final String       repository;
    private final ReviewStatus status;
    private final long       lastUpdate;

    public ReviewItem(String title, String author, String repository, ReviewStatus status, long lastUpdate) {
        this.title      = title;
        this.author     = author;
        this.repository = repository;
        this.status     = status;
        this.lastUpdate    = lastUpdate;
    }

    public ReviewItem(ReviewItem reviewItem) {
        this(reviewItem.title, reviewItem.author, reviewItem.repository, reviewItem.status, reviewItem.lastUpdate);
    }

    public String       getTitle()      { return title; }
    public String       getAuthor()     { return author; }
    public String       getRepository() { return repository; }
    public ReviewStatus getStatus()     { return status; }
    public long         getLastUpdate()    { return lastUpdate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReviewItem that = (ReviewItem) o;
        return Objects.equals(title, that.title) &&
               Objects.equals(author, that.author) &&
               Objects.equals(repository, that.repository) &&
               status == that.status &&
               Objects.equals(lastUpdate, that.lastUpdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, author, repository, status, lastUpdate);
    }
}
