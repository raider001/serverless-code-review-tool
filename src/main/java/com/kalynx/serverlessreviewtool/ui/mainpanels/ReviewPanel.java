package com.kalynx.serverlessreviewtool.ui.mainpanels;

import com.kalynx.serverlessreviewtool.managers.ReviewContextManager;
import com.kalynx.serverlessreviewtool.models.ReviewContext;
import com.kalynx.serverlessreviewtool.models.*;
import com.kalynx.serverlessreviewtool.theme.components.*;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.CodePanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.RejectApprovePanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.ReviewDetailPanel;
import net.miginfocom.swing.MigLayout;

import java.util.List;

/**
 * ReviewPanel - Main panel for reviewing code changes across multiple repositories
 * Supports file navigation, diff viewing (side-by-side and unified), commit comparison, and inline comments
 */
public class ReviewPanel extends ThemedPanel {

    private final ReviewContextManager reviewContextManager = ReviewContextManager.getInstance();

    private final ReviewDetailPanel reviewDetailPanel = new ReviewDetailPanel();
    private final CodePanel codePanel = new CodePanel();
    private final RejectApprovePanel rejectApprovePanel = new RejectApprovePanel();

    public ReviewPanel() {
        initializeSampleReviewContext();
        configureLayout();
    }

    private void configureLayout() {
        setLayout(new MigLayout("fill, insets 0", "[grow]", "[]0[grow]0[]"));

        add(reviewDetailPanel, "growx, wrap");
        add(codePanel, "grow, wrap");
        add(rejectApprovePanel, "growx");
    }

    private void initializeSampleReviewContext() {
        List<ReviewerInfo> reviewers = new java.util.ArrayList<>();
        reviewers.add(new ReviewerInfo("Alice Chen"));
        reviewers.add(new ReviewerInfo("Bob Martin"));
        reviewers.add(new ReviewerInfo("Carlos Rivera"));

        List<Repository> repositories = com.kalynx.serverlessreviewtool.mockdata.RepositoryMockData.createDetailedRepositories();

        ReviewContext reviewContext = new ReviewContext(
            "REVIEW-123",
            "Implement OAuth2 authentication",
            "Adds full OAuth2 support across the backend API and frontend app, " +
            "including token validation, refresh token handling, and a new login UI component.",
            "John Doe",
            ReviewStatus.OPEN,
            reviewers,
            repositories,
            new java.util.ArrayList<>()
        );

        reviewContextManager.setReviewContext(reviewContext);
    }
}
