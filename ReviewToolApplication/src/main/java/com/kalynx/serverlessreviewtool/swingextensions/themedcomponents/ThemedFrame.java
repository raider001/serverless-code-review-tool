package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import java.io.Serial;

import com.kalynx.serverlessreviewtool.theme.LoadingStateManager;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.theme.WindowFrameLoadingIndicator;
import com.kalynx.serverlessreviewtool.theme.WindowResizeHandler;
import com.kalynx.serverlessreviewtool.theme.icons.AppIcon;
import com.kalynx.serverlessreviewtool.theme.icons.ThemeIcon;

import javax.swing.*;
import java.awt.*;

/**
 * ThemedFrame - A standardized base frame with CustomTitleBar, SlideOutMenu, and theme support
 * Extend this class to create application forms with consistent UI elements
 */
public class ThemedFrame extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;

    protected transient final ThemeManager themeManager;
    protected CustomTitleBar titleBar;
    protected ThemedPanel windowPanel;
    protected ThemedPanel contentPanel;
    protected WindowFrameLoadingIndicator loadingIndicator;

    /**
     * Create a themed frame with default size
     * @param title Window title
     */
    public ThemedFrame(String title) {
        this(title, 900, 700);
    }

    /**
     * Create a themed frame with custom size
     * @param title Window title
     * @param width Window width
     * @param height Window height
     */
    public ThemedFrame(String title, int width, int height) {
        super(title);
        this.themeManager = ThemeManager.getInstance();
        initializeFrame(title, width, height);
    }

    /**
     * Initialize the frame with standard components
     */
    private void initializeFrame(String title, int width, int height) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true); // Remove OS title bar
        setSize(themeManager.scale(width), themeManager.scale(height));
        setMinimumSize(new Dimension(themeManager.scale(400), themeManager.scale(300)));
        setLocationRelativeTo(null);

        // Set backgrounds on ALL frame layers to prevent any default colors
        Color bgColor = themeManager.getCurrentTheme().getBackgroundColor();
        setBackground(bgColor);
        getContentPane().setBackground(bgColor);
        getRootPane().setBackground(bgColor);
        getLayeredPane().setBackground(bgColor);
        getGlassPane().setBackground(bgColor);
        getRootPane().setOpaque(true);
        getLayeredPane().setOpaque(true);
        ((JComponent)getGlassPane()).setOpaque(false);

        // Create container with custom title bar
        windowPanel = new ThemedPanel(new BorderLayout());
        windowPanel.setBackground(bgColor);
        windowPanel.setOpaque(true);

        // Add border for window differentiation
        windowPanel.setBorder(BorderFactory.createLineBorder(
            themeManager.getCurrentTheme().getBorderColor(), 1));

        // Add custom title bar
        titleBar = new CustomTitleBar(this, title);
        windowPanel.add(titleBar, BorderLayout.NORTH);

        // Create main content panel (child classes will add content here)
        contentPanel = new ThemedPanel();
        contentPanel.setOpaque(true);
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(
            themeManager.scale(10),
            themeManager.scale(10),
            themeManager.scale(10),
            themeManager.scale(10)
        ));

        windowPanel.add(contentPanel, BorderLayout.CENTER);
        add(windowPanel);

        // Default icon: generic theme icon – subclasses can override via setApplicationIcon()
        setIconImages(ThemeIcon.createIconImages());

        // Add window resize handler for edges and corners
        WindowResizeHandler resizeHandler = new WindowResizeHandler(this, themeManager.scale(8));
        windowPanel.addMouseListener(resizeHandler);
        windowPanel.addMouseMotionListener(resizeHandler);
        contentPanel.addMouseListener(resizeHandler);
        contentPanel.addMouseMotionListener(resizeHandler);

        setupLoadingIndicator();
    }

    private void setupLoadingIndicator() {
        loadingIndicator = new WindowFrameLoadingIndicator();
        JPanel glassPane = (JPanel) getGlassPane();
        glassPane.setLayout(new BorderLayout());
        glassPane.add(loadingIndicator, BorderLayout.CENTER);
        glassPane.setOpaque(false);
        glassPane.setVisible(true);

        LoadingStateManager.getInstance().addListener(() -> {
            if (LoadingStateManager.getInstance().isLoading()) {
                loadingIndicator.startAnimation();
            } else {
                loadingIndicator.stopAnimation();
            }
        });
    }

    /**
     * Replace the application icon. Call this after the constructor if you want
     * a different icon instead of the default {@link AppIcon}.
     *
     * @param images One or more images at different resolutions (16, 32, 48 … px).
     */
    public void setApplicationIcon(java.util.List<? extends Image> images) {
        setIconImages(images);
    }

    /**
     * Get the content panel where child forms can add their components
     * @return The main content panel
     */
    public ThemedPanel getContentPanel() {
        return contentPanel;
    }

    /**
     * Get the custom title bar for customization
     * @return The CustomTitleBar
     */
    public CustomTitleBar getTitleBar() {
        return titleBar;
    }

    /**
     * Get the slide-out menu for customization
     * @return The SlideOutMenu
     */
    public SlideOutMenu getMenu() {
        return titleBar.getSlideOutMenu();
    }

    /**
     * Set custom menu items (clears existing items)
     * @param menuItems Array of menu item definitions (text, action pairs)
     */
    public void setMenuItems(MenuItem... menuItems) {
        SlideOutMenu menu = getMenu();
        menu.clearMenuItems();
        for (MenuItem item : menuItems) {
            menu.addCustomMenuItem(item.text, item.action);
        }
    }

    /**
     * Add a single menu item
     * @param text Menu item text
     * @param action Action to perform when clicked
     */
    public void addMenuItem(String text, Runnable action) {
        getMenu().addCustomMenuItem(text, action);
    }

    /**
     * Update the window title
     * @param newTitle New title text
     */
    public void setWindowTitle(String newTitle) {
        setTitle(newTitle);
        titleBar.setTitle(newTitle);
    }

    /**
     * Helper class for menu item definition
     */
    public static class MenuItem {
        public final String text;
        public final Runnable action;

        public MenuItem(String text, Runnable action) {
            this.text = text;
            this.action = action;
        }
    }
}

