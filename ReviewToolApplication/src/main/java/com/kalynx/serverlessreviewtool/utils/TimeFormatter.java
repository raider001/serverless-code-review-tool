package com.kalynx.serverlessreviewtool.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TimeFormatter - Utility for formatting timestamps in a human-readable way
 */
public class TimeFormatter {

    private static final long MINUTE_MILLIS = 60 * 1000;
    private static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final long DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final long WEEK_MILLIS = 7 * DAY_MILLIS;

    /**
     * Format a timestamp relative to the current time
     * - Less than 1 hour: "X minutes ago"
     * - Less than 24 hours: "X hours Y minutes ago"
     * - 1-7 days: "X days ago"
     * - More than 7 days: "Last updated dd/MM/yyyy"
     *
     * @param timestamp The timestamp in milliseconds
     * @return Formatted time string
     */
    public static String formatRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 0) {
            // Future timestamp, shouldn't happen but handle gracefully
            return formatDate(timestamp);
        }

        if (diff < HOUR_MILLIS) {
            // Less than 1 hour: show minutes
            long minutes = diff / MINUTE_MILLIS;
            if (minutes == 0) {
                return "Just now";
            }
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (diff < DAY_MILLIS) {
            // Less than 24 hours: show hours and minutes
            long hours = diff / HOUR_MILLIS;
            long minutes = (diff % HOUR_MILLIS) / MINUTE_MILLIS;

            StringBuilder sb = new StringBuilder();
            sb.append(hours).append(hours == 1 ? " hour" : " hours");

            if (minutes > 0) {
                sb.append(" ").append(minutes).append(minutes == 1 ? " minute" : " minutes");
            }

            sb.append(" ago");
            return sb.toString();
        } else if (diff < WEEK_MILLIS) {
            // 1-7 days: show days
            long days = diff / DAY_MILLIS;
            return days + (days == 1 ? " day ago" : " days ago");
        } else {
            // More than 7 days: show absolute date
            return "Last updated " + formatDate(timestamp);
        }
    }

    /**
     * Format a timestamp as dd/MM/yyyy
     *
     * @param timestamp The timestamp in milliseconds
     * @return Formatted date string
     */
    private static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(new Date(timestamp));
    }
}

