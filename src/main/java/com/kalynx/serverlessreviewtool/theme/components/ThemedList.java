package com.kalynx.serverlessreviewtool.theme.components;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

/**
 * A JList that automatically applies and updates theme colors
 * Colors are queried on-demand during paint for automatic theme updates
 */
public class ThemedList<T> extends JList<T> {
    
    protected final ThemeManager themeManager;
    
    public ThemedList() {
        super();
        this.themeManager = ThemeManager.getInstance();
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setFixedCellHeight(themeManager.scale(80));
    }
    
    public ThemedList(T[] items) {
        super(items);
        this.themeManager = ThemeManager.getInstance();
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setFixedCellHeight(themeManager.scale(80));
    }
    
    public ThemedList(Vector<? extends T> items) {
        super(items);
        this.themeManager = ThemeManager.getInstance();
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setFixedCellHeight(themeManager.scale(80));
    }
    
    public ThemedList(ListModel<T> model) {
        super(model);
        this.themeManager = ThemeManager.getInstance();
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setFixedCellHeight(themeManager.scale(80));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        // Query theme colors on demand - no caching needed
        Theme theme = themeManager.getCurrentTheme();
        setBackground(theme.getBackgroundColor());
        setForeground(theme.getForegroundColor());
        setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(12)));

        super.paintComponent(g);
    }
}

