package com.kalynx.serverlessreviewtool.theme.components;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

/**
 * ThemedTabbedPane - A theme-aware tabbed pane component
 * Provides automatic theme color integration with custom styling
 */
public class ThemedTabbedPane extends JTabbedPane {

    private final ThemeManager themeManager;

    public ThemedTabbedPane() {
        super();
        this.themeManager = ThemeManager.getInstance();
        initialize();
    }

    public ThemedTabbedPane(int tabPlacement) {
        super(tabPlacement);
        this.themeManager = ThemeManager.getInstance();
        initialize();
    }

    public ThemedTabbedPane(int tabPlacement, int tabLayoutPolicy) {
        super(tabPlacement, tabLayoutPolicy);
        this.themeManager = ThemeManager.getInstance();
        initialize();
    }

    /**
     * Update a tab's title to include an item count
     * @param index The tab index
     * @param baseTitle The base title (e.g., "My Reviews")
     * @param count The count to display
     */
    public void setTabTitleWithCount(int index, String baseTitle, int count) {
        if (index >= 0 && index < getTabCount()) {
            setTitleAt(index, baseTitle + " (" + count + ")");
        }
    }

    /**
     * Update a tab's title to include an item count
     * @param baseTitle The base title to search for
     * @param count The count to display
     */
    public void setTabTitleWithCount(String baseTitle, int count) {
        for (int i = 0; i < getTabCount(); i++) {
            String currentTitle = getTitleAt(i);
            // Check if current title starts with baseTitle (might already have count)
            if (currentTitle.startsWith(baseTitle)) {
                setTitleAt(i, baseTitle + " (" + count + ")");
                break;
            }
        }
    }

