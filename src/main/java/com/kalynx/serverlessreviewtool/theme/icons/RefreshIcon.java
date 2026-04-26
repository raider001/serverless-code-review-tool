package com.kalynx.serverlessreviewtool.theme.icons;

import com.kalynx.serverlessreviewtool.theme.components.QuickButton;

import java.awt.*;
import java.awt.geom.*;

/**
 * RefreshIcon – IconPainter for a circular refresh arrow at any size.
 * Draws a clean circular arrow with a prominent arrowhead to clearly indicate refresh action.
 */
public class RefreshIcon implements QuickButton.IconPainter {

    @Override
    public void paint(Graphics2D g2d, int width, int height, Color foreground) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g2d.setColor(foreground);

        int cx = width / 2;
        int cy = height / 2;
        // Use same sizing as other icons (1/3 of button size)
        int r = Math.min(width, height) / 6;

        float stroke = Math.max(1.5f, Math.min(width, height) / 16f);
        g2d.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Draw circular arc (270 degrees, leaving space for arrow)
        Arc2D arc = new Arc2D.Double(cx - r, cy - r, r * 2, r * 2, 90, -270, Arc2D.OPEN);
        g2d.draw(arc);

        // Calculate arrowhead position at the top (90 degrees)
        double arrowAngle = Math.toRadians(90);
        double arrowX = cx + r * Math.cos(arrowAngle);
        double arrowY = cy - r * Math.sin(arrowAngle);

        // Draw prominent arrowhead pointing clockwise
        int headSize = Math.max(4, Math.min(width, height) / 8);

        GeneralPath arrow = new GeneralPath();
        arrow.moveTo(arrowX, arrowY);
        arrow.lineTo(arrowX - headSize, arrowY - headSize * 0.6);
        arrow.lineTo(arrowX - headSize * 0.3, arrowY);
        arrow.lineTo(arrowX - headSize, arrowY + headSize * 0.6);
        arrow.closePath();

        g2d.fill(arrow);
    }
}



