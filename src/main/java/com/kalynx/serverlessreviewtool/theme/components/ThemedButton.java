package com.kalynx.serverlessreviewtool.theme.components;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * ThemedButton - A modern, flat, theme-aware button component
 */
public class ThemedButton extends JButton {

    private final ThemeManager themeManager;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private boolean useAccentStyle = false; // For primary/accent-styled buttons

    public ThemedButton(String text) {
        super(text);
        this.themeManager = ThemeManager.getInstance();

        // Modern flat button style
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(true);
        setOpaque(true);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(12)));

        // Add padding via border
        setBorder(BorderFactory.createEmptyBorder(
            themeManager.scale(8),
            themeManager.scale(16),
            themeManager.scale(8),
            themeManager.scale(16)
        ));

        // Add hover effects
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public ThemedButton() {
        this("");
    }

    /**
     * Set whether this button should use accent color styling (primary button)
     */
    public void setAccentStyle(boolean useAccentStyle) {
        this.useAccentStyle = useAccentStyle;
        repaint();
    }

    public boolean isAccentStyle() {
        return useAccentStyle;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (themeManager != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Theme theme = themeManager.getCurrentTheme();

            // Calculate background color based on state
            Color bgColor;
            Color fgColor;

            if (!isEnabled()) {
                // Disabled state - more transparent
                if (useAccentStyle) {
                    bgColor = new Color(
                        theme.getAccentColor().getRed(),
                        theme.getAccentColor().getGreen(),
                        theme.getAccentColor().getBlue(),
                        100
                    );
                    fgColor = new Color(255, 255, 255, 100);
                } else {
                    bgColor = new Color(
                        theme.getButtonBackground().getRed(),
                        theme.getButtonBackground().getGreen(),
                        theme.getButtonBackground().getBlue(),
                        100
                    );
                    fgColor = new Color(
                        theme.getButtonForeground().getRed(),
                        theme.getButtonForeground().getGreen(),
                        theme.getButtonForeground().getBlue(),
                        100
                    );
                }
            } else if (useAccentStyle) {
                // Accent/Primary button styling
                if (isPressed) {
                    bgColor = darkenColor(theme.getAccentColor(), 0.2f);
                } else if (isHovered) {
                    bgColor = lightenColor(theme.getAccentColor(), 0.1f);
                } else {
                    bgColor = theme.getAccentColor();
                }
                fgColor = Color.WHITE;
                // Make accent buttons bold
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                // Normal button styling
                if (isPressed) {
                    bgColor = darkenColor(theme.getAccentColor(), 0.2f);
                    fgColor = Color.WHITE;
                } else if (isHovered) {
                    bgColor = theme.getAccentColor();
                    fgColor = Color.WHITE;
                } else {
                    bgColor = theme.getButtonBackground();
                    fgColor = theme.getButtonForeground();
                }
                setFont(getFont().deriveFont(Font.PLAIN));
            }

            setBackground(bgColor);
            setForeground(fgColor);

            g2d.dispose();
        }
        super.paintComponent(g);
    }

    /**
     * Lighten a color by a factor
     */
    private Color lightenColor(Color color, float factor) {
        int r = Math.min((int)(color.getRed() + (255 - color.getRed()) * factor), 255);
        int g = Math.min((int)(color.getGreen() + (255 - color.getGreen()) * factor), 255);
        int b = Math.min((int)(color.getBlue() + (255 - color.getBlue()) * factor), 255);
        return new Color(r, g, b);
    }

    /**
     * Darken a color by a factor
     */
    private Color darkenColor(Color color, float factor) {
        int r = Math.max((int)(color.getRed() * (1 - factor)), 0);
        int g = Math.max((int)(color.getGreen() * (1 - factor)), 0);
        int b = Math.max((int)(color.getBlue() * (1 - factor)), 0);
        return new Color(r, g, b);
    }
}

