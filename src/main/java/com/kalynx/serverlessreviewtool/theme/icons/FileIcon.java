package com.kalynx.serverlessreviewtool.theme.icons;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.Icon;
import java.awt.*;

/**
 * FileIcon - Theme-aware icon for files
 * Shows a document with accent-colored right edge (modern flat style)
 */
public class FileIcon implements Icon {
    private final int size;
    private final Theme theme;

    public FileIcon(int size) {
        this.size = size;
        this.theme = ThemeManager.getInstance().getCurrentTheme();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color foreground = theme.getForegroundColor();
        Color accentColor = theme.getAccentColor();

        int docX = x + 2;
        int docY = y + 1;
        int docWidth = size - 4;
        int docHeight = size - 3;

        // Draw main document body (white/background filled)
        g2d.setColor(new Color(foreground.getRed(), foreground.getGreen(), foreground.getBlue(), 30));
        g2d.fillRect(docX, docY, docWidth, docHeight);

        // Draw left and bottom edge
        g2d.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(foreground);
        g2d.drawLine(docX, docY, docX, docY + docHeight);
        g2d.drawLine(docX, docY + docHeight, docX + docWidth, docY + docHeight);

        // Draw RIGHT edge with accent color (modern flat style detail)
        g2d.setColor(accentColor);
        int accentWidth = 2;
        g2d.fillRect(docX + docWidth - accentWidth, docY, accentWidth, docHeight);

        // Draw top edge
        g2d.setColor(foreground);
        g2d.drawLine(docX, docY, docX + docWidth - accentWidth, docY);

        // Draw horizontal lines (text representation)
        int lineSpacing = docHeight / 4;
        int lineX = docX + 2;
        int lineWidth = docWidth - 4;
        int lineThickness = 1;

        g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 160));
        g2d.setStroke(new BasicStroke(lineThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (int i = 1; i < 3; i++) {
            int lineY = docY + (lineSpacing * i);
            int currentLineWidth = lineWidth;
            if (i == 2) {
                currentLineWidth = (int) (lineWidth * 0.6); // Last line shorter
            }
            g2d.drawLine(lineX, lineY, lineX + currentLineWidth, lineY);
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




