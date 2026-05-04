package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel;

import java.io.Serial;

import com.kalynx.serverlessreviewtool.managers.ReviewContextManager;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedPanel;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedSplitPane;
import com.kalynx.serverlessreviewtool.ui.models.mainpanels.reviewpanel.CodeViewerModel;
import com.kalynx.serverlessreviewtool.ui.review.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class CodePanel extends ThemedPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private transient final ReviewContextManager reviewContextManager;
    private transient final CodeViewerModel codeViewerModel;
    private transient final com.kalynx.serverlessreviewtool.managers.FileDiffManager fileDiffManager;

    private final CommitSelectorPanel commitSelectorPanel;
    private final FileNavigationPanel fileNavigationPanel;
    private final DiffViewerPanel diffViewerPanel;
    private final ThemedSplitPane fileAndDiffSplitPane = new ThemedSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    public CodePanel(ReviewContextManager reviewContextManager, CodeViewerModel codeViewerModel,
                     com.kalynx.serverlessreviewtool.managers.FileDiffManager fileDiffManager) {
        this.reviewContextManager = reviewContextManager;
        this.codeViewerModel = codeViewerModel;
        this.fileDiffManager = fileDiffManager;
        this.commitSelectorPanel = new CommitSelectorPanel(reviewContextManager, codeViewerModel);
        this.fileNavigationPanel = new FileNavigationPanel(reviewContextManager, codeViewerModel);
        this.diffViewerPanel = new DiffViewerPanel(codeViewerModel);
        configureLayout();

    }

    private void configureLayout() {
        setLayout(new MigLayout("fill, insets 0", "[grow]", "[]0[grow]"));

        add(commitSelectorPanel, "growx, wrap");

        fileAndDiffSplitPane.setLeftComponent(fileNavigationPanel);
        fileAndDiffSplitPane.setRightComponent(diffViewerPanel);
        fileAndDiffSplitPane.setResizeWeight(0.20);

        add(fileAndDiffSplitPane, "grow");
    }

}
