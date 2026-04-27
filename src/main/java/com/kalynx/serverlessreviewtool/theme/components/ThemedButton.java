package com.kalynx.serverlessreviewtool.theme.components;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * ThemedButton - A modern, flat, theme-aware button component
 *
 * Clean implementation that calculates colors on-demand during paint
 * without permanently modifying component state.
 */
public class ThemedButton extends JButton {

    private final ThemeManager themeManager;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private boolean useAccentStyle = false;

    public ThemedButton(String text) {
        super(text);
        this.themeManager = ThemeManager.getInstance();
        initializeButton();
    }

    public ThemedButton() {
        this("");
    }

    private void initializeButton() {
        // Modern flat button style
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false); // We'll paint it ourselves
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(12)));

        // Add padding via border
        setBorder(BorderFactory.createEmptyBorder(
            themeManager.scale(8),
            themeManager.scale(16),
            themeManager.scale(8),
            themeManager.scale(16)
        ));

        // Add hover/press listeners
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    /**
     * Set whether this button should use accent color styling (primary button)
     */
    public void setAccentStyle(boolean useAccentStyle) {
        this.useAccentStyle = useAccentStyle;
        // Update font weight for accent buttons
        if (useAccentStyle) {
            setFont(getFont().deriveFont(Font.BOLD));
        } else {
            setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(12)));
        }
        repaint();
    }

    public boolean isAccentStyle() {
        return useAccentStyle;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Theme theme = themeManager.getCurrentTheme();

        // Calculate colors based on current state
        Color bgColor = getButtonBackgroundColor(theme);
        Color fgColor = getButtonForegroundColor(theme);

        // Paint background
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Set foreground for text
        setForeground(fgColor);

        g2d.dispose();

        // Paint text and icon
        super.paintComponent(g);
    }

    /**
     * Calculate background color based on current button state
     */
    private Color getButtonBackgroundColor(Theme theme) {
        if (!isEnabled()) {
            // Disabled state - more transparent
            Color baseColor = useAccentStyle ? theme.getAccentColor() : theme.getButtonBackground();
            return new Color(
                baseColor.getRed(),
                baseColor.getGreen(),
                baseColor.getBlue(),
                100
            );
        }

        if (useAccentStyle) {
            // Accent/Primary button styling
            if (isPressed) {
                return adjustBrightness(theme.getAccentColor(), 0.8f);
            } else if (isHovered) {
                return adjustBrightness(theme.getAccentColor(), 1.1f);
            } else {
                return theme.getAccentColor();
            }
        } else {
            // Normal button styling
            if (isPressed) {
                return adjustBrightness(theme.getAccentColor(), 0.8f);
            } else if (isHovered) {
                return theme.getAccentColor();
            } else {
                return theme.getButtonBackground();
            }
        }
    }

    /**
     * Calculate foreground color based on current button state
     */
    private Color getButtonForegroundColor(Theme theme) {
        if (!isEnabled()) {
            // Disabled state - more transparent
            Color baseColor = useAccentStyle ? Color.WHITE : theme.getButtonForeground();
            return new Color(
                baseColor.getRed(),
                baseColor.getGreen(),
                baseColor.getBlue(),
                100
            );
        }

        if (useAccentStyle) {
            return Color.WHITE;
        } else {
            // Normal button - white text when hovered/pressed, normal color otherwise
            if (isPressed || isHovered) {
                return Color.WHITE;
            } else {
                return theme.getButtonForeground();
            }
        }
    }

    /**
     * Adjust brightness of a color by a factor
     * Factor > 1.0 makes it brighter, < 1.0 makes it darker
     */
    private Color adjustBrightness(Color color, float factor) {
        if (factor > 1.0f) {
            // Lighten
            float f = factor - 1.0f;
            int r = Math.min((int)(color.getRed() + (255 - color.getRed()) * f), 255);
            int g = Math.min((int)(color.getGreen() + (255 - color.getGreen()) * f), 255);
            int b = Math.min((int)(color.getBlue() + (255 - color.getBlue()) * f), 255);
            return new Color(r, g, b);
        } else {
            // Darken
            int r = (int)(color.getRed() * factor);
            int g = (int)(color.getGreen() * factor);
            int b = (int)(color.getBlue() * factor);
            return new Color(r, g, b);
        }
    }
}
