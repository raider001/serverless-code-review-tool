package com.kalynx.serverlessreviewtool.theme.icons;

import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.QuickButton;

import java.awt.*;

/**
 * MinimizeIcon - Self-contained minimize window icon painter
 * Draws a horizontal line (minimize symbol)
 */
public class MinimizeIcon implements QuickButton.IconPainter {

    @Override
    public void paint(Graphics2D g2d, int width, int height, Color foreground) {
        int centerY = height / 2;
        int lineWidth = width / 3;
        int lineThickness = Math.max(2, height / 20);
        int startX = (width - lineWidth) / 2;

        g2d.setColor(foreground);
        g2d.setStroke(new BasicStroke(lineThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Single horizontal line
        g2d.drawLine(startX, centerY, startX + lineWidth, centerY);
    }
}

