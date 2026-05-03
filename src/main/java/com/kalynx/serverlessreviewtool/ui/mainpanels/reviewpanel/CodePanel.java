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

    private final CommitSelectorPanel commitSelectorPanel;
    private final FileNavigationPanel fileNavigationPanel;
    private final DiffViewerPanel diffViewerPanel = new DiffViewerPanel();
    private final ThemedSplitPane fileAndDiffSplitPane = new ThemedSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    public CodePanel(ReviewContextManager reviewContextManager, CodeViewerModel codeViewerModel) {
        this.reviewContextManager = reviewContextManager;
        this.codeViewerModel = codeViewerModel;
        this.commitSelectorPanel = new CommitSelectorPanel(reviewContextManager);
        this.fileNavigationPanel = new FileNavigationPanel(reviewContextManager);
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
        codeViewerModel.startCommit.addChangeListener(commit -> loadDiffForCurrentState());
        codeViewerModel.endCommit.addChangeListener(commit -> loadDiffForCurrentState());
        codeViewerModel.diffMode.addChangeListener(mode -> {
            if (mode != null) {
                diffViewerPanel.setViewMode(mode == CodeViewerModel.DiffMode.SIDE_BY_SIDE
                    ? DiffViewMode.SIDE_BY_SIDE
                    : DiffViewMode.UNIFIED);
            }
        });
    }

    private void onModelFileChanged(ReviewFile file) {
        if (file != null) {
            loadDiffForCurrentState();
            loadCommentsForCurrentFile();
        }
    }

    private void loadDiffForCurrentState() {
        ReviewFile file = codeViewerModel.selectedFile.getValue();
        Commit start = codeViewerModel.startCommit.getValue();
        Commit end = codeViewerModel.endCommit.getValue();

        if (file != null && start != null && end != null) {
            diffViewerPanel.showDiff(file, start, end);
        }
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

        ReviewFile selectedFile = fileNavigationPanel.getSelectedFile();
        if (selectedFile != null) {
            diffViewerPanel.showDiff(selectedFile, startCommit, endCommit);
            loadCommentsForCurrentFile();
        }
    }

    private void onViewModeChanged(DiffViewMode mode) {
        CodeViewerModel.DiffMode modelMode = mode == DiffViewMode.SIDE_BY_SIDE
            ? CodeViewerModel.DiffMode.SIDE_BY_SIDE
            : CodeViewerModel.DiffMode.UNIFIED;
        codeViewerModel.setDiffMode(modelMode);
        diffViewerPanel.setViewMode(mode);
    }

    private void onFileSelected(ReviewFile file) {
        codeViewerModel.selectFile(file);

        Repository repository = findRepositoryForFile(file);
        if (repository != null) {
            commitSelectorPanel.loadCommits(new ArrayList<>());
        }

        Commit startCommit = commitSelectorPanel.getStartCommit();
        Commit endCommit = commitSelectorPanel.getEndCommit();

        if (startCommit != null && endCommit != null) {
            diffViewerPanel.showDiff(file, startCommit, endCommit);
        }

        loadCommentsForCurrentFile();
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

    private Repository findRepositoryForFile(ReviewFile file) {
        ReviewContext context = reviewContextManager.getReviewContext();
        if (context == null) return null;

        return context.getRepositories().stream()
            .filter(repo -> repo.getName().equals(file.getRepository()))
            .findFirst()
            .orElse(null);
    }
}
