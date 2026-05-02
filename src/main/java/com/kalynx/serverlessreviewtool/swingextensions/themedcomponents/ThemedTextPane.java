package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * ThemedTextPane - A theme-aware styled text pane for code/diff viewing
 */
public class ThemedTextPane extends JTextPane {
    private static final long serialVersionUID = 1L;

    private transient final ThemeManager themeManager;

    public ThemedTextPane() {
        super();
        this.themeManager = ThemeManager.getInstance();
        setEditable(false);
        setFont(new Font("Consolas", Font.PLAIN, themeManager.scale(12)));
        setMargin(new Insets(themeManager.scale(5), themeManager.scale(5), themeManager.scale(5), themeManager.scale(5)));
    }

    public ThemedTextPane(boolean editable) {
        this();
        setEditable(editable);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (themeManager != null) {
            Theme theme = themeManager.getCurrentTheme();
            setBackground(theme.getBackgroundColor());
            setForeground(theme.getForegroundColor());
            setCaretColor(theme.getAccentColor());
            setSelectionColor(theme.getAccentColor());
            setSelectedTextColor(Color.WHITE);
        }
        super.paintComponent(g);
    }
}



