package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * ThemedMenuItem - A theme-aware menu item component.
 * Provides hover effects and consistent styling with the application theme.
 */
public class ThemedMenuItem extends JMenuItem {

    private final ThemeManager themeManager;
    private boolean isHovered = false;

    public ThemedMenuItem(String text) {
        super(text);
        this.themeManager = ThemeManager.getInstance();
        initializeMenuItem();
    }

    public ThemedMenuItem(String text, Icon icon) {
        super(text, icon);
        this.themeManager = ThemeManager.getInstance();
        initializeMenuItem();
    }

    private void initializeMenuItem() {
        Theme theme = themeManager.getCurrentTheme();

        setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(13)));
        setOpaque(true);
        setBorderPainted(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        setBackground(theme.getInputBackground());
        setForeground(theme.getForegroundColor());

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
        });
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(
            Math.max(d.width, themeManager.scale(150)),
            themeManager.scale(28)
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        Theme theme = themeManager.getCurrentTheme();

        if (isHovered && isEnabled()) {
            g2d.setColor(theme.getAccentColor());
            g2d.fillRect(0, 0, getWidth(), getHeight());
        } else {
            g2d.setColor(theme.getInputBackground());
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        g2d.dispose();
        super.paintComponent(g);
    }

    @Override
    public void paint(Graphics g) {
        Theme theme = themeManager.getCurrentTheme();

        if (isHovered && isEnabled()) {
            setForeground(Color.WHITE);
        } else if (!isEnabled()) {
            setForeground(new Color(
                theme.getForegroundColor().getRed(),
                theme.getForegroundColor().getGreen(),
                theme.getForegroundColor().getBlue(),
                100
            ));
        } else {
            setForeground(theme.getForegroundColor());
        }

        super.paint(g);
    }
}








