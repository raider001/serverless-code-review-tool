package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import java.io.Serial;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * ThemedScrollPane - A JScrollPane that uses ThemedScrollBar components
 * Colors are queried on-demand for automatic theme updates
 */
public class ThemedScrollPane extends JScrollPane {
    @Serial
    private static final long serialVersionUID = 1L;

    protected transient final ThemeManager themeManager;


    public ThemedScrollPane() {
        super();
        this.themeManager = ThemeManager.getInstance();
        initializeScrollBars();
    }

    public ThemedScrollPane(Component view) {
        super(view);
        this.themeManager = ThemeManager.getInstance();
        initializeScrollBars();
    }

    @SuppressWarnings("MagicConstant")
    public ThemedScrollPane(int vsbPolicy, int hsbPolicy) {
        super(vsbPolicy, hsbPolicy);
        this.themeManager = ThemeManager.getInstance();
        initializeScrollBars();
    }

    @SuppressWarnings("MagicConstant")
    public ThemedScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
        super(view, vsbPolicy, hsbPolicy);
        this.themeManager = ThemeManager.getInstance();
        initializeScrollBars();
    }

    // ========== Type-safe enum constructors ==========

    /**
     * Create a scroll pane with type-safe scrollbar policies
     */
    @SuppressWarnings("MagicConstant")
    public ThemedScrollPane(ScrollBarPolicy vsbPolicy, ScrollBarPolicy hsbPolicy) {
        super(vsbPolicy.getVerticalValue(), hsbPolicy.getHorizontalValue());
        this.themeManager = ThemeManager.getInstance();
        initializeScrollBars();
    }

    /**
     * Create a scroll pane with a view and type-safe scrollbar policies
     */
    @SuppressWarnings("MagicConstant")
    public ThemedScrollPane(Component view, ScrollBarPolicy vsbPolicy, ScrollBarPolicy hsbPolicy) {
        super(view, vsbPolicy.getVerticalValue(), hsbPolicy.getHorizontalValue());
        this.themeManager = ThemeManager.getInstance();
        initializeScrollBars();
    }

    /**
     * Initialize with themed scrollbars
     */
    private void initializeScrollBars() {
        // Replace default scrollbars with themed ones using type-safe enums
        setVerticalScrollBar(new ThemedScrollBar(ScrollBarOrientation.VERTICAL));
        setHorizontalScrollBar(new ThemedScrollBar(ScrollBarOrientation.HORIZONTAL));
        applyThemeColors();
    }

    private void applyThemeColors() {
        Theme theme = themeManager.getCurrentTheme();
        Color bg = theme.getBackgroundColor();
        setBorder(null);
        setBackground(bg);
        getViewport().setBackground(bg);
        getViewport().setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        applyThemeColors();
        super.paintComponent(g);
    }
}
