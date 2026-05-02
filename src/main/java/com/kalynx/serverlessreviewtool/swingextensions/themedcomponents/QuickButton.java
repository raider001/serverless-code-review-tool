package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import com.kalynx.serverlessreviewtool.theme.ScalableComponent;
import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * QuickButton - A lightweight, theme-aware button component for title bar and toolbar use
 * Provides consistent styling and hover effects with minimal setup
 * Icons are injected from the icons package for separation of concerns
 */
public class QuickButton extends JButton {

    private final ThemeManager themeManager;
    private IconPainter customIconPainter;
    private boolean useAccentHover = false;

    // Only store custom hover colors if explicitly set
    private Color customHoverBackground = null;
    private Color customHoverForeground = null;

    /**
     * Functional interface for custom icon painting
     */
    @FunctionalInterface
    public interface IconPainter {
        void paint(Graphics2D g2d, int width, int height, Color foreground);
    }

    /**
     * Create a QuickButton with text
     */
    public QuickButton(String text) {
        super(text);
        this.themeManager = ThemeManager.getInstance();
        initialize();
    }

    /**
     * Create a QuickButton with custom icon painter
     */
    public QuickButton(IconPainter iconPainter) {
        super();
        this.themeManager = ThemeManager.getInstance();
        this.customIconPainter = iconPainter;
        initialize();
    }

    /**
     * Create an empty QuickButton (for custom painting)
     */
    public QuickButton() {
        super();
        this.themeManager = ThemeManager.getInstance();
        initialize();
    }

    /**
     * Initialize button styling and behavior
     */
    private void initialize() {
        // Standard button styling
        Dimension size = ScalableComponent.createDimension(46, 40);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(true);
        setOpaque(true);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Enable rollover for hover detection
        setRolloverEnabled(true);
    }

    /**
     * Enable accent color hover (blue background, white text)
     */
    public QuickButton setAccentHover() {
        this.useAccentHover = true;
        return this;
    }

    /**
     * Set custom hover colors
     */
    public QuickButton setCustomHover(Color background, Color foreground) {
        this.customHoverBackground = background;
        this.customHoverForeground = foreground;
        return this;
    }

    /**
     * Set custom icon painter
     */
    public QuickButton setIconPainter(IconPainter painter) {
        this.customIconPainter = painter;
        repaint();
        return this;
    }

    /**
     * Set tooltip text (fluent API)
     */
    public QuickButton setTooltip(String tooltip) {
        setToolTipText(tooltip);
        return this;
    }

    /**
     * Set font size (fluent API)
     */
    public QuickButton setFontSize(int size) {
        setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(size)));
        return this;
    }


    /**
     * Override paint to query theme colors directly and add custom icon painting
     * Uses JButton's built-in rollover state instead of manual tracking
     */
    @Override
    protected void paintComponent(Graphics g) {
        // Query current theme colors on demand - no caching needed!
        Theme theme = themeManager.getCurrentTheme();

        // Use JButton's built-in rollover state (managed by ButtonModel)
        boolean isHovered = getModel().isRollover();

        // Determine colors based on current state
        if (isHovered) {
            // Custom hover colors take precedence
            if (customHoverBackground != null) {
                setBackground(customHoverBackground);
                setForeground(customHoverForeground);
            } else if (useAccentHover) {
                setBackground(theme.getAccentColor());
                setForeground(Color.WHITE);
            } else {
                setBackground(theme.getButtonBackground());
                setForeground(theme.getForegroundColor());
            }
        } else {
            // Normal state
            setBackground(theme.getBackgroundColor());
            setForeground(theme.getForegroundColor());
        }

        // Paint the button
        super.paintComponent(g);

        // Paint custom icon if configured
        if (customIconPainter != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            customIconPainter.paint(g2d, getWidth(), getHeight(), getForeground());

            g2d.dispose();
        }
    }
}
