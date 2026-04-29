package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel;

import com.kalynx.serverlessreviewtool.managers.ReviewContextManager;
import com.kalynx.serverlessreviewtool.models.Commit;
import com.kalynx.serverlessreviewtool.models.Repository;
import com.kalynx.serverlessreviewtool.models.ReviewContext;
import com.kalynx.serverlessreviewtool.models.ReviewFile;
import com.kalynx.serverlessreviewtool.theme.components.ThemedPanel;
import com.kalynx.serverlessreviewtool.theme.components.ThemedSplitPane;
import com.kalynx.serverlessreviewtool.ui.review.CommitSelectorPanel;
import com.kalynx.serverlessreviewtool.ui.review.DiffViewMode;
import com.kalynx.serverlessreviewtool.ui.review.DiffViewerPanel;
import com.kalynx.serverlessreviewtool.ui.review.FileNavigationPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * CodePanel - Contains commit selector, file navigation, and diff viewer
 * Displays the code review interface with commit comparison and file diffs
 */
public class CodePanel extends ThemedPanel {

    private final ReviewContextManager reviewContextManager = ReviewContextManager.getInstance();

    private final CommitSelectorPanel commitSelectorPanel = new CommitSelectorPanel();
    private final FileNavigationPanel fileNavigationPanel = new FileNavigationPanel();
    private final DiffViewerPanel diffViewerPanel = new DiffViewerPanel();
    private final ThemedSplitPane mainSplitPane = new ThemedSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    public CodePanel() {
        configureLayout();
        setupListeners();
    }

    private void configureLayout() {
        setLayout(new MigLayout("fill, insets 0", "[grow]", "[]0[grow]"));

        add(commitSelectorPanel, "growx, wrap");

        mainSplitPane.setLeftComponent(fileNavigationPanel);
        mainSplitPane.setRightComponent(diffViewerPanel);
        mainSplitPane.setResizeWeight(0.20);

        add(mainSplitPane, "grow");
    }

    private void setupListeners() {
        commitSelectorPanel.addCommitRangeListener(this::onCommitRangeChanged);
        commitSelectorPanel.addViewModeListener(this::onViewModeChanged);
        fileNavigationPanel.addFileSelectionListener(this::onFileSelected);
    }

    private void onCommitRangeChanged(Commit startCommit, Commit endCommit) {
        System.out.println("Commit range changed: " + startCommit.getHash() + " -> " + endCommit.getHash());

        ReviewFile selectedFile = fileNavigationPanel.getSelectedFile();
        if (selectedFile != null) {
            diffViewerPanel.showDiff(selectedFile, startCommit, endCommit);
        }
    }

    private void onViewModeChanged(DiffViewMode mode) {
        System.out.println("View mode changed to: " + mode);
        diffViewerPanel.setViewMode(mode);
    }

    private void onFileSelected(ReviewFile file) {
        System.out.println("File selected: " + file.getPath());

        Repository repository = findRepositoryForFile(file);
        if (repository != null) {
            commitSelectorPanel.loadCommitsForRepository(repository);
        }

        Commit startCommit = commitSelectorPanel.getStartCommit();
        Commit endCommit = commitSelectorPanel.getEndCommit();

        if (startCommit != null && endCommit != null) {
            diffViewerPanel.showDiff(file, startCommit, endCommit);
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
