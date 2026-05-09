package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel;

import java.io.Serial;

import com.kalynx.serverlessreviewtool.configuration.SettingsManager;
import com.kalynx.serverlessreviewtool.git.Git;
import com.kalynx.serverlessreviewtool.managers.ReviewContextManager;
import com.kalynx.serverlessreviewtool.models.Commit;
import com.kalynx.serverlessreviewtool.models.ReviewFile;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedPanel;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedSplitPane;
import com.kalynx.serverlessreviewtool.ui.models.mainpanels.reviewpanel.CodeViewerModel;
import com.kalynx.serverlessreviewtool.ui.review.*;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class CodePanel extends ThemedPanel {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CodePanel.class);

    private final SettingsManager settingsManager;
    private transient final CodeViewerModel codeViewerModel;
    private transient final com.kalynx.serverlessreviewtool.managers.FileDiffManager fileDiffManager;
    private transient final ReviewContextManager reviewContextManager;

    private transient final java.util.Set<String> persistedCommentIds = new java.util.HashSet<>();

    private final CommitSelectorPanel commitSelectorPanel;
    private final FileNavigationPanel fileNavigationPanel;
    private final DiffViewerPanel diffViewerPanel;
    private final ThemedSplitPane fileAndDiffSplitPane = new ThemedSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    
    private boolean commentsEnabled = false;

    public CodePanel(SettingsManager settingsManager, ReviewContextManager reviewContextManager, CodeViewerModel codeViewerModel,
                     com.kalynx.serverlessreviewtool.managers.FileDiffManager fileDiffManager, Git git) {
        this.settingsManager = settingsManager;
        this.reviewContextManager = reviewContextManager;
        this.codeViewerModel = codeViewerModel;
        this.fileDiffManager = fileDiffManager;
        this.commitSelectorPanel = new CommitSelectorPanel(reviewContextManager, codeViewerModel, git);
        this.fileNavigationPanel = new FileNavigationPanel(reviewContextManager, codeViewerModel);
        this.diffViewerPanel = new DiffViewerPanel(codeViewerModel);
        configureLayout();
        setupModelListeners();
    }

    private void configureLayout() {
        setLayout(new MigLayout("fill, insets 0", "[grow]", "[]0[grow]"));

        add(commitSelectorPanel, "growx, wrap");

        fileAndDiffSplitPane.setLeftComponent(fileNavigationPanel);
        fileAndDiffSplitPane.setRightComponent(diffViewerPanel);
        fileAndDiffSplitPane.setResizeWeight(0.20);

        add(fileAndDiffSplitPane, "grow");
    }

    private void setupModelListeners() {
        codeViewerModel.selectedFile.addChangeListener(this::onFileOrCommitChanged);
        codeViewerModel.startCommit.addChangeListener(_ -> onFileOrCommitChanged(codeViewerModel.selectedFile.getValue()));
        codeViewerModel.endCommit.addChangeListener(_ -> onFileOrCommitChanged(codeViewerModel.selectedFile.getValue()));

        diffViewerPanel.setOnLineDoubleClickListener(this::onLineDoubleClicked);
        reviewContextManager.addListener(this::onReviewContextChanged);
    }

    private void onReviewContextChanged(com.kalynx.serverlessreviewtool.models.ReviewContext context) {
        if (context != null) {
            persistedCommentIds.clear();
            context.getComments().forEach(comment -> persistedCommentIds.add(comment.getId()));
            LOGGER.info("Loaded {} persisted comments", persistedCommentIds.size());
        }
        loadCommentsForCurrentFile();
    }

    private void onLineDoubleClicked(Integer lineNumber) {
        if (!commentsEnabled) {
            LOGGER.debug("Comments disabled - user is not a reviewer");
            return;
        }

        ReviewFile file = codeViewerModel.selectedFile.getValue();
        com.kalynx.serverlessreviewtool.models.ReviewContext reviewContext = reviewContextManager.getReviewContext();

        if (file == null || reviewContext == null) {
            LOGGER.debug("Cannot add comment: file or review context is null");
            return;
        }

        LOGGER.info("Line {} double-clicked in file: {}", lineNumber, file.getPath());

        SwingUtilities.invokeLater(() -> {
            java.awt.Window window = SwingUtilities.getWindowAncestor(this);
            InlineCommentDialog dialog = new InlineCommentDialog(
                window,
                settingsManager,
                reviewContext,
                reviewContextManager,
                file,
                lineNumber,
                () -> {
                    onCommentAdded();
                }
            );
            dialog.setVisible(true);
        });
    }

    private void saveCommentsToGit(String reviewId) {
        com.kalynx.serverlessreviewtool.models.ReviewContext context = reviewContextManager.getReviewContext();
        if (context == null) {
            return;
        }

        java.util.List<com.kalynx.serverlessreviewtool.models.ReviewComment> allComments = context.getComments();
        java.util.List<com.kalynx.serverlessreviewtool.models.ReviewComment> newComments = allComments.stream()
            .filter(comment -> !persistedCommentIds.contains(comment.getId()))
            .toList();

        java.util.List<com.kalynx.serverlessreviewtool.models.ReviewComment> existingComments = allComments.stream()
            .filter(comment -> persistedCommentIds.contains(comment.getId()))
            .toList();

        if (newComments.isEmpty() && existingComments.isEmpty()) {
            LOGGER.debug("No comments to save");
            return;
        }

        if (!newComments.isEmpty()) {
            LOGGER.info("Saving {} new comments for review {}", newComments.size(), reviewId);

            for (com.kalynx.serverlessreviewtool.models.ReviewComment comment : newComments) {
                reviewContextManager.saveComment(reviewId, comment)
                    .thenRun(() -> {
                        persistedCommentIds.add(comment.getId());
                        LOGGER.info("Comment {} saved and marked as persisted", comment.getId());
                    })
                    .exceptionally(error -> {
                        LOGGER.error("Failed to save comment: {}", comment.getId(), error);
                        return null;
                    });
            }
        }

        if (!existingComments.isEmpty()) {
            LOGGER.info("Updating {} existing comments (resolution status may have changed) for review {}",
                existingComments.size(), reviewId);

            reviewContextManager.saveAllComments(reviewId, existingComments)
                .exceptionally(error -> {
                    LOGGER.error("Failed to update existing comments", error);
                    return null;
                });
        }
    }

    private void onCommentAdded() {
        LOGGER.info("Comment added, refreshing comments for current file");
        loadCommentsForCurrentFile();
    }

    private void loadCommentsForCurrentFile() {
        ReviewFile file = codeViewerModel.selectedFile.getValue();
        com.kalynx.serverlessreviewtool.models.ReviewContext reviewContext = reviewContextManager.getReviewContext();

        if (file == null || reviewContext == null) {
            diffViewerPanel.setCommentsForCurrentFile(new java.util.ArrayList<>());
            return;
        }

        java.util.List<com.kalynx.serverlessreviewtool.models.ReviewComment> comments =
            reviewContext.getCommentsForFile(file.getPath());

        LOGGER.debug("Loaded {} comments for file: {}", comments.size(), file.getPath());
        diffViewerPanel.setCommentsForCurrentFile(comments);
    }

    private void onFileOrCommitChanged(ReviewFile file) {
        if (file == null) {
            LOGGER.debug("File is null, skipping diff load");
            return;
        }

        Commit startCommit = codeViewerModel.startCommit.getValue();
        Commit endCommit = codeViewerModel.endCommit.getValue();

        LOGGER.info("=== FILE OR COMMIT CHANGED ===");
        LOGGER.info("File: {} (repository: {})", file.getPath(), file.getRepository());
        LOGGER.info("Start commit: {}", startCommit != null ? startCommit.getShortHash() : "null");
        LOGGER.info("End commit: {}", endCommit != null ? endCommit.getShortHash() : "null");

        if (startCommit == null || endCommit == null) {
            LOGGER.warn("Commit range not set, skipping diff load");
            return;
        }

        LOGGER.info("Loading diff for file: {} between commits {} and {}",
            file.getPath(), startCommit.getShortHash(), endCommit.getShortHash());

        fileDiffManager.loadDiffForFile(file.getRepository(), file, startCommit, endCommit)
            .exceptionally(error -> {
                LOGGER.error("Failed to load diff for file: {}", file.getPath(), error);
                return null;
            });

        loadCommentsForCurrentFile();
    }

    public void setCommentsEnabled(boolean enabled) {
        this.commentsEnabled = enabled;
        LOGGER.debug("Comments enabled: {}", enabled);
    }

}
