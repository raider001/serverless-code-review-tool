package com.kalynx.serverlessreviewtool.ui.mainpanels;

import com.kalynx.serverlessreviewtool.git.Git;
import com.kalynx.serverlessreviewtool.managers.RepositoryManager;
import com.kalynx.serverlessreviewtool.managers.ReviewItemManager;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedButton;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedPanel;
import com.kalynx.serverlessreviewtool.theme.LoadingStateManager;
import com.kalynx.serverlessreviewtool.ui.Refreshable;
import com.kalynx.serverlessreviewtool.ui.models.mainpanels.reviewselectionpanel.ReviewSelectionPanelModel;
import com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog.ReviewFormModels;
import com.kalynx.serverlessreviewtool.ui.review.CreateReviewDialog;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewselectionpanel.FilterPanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewselectionpanel.ReviewListPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * ReviewSelectionPanel - Main UI for selecting and filtering code reviews
 */
public class ReviewSelectionPanel extends ThemedPanel implements Refreshable {

    private final ReviewListPanel listPanel;
    private final FilterPanel filterPanel;
    private final ReviewFormModels reviewFormModels;
    private final RepositoryManager repositoryManager;
    private final ReviewItemManager reviewItemManager;
    private final Git git;

    private final ThemedButton createReviewButton = new ThemedButton("Create Review");

    public ReviewSelectionPanel(RepositoryManager repositoryManager,
                               ReviewItemManager reviewItemManager,
                               ReviewSelectionPanelModel reviewSelectionPanelModel,
                               ReviewFormModels reviewFormModels,
                               Git git) {
        this.reviewFormModels = reviewFormModels;
        this.repositoryManager = repositoryManager;
        this.reviewItemManager = reviewItemManager;
        this.git = git;
        this.listPanel = new ReviewListPanel(reviewSelectionPanelModel);
        this.filterPanel = new FilterPanel(repositoryManager).addFilterEventListener(this::onFilterChanged);
        configureLayout();
        configureActions();
    }

    private void onFilterChanged(String title, String author, java.util.List<String> repositories) {
        listPanel.applyFilters(title, author, repositories);
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
        CreateReviewDialog dialog = new CreateReviewDialog(
            SwingUtilities.getWindowAncestor(this),
            reviewFormModels,
            repositoryManager,
            git
        );
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            onRefresh();
        }
    }

    /**
     * Called when the panel is shown in the MainFrame.
     * Loads data on first show.
     */
    public void onPanelShown() {
        onRefresh();
    }

    @Override
    public void onRefresh() {
        LoadingStateManager.getInstance().startLoading("review-refresh");
        reviewItemManager.refresh()
            .whenComplete((result, error) -> {
                LoadingStateManager.getInstance().stopLoading("review-refresh");
                if (error != null) {
                    System.err.println("Error refreshing reviews: " + error.getMessage());
                }
            });
    }
}