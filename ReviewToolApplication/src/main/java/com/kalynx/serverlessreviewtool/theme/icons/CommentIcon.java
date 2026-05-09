package com.kalynx.serverlessreviewtool.theme.icons;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.Icon;
import java.awt.*;

/**
 * CommentIcon - Theme-aware icon showing a comment indicator
 * Displays a speech bubble indicating a comment on a line
 */
public class CommentIcon implements Icon {
    private final int size;
    private final Theme theme;
    private final int commentCount;

    public CommentIcon(int size, int commentCount) {
        this.size = size;
        this.theme = ThemeManager.getInstance().getCurrentTheme();
        this.commentCount = commentCount;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color accentColor = theme.getAccentColor();

        int bubbleX = x + 2;
        int bubbleY = y + 2;
        int bubbleWidth = size - 4;
        int bubbleHeight = size - 6;
        int cornerRadius = 2;
        int tailSize = 2;

        // Draw speech bubble body (filled)
        g2d.setColor(accentColor);
        g2d.fillRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight - tailSize, cornerRadius, cornerRadius);

        // Draw tail pointing down
        int[] tailXPoints = {bubbleX + bubbleWidth / 2 - 2, bubbleX + bubbleWidth / 2 + 2, bubbleX + bubbleWidth / 2};
        int[] tailYPoints = {bubbleY + bubbleHeight - tailSize, bubbleY + bubbleHeight - tailSize, bubbleY + bubbleHeight};
        g2d.fillPolygon(tailXPoints, tailYPoints, 3);

        // Draw border
        g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 200));
        g2d.drawRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight - tailSize, cornerRadius, cornerRadius);

        // Draw comment count if > 1
        if (commentCount > 1) {
            String countStr = String.valueOf(Math.min(commentCount, 9)); // Max single digit
            Font smallFont = new Font("Segoe UI", Font.BOLD, Math.max(6, size / 3));
            FontMetrics fm = g2d.getFontMetrics(smallFont);

            // White text on accent background
            g2d.setColor(Color.WHITE);
            g2d.setFont(smallFont);
            int textX = bubbleX + (bubbleWidth - fm.stringWidth(countStr)) / 2;
            int textY = bubbleY + ((bubbleHeight - tailSize - fm.getHeight()) / 2) + fm.getAscent();
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


