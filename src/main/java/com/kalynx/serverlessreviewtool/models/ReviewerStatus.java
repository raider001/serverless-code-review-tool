package com.kalynx.serverlessreviewtool.models;

import java.awt.Color;

/**
 * The review state for a single reviewer.
 */
public enum ReviewerStatus {
    REVIEWING       ("Reviewing",          new Color(58,  150, 221)),  // blue
    APPROVED        ("Approved",           new Color(66,  184, 131)),  // green
    CHANGES_REQUESTED("Changes Requested", new Color(220,  80,  80)); // red

    private final String displayName;
    private final Color  color;

    ReviewerStatus(String displayName, Color color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public Color  getColor()       { return color; }
}

