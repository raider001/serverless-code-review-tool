package com.kalynx.serverlessreviewtool.theme.icons;

import java.awt.*;
import javax.swing.Icon;

/**
 * AlertIcon - Theme-aware warning/alert icon
 * Displays a triangle with exclamation mark for unresolved issues
 */
public class AlertIcon implements Icon {
    private final int size;
    private final Color color;

    public AlertIcon(int size, Color color) {
        this.size = size;
        this.color = color;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int padding = 2;
        int triangleSize = size - (padding * 2);

        int centerX = x + size / 2;
        int topY = y + padding;
        int bottomY = y + size - padding;

        int[] xPoints = {
            centerX,
            x + padding,
            x + size - padding
        };
        int[] yPoints = {
            topY,
            bottomY,
            bottomY
        };

        g2d.setColor(color);
        g2d.fillPolygon(xPoints, yPoints, 3);

        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 200));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawPolygon(xPoints, yPoints, 3);

        g2d.setColor(Color.WHITE);

        int exclamationCenterY = y + size / 2 + padding / 2;

        int barHeight = (int) (triangleSize * 0.35);
        int barWidth = Math.max(2, size / 8);
        int barX = centerX - barWidth / 2;
        int barY = exclamationCenterY - barHeight - padding;

        g2d.fillRect(barX, barY, barWidth, barHeight);

        int dotSize = Math.max(2, size / 7);
        int dotX = centerX - dotSize / 2;
        int dotY = exclamationCenterY + padding / 2;

        g2d.fillOval(dotX, dotY, dotSize, dotSize);
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}

