package com.kalynx.serverlessreviewtool.ui;

import com.kalynx.lwdi.DI;
import com.kalynx.serverlessreviewtool.configuration.SettingsManager;
import com.kalynx.serverlessreviewtool.git.Git;
import com.kalynx.serverlessreviewtool.managers.RepositoryManager;
import com.kalynx.serverlessreviewtool.managers.ReviewContextManager;
import com.kalynx.serverlessreviewtool.managers.ReviewItemManager;
import com.kalynx.serverlessreviewtool.managers.UserManager;
import com.kalynx.serverlessreviewtool.models.ReviewItem;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.QuickButton;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedFrame;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedPanel;
import com.kalynx.serverlessreviewtool.theme.icons.AppIcon;
import com.kalynx.serverlessreviewtool.theme.icons.RefreshIcon;
import com.kalynx.serverlessreviewtool.ui.mainpanels.LogsPanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.ReviewPanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.ReviewSelectionPanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.SettingsPanel;
import com.kalynx.serverlessreviewtool.ui.models.mainpanels.reviewpanel.ReviewPanelModel;
import com.kalynx.serverlessreviewtool.ui.models.mainpanels.reviewselectionpanel.ReviewSelectionPanelModel;
import com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog.ReviewFormModels;
import com.kalynx.serverlessreviewtool.utils.TeeOutputStream;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;

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
    private final ReviewSelectionPanelModel reviewSelectionPanelModel;
    private final ReviewPanelModel reviewPanelModel;
    private final Git git;

    private ReviewSelectionPanel reviewSelectionPanel;
    private ReviewPanel reviewPanel;
    private SettingsPanel settingsPanel;
    private LogsPanel logsPanel;
    private HelpPanel helpPanel;
    private ThemedPanel currentPanel;
    private QuickButton refreshButton;

    @DI
    public MainFrame(
            SettingsManager settingsManager,
            UserManager userManager,
            RepositoryManager repositoryManager,
            ReviewItemManager reviewItemManager,
            ReviewContextManager reviewContextManager,
            ReviewFormModels reviewFormModels,
            ReviewSelectionPanelModel reviewSelectionPanelModel,
            ReviewPanelModel reviewPanelModel,
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
        this.reviewSelectionPanelModel = reviewSelectionPanelModel;
        this.reviewPanelModel = reviewPanelModel;
        this.git = git;
        setApplicationIcon(AppIcon.createIconImages());
        initializePanels();
        setupMenuItems();
        setupRefreshButton();
        setupReviewDoubleClickHandler();
        showReviewPanel();
    }


    private void setupRefreshButton() {
        refreshButton = createRefreshButton();
        refreshButton.addActionListener(ignored -> onRefresh());
        getTitleBar().addActionButton(refreshButton);
    }

    private void onRefresh() {
        if (currentPanel instanceof Refreshable) {
            ((Refreshable) currentPanel).onRefresh();
        }
    }

    private QuickButton createRefreshButton() {
        return new QuickButton(new RefreshIcon())
            .setTooltip("Refresh")
            .setAccentHover();
    }

    private void initializePanels() {
        reviewSelectionPanel = new ReviewSelectionPanel(repositoryManager, reviewItemManager, reviewSelectionPanelModel, reviewFormModels, git);
        reviewPanel = new ReviewPanel(reviewContextManager, repositoryManager, reviewFormModels, reviewPanelModel, git);
        settingsPanel = new SettingsPanel(settingsManager, git);
        logsPanel = new LogsPanel();
        helpPanel = new HelpPanel();

        redirectConsoleToLogsPanel();
    }

    private void redirectConsoleToLogsPanel() {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        TeeOutputStream teeOut = new TeeOutputStream(originalOut, logsPanel);
        TeeOutputStream teeErr = new TeeOutputStream(originalErr, logsPanel);

        System.setOut(new PrintStream(teeOut, true));
        System.setErr(new PrintStream(teeErr, true));
    }

    private void setupReviewDoubleClickHandler() {
        reviewSelectionPanel.setOnReviewDoubleClick(this::onReviewDoubleClicked);
    }

    private void onReviewDoubleClicked(ReviewItem reviewItem) {
        reviewPanel.loadReview(reviewItem.getReviewId(), reviewItem.getRepository());
        showCodeReviewPanel();
    }

    private void setupMenuItems() {
        setMenuItems(
            new MenuItem("Reviews", this::showReviewPanel),
            new MenuItem("Review Code", this::showCodeReviewPanel),
            new MenuItem("Settings", this::showSettingsPanel),
            new MenuItem("Logs", this::showLogsPanel),
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

    private void showLogsPanel() {
        switchPanel(logsPanel);
        setWindowTitle("Serverless Review Tool - Logs");
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
        updateRefreshButtonVisibility();

        if (newPanel instanceof ReviewSelectionPanel) {
            ((ReviewSelectionPanel) newPanel).onPanelShown();
        }
    }

    private void updateRefreshButtonVisibility() {
        refreshButton.setVisible(currentPanel instanceof Refreshable);
    }
}

