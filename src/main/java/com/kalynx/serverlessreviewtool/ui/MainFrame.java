package com.kalynx.serverlessreviewtool.ui;

import com.kalynx.lwdi.DI;
import com.kalynx.serverlessreviewtool.configuration.SettingsManager;
import com.kalynx.serverlessreviewtool.git.Git;
import com.kalynx.serverlessreviewtool.managers.RepositoryManager;
import com.kalynx.serverlessreviewtool.managers.ReviewContextManager;
import com.kalynx.serverlessreviewtool.managers.ReviewItemManager;
import com.kalynx.serverlessreviewtool.managers.UserManager;
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
import com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog.ReviewFormModels;

import java.awt.*;

/**
 * MainFrame - The main application window
 * Contains ReviewSelectionPanel by default with navigation to Settings and Help
 */
public class MainFrame extends ThemedFrame {

    private final SettingsManager settingsManager;
    private final UserManager userManager;
    private final RepositoryManager repositoryManager;
    private final ReviewItemManager reviewItemManager;
    private final ReviewContextManager reviewContextManager;
    private final ReviewFormModels reviewFormModels;
    private final Git git;

    private ReviewSelectionPanel reviewSelectionPanel;
    private ReviewPanel reviewPanel;
    private SettingsPanel settingsPanel;
    private HelpPanel helpPanel;
    private ThemedPanel currentPanel;

    @DI
    public MainFrame(
            SettingsManager settingsManager,
            UserManager userManager,
            RepositoryManager repositoryManager,
            ReviewItemManager reviewItemManager,
            ReviewContextManager reviewContextManager,
            ReviewFormModels reviewFormModels,
            Git git) {
        super("Serverless Review Tool",
              settingsManager.getSettings().getWindow().getDefaultWidth(),
              settingsManager.getSettings().getWindow().getDefaultHeight());
        this.settingsManager = settingsManager;
        this.userManager = userManager;
        this.repositoryManager = repositoryManager;
        this.reviewItemManager = reviewItemManager;
        this.reviewContextManager = reviewContextManager;
        this.reviewFormModels = reviewFormModels;
        this.git = git;
        setApplicationIcon(AppIcon.createIconImages());
        initializePanels();
        setupMenuItems();
        setupRefreshButton();
        showReviewPanel();
    }


    private void setupRefreshButton() {
        QuickButton refreshButton = createRefreshButton();
        refreshButton.addActionListener(ignored -> {
            UserMockData_Old.refreshMockData(userManager);
            ReviewItemMockData_Old.refreshMockData(reviewItemManager);
            ReviewContextMockData_Old.refreshMockData(reviewContextManager);
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
        reviewSelectionPanel = new ReviewSelectionPanel(repositoryManager, reviewItemManager, reviewFormModels, git);
        reviewPanel = new ReviewPanel(reviewContextManager, repositoryManager, userManager, reviewFormModels, git);
        settingsPanel = new SettingsPanel(settingsManager);
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
}

