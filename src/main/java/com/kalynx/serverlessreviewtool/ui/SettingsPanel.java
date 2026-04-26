package com.kalynx.serverlessreviewtool.ui;

import com.kalynx.serverlessreviewtool.configuration.AppSettings;
import com.kalynx.serverlessreviewtool.configuration.SettingsManager;
import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.theme.components.*;
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
    private final ThemeManager themeManager;
    private final SettingsManager settingsManager;
    private final DefaultListModel<AppSettings.RepositoryConfig> repositoryListModel;
    private final ThemedList<AppSettings.RepositoryConfig> repositoryList;
    private ThemedPanel repositorySectionPanel;
    private ThemedPanel notificationServicePanel;
    private ThemedPanel windowSettingsPanel;
    private ThemedTextField notificationServiceUrlField;
    private ThemedSpinner widthSpinner;
    private ThemedSpinner heightSpinner;
    private ThemedSpinner intervalSpinner;
    private ThemedCheckBox enablePollingCheckBox;

    public SettingsPanel() {
        this.themeManager = ThemeManager.getInstance();
        this.settingsManager = SettingsManager.getInstance();
        this.repositoryListModel = new DefaultListModel<>();
        this.repositoryList = new ThemedList<>(repositoryListModel);

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
        // MigLayout: all columns grow, each row only takes what it needs except repository section
        contentPanel.setLayout(new MigLayout("fill", "", ""));

        // Window Settings Section
        windowSettingsPanel = createWindowSettingsSection();
        contentPanel.add(windowSettingsPanel, "grow, wrap");

        // Notification Service Section - only takes required space
        notificationServicePanel = createNotificationServiceSection();
        contentPanel.add(notificationServicePanel, "grow, wrap");

        // Repository Management Section - expands to fill remaining space
        repositorySectionPanel = createRepositorySection();
        contentPanel.add(repositorySectionPanel, "grow, pushy, wrap");

        // Polling Configuration Section - fixed size at bottom
        contentPanel.add(createPollingSection(), "grow");

        return contentPanel;
    }

    private ThemedPanel createNotificationServiceSection() {
        ThemedPanel section = new ThemedPanel();
        section.setLayout(new BorderLayout());
        section.setBorder(ThemedTitledBorder.create("Automatic Notification Service"));

        // Inner panel with padding
        ThemedPanel innerPanel = new ThemedPanel();
        innerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, themeManager.scale(10), themeManager.scale(10)));
        innerPanel.setOpaque(false);

        // URL label
        ThemedLabel urlLabel = new ThemedLabel("Service URL:");
        innerPanel.add(urlLabel);

        // URL text field
        notificationServiceUrlField = new ThemedTextField(30);
        notificationServiceUrlField.setToolTipText("Enter the notification service URL");
        notificationServiceUrlField.setPreferredSize(new Dimension(
            themeManager.scale(350),
            themeManager.scale(28)
        ));

        // Auto-save when field loses focus
        notificationServiceUrlField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                settingsManager.updateNotificationServiceUrl(notificationServiceUrlField.getText());
            }
        });

        innerPanel.add(notificationServiceUrlField);

        section.add(innerPanel, BorderLayout.CENTER);
        return section;
    }

    private ThemedPanel createWindowSettingsSection() {
        ThemedPanel section = new ThemedPanel();
        section.setLayout(new BorderLayout());
        section.setBorder(ThemedTitledBorder.create("Window Settings"));

        // Inner panel with padding
        ThemedPanel innerPanel = new ThemedPanel();
        innerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, themeManager.scale(10), themeManager.scale(10)));
        innerPanel.setOpaque(false);

        // Default Width
        ThemedLabel widthLabel = new ThemedLabel("Default Width:");
        innerPanel.add(widthLabel);

        widthSpinner = new ThemedSpinner(new SpinnerNumberModel(1000, 800, 3840, 10));
        widthSpinner.setPreferredSize(new Dimension(themeManager.scale(100), themeManager.scale(28)));
        widthSpinner.setToolTipText("Default window width in pixels");
        innerPanel.add(widthSpinner);

        innerPanel.add(Box.createHorizontalStrut(themeManager.scale(20)));

        // Default Height
        ThemedLabel heightLabel = new ThemedLabel("Default Height:");
        innerPanel.add(heightLabel);

        heightSpinner = new ThemedSpinner(new SpinnerNumberModel(700, 600, 2160, 10));
        heightSpinner.setPreferredSize(new Dimension(themeManager.scale(100), themeManager.scale(28)));
        heightSpinner.setToolTipText("Default window height in pixels");
        innerPanel.add(heightSpinner);

        // Add focus listeners AFTER both spinners are created
        JComponent widthEditor = widthSpinner.getEditor();
        if (widthEditor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) widthEditor).getTextField().addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    int width = (Integer) widthSpinner.getValue();
                    int height = (Integer) heightSpinner.getValue();
                    settingsManager.updateWindowDefaults(width, height);
                }
            });
        }

        JComponent heightEditor = heightSpinner.getEditor();
        if (heightEditor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) heightEditor).getTextField().addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    int width = (Integer) widthSpinner.getValue();
                    int height = (Integer) heightSpinner.getValue();
                    settingsManager.updateWindowDefaults(width, height);
                }
            });
        }


        section.add(innerPanel, BorderLayout.CENTER);
        return section;
    }

    private ThemedPanel createRepositorySection() {
        ThemedPanel section = new ThemedPanel();
        section.setLayout(new BorderLayout(0, themeManager.scale(8)));

        // Repository list panel with border
        ThemedPanel listPanel = new ThemedPanel();
        listPanel.setLayout(new BorderLayout());
        listPanel.setBorder(ThemedTitledBorder.create("Repositories"));

        repositoryList.setVisibleRowCount(4);
        repositoryList.setCellRenderer(new RepositoryListCellRenderer());
        ThemedScrollPane listScrollPane = new ThemedScrollPane(repositoryList);
        listPanel.add(listScrollPane, BorderLayout.CENTER);

        section.add(listPanel, BorderLayout.CENTER);

        // Button panel below list
        ThemedPanel buttonPanel = new ThemedPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.setOpaque(false);

        ThemedButton addButton = new ThemedButton("Add Repository");
        addButton.addActionListener(e -> showAddRepositoryDialog());

        ThemedButton editButton = new ThemedButton("Edit Selected");
        editButton.addActionListener(e -> editSelectedRepository());

        ThemedButton removeButton = new ThemedButton("Remove Selected");
        removeButton.addActionListener(e -> removeSelectedRepository());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(removeButton);

        section.add(buttonPanel, BorderLayout.SOUTH);
        return section;
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


    private void showAddRepositoryDialog() {
        RepositoryConfigDialog dialog = new RepositoryConfigDialog(
                SwingUtilities.getWindowAncestor(this),
                "Add Repository",
                null
        );
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            AppSettings.RepositoryConfig config = dialog.getRepositoryConfig();
            settingsManager.getSettings().getRepositories().add(config);
            repositoryListModel.addElement(config);
            settingsManager.saveSettings();
        }
    }

    private void editSelectedRepository() {
        int selectedIndex = repositoryList.getSelectedIndex();
        if (selectedIndex >= 0) {
            AppSettings.RepositoryConfig selected = repositoryListModel.getElementAt(selectedIndex);
            RepositoryConfigDialog dialog = new RepositoryConfigDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Edit Repository",
                    selected
            );
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                AppSettings.RepositoryConfig updated = dialog.getRepositoryConfig();
                settingsManager.getSettings().getRepositories().set(selectedIndex, updated);
                repositoryListModel.setElementAt(updated, selectedIndex);
                settingsManager.saveSettings();
            }
        }
    }

    private void removeSelectedRepository() {
        int selectedIndex = repositoryList.getSelectedIndex();
        if (selectedIndex >= 0) {
            settingsManager.getSettings().getRepositories().remove(selectedIndex);
            repositoryListModel.remove(selectedIndex);
            settingsManager.saveSettings();
        }
    }

    /**
     * Load settings from SettingsManager into UI components
     */
    private void loadSettings() {
        AppSettings settings = settingsManager.getSettings();

        // Load window settings
        widthSpinner.setValue(settings.getWindow().getDefaultWidth());
        heightSpinner.setValue(settings.getWindow().getDefaultHeight());

        // Load notification service URL
        notificationServiceUrlField.setText(settings.getNotificationServiceUrl());

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
    private class RepositoryListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof AppSettings.RepositoryConfig) {
                AppSettings.RepositoryConfig config = (AppSettings.RepositoryConfig) value;
                setText("<html><b>" + config.getName() + "</b><br/>" +
                        "<small>" + config.getUrl() + " (Poll: " + config.getPollingIntervalMinutes() + " min)</small></html>");
            }

            Theme theme = themeManager.getCurrentTheme();
            if (isSelected) {
                setBackground(theme.getAccentColor());
                setForeground(Color.WHITE);
            } else {
                setBackground(theme.getBackgroundColor());
                setForeground(theme.getForegroundColor());
            }

            return this;
        }
    }

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



