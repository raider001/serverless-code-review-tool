package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.Hashtable;
import java.util.Vector;

/**
 * ThemedTree - A theme-aware JTree component with automatic theme color updates
 */
public class ThemedTree extends JTree {

    private final ThemeManager themeManager;

    public ThemedTree() {
        super();
        this.themeManager = ThemeManager.getInstance();
        initializeTheme();
    }

    public ThemedTree(TreeModel newModel) {
        super(newModel);
        this.themeManager = ThemeManager.getInstance();
        initializeTheme();
    }

    public ThemedTree(TreeNode root) {
        super(root);
        this.themeManager = ThemeManager.getInstance();
        initializeTheme();
    }

    public ThemedTree(TreeNode root, boolean asksAllowsChildren) {
        super(root, asksAllowsChildren);
        this.themeManager = ThemeManager.getInstance();
        initializeTheme();
    }

    public ThemedTree(Vector<?> value) {
        super(value);
        this.themeManager = ThemeManager.getInstance();
        initializeTheme();
    }

    public ThemedTree(Hashtable<?, ?> value) {
        super(value);
        this.themeManager = ThemeManager.getInstance();
        initializeTheme();
    }

    public ThemedTree(Object[] value) {
        super(value);
        this.themeManager = ThemeManager.getInstance();
        initializeTheme();
    }

    private void initializeTheme() {
        if (themeManager != null) {
            applyTheme();
        }
    }

    private void applyTheme() {
        Theme theme = themeManager.getCurrentTheme();

        // Set tree colors
        setBackground(theme.getBackgroundColor());
        setForeground(theme.getForegroundColor());

        // Set selection colors
        UIManager.put("Tree.selectionBackground", theme.getAccentColor());
        UIManager.put("Tree.selectionForeground", Color.WHITE);
        UIManager.put("Tree.selectionBorderColor", theme.getAccentColor());

        // Set other tree colors
        UIManager.put("Tree.textBackground", theme.getBackgroundColor());
        UIManager.put("Tree.textForeground", theme.getForegroundColor());

        // Update UI to apply changes
        updateUI();
    }

    @Override
    public void updateUI() {
        super.updateUI();

        // Reapply theme after UI update
        if (themeManager != null) {
            Theme theme = themeManager.getCurrentTheme();
            setBackground(theme.getBackgroundColor());
            setForeground(theme.getForegroundColor());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Ensure theme colors are applied before painting
        if (themeManager != null) {
            Theme theme = themeManager.getCurrentTheme();
            setBackground(theme.getBackgroundColor());
            setForeground(theme.getForegroundColor());
        }

        super.paintComponent(g);
    }
}

