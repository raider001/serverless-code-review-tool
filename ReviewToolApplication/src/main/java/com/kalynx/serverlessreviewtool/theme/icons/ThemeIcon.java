package com.kalynx.serverlessreviewtool.theme.icons;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * ThemeIcon - Generic icon for any ThemedFrame application.
 *
 * Design: a rounded square split diagonally top-left (dark) ↔ bottom-right (light),
 * with a glowing accent line along the split and a sun/moon pair either side —
 * immediately communicating "theme / light-dark switching".
 *
 *   ╭──────────────╮
 *   │ ◐  dark  ╲   │
 *   │       ╲  light │
 *   │        ╲  ◑  │
 *   ╰──────────────╯
 */
public class ThemeIcon {

    private static final Color DARK_BG   = new Color(0x1A2744);
    private static final Color LIGHT_BG  = new Color(0xEDF2FB);
    private static final Color ACCENT    = new Color(0x4A90E2);
    private static final Color DARK_FG   = new Color(0xCDD9F5);
    private static final Color LIGHT_FG  = new Color(0x2C3E6B);

    public static List<Image> createIconImages() {
        return List.of(
            createIcon(16),
            createIcon(32),
            createIcon(48),
            createIcon(64),
            createIcon(128)
        );
    }

    public static BufferedImage createIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,      RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int arc = Math.max(3, size / 5);
        Shape clip = new RoundRectangle2D.Float(0, 0, size, size, arc, arc);
        g.setClip(clip);

        // ── Dark half (top-left triangle) ──────────────────────────────────
        g.setColor(DARK_BG);
        g.fill(new Polygon(
            new int[]{0, size, 0},
            new int[]{0, 0,    size},
            3
        ));

        // ── Light half (bottom-right triangle) ────────────────────────────
        g.setColor(LIGHT_BG);
        g.fill(new Polygon(
            new int[]{size, size, 0},
            new int[]{0,    size, size},
            3
        ));

        // ── Accent dividing line with glow ─────────────────────────────────
        float lineW = Math.max(1.5f, size / 16f);
        // Soft outer glow passes
        float[] glowWidths  = { lineW * 5, lineW * 3, lineW * 1.5f };
        int[]   glowAlphas  = { 30, 60, 120 };
        for (int i = 0; i < glowWidths.length; i++) {
            g.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), glowAlphas[i]));
            g.setStroke(new BasicStroke(glowWidths[i], BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            g.drawLine(size, 0, 0, size);
        }
        // Solid accent line
        g.setColor(ACCENT);
        g.setStroke(new BasicStroke(lineW, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g.drawLine(size, 0, 0, size);

        g.setClip(null);

        // ── Moon symbol on dark side ───────────────────────────────────────
        double sym = size * 0.18;       // symbol radius
        double mx  = size * 0.28;
        double my  = size * 0.28;

        // Full circle
        g.setColor(DARK_FG);
        g.fill(new Ellipse2D.Double(mx - sym, my - sym, sym * 2, sym * 2));
        // Bite out to make crescent
        double bite = sym * 0.6;
        g.setColor(DARK_BG);
        g.fill(new Ellipse2D.Double(mx - sym * 0.4, my - sym, bite * 2, bite * 2));

        // ── Sun symbol on light side ───────────────────────────────────────
        double sx  = size * 0.72;
        double sy  = size * 0.72;
        double sr  = sym * 0.55;

        g.setColor(LIGHT_FG);
        g.fill(new Ellipse2D.Double(sx - sr, sy - sr, sr * 2, sr * 2));

        float rayW = Math.max(1f, (float) sym / 5f);
        g.setStroke(new BasicStroke(rayW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 8; i++) {
            double angle   = Math.toRadians(i * 45);
            double inner   = sr + sym * 0.22;
            double outer   = inner + sym * 0.28;
            g.draw(new Line2D.Double(
                sx + Math.cos(angle) * inner, sy + Math.sin(angle) * inner,
                sx + Math.cos(angle) * outer,  sy + Math.sin(angle) * outer
            ));
        }

        g.dispose();
        return img;
    }
}

