package com.kalynx.serverlessreviewtool.ui;

import com.kalynx.serverlessreviewtool.theme.components.ThemedLabel;
import com.kalynx.serverlessreviewtool.theme.components.ThemedPanel;

import javax.swing.*;
import java.awt.*;

/**
 * SettingsPanel - Application settings and configuration
 * Placeholder for future settings implementation
 */
public class SettingsPanel extends ThemedPanel {

    public SettingsPanel() {
        setLayout(new BorderLayout());
        initializeComponents();
    }

    private void initializeComponents() {
        // Title
        ThemedLabel titleLabel = new ThemedLabel("Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Placeholder content
        ThemedPanel contentPanel = new ThemedPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        ThemedLabel placeholderLabel = new ThemedLabel("Settings configuration will be added here.");
        placeholderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contentPanel.add(placeholderLabel);

        // Add components
        add(titleLabel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }
}


