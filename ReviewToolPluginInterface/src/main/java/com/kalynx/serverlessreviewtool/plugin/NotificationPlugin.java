package com.kalynx.serverlessreviewtool.plugin;

/**
 * Base class for inbound review notification plugins.
 * Extend this class and override the events your plugin needs to handle.
 *
 * <p>All methods default to no-ops. The tool calls these fire-and-forget;
 * exceptions are caught and logged without affecting the core workflow.
 */
public abstract class NotificationPlugin
    extends Notifier<NotificationPayload, NotificationPlugin.NotificationType>
    implements Plugin {

    /**
     * Base notification events emitted by notification plugins.
     */
    public enum NotificationType {
        REVIEW_UPDATED,
        REPOSITORIES_UPDATED,
    }

    /**
     * Initializes this plugin.
     */
    @Override
    public void initialize() {}

    /**
     * Called when an existing review has changed and should be refreshed.
     *
     * @param update updated review list projection and query hints
     */
    public final void onReviewUpdated(ReviewListUpdate update) {
        if (update == null) {
            return;
        }
        handleReviewUpdated(update);
        notifyListeners(NotificationType.REVIEW_UPDATED, update);
    }

    /**
     * Called when the tracked repository listing has changed.
     *
     * @param update repository listing update payload
     */
    public final void onRepositoriesUpdated(RepositoryListUpdate update) {
        if (update == null) {
            return;
        }
        handleRepositoriesUpdated(update);
        notifyListeners(NotificationType.REPOSITORIES_UPDATED, update);
    }

    /**
     * Optional hook called before {@code REVIEW_UPDATED} listeners are notified.
     *
     * @param update updated review list projection and query hints
     */
    protected void handleReviewUpdated(ReviewListUpdate update) {}

    /**
     * Optional hook called before {@code REPOSITORIES_UPDATED} listeners are notified.
     *
     * @param update repository listing update payload
     */
    protected void handleRepositoriesUpdated(RepositoryListUpdate update) {}
}
