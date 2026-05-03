package com.kalynx.serverlessreviewtool.ui.mainpanels;

import com.kalynx.serverlessreviewtool.git.Git;
import com.kalynx.serverlessreviewtool.managers.FileDiffManager;
import com.kalynx.serverlessreviewtool.managers.RepositoryManager;
import com.kalynx.serverlessreviewtool.managers.ReviewContextManager;

import com.kalynx.serverlessreviewtool.models.*;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedPanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.CodePanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.RejectApprovePanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.ReviewDetailPanel;
import com.kalynx.serverlessreviewtool.ui.models.mainpanels.reviewpanel.ReviewPanelModel;
import com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog.ReviewFormModels;
import net.miginfocom.swing.MigLayout;


/**
 * ReviewPanel - Main panel for reviewing code changes across multiple repositories
 * Supports file navigation, diff viewing (side-by-side and unified), commit comparison, and inline comments
 */
public class ReviewPanel extends ThemedPanel {

    private final ReviewContextManager reviewContextManager;
    private final ReviewPanelModel model;


    private final ReviewDetailPanel reviewDetailPanel;
    private final CodePanel codePanel;
    private final RejectApprovePanel rejectApprovePanel = new RejectApprovePanel();

    public ReviewPanel(ReviewContextManager reviewContextManager,
                      RepositoryManager repositoryManager,
                      ReviewFormModels reviewFormModels,
                      ReviewPanelModel reviewPanelModel,
                      Git git) {
        this.reviewContextManager = reviewContextManager;
        this.model = reviewPanelModel;
        FileDiffManager fileDiffManager = new FileDiffManager(git, reviewPanelModel.codeViewerModel);
        this.reviewContextManager.setFileDiffManager(fileDiffManager);
        this.reviewDetailPanel = new ReviewDetailPanel(reviewContextManager, reviewFormModels, repositoryManager, git);
        this.codePanel = new CodePanel(reviewContextManager, reviewPanelModel.codeViewerModel, fileDiffManager);
        configureLayout();
    }

    private void configureLayout() {
        setLayout(new MigLayout("fill, insets 0", "[grow]", "[]0[grow]0[]"));

        add(reviewDetailPanel, "growx, wrap");
        add(codePanel, "grow, wrap");
        add(rejectApprovePanel, "growx");
    }

    public void loadReview(String reviewId, String repositoryName) {
        if (reviewId == null || reviewId.isEmpty()) {
            model.clear();
            return;
        }

        reviewContextManager.loadReview(reviewId, repositoryName)
            .exceptionally(error -> {
                System.err.println("Failed to load review in ReviewPanel: " + error.getMessage());
                return null;
            });
    }
}
