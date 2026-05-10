package com.kalynx.serverlessreviewtool.plugin;

import java.time.Instant;
import java.util.List;

/**
 * Carries a minimal review update signal and repository query hints.
 *
 * @param eventId unique id for this update event
 * @param occurredAt timestamp when the event occurred
 * @param updateType semantic type of this update
 * @param reviewId unique review id
 * @param primaryRepository primary repository name for this review
 * @param repositories full repository snapshot for this review
 */
public record ReviewListUpdate(
        String eventId,
        Instant occurredAt,
        ReviewUpdateType updateType,
        String reviewId,
        String primaryRepository,
        List<String> repositories) implements NotificationPayload {
}


