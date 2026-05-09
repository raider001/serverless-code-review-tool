package com.kalynx.serverlessreviewtool.models.review;

public enum ReviewStatus {
    PENDING("pending"),
    IN_PROGRESS("in_progress"),
    APPROVED("approved"),
    REJECTED("rejected"),
    CHANGES_REQUESTED("changes_requested");

    private final String value;

    ReviewStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ReviewStatus fromValue(String value) {
        for (ReviewStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown review status: " + value);
    }
}

