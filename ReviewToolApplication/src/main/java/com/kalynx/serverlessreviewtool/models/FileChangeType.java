package com.kalynx.serverlessreviewtool.models;

/**
 * FileChangeType - Type of change to a file
 */
public enum FileChangeType {
    ADDED("Added"),
    MODIFIED("Modified"),
    DELETED("Deleted"),
    RENAMED("Renamed");

    private final String displayName;

    FileChangeType(String displayName) {
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

