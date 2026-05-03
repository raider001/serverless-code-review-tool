package com.kalynx.serverlessreviewtool.ui.mainpanels;

import com.kalynx.serverlessreviewtool.configuration.SettingsManager;
import com.kalynx.serverlessreviewtool.git.Git;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedPanel;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedScrollPane;
import com.kalynx.serverlessreviewtool.ui.mainpanels.settingspanel.CacheManagementPanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.settingspanel.NotificationServiceSettingsPanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.settingspanel.PollingSettingsPanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.settingspanel.RepositoriesPanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.settingspanel.UserSettingsPanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.settingspanel.WindowSettingsPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends ThemedPanel {

    private final SettingsManager settingsManager;
    private final Git git;

    public SettingsPanel(SettingsManager settingsManager, Git git) {
        this.settingsManager = settingsManager;
        this.git = git;
        setLayout(new BorderLayout());
        initializeComponents();
    }

    private void initializeComponents() {
        JScrollPane scrollPane = new ThemedScrollPane(createContentPanel());
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createContentPanel() {
        ThemedPanel contentPanel = new ThemedPanel();
        contentPanel.setLayout(new MigLayout("", "[grow][grow]", "[][][grow][]"));

        // Create settings sections with DI
        UserSettingsPanel userSettings = new UserSettingsPanel(settingsManager);
        WindowSettingsPanel windowSettings = new WindowSettingsPanel(settingsManager);
        NotificationServiceSettingsPanel notificationService = new NotificationServiceSettingsPanel(settingsManager);
        RepositoriesPanel repositories = new RepositoriesPanel(settingsManager);
        PollingSettingsPanel polling = new PollingSettingsPanel(settingsManager, git);
        CacheManagementPanel cacheManagement = new CacheManagementPanel();

        // Add sections to content panel
        contentPanel.add(userSettings, "cell 0 0 2 1, growx");
        contentPanel.add(windowSettings, "cell 0 1, growx");
        contentPanel.add(notificationService, "cell 1 1, growx");
        contentPanel.add(repositories, "cell 0 2 2 1, grow");
        contentPanel.add(polling, "cell 0 3, growx");
        contentPanel.add(cacheManagement, "cell 1 3, growx");

        return contentPanel;
    }
}
