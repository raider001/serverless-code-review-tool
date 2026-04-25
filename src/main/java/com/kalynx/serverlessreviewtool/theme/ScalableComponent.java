package com.kalynx.serverlessreviewtool.theme;

import java.awt.*;

/**
 * Utility class for DPI-aware scaling
 * This class is PURELY about scaling - no theming here!
 * Use Themed* component classes for themed components.
 */
public class ScalableComponent {

    private static final ThemeManager themeManager = ThemeManager.getInstance();

    /**
     * Create a scaled insets
     */
    public static Insets createInsets(int top, int left, int bottom, int right) {
        return new Insets(
            themeManager.scale(top),
            themeManager.scale(left),
            themeManager.scale(bottom),
            themeManager.scale(right)
        );
    }

    /**
     * Create a scaled dimension
     */
    public static Dimension createDimension(int width, int height) {
        return new Dimension(
            themeManager.scale(width),
            themeManager.scale(height)
        );
    }

    /**
     * Create a scaled gap for layouts
     */
    public static int scaleGap(int gap) {
        return themeManager.scale(gap);
    }

    /**
     * Get scaled font size
     */
    public static int getScaledFontSize(int baseSize) {
        return themeManager.scale(baseSize);
    }

    /**
     * Create a scaled font
     */
    public static Font createScaledFont(String name, int style, int baseSize) {
        return new Font(name, style, themeManager.scale(baseSize));
    }
}

