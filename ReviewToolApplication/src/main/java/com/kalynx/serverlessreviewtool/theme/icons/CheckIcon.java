package com.kalynx.serverlessreviewtool.theme.icons;

import java.awt.*;
import javax.swing.Icon;

/**
 * CheckIcon - Theme-aware checkmark icon
 * Displays a checkmark for resolved issues
 */
public class CheckIcon implements Icon {
    private final int size;
    private final Color color;

    public CheckIcon(int size, Color color) {
        this.size = size;
        this.color = color;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int padding = 2;
        int checkSize = size - (padding * 2);

        g2d.setColor(color);
        g2d.fillOval(x + padding, y + padding, checkSize, checkSize);

        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 200));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(x + padding, y + padding, checkSize, checkSize);

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int centerX = x + size / 2;
        int centerY = y + size / 2;

        int checkWidth = (int) (checkSize * 0.5);
        int checkHeight = (int) (checkSize * 0.6);

        int startX = centerX - checkWidth / 3;
        int startY = centerY;

        int midX = centerX - checkWidth / 6;
        int midY = centerY + checkHeight / 3;

        int endX = centerX + checkWidth / 2;
        int endY = centerY - checkHeight / 4;

        g2d.drawLine(startX, startY, midX, midY);
        g2d.drawLine(midX, midY, endX, endY);
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

