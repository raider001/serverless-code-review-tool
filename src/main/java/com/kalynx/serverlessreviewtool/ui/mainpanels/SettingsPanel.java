package com.kalynx.serverlessreviewtool.ui.mainpanels;

import com.kalynx.serverlessreviewtool.configuration.SettingsManager;
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

    public SettingsPanel(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        setLayout(new BorderLayout());
        initializeComponents();
    }

    private void initializeComponents() {
        JScrollPane scrollPane = new ThemedScrollPane(createContentPanel());
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createContentPanel() {
        ThemedPanel contentPanel = new ThemedPanel();
        contentPanel.setLayout(new MigLayout("fill", "", ""));

        // Create settings sections with DI
        UserSettingsPanel userSettings = new UserSettingsPanel(settingsManager);
        WindowSettingsPanel windowSettings = new WindowSettingsPanel(settingsManager);
        NotificationServiceSettingsPanel notificationService = new NotificationServiceSettingsPanel(settingsManager);
        RepositoriesPanel repositories = new RepositoriesPanel(settingsManager);
        PollingSettingsPanel polling = new PollingSettingsPanel(settingsManager);
        CacheManagementPanel cacheManagement = new CacheManagementPanel();

        // Add sections to content panel
        contentPanel.add(userSettings, "grow, wrap");
        contentPanel.add(windowSettings, "grow, wrap");
        contentPanel.add(notificationService, "grow, wrap");
        contentPanel.add(repositories, "grow, pushy, wrap");
        contentPanel.add(polling, "grow, wrap");
        contentPanel.add(cacheManagement, "grow");

        return contentPanel;
    }
}
