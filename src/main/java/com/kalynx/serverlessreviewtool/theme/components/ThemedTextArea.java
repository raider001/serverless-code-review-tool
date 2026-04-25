package com.kalynx.serverlessreviewtool.theme.components;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * ThemedTextArea - A theme-aware multi-line text area
 */
public class ThemedTextArea extends JTextArea {

    private final ThemeManager themeManager;

    public ThemedTextArea() {
        this(0, 0);
    }

    public ThemedTextArea(int rows, int columns) {
        super(rows, columns);
        this.themeManager = ThemeManager.getInstance();
        setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(12)));
        setLineWrap(false);
        setWrapStyleWord(false);
        setMargin(new Insets(themeManager.scale(5), themeManager.scale(5), themeManager.scale(5), themeManager.scale(5)));
    }

    public ThemedTextArea(String text) {
        this();
        setText(text);
    }

    public ThemedTextArea(String text, int rows, int columns) {
        this(rows, columns);
        setText(text);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (themeManager != null) {
            Theme theme = themeManager.getCurrentTheme();
            setBackground(theme.getInputBackground());
            setForeground(theme.getForegroundColor());
            setCaretColor(theme.getAccentColor());
            setSelectionColor(theme.getAccentColor());
            setSelectedTextColor(Color.WHITE);
        }
        super.paintComponent(g);
    }
}



