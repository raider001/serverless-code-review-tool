package com.kalynx.serverlessreviewtool.ui.mainpanels;

import com.kalynx.serverlessreviewtool.managers.RepositoryManager;
import com.kalynx.serverlessreviewtool.managers.ReviewItemManager;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedButton;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedPanel;
import com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog.ReviewFormModels;
import com.kalynx.serverlessreviewtool.ui.review.CreateReviewDialog;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewselectionpanel.FilterPanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewselectionpanel.ReviewListPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * ReviewSelectionPanel - Main UI for selecting and filtering code reviews
 */
public class ReviewSelectionPanel extends ThemedPanel {

    private final ReviewListPanel listPanel;
    private final FilterPanel filterPanel;
    private final ReviewFormModels reviewFormModels;

    private final ThemedButton createReviewButton = new ThemedButton("Create Review");

    public ReviewSelectionPanel(RepositoryManager repositoryManager, ReviewItemManager reviewItemManager, ReviewFormModels reviewFormModels) {
        this.reviewFormModels = reviewFormModels;
        this.listPanel = new ReviewListPanel(reviewItemManager);
        this.filterPanel = new FilterPanel(repositoryManager).addFilterEventListener(listPanel::filterLists);
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
        createReviewButton.addActionListener(ignored -> onCreateReview());
    }

    /**
     * Handle create review button click
     */
    private void onCreateReview() {
        CreateReviewDialog dialog = new CreateReviewDialog(SwingUtilities.getWindowAncestor(this), reviewFormModels);
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