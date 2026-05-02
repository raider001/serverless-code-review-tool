package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel;

import com.kalynx.serverlessreviewtool.managers.ReviewContextManager;
import com.kalynx.serverlessreviewtool.models.Commit;
import com.kalynx.serverlessreviewtool.models.Repository;
import com.kalynx.serverlessreviewtool.models.ReviewContext;
import com.kalynx.serverlessreviewtool.models.ReviewFile;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedPanel;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedSplitPane;
import com.kalynx.serverlessreviewtool.ui.review.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.List;

/**
 * CodePanel - Contains commit selector, file navigation, diff viewer, and comments
 * Displays the code review interface with commit comparison, file diffs, and inline comment management
 */
public class CodePanel extends ThemedPanel {

    private final ReviewContextManager reviewContextManager;

    private final CommitSelectorPanel commitSelectorPanel;
    private final FileNavigationPanel fileNavigationPanel;
    private final DiffViewerPanel diffViewerPanel = new DiffViewerPanel();
    private final ThemedSplitPane fileAndDiffSplitPane = new ThemedSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    private ReviewFile currentFile;

    public CodePanel(ReviewContextManager reviewContextManager) {
        this.reviewContextManager = reviewContextManager;
        this.commitSelectorPanel = new CommitSelectorPanel(reviewContextManager);
        this.fileNavigationPanel = new FileNavigationPanel(reviewContextManager);
        configureLayout();
        setupListeners();
        setupCommentIntegration();
    }

    private void configureLayout() {
        setLayout(new MigLayout("fill, insets 0", "[grow]", "[]0[grow]"));

        add(commitSelectorPanel, "growx, wrap");

        fileAndDiffSplitPane.setLeftComponent(fileNavigationPanel);
        fileAndDiffSplitPane.setRightComponent(diffViewerPanel);
        fileAndDiffSplitPane.setResizeWeight(0.20);

        add(fileAndDiffSplitPane, "grow");
    }

    private void setupListeners() {
        commitSelectorPanel.addCommitRangeListener(this::onCommitRangeChanged);
        commitSelectorPanel.addViewModeListener(this::onViewModeChanged);
        fileNavigationPanel.addFileSelectionListener(this::onFileSelected);
    }

    private void setupCommentIntegration() {
        diffViewerPanel.setOnLineDoubleClickListener(lineNumber -> {
            if (currentFile != null) {
                showInlineCommentDialog(lineNumber);
            }
        });

        reviewContextManager.addListener(context -> {
            if (context != null && currentFile != null) {
                List<com.kalynx.serverlessreviewtool.models.ReviewComment> comments =
                    context.getCommentsForFile(currentFile.getPath());
                diffViewerPanel.setCommentsForCurrentFile(comments);
            }
        });
    }

    private void showInlineCommentDialog(int lineNumber) {
        ReviewContext context = reviewContextManager.getReviewContext();
        if (context == null || currentFile == null) return;

        InlineCommentDialog dialog = new InlineCommentDialog(
            SwingUtilities.getWindowAncestor(this),
            context,
            currentFile,
            lineNumber,
            this::loadCommentsForCurrentFile
        );
        dialog.setVisible(true);
    }

    private void onCommitRangeChanged(Commit startCommit, Commit endCommit) {
        ReviewFile selectedFile = fileNavigationPanel.getSelectedFile();
        if (selectedFile != null) {
            diffViewerPanel.showDiff(selectedFile, startCommit, endCommit);
            loadCommentsForCurrentFile();
        }
    }

    private void onViewModeChanged(DiffViewMode mode) {
        diffViewerPanel.setViewMode(mode);
    }

    private void onFileSelected(ReviewFile file) {
        this.currentFile = file;

        Repository repository = findRepositoryForFile(file);
        if (repository != null) {
            commitSelectorPanel.loadCommitsForRepository(repository);
        }

        Commit startCommit = commitSelectorPanel.getStartCommit();
        Commit endCommit = commitSelectorPanel.getEndCommit();

        if (startCommit != null && endCommit != null) {
            diffViewerPanel.showDiff(file, startCommit, endCommit);
        }

        loadCommentsForCurrentFile();
    }

    private void loadCommentsForCurrentFile() {
        if (currentFile == null) return;

        ReviewContext context = reviewContextManager.getReviewContext();
        if (context != null) {
            List<com.kalynx.serverlessreviewtool.models.ReviewComment> comments =
                context.getCommentsForFile(currentFile.getPath());
            diffViewerPanel.setCommentsForCurrentFile(comments);
            fileNavigationPanel.refreshDisplay();
        }
    }

    private Repository findRepositoryForFile(ReviewFile file) {
        ReviewContext context = reviewContextManager.getReviewContext();
        if (context == null) return null;

        return context.getRepositories().stream()
            .filter(repo -> repo.getName().equals(file.getRepository()))
            .findFirst()
            .orElse(null);
    }
}
