package com.kalynx.serverlessreviewtool.theme.icons;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * AppIcon - Programmatically generated application icon for the Serverless Review Tool.
 *
 * Design:
 *   ┌──────────────────┐
 *   │  ▬▬▬▬▬▬▬▬        │   ← code line 1
 *   │  ▬▬▬▬▬▬▬▬▬▬▬▬▬   │   ← code line 2 (longer)
 *   │  ▬▬▬▬▬▬           │   ← code line 3 (short)
 *   │             ⊙     │   ← magnifying glass (accent)
 *   └──────────────────┘
 *
 * Rendered at 16, 32, 48, 64, 128 px for all platform icon slots.
 */
public class AppIcon {

    // Palette – fixed so the icon looks good on any desktop regardless of app theme
    private static final Color BG_TOP    = new Color(0x1A2744);   // deep navy
    private static final Color BG_BOT    = new Color(0x0D1B38);   // darker navy
    private static final Color LINE_1    = new Color(0xCDD9F5);   // light blue-white  (unchanged line)
    private static final Color LINE_2    = new Color(0x4ADDAB);   // teal-green        (added line)
    private static final Color LINE_3    = new Color(0xFF7A7A);   // coral-red         (removed line)
    private static final Color GLASS_RIM = new Color(0xFFFFFF);
    private static final Color GLASS_FILL= new Color(0x4A90E2, true);  // semi-transparent blue

    /** Returns a list of images at standard icon sizes suitable for {@code JFrame.setIconImages}. */
    public static List<Image> createIconImages() {
        return List.of(
            createIcon(16),
            createIcon(32),
            createIcon(48),
            createIcon(64),
            createIcon(128)
        );
    }

    /** Renders the icon at an arbitrary pixel size. */
    public static BufferedImage createIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,    RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

        // ── Background: vertical gradient, rounded square ──────────────────
        int arc = Math.max(3, size / 5);
        GradientPaint grad = new GradientPaint(0, 0, BG_TOP, 0, size, BG_BOT);
        g.setPaint(grad);
        g.fillRoundRect(0, 0, size, size, arc, arc);

        // ── Code lines ─────────────────────────────────────────────────────
        // Three horizontal bars in the upper-left region
        int mx  = (int)(size * 0.15);          // left margin
        int my  = (int)(size * 0.18);          // top margin for first line
        int lh  = (int)(size * 0.14);          // line height (spacing)
        int lth = Math.max(1, size / 18);      // line thickness

        int[] lineWidths = {
            (int)(size * 0.52),   // line 1 – medium  (unchanged, light)
            (int)(size * 0.68),   // line 2 – long    (added,     green)
            (int)(size * 0.38),   // line 3 – short   (removed,   red)
        };
        Color[] lineColors = { LINE_1, LINE_2, LINE_3 };

        g.setStroke(new BasicStroke(lth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 3; i++) {
            int y = my + i * lh + lh / 2;
            g.setColor(lineColors[i]);
            g.draw(new Line2D.Float(mx, y, mx + lineWidths[i], y));
        }

        // ── Magnifying glass (bottom-right) ────────────────────────────────
        double glassR  = size * 0.20;          // lens radius
        double cx      = size * 0.72;          // lens centre x
        double cy      = size * 0.72;          // lens centre y
        double handleL = size * 0.16;          // handle length
        double handleA = Math.toRadians(45);   // handle angle (bottom-right)

        // Lens fill (translucent)
        g.setColor(new Color(GLASS_FILL.getRed(), GLASS_FILL.getGreen(), GLASS_FILL.getBlue(), 80));
        g.fill(new Ellipse2D.Double(cx - glassR, cy - glassR, glassR * 2, glassR * 2));

        // Lens rim
        float rimStroke = Math.max(1f, size / 22f);
        g.setStroke(new BasicStroke(rimStroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(GLASS_RIM);
        g.draw(new Ellipse2D.Double(cx - glassR, cy - glassR, glassR * 2, glassR * 2));

        // Handle
        double hx1 = cx + Math.cos(handleA) * glassR;
        double hy1 = cy + Math.sin(handleA) * glassR;
        double hx2 = hx1 + Math.cos(handleA) * handleL;
        double hy2 = hy1 + Math.sin(handleA) * handleL;
        g.setStroke(new BasicStroke(rimStroke * 1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(hx1, hy1, hx2, hy2));

        g.dispose();
        return img;
    }
}

