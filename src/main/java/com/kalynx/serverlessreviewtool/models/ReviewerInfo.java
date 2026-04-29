package com.kalynx.serverlessreviewtool.models;

/**
 * A reviewer assigned to a code review, with a mutable review status.
 */
public class ReviewerInfo {
    private final String name;
    private ReviewerStatus status;

    public ReviewerInfo(String name) {
        this.name   = name;
        this.status = ReviewerStatus.REVIEWING;
    }

    public String         getName()   { return name; }
    public ReviewerStatus getStatus() { return status; }
    public void setStatus(ReviewerStatus status) { this.status = status; }
}

