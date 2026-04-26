package com.kalynx.serverlessreviewtool.theme;

import java.awt.Color;

/**
 * Theme interface - defines colors for the application
 */
public interface Theme {
    
    // Main colors
    Color getBackgroundColor();
    Color getForegroundColor();
    Color getSecondaryTextColor();  // muted text for subtitles / labels
    Color getAccentColor();
    Color getSecondaryAccentColor();
    
    // Component colors
    Color getButtonBackground();
    Color getButtonForeground();
    Color getInputBackground();
    Color getBorderColor();
    
    // Status colors
    Color getSuccessColor();
    Color getWarningColor();
    Color getErrorColor();
    Color getInfoColor();
    
    // Semantic colors for code review
    Color getApprovedColor();
    Color getPendingColor();
    Color getChangesRequestedColor();
    Color getCommentColor();
    
    // Diff colors
    Color getAddedLineColor();
    Color getRemovedLineColor();
    Color getModifiedLineColor();
    
    // Get theme name
    String getName();
}
