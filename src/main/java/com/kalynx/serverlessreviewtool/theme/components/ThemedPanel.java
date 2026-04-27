package com.kalynx.serverlessreviewtool.theme.components;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * A JPanel that automatically applies and updates theme colors
 * Colors are queried on-demand during paint for automatic theme updates
 */
public class ThemedPanel extends JPanel {
    
    protected final ThemeManager themeManager;
    
    public ThemedPanel() {
        super();
        this.themeManager = ThemeManager.getInstance();
        setOpaque(true);
    }
    
    public ThemedPanel(LayoutManager layout) {
        super(layout);
        this.themeManager = ThemeManager.getInstance();
        setOpaque(true);
    }

    public ThemedPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
        this.themeManager = ThemeManager.getInstance();
        setOpaque(true);
    }

    public ThemedPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
        this.themeManager = ThemeManager.getInstance();
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Query theme colors on demand - no caching needed
        // Null check for safety during initialization
        if (themeManager != null) {
            Theme theme = themeManager.getCurrentTheme();
            setBackground(theme.getBackgroundColor());
            setForeground(theme.getForegroundColor());

            // Automatically update titled borders with current theme
            if (getBorder() instanceof TitledBorder) {
                TitledBorder titledBorder = (TitledBorder) getBorder();
                String title = titledBorder.getTitle();
                if (title != null) {
                    setBorder(ThemedTitledBorder.create(title));
                }
            }
        }

        super.paintComponent(g);
    }
}

