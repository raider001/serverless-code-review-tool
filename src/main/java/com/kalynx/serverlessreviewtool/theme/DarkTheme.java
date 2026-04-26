package com.kalynx.serverlessreviewtool.theme;

import java.awt.Color;

/**
 * Dark Theme - Modern dark color scheme for code review
 */
public class DarkTheme implements Theme {
    
    // Main colors
    private static final Color BACKGROUND = new Color(30, 30, 35);
    private static final Color FOREGROUND = new Color(230, 230, 235);
    private static final Color SECONDARY_TEXT = new Color(150, 155, 165); // muted grey-blue
    private static final Color ACCENT = new Color(58, 150, 221);
    private static final Color SECONDARY_ACCENT = new Color(100, 160, 200);
    
    // Component colors - APCA-compliant
    private static final Color BUTTON_BG = new Color(55, 55, 62); // Darker for better contrast with text
    private static final Color INPUT_BG = new Color(42, 42, 48); // Slightly lighter than background
    private static final Color BORDER = new Color(70, 70, 78); // More visible border
    
    // Status colors
    private static final Color SUCCESS = new Color(66, 184, 131);
    private static final Color WARNING = new Color(255, 183, 77);
    private static final Color ERROR = new Color(240, 80, 80);
    private static final Color INFO = new Color(100, 181, 246);
    
    // Code review semantic colors
    private static final Color APPROVED = new Color(114, 230, 137);
    private static final Color PENDING = new Color(251, 188, 5);
    private static final Color CHANGES_REQUESTED = new Color(248, 91, 103);
    private static final Color COMMENT = new Color(88, 166, 255);
    
    // Diff colors - more visible, elevated with moderate transparency
    private static final Color ADDED = new Color(60, 100, 75, 140);     // Visible dark green
    private static final Color REMOVED = new Color(100, 50, 50, 140);   // Visible dark red
    private static final Color MODIFIED = new Color(80, 75, 55, 140);   // Visible dark olive

    @Override
    public Color getBackgroundColor() {
        return BACKGROUND;
    }
    
    @Override
    public Color getForegroundColor() {
        return FOREGROUND;
    }

    @Override
    public Color getSecondaryTextColor() {
        return SECONDARY_TEXT;
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
        return "Dark";
    }
}

