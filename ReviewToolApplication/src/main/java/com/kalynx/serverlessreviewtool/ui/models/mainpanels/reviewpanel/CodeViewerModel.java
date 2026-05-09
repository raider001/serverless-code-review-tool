package com.kalynx.serverlessreviewtool.ui.models.mainpanels.reviewpanel;

import com.kalynx.serverlessreviewtool.models.Commit;
import com.kalynx.serverlessreviewtool.models.ReviewFile;
import com.kalynx.serverlessreviewtool.swingextensions.ComponentModel;

import java.util.ArrayList;
import java.util.List;

public class CodeViewerModel {

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

    public void selectLine(int lineNumber) {
        selectedLine.setValue(lineNumber);
    }

    public void setCommitRange(Commit start, Commit end) {
        startCommit.setValue(start);
        endCommit.setValue(end);
    }

    public void setStartCommit(Commit commit) {
        startCommit.setValue(commit);
    }

    public void setEndCommit(Commit commit) {
        endCommit.setValue(commit);
    }

    public void setReviewBranches(String branch, String baseBranch) {
        reviewBranch.setValue(branch);
        reviewBaseBranch.setValue(baseBranch);
    }

    public void toggleDiffMode() {
        diffMode.setValue(diffMode.getValue() == DiffMode.SIDE_BY_SIDE
            ? DiffMode.UNIFIED
            : DiffMode.SIDE_BY_SIDE);
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
        System.out.println("[CodeViewerModel] setLeftContent called: " + safeContent.length() + " chars");
        if (safeContent.isEmpty()) {
            System.err.println("[CodeViewerModel] WARNING: Setting EMPTY left content!");
            Thread.dumpStack();
        }
        leftContent.setValue(safeContent);
    }

    public void setRightContent(String content) {
        String safeContent = content != null ? content : "";
        System.out.println("[CodeViewerModel] setRightContent called: " + safeContent.length() + " chars");
        if (safeContent.isEmpty()) {
            System.err.println("[CodeViewerModel] WARNING: Setting EMPTY right content!");
        }
        rightContent.setValue(safeContent);
    }

    public void setUnifiedDiffContent(String content) {
        String safeContent = content != null ? content : "";
        System.out.println("[CodeViewerModel] setUnifiedDiffContent called: " + safeContent.length() + " chars");
        if (safeContent.isEmpty()) {
            System.err.println("[CodeViewerModel] WARNING: Setting EMPTY unified diff content!");
        }
        unifiedDiffContent.setValue(safeContent);
    }

    public boolean hasSelectedFile() {
        ReviewFile file = selectedFile.getValue();
        return file != null;
    }

    public boolean hasCommitRange() {
        return startCommit.getValue() != null && endCommit.getValue() != null;
    }
}

