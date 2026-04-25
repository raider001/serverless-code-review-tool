package com.kalynx.serverlessreviewtool.ui.review;

/**
 * DiffViewMode - The mode for viewing file diffs
 */
public enum DiffViewMode {
    SIDE_BY_SIDE("Side by Side"),
    UNIFIED("Unified");

    private final String displayName;

    DiffViewMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

