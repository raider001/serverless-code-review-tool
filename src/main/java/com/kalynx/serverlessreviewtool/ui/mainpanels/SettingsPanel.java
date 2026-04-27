package com.kalynx.serverlessreviewtool.ui.mainpanels;

import com.kalynx.serverlessreviewtool.configuration.AppSettings;
import com.kalynx.serverlessreviewtool.configuration.SettingsManager;
import com.kalynx.serverlessreviewtool.theme.components.*;
import com.kalynx.serverlessreviewtool.ui.mainpanels.settingspanel.NotificationServiceSettingsPanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.settingspanel.RepositoriesPanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.settingspanel.WindowSettingsPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * SettingsPanel - Application settings and configuration
 * Includes repository polling configuration and management
 */
public class SettingsPanel extends ThemedPanel {
    private final SettingsManager settingsManager;
    private final DefaultListModel<AppSettings.RepositoryConfig> repositoryListModel;
    private ThemedPanel repositorySectionPanel;
    private ThemedPanel notificationServicePanel;
    private WindowSettingsPanel windowSettingsPanel;
    private ThemedSpinner intervalSpinner;
    private ThemedCheckBox enablePollingCheckBox;

    public SettingsPanel() {
        this.settingsManager = SettingsManager.getInstance();
        this.repositoryListModel = new DefaultListModel<>();

        setLayout(new BorderLayout());
        initializeComponents();
        loadSettings();
    }

    private void initializeComponents() {

        // Main content panel with scroll
        JScrollPane scrollPane = new ThemedScrollPane(createContentPanel());
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createContentPanel() {
        ThemedPanel contentPanel = new ThemedPanel();
        contentPanel.setLayout(new MigLayout("fill", "", ""));

        windowSettingsPanel = new WindowSettingsPanel();
        contentPanel.add(windowSettingsPanel, "grow, wrap");

        notificationServicePanel = new NotificationServiceSettingsPanel();
        contentPanel.add(notificationServicePanel, "grow, wrap");


        repositorySectionPanel = new RepositoriesPanel();
        contentPanel.add(repositorySectionPanel, "grow, pushy, wrap");

        // Polling Configuration Section - fixed size at bottom
        contentPanel.add(createPollingSection(), "grow");

        return contentPanel;
    }

    private ThemedPanel createPollingSection() {
        ThemedPanel section = new ThemedPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        // Polling interval
        ThemedLabel intervalLabel = new ThemedLabel("Polling Interval (minutes):");
        intervalSpinner = new ThemedSpinner(new SpinnerNumberModel(15, 1, 1440, 1));

        // Save when spinner loses focus
        JComponent intervalEditor = intervalSpinner.getEditor();
        if (intervalEditor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) intervalEditor).getTextField().addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    int minutes = (Integer) intervalSpinner.getValue();
                    settingsManager.updatePollingInterval(minutes);
                }
            });
        }

        ThemedPanel intervalPanel = new ThemedPanel();
        intervalPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        intervalPanel.setOpaque(false);
        intervalPanel.add(intervalLabel);
        intervalPanel.add(intervalSpinner);

        section.add(intervalPanel);
        section.add(Box.createVerticalStrut(10));

        // Enable polling checkbox
        enablePollingCheckBox = new ThemedCheckBox("Enable automatic polling", true);
        enablePollingCheckBox.addActionListener(e -> {
            settingsManager.updateEnablePolling(enablePollingCheckBox.isSelected());
        });
        section.add(enablePollingCheckBox);

        return section;
    }




    /**
     * Load settings from SettingsManager into UI components
     */
    private void loadSettings() {
        AppSettings settings = settingsManager.getSettings();

        // Load repositories
        repositoryListModel.clear();
        for (AppSettings.RepositoryConfig repo : settings.getRepositories()) {
            repositoryListModel.addElement(repo);
        }

        // Load polling settings
        intervalSpinner.setValue(settings.getPollingIntervalMinutes());
        enablePollingCheckBox.setSelected(settings.isEnablePolling());
    }

    /**
     * Custom list cell renderer for repository items
     */


    @Override
    protected void paintComponent(Graphics g) {
        // Update window settings panel border with current theme colors
        if (windowSettingsPanel != null) {
            windowSettingsPanel.setBorder(ThemedTitledBorder.create("Window Settings"));
        }

        // Update notification service panel border with current theme colors
        if (notificationServicePanel != null) {
            notificationServicePanel.setBorder(ThemedTitledBorder.create("Automatic Notification Service"));
        }

        // Update repository section border with current theme colors
        if (repositorySectionPanel != null) {
            // Find and update the list panel with titled border
            for (Component comp : repositorySectionPanel.getComponents()) {
                if (comp instanceof ThemedPanel) {
                    ThemedPanel panel = (ThemedPanel) comp;
                    // Check if this is the list panel by looking for the titled border
                    if (panel.getBorder() instanceof javax.swing.border.TitledBorder) {
                        panel.setBorder(ThemedTitledBorder.create("Repositories"));
                    }
                }
            }
        }
        super.paintComponent(g);
    }
}



