package com.kalynx.serverlessreviewtool.theme.components;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * A JTextField that automatically applies and updates theme colors
 * Colors are queried on-demand during paint for automatic theme updates
 */
public class ThemedTextField extends JTextField {
    
    protected final ThemeManager themeManager;
    
    public ThemedTextField() {
        this(0);
    }
    
    public ThemedTextField(int columns) {
        super(columns);
        this.themeManager = ThemeManager.getInstance();
    }
    
    public ThemedTextField(String text) {
        super(text);
        this.themeManager = ThemeManager.getInstance();
    }
    
    public ThemedTextField(String text, int columns) {
        super(text, columns);
        this.themeManager = ThemeManager.getInstance();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        // Query theme colors on demand - no caching needed
        Theme theme = themeManager.getCurrentTheme();
        setBackground(theme.getInputBackground());
        setForeground(theme.getForegroundColor());
        setCaretColor(theme.getAccentColor());
        // Simple padding border - no line border for cleaner look
        setBorder(BorderFactory.createEmptyBorder(
            themeManager.scale(5),
            themeManager.scale(8),
            themeManager.scale(5),
            themeManager.scale(8)
        ));

        super.paintComponent(g);
    }
}

