package com.kalynx.serverlessreviewtool.ui.mainpanels.settingspanel;

import com.kalynx.serverlessreviewtool.configuration.AppSettings;
import com.kalynx.serverlessreviewtool.configuration.SettingsManager;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.*;
import com.kalynx.serverlessreviewtool.ui.RepositoryConfigDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class RepositoriesPanel extends ThemedPanel {

    private final SettingsManager settingsManager;
    private final DefaultListModel<AppSettings.RepositoryConfig> repositoryListModel = new DefaultListModel<>();

    private final ThemedList<AppSettings.RepositoryConfig> repositoryList = new ThemedList<>();
    private final ThemedButton addRepositoryButton = new ThemedButton("Add Repository");
    private final ThemedButton editRepositoryButton = new ThemedButton("Edit Repository");
    private final ThemedButton removeRepositoryButton = new ThemedButton("Remove Repository");

    public RepositoriesPanel(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        setupRepositoriesList();
        loadRepositories();
        configureLayout();
        setupActions();
    }

    private void configureLayout() {
        setLayout(new MigLayout("", "[][][][grow]", "[grow][]"));
        setBorder(ThemedTitledBorder.create("Repositories"));
        ThemedScrollPane listScrollPane = new ThemedScrollPane(repositoryList);
        add(listScrollPane, "cell 0 0 4 1, grow");
        add(addRepositoryButton, "cell 0 1");
        add(editRepositoryButton, "cell 1 1");
        add(removeRepositoryButton, "cell 2 1");
    }

    private void setupRepositoriesList() {
        repositoryList.setModel(repositoryListModel);
        repositoryList.setVisibleRowCount(4);
        repositoryList.setCellRenderer(new RepositoryListCellRenderer());
    }

    private void loadRepositories() {
        repositoryListModel.clear();
        for (AppSettings.RepositoryConfig config : settingsManager.getSettings().getRepositories()) {
            repositoryListModel.addElement(config);
        }
    }

    private void setupActions() {
        // Initialize button states
        updateButtonStates();

        // Update button states on selection change
        repositoryList.addListSelectionListener(ignored -> updateButtonStates());

        addRepositoryButton.addActionListener(ignored -> showAddRepositoryDialog());
        editRepositoryButton.addActionListener(ignored -> editSelectedRepository());
        removeRepositoryButton.addActionListener(ignored -> removeSelectedRepository());
    }

    private void updateButtonStates() {
        boolean hasSelection = repositoryList.getSelectedIndex() >= 0;
        editRepositoryButton.setEnabled(hasSelection);
        removeRepositoryButton.setEnabled(hasSelection);
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
            addRepository(config);
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
                updateRepository(selectedIndex, updated);
            }
        }
    }

    private void removeSelectedRepository() {
        int selectedIndex = repositoryList.getSelectedIndex();
        if (selectedIndex >= 0) {
            AppSettings.RepositoryConfig config = repositoryListModel.getElementAt(selectedIndex);

            boolean confirmed = ThemedConfirmDialog.showConfirmation(
                SwingUtilities.getWindowAncestor(this),
                "Confirm Delete",
                "Are you sure you want to remove '" + config.getName() + "'?"
            );

            if (confirmed) {
                removeRepository(selectedIndex);
            }
        }
    }

    // Helper methods to keep settings and UI model synchronized

    private void addRepository(AppSettings.RepositoryConfig config) {
        settingsManager.addRepository(config);
        repositoryListModel.addElement(config);
    }

    private void updateRepository(int index, AppSettings.RepositoryConfig updated) {
        settingsManager.updateRepository(index, updated);
        repositoryListModel.setElementAt(updated, index);
    }

    private void removeRepository(int index) {
        settingsManager.removeRepository(index);
        repositoryListModel.remove(index);
    }
}
