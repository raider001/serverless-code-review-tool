package com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog;

import com.kalynx.serverlessreviewtool.swingextensions.ComponentModel;

import java.util.ArrayList;
import java.util.List;

public class ReviewFormModels {

    public final ComponentModel<String> title = new ComponentModel<>();
    public final ComponentModel<String> author = new ComponentModel<>();
    public final ComponentModel<String> summary = new ComponentModel<>();
    public final ComponentModel<ReviewMode> mode = new ComponentModel<>();

    public final ComponentModel<String> selectedBranchModel = new ComponentModel<>();
    public final ComponentModel<String> selectedBaseBranchModel = new ComponentModel<>();
    public final ComponentModel<String> commitBranchFilter = new ComponentModel<>();
    public final ComponentModel<List<String>> selectedCommitsModel = new ComponentModel<>();

    public final ComponentModel<List<String>> selectedRepositories = new ComponentModel<>();
    public final ComponentModel<List<String>> selectedReviewers = new ComponentModel<>();

    public final ComponentModel<List<String>> availableRepositories = new ComponentModel<>();
    public final ComponentModel<List<String>> availableReviewers = new ComponentModel<>();
    public final ComponentModel<List<String>> availableBranches = new ComponentModel<>();

    public ReviewFormModels() {
    }

    private void initializeDefaults() {
        title.setValue("");
        author.setValue("");
        summary.setValue("");
        mode.setValue(ReviewMode.BRANCH);
        selectedBranchModel.setValue("");
        selectedBaseBranchModel.setValue("main");
        commitBranchFilter.setValue("All Branches");
        selectedCommitsModel.setValue(new ArrayList<>());
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

