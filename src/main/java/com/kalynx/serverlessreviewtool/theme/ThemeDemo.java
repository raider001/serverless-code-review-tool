package com.kalynx.serverlessreviewtool.theme;

import com.kalynx.serverlessreviewtool.theme.components.ThemedFrame;
import com.kalynx.serverlessreviewtool.theme.components.ThemedPanel;
import com.kalynx.serverlessreviewtool.ui.ReviewSelectionPanel;

import javax.swing.*;
import java.awt.*;

/**
 * ThemeDemo - Demonstrates the custom theme system with DPI scaling
 * Now extends ThemedFrame for standardized layout
 */
public class ThemeDemo extends ThemedFrame {

    private ReviewSelectionPanel reviewSelectionPanel;

    public ThemeDemo() {
        super("ServerlessReviewTool - Theme Demo", 900, 700);

        // Customize the menu
        customizeMenu();

        // Build the content
        buildContent();
    }

    /**
     * Customize the slide-out menu with application-specific items
     */
    private void customizeMenu() {
        // Clear default menu items and add custom ones
        setMenuItems(
            new MenuItem("🏠 Home", () -> {
                JOptionPane.showMessageDialog(this, "Home clicked!", "Navigation", JOptionPane.INFORMATION_MESSAGE);
            }),
            new MenuItem("📋 Reviews", () -> {
                JOptionPane.showMessageDialog(this, "Reviews clicked!", "Navigation", JOptionPane.INFORMATION_MESSAGE);
            }),
            new MenuItem("⚙️ Settings", () -> {
                JOptionPane.showMessageDialog(this, "Settings clicked!", "Navigation", JOptionPane.INFORMATION_MESSAGE);
            }),
            new MenuItem("ℹ️ About", () -> {
                JOptionPane.showMessageDialog(this,
                    "ServerlessReviewTool v1.0\nA modern review management application",
                    "About",
                    JOptionPane.INFORMATION_MESSAGE);
            }),
            new MenuItem("❓ Help", () -> {
                JOptionPane.showMessageDialog(this, "Help documentation coming soon!", "Help", JOptionPane.INFORMATION_MESSAGE);
            })
        );
    }

    /**
     * Build the demo content
     */
    private void buildContent() {
        // Get the content panel from ThemedFrame
        ThemedPanel content = getContentPanel();
        content.setLayout(new BorderLayout());

        // Add the review selection panel
        reviewSelectionPanel = new ReviewSelectionPanel();
        content.add(reviewSelectionPanel, BorderLayout.CENTER);
    }


    public static void main(String[] args) {
        // Enable anti-aliasing for better text rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Run on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            ThemeManager themeManager = ThemeManager.getInstance();
            themeManager.applyTheme();

            ThemeDemo demo = new ThemeDemo();
            demo.setVisible(true);
        });
    }
}




























