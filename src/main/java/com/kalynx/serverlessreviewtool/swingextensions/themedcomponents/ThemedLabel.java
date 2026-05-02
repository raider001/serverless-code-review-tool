package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * A JLabel that automatically applies and updates theme colors
 * Colors are queried on-demand during paint for automatic theme updates
 */
public class ThemedLabel extends JLabel {
    
    protected final ThemeManager themeManager;
    
    public ThemedLabel() {
        this("");
    }
    
    public ThemedLabel(String text) {
        super(text);
        this.themeManager = ThemeManager.getInstance();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        // Query theme colors on demand - no caching needed
        Theme theme = themeManager.getCurrentTheme();
        setForeground(theme.getForegroundColor());

        super.paintComponent(g);
    }

    public ThemedLabel setThemedToolTipText(String text) {
        super.setToolTipText(text);
        return this;
    }
}

