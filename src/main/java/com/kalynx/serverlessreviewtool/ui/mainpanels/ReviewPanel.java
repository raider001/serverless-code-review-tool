package com.kalynx.serverlessreviewtool.ui.mainpanels;

import com.kalynx.serverlessreviewtool.git.Git;
import com.kalynx.serverlessreviewtool.managers.FileDiffManager;
import com.kalynx.serverlessreviewtool.managers.RepositoryManager;
import com.kalynx.serverlessreviewtool.managers.ReviewContextManager;

import com.kalynx.serverlessreviewtool.models.*;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedPanel;
import com.kalynx.serverlessreviewtool.theme.LoadingStateManager;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.CodePanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.RejectApprovePanel;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.ReviewDetailPanel;
import com.kalynx.serverlessreviewtool.ui.models.mainpanels.reviewpanel.ReviewPanelModel;
import com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog.ReviewFormModels;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * ReviewPanel - Main panel for reviewing code changes across multiple repositories
 * Supports file navigation, diff viewing (side-by-side and unified), commit comparison, and inline comments
 */
public class ReviewPanel extends ThemedPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewPanel.class);

    private final ReviewContextManager reviewContextManager;
    private final ReviewPanelModel model;
    private final FileDiffManager fileDiffManager;
    private final Git git;

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
        this.git = git;
        this.fileDiffManager = new FileDiffManager(git, reviewPanelModel.codeViewerModel);
        this.reviewDetailPanel = new ReviewDetailPanel(reviewPanelModel.reviewDetailModel);
        this.codePanel = new CodePanel(reviewContextManager, reviewPanelModel.codeViewerModel, fileDiffManager);
        configureLayout();
    }


    private void configureLayout() {
        setLayout(new MigLayout("fill, insets 0", "[grow]", "[]0[grow]0[]"));

        add(reviewDetailPanel, "cell 0 0, growx, wrap");
        add(codePanel, "grow, wrap");
        add(rejectApprovePanel, "cell 0 2, growx");
    }

    public void loadReview(ReviewItem reviewItem) {
        this.model.clear();
        if (reviewItem == null || reviewItem.getReviewId() == null || reviewItem.getReviewId().isEmpty()) {
            model.clear();
            return;
        }

        String reviewId = reviewItem.getReviewId();
        List<String> repositoryNames = reviewItem.getRepositories();

        model.setCurrentReview(reviewId);
        LoadingStateManager.getInstance().startLoading("Loading review context...");

        LOGGER.info("=== REVIEW PANEL LOAD START ===");
        LOGGER.info("Review ID: {}", reviewId);
        LOGGER.info("Repository Names from ReviewItem: {}", repositoryNames);

        reviewContextManager.loadReviewMetadata(reviewId, repositoryNames)
            .thenCompose(reviewContext -> {
                if (reviewContext == null) {
                    LOGGER.warn("ReviewContext is null for review: {}", reviewId);
                    model.setError("Review not found");
                    return CompletableFuture.completedFuture(null);
                }

                List<com.kalynx.serverlessreviewtool.models.Repository> repositories = reviewContext.getRepositories();

                LOGGER.info("=== REPOSITORIES FROM REVIEW CONTEXT ===");
                LOGGER.info("Number of repositories: {}", repositories.size());
                for (com.kalynx.serverlessreviewtool.models.Repository repo : repositories) {
                    LOGGER.info("  - Repository: {} (url: {})", repo.getName(), repo.getUrl());
                }

                if (repositories.isEmpty()) {
                    LOGGER.warn("No repositories found in ReviewContext for review: {}", reviewId);
                    model.setError("No repositories found for review");
                    return CompletableFuture.completedFuture(null);
                }

                model.setRepositories(repositories);

                List<CompletableFuture<Void>> fetchFutures = repositories.stream()
                    .map(repo -> git.fetch(repo.getName()))
                    .toList();

                return CompletableFuture.allOf(fetchFutures.toArray(new CompletableFuture[0]))
                    .thenCompose(ignored -> {
                        com.kalynx.serverlessreviewtool.models.Repository primaryRepo = repositories.getFirst();

                        return git.getDefaultBranch(primaryRepo.getName())
                            .thenCompose(defaultBranch -> {
                                String remoteBranch = "origin/" + defaultBranch;

                                CompletableFuture<Void> commitsFuture = fileDiffManager
                                    .loadCommitsForReview(primaryRepo.getName(), remoteBranch, 1000);

                                LOGGER.info("=== LOADING FILES FROM REPOSITORIES ===");
                                LOGGER.info("Repositories being passed to loadFilesFromReviewCommits:");
                                for (com.kalynx.serverlessreviewtool.models.Repository repo : repositories) {
                                    LOGGER.info("  - {}", repo.getName());
                                }

                                CompletableFuture<List<ReviewFile>> filesFuture = reviewContextManager
                                    .loadFilesFromReviewCommits(reviewId, repositories);

                                return CompletableFuture.allOf(commitsFuture, filesFuture)
                                    .thenAccept(v -> {
                                        List<ReviewFile> allFiles = filesFuture.join();

                                        LOGGER.info("=== FILES LOADED FROM REVIEW ===");
                                        LOGGER.info("Total files: {}", allFiles.size());

                                        for (ReviewFile file : allFiles) {
                                            LOGGER.info("  - {} (repository: {})", file.getPath(), file.getRepository());
                                        }

                                        LOGGER.info("=== SETTING FILES TO MODEL ===");
                                        model.codeViewerModel.setAvailableFiles(allFiles);
                                        LOGGER.info("=== REVIEW PANEL LOAD COMPLETE ===");
                                    });
                            });
                    });
            })
            .whenComplete((ignored, error) -> LoadingStateManager.getInstance().stopLoading("Loading review context..."))
            .exceptionally(error -> {
                model.setError("Failed to load review: " + error.getMessage());
                return null;
            });
    }
}
