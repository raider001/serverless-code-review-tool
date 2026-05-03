package com.kalynx.serverlessreviewtool.ui.mainpanels.settingspanel;

import com.kalynx.serverlessreviewtool.configuration.GitConfigReader;
import com.kalynx.serverlessreviewtool.configuration.SettingsManager;
import com.kalynx.serverlessreviewtool.theme.icons.AlertIcon;
import com.kalynx.serverlessreviewtool.theme.icons.CheckIcon;
import com.kalynx.serverlessreviewtool.utils.ListenerFactory;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.*;
import net.miginfocom.swing.MigLayout;

import java.awt.Color;

public class UserSettingsPanel extends ThemedPanel {

    private final SettingsManager settingsManager;

    private final ThemedCheckBox useGitConfigCheckBox = new ThemedCheckBox("Use Git Config", true);
    private final ThemedTextField userNameField = new ThemedTextField(15);
    private final ThemedTextField userEmailField = new ThemedTextField(20);
    private final ThemedLabel gitStatusLabel = new ThemedLabel("");

    public UserSettingsPanel(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        setBorder(ThemedTitledBorder.create("User Identity"));
        configureLayout();
        loadSettings();
        setupListeners();
        updateFieldStates();
    }

    private void configureLayout() {
        setLayout(new MigLayout("", "[][]", "[][][][][]"));

        add(useGitConfigCheckBox, "cell 0 0 2 1");
        add(gitStatusLabel, "cell 2 0 2 1");

        add(new ThemedLabel("Name:"), "cell 0 1");
        add(userNameField, "cell 1 1");

        add(new ThemedLabel("Email:"), "cell 2 1");
        add(userEmailField, "cell 3 1");
    }

    private void loadSettings() {
        useGitConfigCheckBox.setSelected(settingsManager.getSettings().isUseGitConfig());
        userNameField.setText(settingsManager.getSettings().getUserName());
        userEmailField.setText(settingsManager.getSettings().getUserEmail());
        updateGitStatus();
    }

    private void setupListeners() {
        useGitConfigCheckBox.addActionListener(e -> {
            boolean useGitConfig = useGitConfigCheckBox.isSelected();
            settingsManager.updateUseGitConfig(useGitConfig);
            updateFieldStates();
            updateGitStatus();
        });

        userNameField.addFocusListener(ListenerFactory.createFocusLostAdapter(e -> {
            settingsManager.updateUserName(userNameField.getText());
        }));

        userEmailField.addFocusListener(ListenerFactory.createFocusLostAdapter(e -> {
            settingsManager.updateUserEmail(userEmailField.getText());
        }));
    }

    private void updateFieldStates() {
        boolean useGitConfig = useGitConfigCheckBox.isSelected();
        userNameField.setEnabled(!useGitConfig);
        userEmailField.setEnabled(!useGitConfig);
    }

    private void updateGitStatus() {
        if (useGitConfigCheckBox.isSelected()) {
            String gitName = GitConfigReader.getUserName();
            String gitEmail = GitConfigReader.getUserEmail();

            if (gitName != null && !gitName.isEmpty()) {
                gitStatusLabel.setIcon(new CheckIcon(16, new Color(66, 184, 131)));
                gitStatusLabel.setText("Git configured: " + gitName +
                    (gitEmail != null && !gitEmail.isEmpty() ? " <" + gitEmail + ">" : ""));
            } else {
                gitStatusLabel.setIcon(new AlertIcon(16, new Color(220, 180, 80)));
                gitStatusLabel.setText("Git not configured. Using manual settings below.");
            }
        } else {
            gitStatusLabel.setIcon(null);
            gitStatusLabel.setText("Using manual identity settings");
        }
    }
}










