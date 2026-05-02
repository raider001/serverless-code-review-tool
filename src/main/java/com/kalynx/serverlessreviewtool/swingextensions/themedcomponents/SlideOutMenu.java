package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * SlideOutMenu - A theme-aware slide-out menu panel that appears from the left
 * Provides navigation options and settings
 */
public class SlideOutMenu extends ThemedPanel {

    private final ThemeManager themeManager;
    private final JFrame parentFrame;
    private final int menuWidth;
    private boolean isOpen = false;
    private Timer animationTimer;
    private int currentX;
    private final int animationSpeed = 20; // pixels per frame
    private ThemedPanel overlayPanel;
    private final ThemedPanel menuContentPanel;

    public SlideOutMenu(JFrame parentFrame) {
        this(parentFrame, 250);
    }

    public SlideOutMenu(JFrame parentFrame, int width) {
        this.parentFrame = parentFrame;
        this.themeManager = ThemeManager.getInstance();
        this.menuWidth = themeManager.scale(width);
        this.currentX = -this.menuWidth; // Start hidden off-screen

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(this.menuWidth, parentFrame.getHeight()));
        setSize(this.menuWidth, parentFrame.getHeight());
        setOpaque(true); // Ensure background is painted

        // Apply initial theme colors
        Theme theme = themeManager.getCurrentTheme();
        setBackground(theme.getBackgroundColor());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, theme.getBorderColor()));

        // Create menu content panel
        menuContentPanel = new ThemedPanel();
        menuContentPanel.setLayout(new BoxLayout(menuContentPanel, BoxLayout.Y_AXIS));
        menuContentPanel.setBorder(BorderFactory.createEmptyBorder(
                themeManager.scale(50),
                themeManager.scale(15),
                themeManager.scale(15),
                themeManager.scale(15)
        ));
        menuContentPanel.setOpaque(true); // Ensure background is painted
        menuContentPanel.setBackground(theme.getBackgroundColor());

        // Add menu title
        JLabel menuTitle = new JLabel("Menu");
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, themeManager.scale(18)));
        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuTitle.setForeground(theme.getAccentColor());
        menuContentPanel.add(menuTitle);
        menuContentPanel.add(Box.createVerticalStrut(themeManager.scale(20)));

        // Add sample menu items
        addMenuItem("Dashboard", () -> System.out.println("Dashboard clicked"));
        addMenuItem("Settings", () -> System.out.println("Settings clicked"));
        addMenuItem("About", () -> System.out.println("About clicked"));
        addMenuItem("Help", () -> System.out.println("Help clicked"));

        // Use ThemedScrollPane for proper theming
        ThemedScrollPane scrollPane = new ThemedScrollPane(
            menuContentPanel,
            ScrollBarPolicy.AS_NEEDED,
            ScrollBarPolicy.NEVER
        );
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);


        // Initialize position
        setBounds(currentX, 0, this.menuWidth, parentFrame.getHeight());
    }

    /**
     * Apply current theme to all components
     */
    private void applyTheme() {
        Theme theme = themeManager.getCurrentTheme();
        setBackground(theme.getBackgroundColor());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, theme.getBorderColor()));

        if (menuContentPanel != null) {
            menuContentPanel.setBackground(theme.getBackgroundColor());

            // Update all menu items
            for (Component comp : menuContentPanel.getComponents()) {
                if (comp instanceof JLabel) {
                    comp.setForeground(theme.getAccentColor());
                } else if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    btn.setBackground(theme.getBackgroundColor());
                    btn.setForeground(theme.getForegroundColor());
                }
            }
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    /**
     * Add a menu item
     */
    private void addMenuItem(String text, Runnable action) {
        JButton menuItem = new JButton(text);
        menuItem.setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(14)));
        menuItem.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, themeManager.scale(40)));
        menuItem.setFocusPainted(false);
        menuItem.setBorderPainted(false);
        menuItem.setContentAreaFilled(true);
        menuItem.setOpaque(true); // Ensure background is painted
        menuItem.setHorizontalAlignment(SwingConstants.LEFT);
        menuItem.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Theme theme = themeManager.getCurrentTheme();
        menuItem.setBackground(theme.getBackgroundColor());
        menuItem.setForeground(theme.getForegroundColor());

        // Hover effect
        menuItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                Theme currentTheme = themeManager.getCurrentTheme();
                menuItem.setBackground(currentTheme.getButtonBackground());
                menuItem.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Theme currentTheme = themeManager.getCurrentTheme();
                menuItem.setBackground(currentTheme.getBackgroundColor());
                menuItem.repaint();
            }
        });

        menuItem.addActionListener(e -> {
            action.run();
            toggle(); // Close menu after selection
        });

        menuContentPanel.add(menuItem);
        menuContentPanel.add(Box.createVerticalStrut(themeManager.scale(5)));
    }


    /**
     * Toggle menu open/close
     */
    public void toggle() {
        if (isOpen) {
            close();
        } else {
            open();
        }
    }

    /**
     * Open the menu with animation
     */
    public void open() {
        if (isOpen || animationTimer != null && animationTimer.isRunning()) {
            return;
        }

        isOpen = true;

        // Apply current theme before showing
        applyTheme();

        // Create fully transparent overlay for click detection (no visual darkening)
        overlayPanel = new ThemedPanel();
        overlayPanel.setOpaque(false); // Fully transparent
        overlayPanel.setBounds(0, 0, parentFrame.getWidth(), parentFrame.getHeight());
        overlayPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                close(); // Close menu when clicking outside
            }
        });

        // Add overlay and menu to layered pane
        JLayeredPane layeredPane = parentFrame.getLayeredPane();
        layeredPane.add(overlayPanel, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(this, JLayeredPane.MODAL_LAYER);

        // Animate sliding in
        animationTimer = new Timer(10, null);
        animationTimer.addActionListener(e -> {
            currentX += animationSpeed;
            if (currentX >= 0) {
                currentX = 0;
                animationTimer.stop();
            }
            setBounds(currentX, 0, menuWidth, parentFrame.getHeight());
        });
        animationTimer.start();
    }

    /**
     * Close the menu with animation
     */
    public void close() {
        if (!isOpen || animationTimer != null && animationTimer.isRunning()) {
            return;
        }

        isOpen = false;

        // Animate sliding out
        animationTimer = new Timer(10, null);
        animationTimer.addActionListener(e -> {
            currentX -= animationSpeed;
            if (currentX <= -menuWidth) {
                currentX = -menuWidth;
                animationTimer.stop();

                // Remove from layered pane
                JLayeredPane layeredPane = parentFrame.getLayeredPane();
                layeredPane.remove(this);
                if (overlayPanel != null) {
                    layeredPane.remove(overlayPanel);
                    overlayPanel = null;
                }
                layeredPane.repaint();
            }
            setBounds(currentX, 0, menuWidth, parentFrame.getHeight());
        });
        animationTimer.start();
    }

    /**
     * Check if menu is currently open
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Add a custom menu item
     */
    public void addCustomMenuItem(String text, Runnable action) {
        addMenuItem(text, action);
    }

    /**
     * Clear all menu items and rebuild
     */
    public void clearMenuItems() {
        menuContentPanel.removeAll();

        // Re-add title
        Theme theme = themeManager.getCurrentTheme();
        JLabel menuTitle = new JLabel("Menu");
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, themeManager.scale(18)));
        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuTitle.setForeground(theme.getAccentColor());
        menuContentPanel.add(menuTitle);
        menuContentPanel.add(Box.createVerticalStrut(themeManager.scale(20)));

        applyTheme();
        menuContentPanel.revalidate();
        menuContentPanel.repaint();
    }
}






