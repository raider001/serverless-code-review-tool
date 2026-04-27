package com.kalynx.serverlessreviewtool.ui.mainpanels;

import com.kalynx.serverlessreviewtool.theme.components.*;
import com.kalynx.serverlessreviewtool.ui.mainpanels.settingspanel.NotificationServiceSettingsPanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.settingspanel.PollingSettingsPanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.settingspanel.RepositoriesPanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.settingspanel.WindowSettingsPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends ThemedPanel {

    public SettingsPanel() {
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

        // Create settings sections
        WindowSettingsPanel windowSettings = new WindowSettingsPanel();
        NotificationServiceSettingsPanel notificationService = new NotificationServiceSettingsPanel();
        RepositoriesPanel repositories = new RepositoriesPanel();
        PollingSettingsPanel polling = new PollingSettingsPanel();

        // Add sections to content panel
        contentPanel.add(windowSettings, "grow, wrap");
        contentPanel.add(notificationService, "grow, wrap");
        contentPanel.add(repositories, "grow, pushy, wrap");
        contentPanel.add(polling, "grow");

        return contentPanel;
    }
}
