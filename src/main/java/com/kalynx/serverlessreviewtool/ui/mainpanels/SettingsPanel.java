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
    private final ThemedPanel repositorySectionPanel = new RepositoriesPanel();
    private final ThemedPanel notificationServicePanel = new NotificationServiceSettingsPanel();
    private final WindowSettingsPanel windowSettingsPanel = new WindowSettingsPanel();
    private final PollingSettingsPanel pollingSettingsPanel = new PollingSettingsPanel();

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
        contentPanel.add(windowSettingsPanel, "grow, wrap");
        contentPanel.add(notificationServicePanel, "grow, wrap");
        contentPanel.add(repositorySectionPanel, "grow, pushy, wrap");
        contentPanel.add(pollingSettingsPanel, "grow");
        return contentPanel;
    }
}