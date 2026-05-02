package com.kalynx.serverlessreviewtool.ui;

import java.io.Serial;

import com.kalynx.serverlessreviewtool.configuration.AppSettings;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedButton;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedLabel;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedPanel;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedPopupDialog;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedTextField;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedSpinner;

import javax.swing.*;
import java.awt.*;

/**
 * RepositoryConfigDialog - Dialog for configuring repository polling settings
 * Uses themed components for consistent styling
 */
public class RepositoryConfigDialog extends ThemedPopupDialog {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private final ThemedTextField nameField;
    private final ThemedTextField urlField;
    private final ThemedSpinner intervalSpinner;
    private boolean confirmed = false;

    public RepositoryConfigDialog(Component parent, String title, AppSettings.RepositoryConfig existingConfig) {
        super(parent, title);
        setDialogSize(450, 300);


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

    public AppSettings.RepositoryConfig getRepositoryConfig() {
        return new AppSettings.RepositoryConfig(
                nameField.getText(),
                urlField.getText(),
                (Integer) intervalSpinner.getValue()
        );
    }
}






