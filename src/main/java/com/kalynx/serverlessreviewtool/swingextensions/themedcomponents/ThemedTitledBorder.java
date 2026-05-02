package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Utility for creating themed titled borders
 * Since borders aren't components, this is a static utility
 */
public class ThemedTitledBorder {

    private static final ThemeManager themeManager = ThemeManager.getInstance();

    /**
     * Create a themed titled border with explicit colors
     */
    public static TitledBorder create(String title) {
        Theme theme = themeManager.getCurrentTheme();

        // Create a fresh LineBorder with explicit color
        javax.swing.border.LineBorder lineBorder = (javax.swing.border.LineBorder)
            BorderFactory.createLineBorder(theme.getBorderColor(), 1);

        // Create titled border with EXPLICIT font and color at creation time
        return BorderFactory.createTitledBorder(
            lineBorder,
            title,
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            new Font("Segoe UI", Font.BOLD, themeManager.scale(12)),
            theme.getForegroundColor()
        );
    }
}

