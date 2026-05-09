package com.kalynx.serverlessreviewtool.models.review;

public enum ReviewStatus {
    PENDING("pending"),
    IN_PROGRESS("in_progress"),
    APPROVED("approved"),
    REJECTED("rejected"),
    CHANGES_REQUESTED("changes_requested"),
    CANCELLED("cancelled");

    private final String value;

    ReviewStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

