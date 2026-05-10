package com.kalynx.serverlessreviewtool.ui.models.mainpanels.reviewpanel;

import com.kalynx.serverlessreviewtool.models.Commit;
import com.kalynx.serverlessreviewtool.models.ReviewFile;
import com.kalynx.serverlessreviewtool.swingextensions.ComponentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CodeViewerModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeViewerModel.class);

    public enum DiffMode {
        SIDE_BY_SIDE,
        UNIFIED
    }

    public final ComponentModel<List<ReviewFile>> availableFiles = new ComponentModel<>();
    public final ComponentModel<ReviewFile> selectedFile = new ComponentModel<>();
    public final ComponentModel<DiffMode> diffMode = new ComponentModel<>();

    public final ComponentModel<Commit> startCommit = new ComponentModel<>();
    public final ComponentModel<Commit> endCommit = new ComponentModel<>();
    public final ComponentModel<List<Commit>> availableCommits = new ComponentModel<>();

    public final ComponentModel<String> reviewBranch = new ComponentModel<>();
    public final ComponentModel<String> reviewBaseBranch = new ComponentModel<>();

    public final ComponentModel<String> leftContent = new ComponentModel<>();
    public final ComponentModel<String> rightContent = new ComponentModel<>();
    public final ComponentModel<String> unifiedDiffContent = new ComponentModel<>();

    public final ComponentModel<Integer> selectedLine = new ComponentModel<>();
    public final ComponentModel<Boolean> isLoadingFile = new ComponentModel<>();

    public CodeViewerModel() {
        initializeDefaults();
    }

    private void initializeDefaults() {
        availableFiles.setValue(new ArrayList<>());
        selectedFile.setValue(null);
        diffMode.setValue(DiffMode.SIDE_BY_SIDE);
        
        startCommit.setValue(null);
        endCommit.setValue(null);
        availableCommits.setValue(new ArrayList<>());
        
        reviewBranch.setValue(null);
        reviewBaseBranch.setValue(null);

        leftContent.setValue("");
        rightContent.setValue("");
        unifiedDiffContent.setValue("");
        selectedLine.setValue(-1);
        isLoadingFile.setValue(false);
    }

    public void clear() {
        initializeDefaults();
    }

    public void setAvailableFiles(List<ReviewFile> files) {
        availableFiles.setValue(files != null ? new ArrayList<>(files) : new ArrayList<>());
        if (files != null && !files.isEmpty() && selectedFile.getValue() == null) {
            selectedFile.setValue(files.getFirst());
        }
    }

    public void setAvailableCommits(List<Commit> commits) {
        availableCommits.setValue(commits != null ? new ArrayList<>(commits) : new ArrayList<>());
    }

    public void selectFile(ReviewFile file) {
        selectedFile.setValue(file);
        selectedLine.setValue(-1);
    }

    public void setCommitRange(Commit start, Commit end) {
        startCommit.setValue(start);
        endCommit.setValue(end);
    }

    public void setReviewBranches(String branch, String baseBranch) {
        reviewBranch.setValue(branch);
        reviewBaseBranch.setValue(baseBranch);
    }

    public void setDiffMode(DiffMode mode) {
        diffMode.setValue(mode);
    }

    public void setFileContent(String left, String right, String unified) {
        leftContent.setValue(left != null ? left : "");
        rightContent.setValue(right != null ? right : "");
        unifiedDiffContent.setValue(unified != null ? unified : "");
    }

    public void setLeftContent(String content) {
        String safeContent = content != null ? content : "";
        LOGGER.debug("[CodeViewerModel] setLeftContent called: {} chars", safeContent.length());
        if (safeContent.isEmpty()) {
            LOGGER.warn("[CodeViewerModel] Setting empty left content");
        }
        leftContent.setValue(safeContent);
    }

    public void setRightContent(String content) {
        String safeContent = content != null ? content : "";
        LOGGER.debug("[CodeViewerModel] setRightContent called: {} chars", safeContent.length());
        if (safeContent.isEmpty()) {
            LOGGER.warn("[CodeViewerModel] Setting empty right content");
        }
        rightContent.setValue(safeContent);
    }

    public void setUnifiedDiffContent(String content) {
        String safeContent = content != null ? content : "";
        LOGGER.debug("[CodeViewerModel] setUnifiedDiffContent called: {} chars", safeContent.length());
        if (safeContent.isEmpty()) {
            LOGGER.warn("[CodeViewerModel] Setting empty unified diff content");
        }
        unifiedDiffContent.setValue(safeContent);
    }
}