    private void initialize() {
        setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(12)));
        setOpaque(true);

        // Install custom UI for better theming
        setUI(new ThemedTabbedPaneUI());

        applyTheme();
    }

    private void applyTheme() {
        if (themeManager == null) {
            return;
        }

        Theme theme = themeManager.getCurrentTheme();

        setBackground(theme.getBackgroundColor());
        setForeground(theme.getForegroundColor());

        // Remove default border for cleaner look
        setBorder(BorderFactory.createEmptyBorder());
    }

    @Override
    public void updateUI() {
        // Override to prevent Look & Feel from changing our custom UI
        if (themeManager != null) {
            setUI(new ThemedTabbedPaneUI());
            SwingUtilities.invokeLater(this::applyTheme);
        } else {
            super.updateUI();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (themeManager != null) {
            Theme theme = themeManager.getCurrentTheme();
            setBackground(theme.getBackgroundColor());
            setForeground(theme.getForegroundColor());
        }
        super.paintComponent(g);
    }

    /**
     * Custom UI for the tabbed pane that respects theme colors
     */
    private class ThemedTabbedPaneUI extends BasicTabbedPaneUI {

        @Override
        protected void installDefaults() {
            super.installDefaults();

            Theme theme = themeManager.getCurrentTheme();

            // Remove focus indicator
            focus = theme.getAccentColor();
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                         int x, int y, int w, int h, boolean isSelected) {
            Theme theme = themeManager.getCurrentTheme();
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (tabPlacement == TOP) {
                if (isSelected) {
                    // Selected tab - seamless connection to content
                    g2d.setColor(theme.getBackgroundColor());

                    // Create rounded rectangle path for top corners
                    int arc = themeManager.scale(8);

                    // Fill main body
                    g2d.fillRect(x + 1, y + arc/2, w - 2, h - arc/2 + 2);

                    // Fill rounded top
                    g2d.fillRoundRect(x, y, w, arc * 2, arc, arc);

                } else {
                    // Unselected tab - recessed appearance
                    g2d.setColor(theme.getButtonBackground());

                    int arc = themeManager.scale(6);
                    int offset = 3; // Push back slightly

                    g2d.fillRoundRect(x + offset, y + offset + 2, w - (offset * 2), h - offset, arc, arc);
                }
            }
        }

        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                     int x, int y, int w, int h, boolean isSelected) {
            if (!isSelected || tabPlacement != TOP) {
                return; // Only draw border for selected top tabs
            }

            Theme theme = themeManager.getCurrentTheme();
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(theme.getBorderColor());
            g2d.setStroke(new BasicStroke(1.0f));

            int arc = themeManager.scale(8);

            // Create a path for the tab border (excluding bottom)
            java.awt.geom.Path2D path = new java.awt.geom.Path2D.Float();

            // Start from bottom-left
            path.moveTo(x, y + h + 1);

            // Left side
            path.lineTo(x, y + arc);

            // Top-left curve
            path.quadTo(x, y, x + arc, y);

            // Top side
            path.lineTo(x + w - arc, y);

            // Top-right curve
            path.quadTo(x + w, y, x + w, y + arc);

            // Right side
            path.lineTo(x + w, y + h + 1);

            g2d.draw(path);
        }

        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                                int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            Theme theme = themeManager.getCurrentTheme();
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Slight vertical adjustment for visual balance
            int yOffset = isSelected ? -1 : 1; // Selected tabs slightly higher

            // Set text appearance based on selection
            if (isSelected) {
                g2d.setColor(theme.getAccentColor());
                g2d.setFont(font.deriveFont(Font.BOLD, (float)themeManager.scale(13)));
            } else {
                // Slightly muted color for inactive tabs
                Color fgColor = theme.getForegroundColor();
                g2d.setColor(new Color(
                    fgColor.getRed(),
                    fgColor.getGreen(),
                    fgColor.getBlue(),
                    180  // Slightly transparent
                ));
                g2d.setFont(font.deriveFont(Font.PLAIN, (float)themeManager.scale(12)));
            }

            // Draw the text with proper metrics
            FontMetrics fm = g2d.getFontMetrics();
            int textY = textRect.y + fm.getAscent() + yOffset;
            g2d.drawString(title, textRect.x, textY);
        }

        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            if (tabPlacement != TOP) {
                super.paintContentBorder(g, tabPlacement, selectedIndex);
                return;
            }

            Theme theme = themeManager.getCurrentTheme();
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setStroke(new BasicStroke(1.0f));

            int width = tabPane.getWidth();
            int height = tabPane.getHeight();
            Insets insets = tabPane.getInsets();

            int x = insets.left;
            int y = insets.top + calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
            int w = width - insets.right - insets.left;
            int h = height - y - insets.bottom;

            // Fill content background
            g2d.setColor(theme.getBackgroundColor());
            g2d.fillRect(x, y, w, h);

            // Draw content border
            g2d.setColor(theme.getBorderColor());

            // Get selected tab bounds to skip border underneath it
            Rectangle tabBounds = null;
            if (selectedIndex >= 0 && selectedIndex < tabPane.getTabCount()) {
                tabBounds = getTabBounds(tabPane, selectedIndex);
            }

            // Top border - skip the part under the selected tab
            if (tabBounds != null) {
                int tabStart = Math.max(x, tabBounds.x);
                int tabEnd = Math.min(x + w, tabBounds.x + tabBounds.width);

                // Left part of top border (before tab)
                if (tabStart > x) {
                    g2d.drawLine(x, y, tabStart, y);
                }

                // Right part of top border (after tab)
                if (tabEnd < x + w) {
                    g2d.drawLine(tabEnd, y, x + w - 1, y);
                }
            } else {
                // No selected tab, draw full top border
                g2d.drawLine(x, y, x + w - 1, y);
            }

            // Left border
            g2d.drawLine(x, y, x, y + h - 1);

            // Right border
            g2d.drawLine(x + w - 1, y, x + w - 1, y + h - 1);

            // Bottom border
            g2d.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
        }

        @Override
        protected Insets getContentBorderInsets(int tabPlacement) {
            return new Insets(
                themeManager.scale(1),  // Tight top border to connect with tabs
                themeManager.scale(8),
                themeManager.scale(8),
                themeManager.scale(8)
            );
        }

        @Override
        protected Insets getTabAreaInsets(int tabPlacement) {
            return new Insets(
                themeManager.scale(4),
                themeManager.scale(8),
                themeManager.scale(0),  // No bottom margin for tabs
                themeManager.scale(8)
            );
        }

        @Override
        protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
            return themeManager.scale(38);  // Slightly taller for better proportions
        }

        @Override
        protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
            int width = super.calculateTabWidth(tabPlacement, tabIndex, metrics);
            return width + themeManager.scale(32); // More horizontal padding
        }

        @Override
        protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
            return 0; // No vertical shift
        }

        @Override
        protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
            return 0; // No horizontal shift
        }
    }
}



