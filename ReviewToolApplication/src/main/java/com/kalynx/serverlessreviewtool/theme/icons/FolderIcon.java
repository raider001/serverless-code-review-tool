package com.kalynx.serverlessreviewtool.theme.icons;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.Icon;
import java.awt.*;

/**
 * FolderIcon - Theme-aware icon for folders
 * Shows an open folder with dynamic perspective
 */
public class FolderIcon implements Icon {
    private final int size;
    private final Theme theme;

    public FolderIcon(int size) {
        this.size = size;
        this.theme = ThemeManager.getInstance().getCurrentTheme();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color foreground = theme.getForegroundColor();
        Color accentColor = theme.getAccentColor();

        int folderX = x + 1;
        int folderY = y + 3;
        int folderWidth = size - 3;
        int folderHeight = size - 6;
        int tabWidth = folderWidth / 2;
        int tabHeight = size / 5;

        g2d.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Draw tab/back part
        g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 140));
        int[] tabXPoints = {folderX + 1, folderX + tabWidth, folderX + tabWidth + 1, folderX + 2};
        int[] tabYPoints = {folderY + tabHeight, folderY, folderY - 1, folderY + tabHeight - 1};
        g2d.fillPolygon(tabXPoints, tabYPoints, 4);

        // Draw folder body (open)
        g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 100));
        int[] folderXPoints = {folderX + 1, folderX + folderWidth - 2, folderX + folderWidth - 2, folderX + 1};
        int[] folderYPoints = {folderY + tabHeight, folderY + tabHeight, folderY + folderHeight, folderY + folderHeight};
        g2d.fillPolygon(folderXPoints, folderYPoints, 4);

        // Draw folder outline
        g2d.setColor(foreground);

        // Tab outline
        g2d.drawLine(folderX + 1, folderY + tabHeight, folderX + tabWidth, folderY);
        g2d.drawLine(folderX + tabWidth, folderY, folderX + tabWidth + 1, folderY - 1);

        // Main folder outline
        g2d.drawLine(folderX + tabWidth + 1, folderY - 1, folderX + folderWidth - 2, folderY);
        g2d.drawLine(folderX + folderWidth - 2, folderY, folderX + folderWidth - 2, folderY + folderHeight);
        g2d.drawLine(folderX + folderWidth - 2, folderY + folderHeight, folderX + 1, folderY + folderHeight);
        g2d.drawLine(folderX + 1, folderY + folderHeight, folderX + 1, folderY + tabHeight);

        // Inner detail line
        int detailY = folderY + tabHeight + 2;
        g2d.setColor(new Color(foreground.getRed(), foreground.getGreen(), foreground.getBlue(), 100));
        g2d.drawLine(folderX + 2, detailY, folderX + folderWidth - 3, detailY);
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





