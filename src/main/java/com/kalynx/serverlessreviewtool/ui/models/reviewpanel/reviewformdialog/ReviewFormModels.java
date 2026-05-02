package com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog;

import com.kalynx.serverlessreviewtool.swingextensions.ComponentModel;

import java.util.ArrayList;
import java.util.List;

public class ReviewFormModels {

    public final ComponentModel<String> title = new ComponentModel<>();
    public final ComponentModel<String> author = new ComponentModel<>();
    public final ComponentModel<String> summary = new ComponentModel<>();
    public final ComponentModel<ReviewMode> mode = new ComponentModel<>();

    public final ComponentModel<String> branchName = new ComponentModel<>();
    public final ComponentModel<String> reviewAgainstBranch = new ComponentModel<>();

    public final ComponentModel<String> commitBranchFilter = new ComponentModel<>();
    public final ComponentModel<List<String>> selectedCommits = new ComponentModel<>();

    public final ComponentModel<List<String>> selectedRepositories = new ComponentModel<>();
    public final ComponentModel<List<String>> selectedReviewers = new ComponentModel<>();

    public final ComponentModel<List<String>> availableRepositories = new ComponentModel<>();
    public final ComponentModel<List<String>> availableReviewers = new ComponentModel<>();
    public final ComponentModel<List<String>> availableBranches = new ComponentModel<>();
    public final ComponentModel<List<String>> availableCommits = new ComponentModel<>();

    public ReviewFormModels() {
    }

    private void initializeDefaults() {
        title.setValue("");
        author.setValue("");
        summary.setValue("");
        mode.setValue(ReviewMode.BRANCH);
        branchName.setValue("");
        reviewAgainstBranch.setValue("main");
        commitBranchFilter.setValue("All Branches");
        selectedCommits.setValue(new ArrayList<>());
        selectedRepositories.setValue(new ArrayList<>());
        selectedReviewers.setValue(new ArrayList<>());
    }

    public void clear() {
        initializeDefaults();
    }

    public enum ReviewMode {
        BRANCH,
        COMMIT
    }
}

