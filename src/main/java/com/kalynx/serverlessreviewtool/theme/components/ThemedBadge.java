package com.kalynx.serverlessreviewtool.theme.components;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * ThemedBadge - A compact, pill-shaped tag component.
 *
 * Clicking anywhere on the badge triggers removal.
 * Hover darkens the badge slightly to signal interactivity.
 */
public class ThemedBadge extends JPanel {

    private final ThemeManager themeManager;
    private String text;
    private boolean hovering = false;
    private Color customColor = null;

    private static final int PAD_H        = 10;
    private static final int PAD_V        = 4;
    private static final int CORNER       = 12;

    public ThemedBadge(String text) {
        this(text, null);
    }

    public ThemedBadge(String text, Runnable onRemove) {
        this.text        = text;
        this.themeManager = ThemeManager.getInstance();

        setOpaque(false);
        setLayout(null);

        if (onRemove != null) {
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText("Click to remove");
        }

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovering = true;  repaint(); }
            @Override public void mouseExited (MouseEvent e) { hovering = false; repaint(); }
            @Override public void mouseClicked(MouseEvent e) { if (onRemove != null) onRemove.run(); }
        });
    }

    // ── painting ──────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        Theme theme = themeManager.getCurrentTheme();
        int   w     = getWidth();
        int   h     = getHeight();
        int   arc   = themeManager.scale(CORNER);

        // Pill background – use customColor if set, otherwise blend accent with bg
        Color base = (customColor != null)
            ? customColor
            : blendColor(theme.getAccentColor(), theme.getBackgroundColor(), 0.55f);
        Color fill = hovering ? darken(base, 0.12f) : base;
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, w, h, arc, arc);

        // Label
        Font font = new Font("Segoe UI", Font.PLAIN, themeManager.scale(11));
        g2.setFont(font);
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        int tx = themeManager.scale(PAD_H);
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, tx, ty);

        g2.dispose();
    }

    // ── sizing ────────────────────────────────────────────────────────────────

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(new Font("Segoe UI", Font.PLAIN, themeManager.scale(11)));
        int w = themeManager.scale(PAD_H) * 2 + fm.stringWidth(text);
        int h = fm.getAscent() + fm.getDescent() + themeManager.scale(PAD_V) * 2;
        return new Dimension(w, h);
    }

    @Override public Dimension getMinimumSize() { return getPreferredSize(); }
    @Override public Dimension getMaximumSize() { return getPreferredSize(); }

    /** Override the pill colour. Pass {@code null} to restore the default blended accent. */
    public ThemedBadge setCustomColor(Color color) {
        this.customColor = color;
        repaint();
        return this;
    }

    /**
     * Update the badge text
     */
    public void setText(String text) {
        this.text = text;
        revalidate();
        repaint();
    }

    private Color blendColor(Color a, Color b, float ratio) {
        float inv = 1f - ratio;
        return new Color(
            Math.min(Math.round(a.getRed()   * ratio + b.getRed()   * inv), 255),
            Math.min(Math.round(a.getGreen() * ratio + b.getGreen() * inv), 255),
            Math.min(Math.round(a.getBlue()  * ratio + b.getBlue()  * inv), 255)
        );
    }

    private Color darken(Color c, float factor) {
        return new Color(
            Math.max((int)(c.getRed()   * (1 - factor)), 0),
            Math.max((int)(c.getGreen() * (1 - factor)), 0),
            Math.max((int)(c.getBlue()  * (1 - factor)), 0)
        );
    }
}











