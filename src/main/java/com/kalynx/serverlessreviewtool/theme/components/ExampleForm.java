package com.kalynx.serverlessreviewtool.theme.components;

import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * ExampleForm - Demonstrates how to create a form using ThemedFrame
 * This shows the simplicity of creating new application windows
 */
public class ExampleForm extends ThemedFrame {

    public ExampleForm() {
        super("Example Application Form", 800, 600);

        // Customize the menu
        setupMenu();

        // Build the form content
        buildContent();
    }

    /**
     * Setup custom menu items
     */
    private void setupMenu() {
        // Method 1: Set all menu items at once
        setMenuItems(
            new MenuItem("🏠 Dashboard", this::showDashboard),
            new MenuItem("📋 Projects", this::showProjects),
            new MenuItem("⚙️ Settings", this::showSettings),
            new MenuItem("ℹ️ About", this::showAbout),
            new MenuItem("❌ Exit", this::exitApplication)
        );

        // Method 2: Add individual items (commented out - showing alternative)
        // addMenuItem("🏠 Dashboard", this::showDashboard);
        // addMenuItem("📋 Projects", this::showProjects);
    }

    /**
     * Build the form content
     */
    private void buildContent() {
        // Get the content panel provided by ThemedFrame
        ThemedPanel content = getContentPanel();
        content.setLayout(new BorderLayout(10, 10));

        // Add a welcome message
        ThemedLabel welcomeLabel = new ThemedLabel("Welcome to the Example Application!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, ThemeManager.getInstance().scale(24)));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        content.add(welcomeLabel, BorderLayout.NORTH);

        // Add main content area
        ThemedPanel mainContent = new ThemedPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add some example components
        ThemedLabel descLabel = new ThemedLabel(
            "This form demonstrates the ThemedFrame base class. " +
            "All forms that extend ThemedFrame automatically get:"
        );
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(descLabel);
        mainContent.add(Box.createVerticalStrut(15));

        String[] features = {
            "✓ Custom title bar with minimize, maximize, close buttons",
            "✓ Hamburger menu with slide-out panel",
            "✓ Automatic theme switching support",
            "✓ Window resize and drag functionality",
            "✓ Consistent styling across the application"
        };

        for (String feature : features) {
            ThemedLabel featureLabel = new ThemedLabel(feature);
            featureLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            mainContent.add(featureLabel);
            mainContent.add(Box.createVerticalStrut(8));
        }

        mainContent.add(Box.createVerticalStrut(20));

        // Add some interactive components
        ThemedLabel inputLabel = new ThemedLabel("Try the themed components:");
        inputLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(inputLabel);
        mainContent.add(Box.createVerticalStrut(10));

        ThemedTextField textField = new ThemedTextField(30);
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, textField.getPreferredSize().height));
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(textField);
        mainContent.add(Box.createVerticalStrut(10));

        String[] options = {"Option 1", "Option 2", "Option 3"};
        ThemedComboBox<String> comboBox = new ThemedComboBox<>(options);
        comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, comboBox.getPreferredSize().height));
        comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(comboBox);
        mainContent.add(Box.createVerticalStrut(20));

        // Add action button
        JButton actionButton = new JButton("Click Me!");
        actionButton.setFont(new Font("Segoe UI", Font.BOLD, ThemeManager.getInstance().scale(12)));
        actionButton.setBackground(ThemeManager.getInstance().getCurrentTheme().getAccentColor());
        actionButton.setForeground(Color.WHITE);
        actionButton.setFocusPainted(false);
        actionButton.setBorderPainted(false);
        actionButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                this,
                "Button clicked! Text field contains: \"" + textField.getText() + "\"",
                "Action",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
        mainContent.add(actionButton);

        // Wrap in scroll pane
        ThemedScrollPane scrollPane = new ThemedScrollPane(mainContent);
        content.add(scrollPane, BorderLayout.CENTER);

        // Add footer
        ThemedPanel footer = new ThemedPanel();
        footer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        ThemedLabel footerLabel = new ThemedLabel("Click the hamburger menu (☰) to navigate • Toggle theme with the sun/moon button");
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        footerLabel.setFont(new Font("Segoe UI", Font.ITALIC, ThemeManager.getInstance().scale(11)));
        footer.add(footerLabel);
        content.add(footer, BorderLayout.SOUTH);
    }

    // Menu action methods

    private void showDashboard() {
        JOptionPane.showMessageDialog(this, "Dashboard view would be shown here", "Dashboard", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showProjects() {
        JOptionPane.showMessageDialog(this, "Projects list would be shown here", "Projects", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSettings() {
        JOptionPane.showMessageDialog(this, "Settings panel would be shown here", "Settings", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(
            this,
            "Example Application\nVersion 1.0\nBuilt with ThemedFrame",
            "About",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void exitApplication() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit?",
            "Exit",
            JOptionPane.YES_NO_OPTION
        );
        if (result == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    /**
     * Main method to run the example form
     */
    public static void main(String[] args) {
        // Enable anti-aliasing for better text rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Run on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            ThemeManager themeManager = ThemeManager.getInstance();
            themeManager.applyTheme();

            ExampleForm form = new ExampleForm();
            form.setVisible(true);
        });
    }
}

