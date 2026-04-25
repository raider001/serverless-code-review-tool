package com.kalynx.serverlessreviewtool.theme.components;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 * ThemedScrollBar - A JScrollBar that queries theme colors on-demand
 * Provides modern, minimal scrollbar styling with automatic theme integration
 */
public class ThemedScrollBar extends JScrollBar {

    private final ThemeManager themeManager;

    public ThemedScrollBar() {
        super();
        this.themeManager = ThemeManager.getInstance();
        initialize();
    }

    @SuppressWarnings("MagicConstant")
    public ThemedScrollBar(int orientation) {
        super(orientation);
        this.themeManager = ThemeManager.getInstance();
        initialize();
    }

    @SuppressWarnings("MagicConstant")
    public ThemedScrollBar(int orientation, int value, int extent, int min, int max) {
        super(orientation, value, extent, min, max);
        this.themeManager = ThemeManager.getInstance();
        initialize();
    }

    /**
     * Create a scrollbar with type-safe orientation
     */
    @SuppressWarnings("MagicConstant")
    public ThemedScrollBar(ScrollBarOrientation orientation) {
        super(orientation.getValue());
        this.themeManager = ThemeManager.getInstance();
        initialize();
    }

    /**
     * Create a scrollbar with type-safe orientation and values
     */
    @SuppressWarnings("MagicConstant")
    public ThemedScrollBar(ScrollBarOrientation orientation, int value, int extent, int min, int max) {
        super(orientation.getValue(), value, extent, min, max);
        this.themeManager = ThemeManager.getInstance();
        initialize();
    }

    /**
     * Initialize with modern UI
     */
    private void initialize() {
        setOpaque(true);
        setPreferredSize(new Dimension(
            themeManager.scale(12),
            themeManager.scale(12)
        ));
        setUI(new ModernScrollBarUI());

        // Set faster scroll speeds (roughly 2x default)
        setUnitIncrement(20);   // Mouse wheel scroll (default is ~10)
        setBlockIncrement(100); // Page up/down and scrollbar click (default is ~50)
    }

    @Override
    public void updateUI() {
        // Override to prevent Look & Feel from changing our custom UI
        if (themeManager != null) {
            setUI(new ModernScrollBarUI());
        } else {
            super.updateUI();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Query theme on-demand
        Theme theme = themeManager.getCurrentTheme();
        setBackground(theme.getBackgroundColor());
        super.paintComponent(g);
    }

    /**
     * Modern, minimal scrollbar UI that queries theme on demand
     */
    private class ModernScrollBarUI extends BasicScrollBarUI {

        @Override
        protected void configureScrollBarColors() {
            // Query theme on demand
            Theme theme = themeManager.getCurrentTheme();
            thumbColor = theme.getBorderColor();
            thumbDarkShadowColor = theme.getBorderColor();
            thumbHighlightColor = theme.getAccentColor();
            thumbLightShadowColor = theme.getBorderColor();
            trackColor = theme.getBackgroundColor();
            trackHighlightColor = theme.getBackgroundColor();
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createInvisibleButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createInvisibleButton();
        }

        /**
         * Create invisible button to hide arrow buttons (modern look)
         */
        private JButton createInvisibleButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }

            // Query theme colors on demand
            Theme theme = themeManager.getCurrentTheme();

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Rounded thumb with padding for modern look
            int arc = themeManager.scale(8);
            int padding = themeManager.scale(2);

            Color thumbCol = theme.getBorderColor();
            if (isDragging) {
                thumbCol = theme.getAccentColor();
            } else if (isThumbRollover()) {
                thumbCol = adjustBrightness(theme.getBorderColor());
            }

            g2.setColor(thumbCol);
            g2.fillRoundRect(
                thumbBounds.x + padding,
                thumbBounds.y + padding,
                thumbBounds.width - 2 * padding,
                thumbBounds.height - 2 * padding,
                arc, arc
            );

            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            // Query theme on demand
            Theme theme = themeManager.getCurrentTheme();
            g.setColor(theme.getBackgroundColor());
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }

        /**
         * Adjust color brightness for hover effect
         */
        private Color adjustBrightness(Color color) {
            int r = Math.min(255, (int)(color.getRed() * (float) 1.2));
            int g = Math.min(255, (int)(color.getGreen() * (float) 1.2));
            int b = Math.min(255, (int)(color.getBlue() * (float) 1.2));
            return new Color(r, g, b);
        }
    }
}

