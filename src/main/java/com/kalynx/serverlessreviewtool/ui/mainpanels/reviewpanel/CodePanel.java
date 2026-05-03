package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel;

import java.io.Serial;

import com.kalynx.serverlessreviewtool.managers.ReviewContextManager;
import com.kalynx.serverlessreviewtool.models.Commit;
import com.kalynx.serverlessreviewtool.models.Repository;
import com.kalynx.serverlessreviewtool.models.ReviewContext;
import com.kalynx.serverlessreviewtool.models.ReviewFile;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedPanel;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedSplitPane;
import com.kalynx.serverlessreviewtool.ui.models.mainpanels.reviewpanel.CodeViewerModel;
import com.kalynx.serverlessreviewtool.ui.review.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CodePanel - Contains commit selector, file navigation, diff viewer, and comments
 * Displays the code review interface with commit comparison, file diffs, and inline comment management
 */
public class CodePanel extends ThemedPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private transient final ReviewContextManager reviewContextManager;
    private transient final CodeViewerModel codeViewerModel;
    private transient final com.kalynx.serverlessreviewtool.managers.FileDiffManager fileDiffManager;

    private final CommitSelectorPanel commitSelectorPanel;
    private final FileNavigationPanel fileNavigationPanel;
    private final DiffViewerPanel diffViewerPanel = new DiffViewerPanel();
    private final ThemedSplitPane fileAndDiffSplitPane = new ThemedSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    public CodePanel(ReviewContextManager reviewContextManager, CodeViewerModel codeViewerModel,
                     com.kalynx.serverlessreviewtool.managers.FileDiffManager fileDiffManager) {
        this.reviewContextManager = reviewContextManager;
        this.codeViewerModel = codeViewerModel;
        this.fileDiffManager = fileDiffManager;
        this.commitSelectorPanel = new CommitSelectorPanel(reviewContextManager, codeViewerModel);
        this.fileNavigationPanel = new FileNavigationPanel(reviewContextManager, codeViewerModel);
        configureLayout();
        setupListeners();
        setupModelListeners();
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

    private void setupModelListeners() {
        codeViewerModel.selectedFile.addChangeListener(this::onModelFileChanged);
        codeViewerModel.startCommit.addChangeListener(commit -> onCommitRangeChangedFromModel());
        codeViewerModel.endCommit.addChangeListener(commit -> onCommitRangeChangedFromModel());
        codeViewerModel.diffMode.addChangeListener(mode -> {
            if (mode != null) {
                diffViewerPanel.setViewMode(mode == CodeViewerModel.DiffMode.SIDE_BY_SIDE
                    ? DiffViewMode.SIDE_BY_SIDE
                    : DiffViewMode.UNIFIED);
            }
        });

        codeViewerModel.leftContent.addChangeListener(this::updateDiffViewerContent);
        codeViewerModel.rightContent.addChangeListener(content -> updateDiffViewerContent(null));
        codeViewerModel.unifiedDiffContent.addChangeListener(content -> updateDiffViewerContent(null));
    }

    private void onModelFileChanged(ReviewFile file) {
        if (file != null) {
            String repositoryName = getRepositoryNameForCurrentReview();
            if (repositoryName != null) {
                Commit start = codeViewerModel.startCommit.getValue();
                Commit end = codeViewerModel.endCommit.getValue();
                fileDiffManager.loadDiffForFile(repositoryName, file, start, end);
            }
            loadCommentsForCurrentFile();
        }
    }

    private void onCommitRangeChangedFromModel() {
        Commit start = codeViewerModel.startCommit.getValue();
        Commit end = codeViewerModel.endCommit.getValue();
        String repositoryName = getRepositoryNameForCurrentReview();

        if (start != null && end != null && repositoryName != null) {
            fileDiffManager.loadChangedFiles(repositoryName, start, end);

            ReviewFile currentFile = codeViewerModel.selectedFile.getValue();
            if (currentFile != null) {
                fileDiffManager.loadDiffForFile(repositoryName, currentFile, start, end);
            }
        }
    }

    private void updateDiffViewerContent(String ignored) {
        ReviewFile file = codeViewerModel.selectedFile.getValue();
        Commit start = codeViewerModel.startCommit.getValue();
        Commit end = codeViewerModel.endCommit.getValue();

        if (file != null && start != null && end != null) {
            diffViewerPanel.showDiff(file, start, end);
        }
    }

    private String getRepositoryNameForCurrentReview() {
        ReviewContext context = reviewContextManager.getReviewContext();
        if (context == null || context.getRepositories().isEmpty()) {
            return null;
        }
        return context.getRepositories().get(0).getName();
    }

    private void setupCommentIntegration() {
        diffViewerPanel.setOnLineDoubleClickListener(lineNumber -> {
            ReviewFile file = codeViewerModel.selectedFile.getValue();
            if (file != null) {
                codeViewerModel.selectLine(lineNumber);
                showInlineCommentDialog(lineNumber);
            }
        });

        reviewContextManager.addListener(context -> {
            ReviewFile file = codeViewerModel.selectedFile.getValue();
            if (context != null && file != null) {
                List<com.kalynx.serverlessreviewtool.models.ReviewComment> comments =
                    context.getCommentsForFile(file.getPath());
                diffViewerPanel.setCommentsForCurrentFile(comments);
            }
        });
    }

    private void showInlineCommentDialog(int lineNumber) {
        ReviewContext context = reviewContextManager.getReviewContext();
        ReviewFile file = codeViewerModel.selectedFile.getValue();
        if (context == null || file == null) return;

        InlineCommentDialog dialog = new InlineCommentDialog(
            SwingUtilities.getWindowAncestor(this),
            context,
            file,
            lineNumber,
            this::loadCommentsForCurrentFile
        );
        dialog.setVisible(true);
    }

    private void onCommitRangeChanged(Commit startCommit, Commit endCommit) {
        codeViewerModel.setCommitRange(startCommit, endCommit);
    }

    private void onViewModeChanged(DiffViewMode mode) {
        CodeViewerModel.DiffMode modelMode = mode == DiffViewMode.SIDE_BY_SIDE
            ? CodeViewerModel.DiffMode.SIDE_BY_SIDE
            : CodeViewerModel.DiffMode.UNIFIED;
        codeViewerModel.setDiffMode(modelMode);
    }

    private void onFileSelected(ReviewFile file) {
        codeViewerModel.selectFile(file);
    }

    private void loadCommentsForCurrentFile() {
        ReviewFile file = codeViewerModel.selectedFile.getValue();
        if (file == null) return;

        ReviewContext context = reviewContextManager.getReviewContext();
        if (context != null) {
            List<com.kalynx.serverlessreviewtool.models.ReviewComment> comments =
                context.getCommentsForFile(file.getPath());
            diffViewerPanel.setCommentsForCurrentFile(comments);
            fileNavigationPanel.refreshDisplay();
        }
    }

    public void refreshView() {
        String repositoryName = getRepositoryNameForCurrentReview();
        if (repositoryName != null) {
            fileDiffManager.refreshCurrentView(repositoryName);
        }
    }
}
