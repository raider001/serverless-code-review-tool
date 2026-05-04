package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel;

import java.io.Serial;

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

    private transient final CodeViewerModel codeViewerModel;
    private transient final com.kalynx.serverlessreviewtool.managers.FileDiffManager fileDiffManager;

    private final CommitSelectorPanel commitSelectorPanel;
    private final FileNavigationPanel fileNavigationPanel;
    private final DiffViewerPanel diffViewerPanel;
    private final ThemedSplitPane fileAndDiffSplitPane = new ThemedSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    public CodePanel(ReviewContextManager reviewContextManager, CodeViewerModel codeViewerModel,
                     com.kalynx.serverlessreviewtool.managers.FileDiffManager fileDiffManager, Git git) {
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
        codeViewerModel.startCommit.addChangeListener(commit -> onFileOrCommitChanged(codeViewerModel.selectedFile.getValue()));
        codeViewerModel.endCommit.addChangeListener(commit -> onFileOrCommitChanged(codeViewerModel.selectedFile.getValue()));
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
    }

}
