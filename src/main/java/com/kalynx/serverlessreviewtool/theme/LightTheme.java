package com.kalynx.serverlessreviewtool.theme;

import java.awt.Color;

/**
 * Light Theme - Clean light color scheme for code review
 */
public class LightTheme implements Theme {

    // Main colors - Warmer tones
    private static final Color BACKGROUND = new Color(252, 250, 245); // Warm off-white (cream)
    private static final Color FOREGROUND = new Color(35, 30, 25); // Warm dark brown
    private static final Color ACCENT = new Color(200, 115, 50); // Warm orange-brown
    private static final Color SECONDARY_ACCENT = new Color(185, 135, 90); // Warm tan
    
    // Component colors - APCA-compliant with warm tones
    private static final Color BUTTON_BG = new Color(242, 238, 230); // Warm light beige
    private static final Color INPUT_BG = new Color(240, 235, 225); // Noticeably darker for clear input field visibility
    private static final Color BORDER = new Color(210, 200, 185); // Warm gray-brown

    // Status colors
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color WARNING = new Color(255, 152, 0);
    private static final Color ERROR = new Color(220, 53, 69);
    private static final Color INFO = new Color(23, 162, 184);

    // Code review semantic colors
    private static final Color APPROVED = new Color(40, 167, 69);
    private static final Color PENDING = new Color(255, 193, 7);
    private static final Color CHANGES_REQUESTED = new Color(220, 53, 69);
    private static final Color COMMENT = new Color(0, 123, 255);

    // Diff colors - warm pastels harmonizing with cream background and warm palette
    private static final Color ADDED = new Color(225, 242, 220);    // Warm sage - yellowy green
    private static final Color REMOVED = new Color(245, 210, 210);  // Warm coral rose
    private static final Color MODIFIED = new Color(248, 240, 215); // Warm sand

    @Override
    public Color getBackgroundColor() {
        return BACKGROUND;
    }

    @Override
    public Color getForegroundColor() {
        return FOREGROUND;
    }

    @Override
    public Color getAccentColor() {
        return ACCENT;
    }

    @Override
    public Color getSecondaryAccentColor() {
        return SECONDARY_ACCENT;
    }

    @Override
    public Color getButtonBackground() {
        return BUTTON_BG;
    }

    @Override
    public Color getButtonForeground() {
        return FOREGROUND;
    }

    @Override
    public Color getInputBackground() {
        return INPUT_BG;
    }

    @Override
    public Color getBorderColor() {
        return BORDER;
    }

    @Override
    public Color getSuccessColor() {
        return SUCCESS;
    }

    @Override
    public Color getWarningColor() {
        return WARNING;
    }

    @Override
    public Color getErrorColor() {
        return ERROR;
    }

    @Override
    public Color getInfoColor() {
        return INFO;
    }

    @Override
    public Color getApprovedColor() {
        return APPROVED;
    }

    @Override
    public Color getPendingColor() {
        return PENDING;
    }

    @Override
    public Color getChangesRequestedColor() {
        return CHANGES_REQUESTED;
    }

    @Override
    public Color getCommentColor() {
        return COMMENT;
    }

    @Override
    public Color getAddedLineColor() {
        return ADDED;
    }

    @Override
    public Color getRemovedLineColor() {
        return REMOVED;
    }

    @Override
    public Color getModifiedLineColor() {
        return MODIFIED;
    }

    @Override
    public String getName() {
        return "Light";
    }
}


