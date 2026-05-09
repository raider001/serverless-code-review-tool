package com.kalynx.serverlessreviewtool.models;

import java.awt.Color;

/**
 * The overall status of a code review.
 */
public enum ReviewStatus {
    OPEN             ("Open",              new Color(58,  150, 221)),  // blue
    IN_PROGRESS      ("In Progress",       new Color(235, 152,  78)),  // amber
    CHANGES_REQUESTED("Changes Requested", new Color(220,  80,  80)),  // red
    COMPLETED        ("Completed",         new Color(66,  184, 131)),  // green
    CANCELLED        ("Cancelled",         new Color(150, 150, 150));  // grey

    private final String displayName;
    private final Color  color;

    ReviewStatus(String displayName, Color color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public Color  getColor()       { return color; }
}

