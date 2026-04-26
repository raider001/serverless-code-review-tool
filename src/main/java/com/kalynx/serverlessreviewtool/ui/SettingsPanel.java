package com.kalynx.serverlessreviewtool.ui;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.theme.components.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SettingsPanel - Application settings and configuration
 * Includes repository polling configuration and management
 */
public class SettingsPanel extends ThemedPanel {
    private final ThemeManager themeManager;
    private final DefaultListModel<RepositoryConfig> repositoryListModel;
    private final ThemedList<RepositoryConfig> repositoryList;
    private final List<RepositoryConfig> repositories;
    private ThemedPanel repositorySectionPanel;
    private ThemedPanel notificationServicePanel;
    private ThemedTextField notificationServiceUrlField;

    public SettingsPanel() {
        this.themeManager = ThemeManager.getInstance();
        this.repositories = new ArrayList<>();
        this.repositoryListModel = new DefaultListModel<>();
        this.repositoryList = new ThemedList<>(repositoryListModel);

        setLayout(new BorderLayout());
        initializeComponents();
    }

    private void initializeComponents() {

        // Main content panel with scroll
        JScrollPane scrollPane = new ThemedScrollPane(createContentPanel());
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createContentPanel() {
        ThemedPanel contentPanel = new ThemedPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Notification Service Section
        notificationServicePanel = createNotificationServiceSection();
        contentPanel.add(notificationServicePanel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Repository Management Section - expands to fill space
        repositorySectionPanel = createRepositorySection();
        contentPanel.add(repositorySectionPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Polling Configuration Section
        contentPanel.add(createPollingSection());

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
        innerPanel.add(notificationServiceUrlField);

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
        ThemedSpinner intervalSpinner = new ThemedSpinner(new SpinnerNumberModel(15, 1, 1440, 1));

        ThemedPanel intervalPanel = new ThemedPanel();
        intervalPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        intervalPanel.setOpaque(false);
        intervalPanel.add(intervalLabel);
        intervalPanel.add(intervalSpinner);

        section.add(intervalPanel);
        section.add(Box.createVerticalStrut(10));

        // Enable polling checkbox
        ThemedCheckBox enablePollingCheckBox = new ThemedCheckBox("Enable automatic polling", true);
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
            RepositoryConfig config = dialog.getRepositoryConfig();
            repositories.add(config);
            repositoryListModel.addElement(config);
        }
    }

    private void editSelectedRepository() {
        int selectedIndex = repositoryList.getSelectedIndex();
        if (selectedIndex >= 0) {
            RepositoryConfig selected = repositoryListModel.getElementAt(selectedIndex);
            RepositoryConfigDialog dialog = new RepositoryConfigDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Edit Repository",
                    selected
            );
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                RepositoryConfig updated = dialog.getRepositoryConfig();
                repositories.set(selectedIndex, updated);
                repositoryListModel.setElementAt(updated, selectedIndex);
            }
        }
    }

    private void removeSelectedRepository() {
        int selectedIndex = repositoryList.getSelectedIndex();
        if (selectedIndex >= 0) {
            repositories.remove(selectedIndex);
            repositoryListModel.remove(selectedIndex);
        }
    }

    /**
     * Get the notification service URL
     */
    public String getNotificationServiceUrl() {
        return notificationServiceUrlField != null ? notificationServiceUrlField.getText() : "";
    }

    /**
     * Set the notification service URL
     */
    public void setNotificationServiceUrl(String url) {
        if (notificationServiceUrlField != null) {
            notificationServiceUrlField.setText(url);
        }
    }

    /**
     * Repository configuration data class
     */
    public static class RepositoryConfig {
        private final String name;
        private final String url;
        private final int pollingIntervalMinutes;

        public RepositoryConfig(String name, String url, int pollingIntervalMinutes) {
            this.name = name;
            this.url = url;
            this.pollingIntervalMinutes = pollingIntervalMinutes;
        }

        public String getName() { return name; }
        public String getUrl() { return url; }
        public int getPollingIntervalMinutes() { return pollingIntervalMinutes; }

        @Override
        public String toString() {
            return name + " (" + url + ")";
        }
    }

    /**
     * Custom list cell renderer for repository items
     */
    private class RepositoryListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof RepositoryConfig) {
                RepositoryConfig config = (RepositoryConfig) value;
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



