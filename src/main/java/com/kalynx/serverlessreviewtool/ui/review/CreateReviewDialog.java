package com.kalynx.serverlessreviewtool.ui.review;

import com.kalynx.serverlessreviewtool.git.Git;
import com.kalynx.serverlessreviewtool.git.GitReviewNotesManager;
import com.kalynx.serverlessreviewtool.managers.RepositoryManager;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedOptionPane;
import com.kalynx.serverlessreviewtool.theme.LoadingStateManager;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.ReviewFormDialog;
import com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog.ReviewFormModels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CreateReviewDialog extends ReviewFormDialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateReviewDialog.class);

    public CreateReviewDialog(Component parent,
                             ReviewFormModels models,
                             RepositoryManager repositoryManager,
                             Git git) {
        super(parent, "Create Code Review", models, repositoryManager, git);
        models.clear();
    }

    @Override
    protected String getSubmitButtonLabel() { return "Create Review"; }

    @Override
    protected void onFormSubmit() {
        String reviewId = models.reviewId.getValue();
        String title = models.title.getValue();
        String author = models.author.getValue();
        String summary = models.summary.getValue();
        String branch = models.selectedBranchModel.getValue();
        String baseBranch = models.selectedBaseBranchModel.getValue();
        List<String> repositories = models.selectedRepositories.getValue();
        List<String> reviewers = models.selectedReviewers.getValue();

        if (repositories == null || repositories.isEmpty()) {
            ThemedOptionPane.showWarning(this, "Please select at least one repository");
            return;
        }

        String primaryRepo = repositories.getFirst();
        String editor = author.isEmpty() ? "unknown" : author;

        createReview(primaryRepo, reviewId, editor, title, author, summary, branch, baseBranch, reviewers);
    }

    private void createReview(String repositoryName,
                             String reviewId,
                             String editor,
                             String title,
                             String author,
                             String summary,
                             String branch,
                             String baseBranch,
                             List<String> reviewers) {

        LoadingStateManager.getInstance().startLoading("create-review");
        GitReviewNotesManager notesManager = new GitReviewNotesManager(git, repositoryName);

        String commitRange = baseBranch + ".." + branch;

        git.fetch(repositoryName)
            .thenCompose(v -> git.listCommits(repositoryName, commitRange, 1000))
            .thenCompose(commitMessages -> {
                List<String> commitHashes = extractCommitHashes(commitMessages);

                if (commitHashes.isEmpty()) {
                    return CompletableFuture.failedFuture(
                        new IllegalArgumentException("No commits found between '" + baseBranch +
                            "' and '" + branch + "'. Either the branches don't exist or they are identical.")
                    );
                }

                return notesManager.createReview(
                    reviewId,
                    editor,
                    title,
                    author,
                    summary,
                    "open",
                    commitHashes,
                    reviewers
                );
            })
            .thenAccept(v -> SwingUtilities.invokeLater(() -> {
                LoadingStateManager.getInstance().stopLoading("create-review");
                confirmed = true;
                dispose();
                LOGGER.info("Review created successfully: {}", reviewId);
            }))
            .exceptionally(ex -> {
                LoadingStateManager.getInstance().stopLoading("create-review");
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                String errorMessage;

                if (cause.getMessage() != null && cause.getMessage().contains("unknown revision")) {
                    errorMessage = "Branch '" + branch + "' or '" + baseBranch +
                        "' does not exist in repository '" + repositoryName + "'.";
                } else if (cause instanceof IllegalArgumentException) {
                    errorMessage = cause.getMessage();
                } else {
                    errorMessage = "Failed to create review: " + cause.getMessage();
                }

                SwingUtilities.invokeLater(() -> ThemedOptionPane.showError(this, errorMessage));
                LOGGER.error("Failed to create review {}: {}", reviewId, cause.getMessage(), cause);
                return null;
            });
    }

    private List<String> extractCommitHashes(List<String> commitMessages) {
        List<String> hashes = new ArrayList<>();
        for (String commit : commitMessages) {
            String[] parts = commit.split("\\|", 2);
            if (parts.length > 0) {
                hashes.add(parts[0].trim());
            }
        }
        return hashes;
    }
}
