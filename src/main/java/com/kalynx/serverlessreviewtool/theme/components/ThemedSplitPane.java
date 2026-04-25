package com.kalynx.serverlessreviewtool.theme.components;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * ThemedSplitPane - A modern, theme-aware split pane component with styled divider
 */
public class ThemedSplitPane extends JSplitPane {

    private final ThemeManager themeManager;

    public ThemedSplitPane(int orientation) {
        super(orientation);
        this.themeManager = ThemeManager.getInstance();

        // Configure split pane
        setDividerSize(themeManager.scale(4));
        setContinuousLayout(true);
        setOneTouchExpandable(false);
        // Remove default 3D border
        setBorder(null);
    }

    public ThemedSplitPane(int orientation, Component leftComponent, Component rightComponent) {
        this(orientation);
        setLeftComponent(leftComponent);
        setRightComponent(rightComponent);
    }

    public ThemedSplitPane(int orientation, boolean continuousLayout, Component leftComponent, Component rightComponent) {
        this(orientation, leftComponent, rightComponent);
        setContinuousLayout(continuousLayout);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        // Only apply theme if themeManager is initialized (avoid NPE during construction)
        if (themeManager != null) {
            applyTheme();
        }
    }

    /**
     * Apply theme colors to the split pane and divider.
     */
    private void applyTheme() {
        if (themeManager == null) {
            return; // Not yet initialized
        }
        Theme theme = themeManager.getCurrentTheme();
        setBackground(theme.getBackgroundColor());
        // Ensure no border is set
        setBorder(null);
        updateDividerStyle(theme);
    }

    /**
     * Style the divider with the current theme.
     */
    private void updateDividerStyle(Theme theme) {
        if (getUI() instanceof BasicSplitPaneUI) {
            BasicSplitPaneUI ui = (BasicSplitPaneUI) getUI();
            BasicSplitPaneDivider divider = ui.getDivider();
            if (divider != null) {
                divider.setBackground(theme.getBorderColor());
                divider.setBorder(null);
            }
        }
    }

    @Override
    public void setDividerLocation(int location) {
        super.setDividerLocation(location);
        if (themeManager != null) {
            applyTheme();
        }
    }

    @Override
    public void setDividerLocation(double proportionalLocation) {
        super.setDividerLocation(proportionalLocation);
        if (themeManager != null) {
            applyTheme();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Query theme colors on every paint for theme switching
        if (themeManager != null) {
            applyTheme();
            // Ensure no border is set (remove 3D border effect)
            setBorder(null);
        }
        super.paintComponent(g);
    }
}
