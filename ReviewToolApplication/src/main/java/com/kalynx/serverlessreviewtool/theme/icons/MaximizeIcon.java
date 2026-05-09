package com.kalynx.serverlessreviewtool.theme.icons;

import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.QuickButton;

import java.awt.*;

/**
 * MaximizeIcon - Self-contained maximize window icon painter
 * Draws a rectangular outline (maximize symbol)
 */
public class MaximizeIcon implements QuickButton.IconPainter {

    @Override
    public void paint(Graphics2D g2d, int width, int height, Color foreground) {
        int size = Math.min(width, height) / 3;
        int centerX = width / 2;
        int centerY = height / 2;
        int x = centerX - size / 2;
        int y = centerY - size / 2;
        int lineThickness = Math.max(2, height / 20);

        g2d.setColor(foreground);
        g2d.setStroke(new BasicStroke(lineThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Draw rectangle outline
        g2d.drawRect(x, y, size, size);
    }
}

