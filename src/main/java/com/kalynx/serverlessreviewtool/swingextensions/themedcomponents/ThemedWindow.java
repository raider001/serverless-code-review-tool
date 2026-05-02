package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * ThemedWindow - A lightweight window that automatically applies theme styling
 * Useful for tooltips, popups, and floating windows
 */
public class ThemedWindow extends JWindow {

    private final ThemeManager themeManager = ThemeManager.getInstance();
    private final ThemedPanel contentPanel;

    public ThemedWindow() {
        this(null);
    }

    public ThemedWindow(Window owner) {
        super(owner);
        setFocusableWindowState(false);

        contentPanel = new ThemedPanel(new MigLayout("insets 6", "[]", "[]"));
        contentPanel.setBorder(BorderFactory.createLineBorder(
            themeManager.getCurrentTheme().getBorderColor(), 1));

        add(contentPanel);
    }

    public ThemedPanel getContentPanel() {
        return contentPanel;
    }

    public void setContent(JComponent component) {
        contentPanel.removeAll();
        contentPanel.add(component);
    }

    public void showAt(Point screenLocation) {
        pack();
        setLocation(screenLocation);
        setVisible(true);
    }

    public void showAt(int x, int y) {
        showAt(new Point(x, y));
    }
}

