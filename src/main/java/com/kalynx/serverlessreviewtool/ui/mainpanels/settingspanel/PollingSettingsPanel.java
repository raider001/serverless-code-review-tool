package com.kalynx.serverlessreviewtool.ui.mainpanels.settingspanel;

import com.kalynx.serverlessreviewtool.configuration.SettingsManager;
import com.kalynx.serverlessreviewtool.theme.components.ThemedCheckBox;
import com.kalynx.serverlessreviewtool.theme.components.ThemedLabel;
import com.kalynx.serverlessreviewtool.theme.components.ThemedPanel;
import com.kalynx.serverlessreviewtool.theme.components.ThemedSpinner;
import com.kalynx.serverlessreviewtool.theme.components.ThemedTitledBorder;
import com.kalynx.serverlessreviewtool.utils.ListenerFactory;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * PollingSettingsPanel - Configuration for automatic polling settings
 */
public class PollingSettingsPanel extends ThemedPanel {

    private final SettingsManager settingsManager = SettingsManager.getInstance();
    private final ThemedSpinner intervalSpinner = new ThemedSpinner(new SpinnerNumberModel(15, 1, 1440, 1));
    private final ThemedCheckBox enablePollingCheckBox = new ThemedCheckBox("Enable automatic polling", true);

    public PollingSettingsPanel() {
        setBorder(ThemedTitledBorder.create("Polling Settings"));
        configureLayout();
        setupListeners();
        loadSettings();
    }

    private void configureLayout() {
        setLayout(new MigLayout("", "[][]", "[]10[]"));

        ThemedLabel intervalLabel = new ThemedLabel("Polling Interval (minutes):");
        add(intervalLabel, "cell 0 0");
        add(intervalSpinner, "cell 1 0");
        add(enablePollingCheckBox, "cell 0 1 2 1");
    }

    private void setupListeners() {
        addSpinnerFocusListener(intervalSpinner, () -> {
            int minutes = (Integer) intervalSpinner.getValue();
            settingsManager.updatePollingInterval(minutes);
        });

        enablePollingCheckBox.addActionListener(e ->
            settingsManager.updateEnablePolling(enablePollingCheckBox.isSelected())
        );
    }

    private void addSpinnerFocusListener(JSpinner spinner, Runnable onFocusLost) {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField()
                .addFocusListener(ListenerFactory.createFocusLostAdapter(e -> onFocusLost.run()));
        }
    }

    private void loadSettings() {
        intervalSpinner.setValue(settingsManager.getSettings().getPollingIntervalMinutes());
        enablePollingCheckBox.setSelected(settingsManager.getSettings().isEnablePolling());
    }
}

