package com.kalynx.serverlessreviewtool.models.review;

public class ReviewerData {
    private final String status;
    private final String summaryComment;

    public ReviewerData(String status, String summaryComment) {
        this.status = status;
        this.summaryComment = summaryComment;
    }

    public String getStatus() {
        return status;
    }

    public String getSummaryComment() {
        return summaryComment;
    }

    public enum Status {
        PENDING("pending"),
        APPROVED("approved"),
        REJECTED("rejected"),
        CHANGES_REQUESTED("changes_requested");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Status fromValue(String value) {
            for (Status status : values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown reviewer status: " + value);
        }
    }
}

