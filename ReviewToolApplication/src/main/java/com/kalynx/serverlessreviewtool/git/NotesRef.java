package com.kalynx.serverlessreviewtool.git;

/**
 * NotesRef - Typed ref path builders for the review notes hierarchy.
 *
 * <p>All review data lives under {@code refs/notes/reviews/} in an append-only
 * NDJSON stream structure:
 * <pre>
 *   refs/notes/reviews/{reviewId}/metadata/{field}
 *   refs/notes/reviews/{reviewId}/reviewers
 *   refs/notes/reviews/{reviewId}/comments
 * </pre>
 */
public final class NotesRef {

    static final String ROOT = "refs/notes/reviews/";

    private static final String METADATA = "/metadata/";
    static final String FIELD_AUTHOR = "author";
    static final String FIELD_TITLE = "title";
    static final String FIELD_DESCRIPTION = "description";
    static final String FIELD_STATUS = "status";
    static final String FIELD_COMMITS = "commits";
    static final String FIELD_REVIEW_STRATEGY = "reviewStrategy";

    private NotesRef() {
    }

    /** @return ref path for the author metadata stream */
    public static String metadataAuthor(String reviewId) {
        return metadata(reviewId, FIELD_AUTHOR);
    }

    /** @return ref path for the title metadata stream */
    public static String metadataTitle(String reviewId) {
        return metadata(reviewId, FIELD_TITLE);
    }

    /** @return ref path for the description metadata stream */
    public static String metadataDescription(String reviewId) {
        return metadata(reviewId, FIELD_DESCRIPTION);
    }

    /** @return ref path for the status metadata stream */
    public static String metadataStatus(String reviewId) {
        return metadata(reviewId, FIELD_STATUS);
    }

    /** @return ref path for the commits metadata stream */
    public static String metadataCommits(String reviewId) {
        return metadata(reviewId, FIELD_COMMITS);
    }

    /** @return ref path for the reviewStrategy metadata stream */
    public static String metadataReviewStrategy(String reviewId) {
        return metadata(reviewId, FIELD_REVIEW_STRATEGY);
    }

    /** @return ref path for the reviewers stream */
    public static String reviewers(String reviewId) {
        return ROOT + reviewId + "/reviewers";
    }

    /** @return ref path for the comments stream */
    public static String comments(String reviewId) {
        return ROOT + reviewId + "/comments";
    }

    /** @return the wildcard refspec matching all review notes refs */
    public static String wildcard() {
        return ROOT + "*";
    }

    private static String metadata(String reviewId, String field) {
        return ROOT + reviewId + METADATA + field;
    }
}

