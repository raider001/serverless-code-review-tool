package com.kalynx.serverlessreviewtool.ui;

import com.kalynx.serverlessreviewtool.theme.components.*;
import com.kalynx.serverlessreviewtool.ui.review.*;
import com.kalynx.serverlessreviewtool.ui.review.model.*;

import javax.swing.*;
import java.awt.*;

/**
 * ReviewPanel - Main panel for reviewing code changes across multiple repositories
 * Supports file navigation, diff viewing (side-by-side and unified), commit comparison, and inline comments
 */
public class ReviewPanel extends ThemedPanel {

    private ReviewContext reviewContext;

    // Main components
    private CommitSelectorPanel commitSelectorPanel;
    private FileNavigationPanel fileNavigationPanel;
    private DiffViewerPanel diffViewerPanel;

    public ReviewPanel() {
        setLayout(new BorderLayout());

        // Initialize with sample review context
        initializeSampleReviewContext();

        // Build UI
        initializeComponents();
        layoutComponents();
    }

    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        // Commit selector at the top
        commitSelectorPanel = new CommitSelectorPanel(reviewContext);
        commitSelectorPanel.addCommitSelectionListener(this::onCommitSelectionChanged);
        commitSelectorPanel.addViewModeListener(this::onViewModeChanged);

        // File navigation on the left
        fileNavigationPanel = new FileNavigationPanel(reviewContext);
        fileNavigationPanel.addFileSelectionListener(this::onFileSelected);

        // Diff viewer in the center
        diffViewerPanel = new DiffViewerPanel();
    }

    /**
     * Layout all components
     */
    private void layoutComponents() {
        // Top: Commit selector
        add(commitSelectorPanel, BorderLayout.NORTH);

        // Main split pane: File navigation and Diff viewer
        ThemedSplitPane mainSplitPane = new ThemedSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setLeftComponent(fileNavigationPanel);
        mainSplitPane.setRightComponent(diffViewerPanel);
        mainSplitPane.setResizeWeight(0.20); // Give 20% to file navigation

        add(mainSplitPane, BorderLayout.CENTER);
    }

    /**
     * Initialize sample review context for demonstration
     */
    private void initializeSampleReviewContext() {
        reviewContext = new ReviewContext("REVIEW-123", "Implement OAuth2 authentication");

        // Add sample repositories
        Repository backendRepo = new Repository("backend-api", "https://github.com/company/backend-api");
        Repository frontendRepo = new Repository("frontend-app", "https://github.com/company/frontend-app");

        // Add sample commits to backend repo
        backendRepo.addCommit(new Commit("abc123", "Initial OAuth2 setup", "John Doe", "2024-04-20"));
        backendRepo.addCommit(new Commit("def456", "Add token validation", "John Doe", "2024-04-21"));
        backendRepo.addCommit(new Commit("ghi789", "Implement refresh token", "John Doe", "2024-04-22"));

        // Add sample files to backend repo
        backendRepo.addFile(new ReviewFile("src/auth/OAuthService.java", "backend-api", FileChangeType.MODIFIED));
        backendRepo.addFile(new ReviewFile("src/auth/TokenValidator.java", "backend-api", FileChangeType.ADDED));
        backendRepo.addFile(new ReviewFile("src/config/SecurityConfig.java", "backend-api", FileChangeType.MODIFIED));
        backendRepo.addFile(new ReviewFile("src/models/User.java", "backend-api", FileChangeType.MODIFIED));

        // Add sample commits to frontend repo
        frontendRepo.addCommit(new Commit("jkl012", "Add OAuth login UI", "Jane Smith", "2024-04-21"));
        frontendRepo.addCommit(new Commit("mno345", "Handle OAuth callbacks", "Jane Smith", "2024-04-22"));

        // Add sample files to frontend repo
        frontendRepo.addFile(new ReviewFile("src/components/LoginForm.tsx", "frontend-app", FileChangeType.MODIFIED));
        frontendRepo.addFile(new ReviewFile("src/services/AuthService.ts", "frontend-app", FileChangeType.ADDED));
        frontendRepo.addFile(new ReviewFile("src/utils/TokenStorage.ts", "frontend-app", FileChangeType.ADDED));

        reviewContext.addRepository(backendRepo);
        reviewContext.addRepository(frontendRepo);
    }

    /**
     * Handle commit selection change
     */
    private void onCommitSelectionChanged(Commit startCommit, Commit endCommit) {
        System.out.println("Commit selection changed: " + startCommit.getHash() + " -> " + endCommit.getHash());
        // Update file navigation to show files changed between these commits
        fileNavigationPanel.setCommitRange(startCommit, endCommit);
        // Update diff viewer if a file is selected
        ReviewFile selectedFile = fileNavigationPanel.getSelectedFile();
        if (selectedFile != null) {
            diffViewerPanel.showDiff(selectedFile, startCommit, endCommit);
        }
    }

    /**
     * Handle file selection
     */
    private void onFileSelected(ReviewFile file) {
        System.out.println("File selected: " + file.getPath());
        // Get current commit range
        Commit startCommit = commitSelectorPanel.getStartCommit();
        Commit endCommit = commitSelectorPanel.getEndCommit();

        // Show diff for this file
        diffViewerPanel.showDiff(file, startCommit, endCommit);
    }

    /**
     * Handle view mode change
     */
    private void onViewModeChanged(DiffViewMode mode) {
        System.out.println("View mode changed to: " + mode);
        // Update diff viewer with new view mode
        diffViewerPanel.setViewMode(mode);
    }

    /**
     * Set the review context
     */
    public void setReviewContext(ReviewContext context) {
        this.reviewContext = context;
        commitSelectorPanel.setReviewContext(context);
        fileNavigationPanel.setReviewContext(context);

        // Reset selection
        diffViewerPanel.clear();
    }

    /**
     * Get the current review context
     */
    public ReviewContext getReviewContext() {
        return reviewContext;
    }
}

