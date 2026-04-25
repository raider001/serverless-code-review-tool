package com.kalynx.serverlessreviewtool.ui;

import com.kalynx.serverlessreviewtool.theme.components.ThemedFrame;
import com.kalynx.serverlessreviewtool.theme.components.ThemedPanel;

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

    public MainFrame() {
        super("Serverless Review Tool", 1000, 700);
        initializePanels();
        setupMenuItems();
        showReviewPanel();
    }

    /**
     * Initialize all panels
     */
    private void initializePanels() {
        reviewSelectionPanel = new ReviewSelectionPanel();
        reviewPanel = new ReviewPanel();
        settingsPanel = new SettingsPanel();
        helpPanel = new HelpPanel();
    }

    /**
     * Setup menu items for navigation
     */
    private void setupMenuItems() {
        setMenuItems(
            new MenuItem("Reviews", this::showReviewPanel),
            new MenuItem("Review Code", this::showCodeReviewPanel),
            new MenuItem("Settings", this::showSettingsPanel),
            new MenuItem("Help", this::showHelpPanel)
        );
    }

    /**
     * Show the Review Selection panel
     */
    private void showReviewPanel() {
        switchPanel(reviewSelectionPanel);
        setWindowTitle("Serverless Review Tool - Reviews");
    }

    /**
     * Show the Code Review panel
     */
    private void showCodeReviewPanel() {
        switchPanel(reviewPanel);
        setWindowTitle("Serverless Review Tool - Code Review");
    }

    /**
     * Show the Settings panel
     */
    private void showSettingsPanel() {
        switchPanel(settingsPanel);
        setWindowTitle("Serverless Review Tool - Settings");
    }

    /**
     * Show the Help panel
     */
    private void showHelpPanel() {
        switchPanel(helpPanel);
        setWindowTitle("Serverless Review Tool - Help");
    }

    /**
     * Switch the active panel in the content area
     */
    private void switchPanel(ThemedPanel newPanel) {
        if (currentPanel != null) {
            getContentPanel().remove(currentPanel);
        }
        currentPanel = newPanel;
        getContentPanel().add(currentPanel, BorderLayout.CENTER);
        getContentPanel().revalidate();
        getContentPanel().repaint();
    }

    /**
     * Main entry point for the application
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}

