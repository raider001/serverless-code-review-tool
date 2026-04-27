package com.kalynx.serverlessreviewtool.ui.mainpanels.settingspanel;

import com.kalynx.serverlessreviewtool.configuration.SettingsManager;
import com.kalynx.serverlessreviewtool.theme.components.ThemedLabel;
import com.kalynx.serverlessreviewtool.theme.components.ThemedPanel;
import com.kalynx.serverlessreviewtool.theme.components.ThemedTextField;
import com.kalynx.serverlessreviewtool.theme.components.ThemedTitledBorder;
import com.kalynx.serverlessreviewtool.utils.Validator;
import net.miginfocom.swing.MigLayout;


public class NotificationServiceSettings extends ThemedPanel {

    private final SettingsManager settingsManager;

    private final ThemedLabel urlLabel = new ThemedLabel("Service URL:");
    private final ThemedTextField urlTextField = new ThemedTextField(30);

    public NotificationServiceSettings() {
        settingsManager = SettingsManager.getInstance();
        configureLayout();
        setupValidation();
        loadDefaults();
    }

    private void configureLayout() {
        setLayout(new MigLayout("", "[][]", "[]"));
        setBorder(ThemedTitledBorder.create("Automatic Notification Service"));
        add(urlLabel, "cell 0 0, align right");
        add(urlTextField, "cell 1 0, growx");
    }

    private void setupValidation() {
        urlTextField.setupValidation(
            this::validateUrl,
            settingsManager::updateNotificationServiceUrl
        );
    }

    private void loadDefaults() {
        urlTextField.setText(settingsManager.getSettings().getNotificationServiceUrl());
    }

    private Validator.ValidationResult validateUrl(String urlString) {
        if (urlString.isEmpty()) {
            return Validator.ValidationResult.valid();
        }

        String urlPattern = "^(https?|wss?)://[a-zA-Z0-9.-]+(:[0-9]+)?(/.*)?$";

        return urlString.matches(urlPattern)
            ? Validator.ValidationResult.valid()
            : Validator.ValidationResult.invalid("Invalid URL format. Please enter a valid URL starting with http://, https://, ws://, or wss://");
    }
}
