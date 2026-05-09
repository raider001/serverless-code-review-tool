package com.kalynx.serverlessreviewtool.plugin;

/**
 * Base class for review lifecycle notification plugins.
 * Extend this class and override only the events your plugin needs to handle.
 *
 * <p>All methods default to no-ops. The tool calls these fire-and-forget;
 * exceptions are caught and logged without affecting the core workflow.
 */
public abstract class NotificationPlugin implements Plugin {

    @Override
    public void initialize() {}

    /**
     * Called when a new review is created.
     *
     * @param reviewId the unique identifier of the created review
     * @param author   the git username of the review creator
     * @param title    the review title
     */
    public void onReviewCreated(String reviewId, String author, String title) {}

    /**
     * Called when a reviewer joins a review.
     *
     * @param reviewId the unique identifier of the review
     * @param reviewer the git username of the joining reviewer
     */
    public void onReviewerJoined(String reviewId, String reviewer) {}

    /**
     * Called when a reviewer leaves a review.
     *
     * @param reviewId the unique identifier of the review
     * @param reviewer the git username of the leaving reviewer
     */
    public void onReviewerLeft(String reviewId, String reviewer) {}

    /**
     * Called when a comment is added to a review.
     *
     * @param reviewId   the unique identifier of the review
     * @param author     the git username of the comment author
     * @param filePath   the file the comment is on, null for general comments
     * @param lineNumber the line number of the comment, -1 if not line-specific
     */
    public void onCommentAdded(String reviewId, String author, String filePath, int lineNumber) {}

    /**
     * Called when a reviewer approves a review.
     *
     * @param reviewId the unique identifier of the review
     * @param reviewer the git username of the approving reviewer
     */
    public void onReviewApproved(String reviewId, String reviewer) {}

    /**
     * Called when a reviewer requests changes on a review.
     *
     * @param reviewId the unique identifier of the review
     * @param reviewer the git username of the reviewer requesting changes
     */
    public void onChangesRequested(String reviewId, String reviewer) {}
}
