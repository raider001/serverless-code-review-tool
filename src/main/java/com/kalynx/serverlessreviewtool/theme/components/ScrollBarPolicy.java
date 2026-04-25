package com.kalynx.serverlessreviewtool.theme.components;

import javax.swing.*;

/**
 * ScrollBarPolicy - Type-safe enum for JScrollPane scrollbar policies
 * Can be used for both vertical and horizontal scrollbars
 */
public enum ScrollBarPolicy {
    /**
     * Always display the scrollbar
     */
    ALWAYS,

    /**
     * Display the scrollbar only when content exceeds viewport (default)
     */
    AS_NEEDED,

    /**
     * Never display the scrollbar
     */
    NEVER;

    /**
     * Get the vertical scrollbar policy value
     */
    public int getVerticalValue() {
        switch (this) {
            case ALWAYS: return JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;
            case AS_NEEDED: return JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
            case NEVER: return JScrollPane.VERTICAL_SCROLLBAR_NEVER;
            default: return JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
        }
    }

    /**
     * Get the horizontal scrollbar policy value
     */
    public int getHorizontalValue() {
        switch (this) {
            case ALWAYS: return JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;
            case AS_NEEDED: return JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
            case NEVER: return JScrollPane.HORIZONTAL_SCROLLBAR_NEVER;
            default: return JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
        }
    }
}

