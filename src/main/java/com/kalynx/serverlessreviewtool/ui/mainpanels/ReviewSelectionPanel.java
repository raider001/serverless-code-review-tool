package com.kalynx.serverlessreviewtool.ui.mainpanels;

import com.kalynx.serverlessreviewtool.theme.components.*;
import com.kalynx.serverlessreviewtool.ui.CreateReviewDialog;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewselectionpanel.FilterPanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewselectionpanel.ReviewListPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * ReviewSelectionPanel - Main UI for selecting and filtering code reviews
 */
public class ReviewSelectionPanel extends ThemedPanel {

    private final ReviewListPanel listPanel = new ReviewListPanel();
    private final FilterPanel filterPanel = new FilterPanel().addFilterEventListener(listPanel::filterLists);

    private final ThemedButton createReviewButton = new ThemedButton("Create Review");

    public ReviewSelectionPanel() {
        configureLayout();
        configureActions();
    }

    private void configureLayout() {
        setLayout(new MigLayout("", "[grow]", "[][grow][]"));
        setOpaque(true);

        add(filterPanel, "cell 0 0, grow");
        add(listPanel, "cell 0 1, grow");

        add(createReviewButton, "cell 0 2, align right");
    }

    private void configureActions() {
        createReviewButton.addActionListener(e -> onCreateReview());
    }

    /**
     * Handle create review button click
     */
    private void onCreateReview() {
        // Get list of available repositories (in real app, fetch from data source)
        java.util.List<String> availableRepositories = java.util.Arrays.asList(
            "frontend-app", "backend-api", "mobile-app", "shared-lib"
        );

        // Get list of available reviewers (in real app, fetch from data source)
        java.util.List<String> availableReviewers = java.util.Arrays.asList(
            "John Doe", "Jane Smith", "Bob Johnson", "Eve Anderson", "Michael Scott", "Sarah Connor", "James Bond", "Tony Stark"
        );

        CreateReviewDialog dialog = new CreateReviewDialog(SwingUtilities.getWindowAncestor(this), availableRepositories, availableReviewers);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            String title = dialog.getReviewTitle();
            String summary = dialog.getSummary();
            boolean isBranchMode = dialog.isBranchMode();
            java.util.List<String> selectedRepos = dialog.getSelectedRepositories();
            java.util.List<String> selectedReviewers = dialog.getSelectedReviewers();

            System.out.println("Creating review:");
            System.out.println("  Title: " + title);
            System.out.println("  Summary: " + summary);
            System.out.println("  Mode: " + (isBranchMode ? "Branch-based" : "Commit-based"));
            System.out.println("  Repositories: " + selectedRepos);
            System.out.println("  Reviewers: " + selectedReviewers);

            // TODO: Create the review in the backend and navigate to review details
        }
    }
}