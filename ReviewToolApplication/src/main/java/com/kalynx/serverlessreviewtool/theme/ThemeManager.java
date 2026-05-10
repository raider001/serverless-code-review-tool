package com.kalynx.serverlessreviewtool.theme;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Enumeration;

/**
 * ThemeManager - Manages custom themes with HiDPI scaling support
 * Uses only native Java Swing components
 */
public class ThemeManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThemeManager.class);

    private static ThemeManager instance;
    private Theme currentTheme;
    private float dpiScale = 1.0f;
    private boolean lafInitialized = false; // Track if Look and Feel is already set
    private boolean fontsInitialized = false; // Track if fonts have been set (expensive operation)

    private ThemeManager() {
        calculateDpiScale();
        currentTheme = new DarkTheme();
        // Initialize theme immediately so first switch is fast
        applyTheme();
    }
    
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    /**
     * Calculate DPI scaling factor for HiDPI displays
     */
    private void calculateDpiScale() {
        // Get the default toolkit
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        int dpi = toolkit.getScreenResolution();
        
        // Standard DPI is 96 on Windows, 72 on Mac (but Java normalizes to 96)
        dpiScale = dpi / 96.0f;
        
        // Clamp to reasonable values
        if (dpiScale < 1.0f) dpiScale = 1.0f;
        if (dpiScale > 3.0f) dpiScale = 3.0f;
        
        LOGGER.info("DPI Scale Factor: {} (Screen DPI: {})", dpiScale, dpi);
    }
    
    /**
     * Get scaled size based on DPI
     */
    public int scale(int size) {
        return Math.round(size * dpiScale);
    }

    /**
     * Apply the current theme to all Swing components
     */
    public void applyTheme() {
        try {
            // Only set Look and Feel on first call (expensive operation)
            if (!lafInitialized) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                lafInitialized = true;
            }

            // Apply custom theme colors (fonts only on first call)
            applyThemeDefaults();
            
            // Immediately repaint all visible windows
            // Our components query theme on-demand in paintComponent, so this is fast
            for (Window window : Window.getWindows()) {
                if (window.isVisible()) {
                    window.repaint();
                }
            }

        } catch (Exception e) {
            LOGGER.error("Failed to apply theme", e);
        }
    }
    
    /**
     * Apply theme defaults to UIManager
     */
    private void applyThemeDefaults() {
        // Colors
        UIManager.put("Panel.background", new ColorUIResource(currentTheme.getBackgroundColor()));
        UIManager.put("Panel.foreground", new ColorUIResource(currentTheme.getForegroundColor()));
        
        UIManager.put("Button.background", new ColorUIResource(currentTheme.getButtonBackground()));
        UIManager.put("Button.foreground", new ColorUIResource(currentTheme.getButtonForeground()));
        UIManager.put("Button.select", new ColorUIResource(currentTheme.getAccentColor()));
        
        UIManager.put("TextField.background", new ColorUIResource(currentTheme.getInputBackground()));
        UIManager.put("TextField.foreground", new ColorUIResource(currentTheme.getForegroundColor()));
        UIManager.put("TextField.caretForeground", new ColorUIResource(currentTheme.getAccentColor()));
        
        UIManager.put("TextArea.background", new ColorUIResource(currentTheme.getInputBackground()));
        UIManager.put("TextArea.foreground", new ColorUIResource(currentTheme.getForegroundColor()));
        UIManager.put("TextArea.caretForeground", new ColorUIResource(currentTheme.getAccentColor()));
        
        UIManager.put("TabbedPane.background", new ColorUIResource(currentTheme.getBackgroundColor()));
        UIManager.put("TabbedPane.foreground", new ColorUIResource(currentTheme.getForegroundColor()));
        UIManager.put("TabbedPane.selected", new ColorUIResource(currentTheme.getAccentColor()));
        
        UIManager.put("Table.background", new ColorUIResource(currentTheme.getBackgroundColor()));
        UIManager.put("Table.foreground", new ColorUIResource(currentTheme.getForegroundColor()));
        UIManager.put("Table.selectionBackground", new ColorUIResource(currentTheme.getAccentColor()));
        UIManager.put("Table.selectionForeground", new ColorUIResource(Color.WHITE));
        UIManager.put("Table.gridColor", new ColorUIResource(currentTheme.getBorderColor()));
        
        UIManager.put("Tree.background", new ColorUIResource(currentTheme.getBackgroundColor()));
        UIManager.put("Tree.foreground", new ColorUIResource(currentTheme.getForegroundColor()));
        UIManager.put("Tree.selectionBackground", new ColorUIResource(currentTheme.getAccentColor()));
        UIManager.put("Tree.selectionForeground", new ColorUIResource(Color.WHITE));
        
        UIManager.put("Menu.background", new ColorUIResource(currentTheme.getBackgroundColor()));
        UIManager.put("Menu.foreground", new ColorUIResource(currentTheme.getForegroundColor()));
        UIManager.put("MenuBar.background", new ColorUIResource(currentTheme.getBackgroundColor()));
        UIManager.put("MenuItem.background", new ColorUIResource(currentTheme.getBackgroundColor()));
        UIManager.put("MenuItem.foreground", new ColorUIResource(currentTheme.getForegroundColor()));
        UIManager.put("MenuItem.selectionBackground", new ColorUIResource(currentTheme.getAccentColor()));
        
        UIManager.put("ToolBar.background", new ColorUIResource(currentTheme.getBackgroundColor()));
        UIManager.put("ToolBar.foreground", new ColorUIResource(currentTheme.getForegroundColor()));
        
        UIManager.put("ScrollPane.background", new ColorUIResource(currentTheme.getBackgroundColor()));

        // CRITICAL: Set TitledBorder defaults so titled borders use theme colors
        UIManager.put("TitledBorder.titleColor", new ColorUIResource(currentTheme.getForegroundColor()));
        UIManager.put("TitledBorder.border", BorderFactory.createLineBorder(currentTheme.getBorderColor()));

        UIManager.put("ScrollBar.background", new ColorUIResource(currentTheme.getBackgroundColor()));
        UIManager.put("ScrollBar.thumb", new ColorUIResource(currentTheme.getAccentColor()));
        
        // Fonts with DPI scaling - only set on first initialization (VERY EXPENSIVE)
        if (!fontsInitialized) {
            Font baseFont = new Font("Segoe UI", Font.PLAIN, scale(12));
            Font boldFont = new Font("Segoe UI", Font.BOLD, scale(12));
            Font titleFont = new Font("Segoe UI", Font.BOLD, scale(14));

            setUIFont(new FontUIResource(baseFont));
            UIManager.put("TabbedPane.font", new FontUIResource(baseFont));
            UIManager.put("Table.font", new FontUIResource(baseFont));
            UIManager.put("Tree.font", new FontUIResource(baseFont));
            UIManager.put("Button.font", new FontUIResource(boldFont));
            UIManager.put("TitledBorder.font", new FontUIResource(titleFont));

            fontsInitialized = true;
        }
    }
    
    /**
     * Set the default font for all Swing components
     */
    private void setUIFont(FontUIResource font) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }
    
    /**
     * Switch to a different theme
     */
    public void setTheme(Theme theme) {
        this.currentTheme = theme;
        applyTheme();
    }
    
    /**
     * Switch to light theme
     */
    public void setLightTheme() {
        setTheme(new LightTheme());
    }
    
    /**
     * Switch to dark theme
     */
    public void setDarkTheme() {
        setTheme(new DarkTheme());
    }
    
    /**
     * Get current theme
     */
    public Theme getCurrentTheme() {
        return currentTheme;
    }

}
