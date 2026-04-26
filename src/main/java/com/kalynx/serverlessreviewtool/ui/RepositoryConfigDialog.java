package com.kalynx.serverlessreviewtool.ui;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.theme.components.ThemedButton;
import com.kalynx.serverlessreviewtool.theme.components.ThemedLabel;
import com.kalynx.serverlessreviewtool.theme.components.ThemedPanel;
import com.kalynx.serverlessreviewtool.theme.components.ThemedPopupDialog;
import com.kalynx.serverlessreviewtool.theme.components.ThemedTextField;
import com.kalynx.serverlessreviewtool.theme.components.ThemedSpinner;

import javax.swing.*;
import java.awt.*;

/**
 * RepositoryConfigDialog - Dialog for configuring repository polling settings
 * Uses themed components for consistent styling
 */
public class RepositoryConfigDialog extends ThemedPopupDialog {
    private final ThemedTextField nameField;
    private final ThemedTextField urlField;
    private final ThemedSpinner intervalSpinner;
    private boolean confirmed = false;

    public RepositoryConfigDialog(Component parent, String title, SettingsPanel.RepositoryConfig existingConfig) {
        super(parent, title);
        setDialogSize(450, 300);

        ThemeManager themeManager = ThemeManager.getInstance();
        Theme theme = themeManager.getCurrentTheme();

        ThemedPanel contentPanel = (ThemedPanel) getContentPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Name field
        contentPanel.add(createLabeledField("Repository Name:", nameField = new ThemedTextField(20)));
        contentPanel.add(Box.createVerticalStrut(12));

        // URL field
        contentPanel.add(createLabeledField("Repository URL:", urlField = new ThemedTextField(20)));
        contentPanel.add(Box.createVerticalStrut(12));

        // Polling interval
        intervalSpinner = new ThemedSpinner(new SpinnerNumberModel(15, 1, 1440, 1));
        contentPanel.add(createLabeledField("Polling Interval (minutes):", intervalSpinner));
        contentPanel.add(Box.createVerticalStrut(20));

        // Button panel
        ThemedPanel buttonPanel = new ThemedPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        ThemedButton cancelButton = new ThemedButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        ThemedButton acceptButton = new ThemedButton("Save");
        acceptButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(acceptButton);
        contentPanel.add(buttonPanel);

        // Populate fields if editing
        if (existingConfig != null) {
            nameField.setText(existingConfig.getName());
            urlField.setText(existingConfig.getUrl());
            intervalSpinner.setValue(existingConfig.getPollingIntervalMinutes());
        }
    }

    private ThemedPanel createLabeledField(String label, JComponent field) {
        ThemedPanel panel = new ThemedPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        ThemedLabel jLabel = new ThemedLabel(label);
        jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        panel.add(jLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);

        return panel;
    }


    public boolean isConfirmed() {
        return confirmed;
    }

    public SettingsPanel.RepositoryConfig getRepositoryConfig() {
        return new SettingsPanel.RepositoryConfig(
                nameField.getText(),
                urlField.getText(),
                (Integer) intervalSpinner.getValue()
        );
    }
}






