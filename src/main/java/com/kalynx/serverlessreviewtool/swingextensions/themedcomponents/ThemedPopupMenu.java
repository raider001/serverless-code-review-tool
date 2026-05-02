package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * ThemedPopupMenu - A theme-aware popup menu component.
 * Provides consistent styling with the application theme.
 */
public class ThemedPopupMenu extends JPopupMenu {

    private final ThemeManager themeManager;

    public ThemedPopupMenu() {
        this.themeManager = ThemeManager.getInstance();
        initializeMenu();
    }

    private void initializeMenu() {
        Theme theme = themeManager.getCurrentTheme();

        setBackground(theme.getInputBackground());
        setOpaque(true);
        setLightWeightPopupEnabled(true);

        setBorder(new CompoundBorder(
            new LineBorder(theme.getBorderColor(), 1),
            new EmptyBorder(
                themeManager.scale(2),
                0,
                themeManager.scale(2),
                0
            )
        ));
    }

    @Override
    public void show(Component invoker, int x, int y) {
        Theme theme = themeManager.getCurrentTheme();
        setBackground(theme.getInputBackground());
        setBorder(new CompoundBorder(
            new LineBorder(theme.getBorderColor(), 1),
            new EmptyBorder(
                themeManager.scale(2),
                0,
                themeManager.scale(2),
                0
            )
        ));
        super.show(invoker, x, y);
    }

    @Override
    public JMenuItem add(JMenuItem menuItem) {
        if (!(menuItem instanceof ThemedMenuItem)) {
            ThemedMenuItem themedItem = new ThemedMenuItem(menuItem.getText());
            for (java.awt.event.ActionListener listener : menuItem.getActionListeners()) {
                themedItem.addActionListener(listener);
            }
            menuItem = themedItem;
        }
        return super.add(menuItem);
    }

    @Override
    public JMenuItem add(String text) {
        return add(new ThemedMenuItem(text));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Theme theme = themeManager.getCurrentTheme();
        g2d.setColor(theme.getInputBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.dispose();
    }
}








