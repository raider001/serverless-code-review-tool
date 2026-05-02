package com.kalynx.serverlessreviewtool.ui.mainpanels;

import com.kalynx.serverlessreviewtool.managers.RepositoryManager;
import com.kalynx.serverlessreviewtool.managers.ReviewContextManager;
import com.kalynx.serverlessreviewtool.managers.UserManager;
import com.kalynx.serverlessreviewtool.mockdata.RepositoryMockData_Old;
import com.kalynx.serverlessreviewtool.models.ReviewContext;
import com.kalynx.serverlessreviewtool.models.*;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedPanel;
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

    private final ReviewContextManager reviewContextManager;

    private final ReviewDetailPanel reviewDetailPanel;
    private final CodePanel codePanel;
    private final RejectApprovePanel rejectApprovePanel = new RejectApprovePanel();

    public ReviewPanel(ReviewContextManager reviewContextManager, RepositoryManager repositoryManager, UserManager userManager) {
        this.reviewContextManager = reviewContextManager;
        this.reviewDetailPanel = new ReviewDetailPanel(reviewContextManager, repositoryManager, userManager);
        this.codePanel = new CodePanel(reviewContextManager);
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

        List<Repository> repositories = RepositoryMockData_Old.createDetailedRepositories();

        List<ReviewComment> comments = createSampleComments();

        ReviewContext reviewContext = new ReviewContext(
            "REVIEW-123",
            "Implement OAuth2 authentication",
            "Adds full OAuth2 support across the backend API and frontend app, " +
            "including token validation, refresh token handling, and a new login UI component.",
            "John Doe",
            ReviewStatus.OPEN,
            reviewers,
            repositories,
            comments
        );

        reviewContextManager.setReviewContext(reviewContext);
    }

    private List<ReviewComment> createSampleComments() {
        List<ReviewComment> comments = new java.util.ArrayList<>();

        String filePath = "src/auth/AuthService.java";

        ReviewComment comment1 = new ReviewComment(
            "C1", filePath, 45, "Alice Chen",
            "This authentication logic could be simplified. Consider extracting the token validation into a separate method.",
            "2 hours ago", null, true
        );
        comments.add(comment1);

        ReviewComment reply1 = new ReviewComment(
            "C2", filePath, 45, "Bob Martin",
            "Good catch! I'll refactor this in the next commit. Should I also extract the refresh token logic?",
            "1 hour ago", "C1", false
        );
        comments.add(reply1);

        ReviewComment reply2 = new ReviewComment(
            "C3", filePath, 45, "Alice Chen",
            "Yes, that would make it much more maintainable. Thanks!",
            "45 minutes ago", "C1", false
        );
        comments.add(reply2);

        comment1.markResolved("Bob Martin");

        ReviewComment comment2 = new ReviewComment(
            "C4", filePath, 12, "Carlos Rivera",
            "Missing null check here. What happens if the user object is null?",
            "3 hours ago", null, true
        );
        comments.add(comment2);

        ReviewComment comment3 = new ReviewComment(
            "C5", filePath, 78, "Alice Chen",
            "Nice implementation! This is much cleaner than the old approach.",
            "30 minutes ago", null, false
        );
        comments.add(comment3);

        String filePath2 = "src/ui/LoginComponent.java";

        ReviewComment comment4 = new ReviewComment(
            "C6", filePath2, 23, "Bob Martin",
            "Should we add input validation for the email field?",
            "1 hour ago", null, true
        );
        comments.add(comment4);

        return comments;
    }
}
