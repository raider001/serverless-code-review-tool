package com.kalynx.serverlessreviewtool.theme.icons;

import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.QuickButton;

import java.awt.*;

/**
 * HamburgerIcon - Self-contained hamburger menu icon painter
 * Draws three horizontal lines (hamburger menu symbol)
 */
public class HamburgerIcon implements QuickButton.IconPainter {

    @Override
    public void paint(Graphics2D g2d, int width, int height, Color foreground) {
        int centerY = height / 2;
        int lineWidth = width / 3;
        int lineThickness = Math.max(2, height / 25);
        int spacing = height / 7;
        int startX = (width - lineWidth) / 2;

        g2d.setColor(foreground);
        g2d.setStroke(new BasicStroke(lineThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Three horizontal lines
        g2d.drawLine(startX, centerY - spacing, startX + lineWidth, centerY - spacing);
        g2d.drawLine(startX, centerY, startX + lineWidth, centerY);
        g2d.drawLine(startX, centerY + spacing, startX + lineWidth, centerY + spacing);
    }
}

