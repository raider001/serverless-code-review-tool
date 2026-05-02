package com.kalynx.serverlessreviewtool.ui;

import com.kalynx.serverlessreviewtool.configuration.SettingsManager;
import com.kalynx.serverlessreviewtool.mockdata.RepositoryMockData_Old;
import com.kalynx.serverlessreviewtool.mockdata.ReviewContextMockData_Old;
import com.kalynx.serverlessreviewtool.mockdata.ReviewItemMockData_Old;
import com.kalynx.serverlessreviewtool.mockdata.UserMockData_Old;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.QuickButton;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedFrame;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedPanel;
import com.kalynx.serverlessreviewtool.theme.icons.AppIcon;
import com.kalynx.serverlessreviewtool.theme.icons.RefreshIcon;
import com.kalynx.serverlessreviewtool.ui.mainpanels.ReviewPanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.ReviewSelectionPanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.SettingsPanel;

import javax.swing.*;
import java.awt.*;

/**
 * MainFrame - The main application window
 * Contains ReviewSelectionPanel by default with navigation to Settings and Help
 */
public class MainFrame extends ThemedFrame {

    private ReviewSelectionPanel reviewSelectionPanel;
    private ReviewPanel reviewPanel;
    private SettingsPanel settingsPanel;
    private HelpPanel helpPanel;
    private ThemedPanel currentPanel;
    private QuickButton refreshButton;

    public MainFrame() {
        // Use static method to get settings before super() call
        super("Serverless Review Tool",
              getInitialWidth(),
              getInitialHeight());
        setApplicationIcon(AppIcon.createIconImages());
        initializePanels();
        setupMenuItems();
        setupRefreshButton();
        showReviewPanel();
    }

    private static int getInitialWidth() {
        return SettingsManager.getInstance().getSettings().getWindow().getDefaultWidth();
    }

    private static int getInitialHeight() {
        return SettingsManager.getInstance().getSettings().getWindow().getDefaultHeight();
    }


    private void setupRefreshButton() {
        refreshButton = createRefreshButton();
        refreshButton.addActionListener(e -> {
            UserMockData_Old.refreshMockData();
            RepositoryMockData_Old.refreshMockData();
            ReviewItemMockData_Old.refreshMockData();
            ReviewContextMockData_Old.refreshMockData();
        });
        refreshButton.setVisible(true);
        getTitleBar().addActionButton(refreshButton);
    }

    private QuickButton createRefreshButton() {
        return new QuickButton(new RefreshIcon())
            .setTooltip("Refresh")
            .setAccentHover();
    }

    private void initializePanels() {
        reviewSelectionPanel = new ReviewSelectionPanel();
        reviewPanel = new ReviewPanel();
        settingsPanel = new SettingsPanel();
        helpPanel = new HelpPanel();
    }

    private void setupMenuItems() {
        setMenuItems(
            new MenuItem("Reviews", this::showReviewPanel),
            new MenuItem("Review Code", this::showCodeReviewPanel),
            new MenuItem("Settings", this::showSettingsPanel),
            new MenuItem("Help", this::showHelpPanel)
        );
    }

    private void showReviewPanel() {
        switchPanel(reviewSelectionPanel);
        setWindowTitle("Serverless Review Tool - Reviews");
    }

    private void showCodeReviewPanel() {
        switchPanel(reviewPanel);
        setWindowTitle("Serverless Review Tool - Code Review");
    }

    private void showSettingsPanel() {
        switchPanel(settingsPanel);
        setWindowTitle("Serverless Review Tool - Settings");
    }

    private void showHelpPanel() {
        switchPanel(helpPanel);
        setWindowTitle("Serverless Review Tool - Help");
    }

    private void switchPanel(ThemedPanel newPanel) {
        if (currentPanel != null) {
            getContentPanel().remove(currentPanel);
        }
        currentPanel = newPanel;
        getContentPanel().add(currentPanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        UserMockData_Old.loadMockData();
        RepositoryMockData_Old.loadMockData();
        ReviewItemMockData_Old.loadMockData();
        ReviewContextMockData_Old.loadMockData();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}

