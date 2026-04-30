package com.kalynx.serverlessreviewtool.theme.icons;

import javax.swing.Icon;
import java.awt.*;

/**
 * FileCommentIcon - Shows comment status indicator for files
 * Displays a colored dot to show if file has comments (orange=unresolved, green=resolved)
 * Can optionally show comment count
 */
public class FileCommentIcon implements Icon {
    private final int size;
    private final Color color;
    private final int commentCount;
    private final boolean showCount;

    public FileCommentIcon(int size, Color color, int commentCount, boolean showCount) {
        this.size = size;
        this.color = color;
        this.commentCount = commentCount;
        this.showCount = showCount;
    }

    public FileCommentIcon(int size, Color color) {
        this(size, color, 0, false);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int dotSize = size - 2;
        int dotX = x + 1;
        int dotY = y + 1;

        // Draw white background for contrast
        g2d.setColor(Color.WHITE);
        g2d.fillOval(dotX, dotY, dotSize, dotSize);

        // Draw colored dot
        g2d.setColor(color);
        g2d.fillOval(dotX + 1, dotY + 1, dotSize - 2, dotSize - 2);

        // Draw border
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 230));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(dotX, dotY, dotSize, dotSize);

        if (showCount && commentCount > 0) {
            String countStr = commentCount > 9 ? "9+" : String.valueOf(commentCount);
            Font smallFont = new Font("Segoe UI", Font.BOLD, Math.max(5, size / 2));
            FontMetrics fm = g2d.getFontMetrics(smallFont);

            g2d.setColor(Color.WHITE);
            g2d.setFont(smallFont);
            int textX = dotX + (dotSize - fm.stringWidth(countStr)) / 2;
            int textY = dotY + ((dotSize - fm.getHeight()) / 2) + fm.getAscent();
            g2d.drawString(countStr, textX, textY);
        }
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


