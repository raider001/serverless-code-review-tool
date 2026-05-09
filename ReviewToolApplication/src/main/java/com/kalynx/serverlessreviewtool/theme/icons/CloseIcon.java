package com.kalynx.serverlessreviewtool.theme.icons;

import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.QuickButton;

import java.awt.*;

/**
 * CloseIcon - Self-contained close window icon painter
 * Draws an X (close symbol)
 */
public class CloseIcon implements QuickButton.IconPainter {

    @Override
    public void paint(Graphics2D g2d, int width, int height, Color foreground) {
        int size = Math.min(width, height) / 3;
        int centerX = width / 2;
        int centerY = height / 2;
        int halfSize = size / 2;
        int lineThickness = Math.max(2, height / 20);

        g2d.setColor(foreground);
        g2d.setStroke(new BasicStroke(lineThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Draw X (two diagonal lines)
        g2d.drawLine(centerX - halfSize, centerY - halfSize, centerX + halfSize, centerY + halfSize);
        g2d.drawLine(centerX + halfSize, centerY - halfSize, centerX - halfSize, centerY + halfSize);
    }
}

