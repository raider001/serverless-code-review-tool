package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import javax.swing.*;

/**
 * ScrollBarOrientation - Type-safe enum for scrollbar orientation
 * Provides a cleaner interface than raw integer constants
 */
public enum ScrollBarOrientation {
    /**
     * Vertical orientation (top to bottom)
     */
    VERTICAL(JScrollBar.VERTICAL),

    /**
     * Horizontal orientation (left to right)
     */
    HORIZONTAL(JScrollBar.HORIZONTAL);

    private final int value;

    ScrollBarOrientation(int value) {
        this.value = value;
    }

    /**
     * Get the integer value for the orientation
     */
    public int getValue() {
        return value;
    }
}

