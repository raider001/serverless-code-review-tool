package com.kalynx.serverlessreviewtool.ui.mainpanels.settingspanel;

import com.kalynx.serverlessreviewtool.configuration.SettingsManager;
import com.kalynx.serverlessreviewtool.git.Git;
import com.kalynx.serverlessreviewtool.managers.RepositorySyncManager;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.*;
import com.kalynx.serverlessreviewtool.utils.ListenerFactory;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

/**
 * PollingSettingsPanel - Configuration for automatic polling settings
 */
public class PollingSettingsPanel extends ThemedPanel {

    private static final Logger logger = LoggerFactory.getLogger(PollingSettingsPanel.class);

    private final SettingsManager settingsManager;
    private final Git git;
    private final ThemedSpinner intervalSpinner = new ThemedSpinner(new SpinnerNumberModel(15, 1, 1440, 1));
    private final ThemedCheckBox enablePollingCheckBox = new ThemedCheckBox("Enable automatic polling", true);
    private final ThemedButton forceSyncButton = new ThemedButton("Force Sync All Repositories");

    public PollingSettingsPanel(SettingsManager settingsManager, Git git) {
        this.settingsManager = settingsManager;
        this.git = git;
        setBorder(ThemedTitledBorder.create("Polling Settings"));
        configureLayout();
        setupListeners();
        loadSettings();
    }

    private void configureLayout() {
        setLayout(new MigLayout("", "[][]", "[]10[]10[]"));

        ThemedLabel intervalLabel = new ThemedLabel("Polling Interval (minutes):");
        add(intervalLabel, "cell 0 0");
        add(intervalSpinner, "cell 1 0");
        add(enablePollingCheckBox, "cell 0 1 2 1");
        add(forceSyncButton, "cell 0 2 2 1, growx");
    }

    private void setupListeners() {
        addSpinnerFocusListener(intervalSpinner, () -> {
            int minutes = (Integer) intervalSpinner.getValue();
            settingsManager.updatePollingInterval(minutes);
        });

        enablePollingCheckBox.addActionListener(ignored ->
            settingsManager.updateEnablePolling(enablePollingCheckBox.isSelected())
        );

        forceSyncButton.addActionListener(ignored -> handleForceSync());
    }

    private void addSpinnerFocusListener(JSpinner spinner, Runnable onFocusLost) {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField()
                .addFocusListener(ListenerFactory.createFocusLostAdapter(ignored -> onFocusLost.run()));
        }
    }

    private void loadSettings() {
        intervalSpinner.setValue(settingsManager.getSettings().getPollingIntervalMinutes());
        enablePollingCheckBox.setSelected(settingsManager.getSettings().isEnablePolling());
    }

    private void handleForceSync() {
        forceSyncButton.setEnabled(false);
        forceSyncButton.setText("Syncing...");

        RepositorySyncManager syncManager = new RepositorySyncManager(
            git,
            settingsManager.getSettings().getRepositories()
        );

        syncManager.syncAllRepositories(
            message -> SwingUtilities.invokeLater(() -> logger.info("Sync: {}", message)),
            "force-sync-all-repositories"
        ).thenAccept(result -> SwingUtilities.invokeLater(() -> {
            forceSyncButton.setEnabled(true);
            forceSyncButton.setText("Force Sync All Repositories");

            if (result.success) {
                logger.info("Force sync completed: {}", result.message);
            } else {
                logger.warn("Force sync completed with errors: {}", result.message);
                for (RepositorySyncManager.RepositorySyncStatus status : result.repositoryResults) {
                    logger.warn("  {}: {} {}", status.repositoryName,
                        (status.success ? "✓" : "✗"), status.message);
                }
            }
        })).exceptionally(error -> {
            SwingUtilities.invokeLater(() -> {
                forceSyncButton.setEnabled(true);
                forceSyncButton.setText("Force Sync All Repositories");
                logger.error("Force sync failed: {}", error.getMessage());
            });
            return null;
        });
    }
}

