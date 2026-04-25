package com.kalynx.serverlessreviewtool.theme.icons;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.Icon;
import java.awt.*;

/**
 * RepositoryIcon - Theme-aware icon for git repositories
 * Shows stacked code brackets representing a repository
 */
public class RepositoryIcon implements Icon {
    private final int size;
    private final Theme theme;

    public RepositoryIcon(int size) {
        this.size = size;
        this.theme = ThemeManager.getInstance().getCurrentTheme();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color foreground = theme.getForegroundColor();
        Color accentColor = theme.getAccentColor();

        int centerX = x + size / 2;
        int centerY = y + size / 2;
        int bracketHeight = size - 5;
        int bracketWidth = size / 3;
        int spacing = 2;

        g2d.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Left bracket (code opening brace style)
        g2d.setColor(foreground);
        g2d.drawLine(centerX - bracketWidth / 2, centerY - bracketHeight / 2,
                     centerX - bracketWidth / 2 - 2, centerY - bracketHeight / 2);
        g2d.drawLine(centerX - bracketWidth / 2 - 2, centerY - bracketHeight / 2,
                     centerX - bracketWidth / 2 - 2, centerY);
        g2d.drawLine(centerX - bracketWidth / 2 - 2, centerY,
                     centerX - bracketWidth / 2, centerY + bracketHeight / 2);
        g2d.drawLine(centerX - bracketWidth / 2, centerY + bracketHeight / 2,
                     centerX - bracketWidth / 2, centerY + bracketHeight / 2 - 1);

        // Right bracket
        g2d.setColor(accentColor);
        g2d.drawLine(centerX + bracketWidth / 2, centerY - bracketHeight / 2,
                     centerX + bracketWidth / 2 + 2, centerY - bracketHeight / 2);
        g2d.drawLine(centerX + bracketWidth / 2 + 2, centerY - bracketHeight / 2,
                     centerX + bracketWidth / 2 + 2, centerY);
        g2d.drawLine(centerX + bracketWidth / 2 + 2, centerY,
                     centerX + bracketWidth / 2, centerY + bracketHeight / 2);
        g2d.drawLine(centerX + bracketWidth / 2, centerY + bracketHeight / 2,
                     centerX + bracketWidth / 2, centerY + bracketHeight / 2 - 1);

        // Center dot representing repository node
        g2d.setColor(accentColor);
        g2d.fillOval(centerX - 2, centerY - 2, 4, 4);
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





