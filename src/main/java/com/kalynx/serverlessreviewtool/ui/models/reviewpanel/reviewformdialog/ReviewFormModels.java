package com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog;

import com.kalynx.serverlessreviewtool.swingextensions.ComponentModel;

import java.util.ArrayList;
import java.util.List;

public class ReviewFormModels {

    public final ComponentModel<String> title = new ComponentModel<>();
    public final ComponentModel<String> author = new ComponentModel<>();
    public final ComponentModel<String> summary = new ComponentModel<>();
    public final ComponentModel<ReviewFormModel.ReviewMode> mode = new ComponentModel<>();

    public final ComponentModel<String> branchName = new ComponentModel<>();
    public final ComponentModel<String> reviewAgainstBranch = new ComponentModel<>();

    public final ComponentModel<String> commitBranchFilter = new ComponentModel<>();
    public final ComponentModel<List<String>> selectedCommits = new ComponentModel<>();

    public final ComponentModel<List<String>> selectedRepositories = new ComponentModel<>();
    public final ComponentModel<List<String>> selectedReviewers = new ComponentModel<>();

    private List<String> availableRepositories = new ArrayList<>();
    private List<String> availableReviewers = new ArrayList<>();
    private List<String> availableBranches = new ArrayList<>();
    private List<String> availableCommits = new ArrayList<>();

    public ReviewFormModels() {
        initializeDefaults();
    }

    private void initializeDefaults() {
        title.setValue("");
        author.setValue("");
        summary.setValue("");
        mode.setValue(ReviewFormModel.ReviewMode.BRANCH);
        branchName.setValue("");
        reviewAgainstBranch.setValue("main");
        commitBranchFilter.setValue("All Branches");
        selectedCommits.setValue(new ArrayList<>());
        selectedRepositories.setValue(new ArrayList<>());
        selectedReviewers.setValue(new ArrayList<>());
    }


    public List<String> getAvailableRepositories() {
        return new ArrayList<>(availableRepositories);
    }

    public void setAvailableRepositories(List<String> repositories) {
        this.availableRepositories = repositories != null ? new ArrayList<>(repositories) : new ArrayList<>();
    }

    public List<String> getAvailableReviewers() {
        return new ArrayList<>(availableReviewers);
    }

    public void setAvailableReviewers(List<String> reviewers) {
        this.availableReviewers = reviewers != null ? new ArrayList<>(reviewers) : new ArrayList<>();
    }

    public List<String> getAvailableBranches() {
        return new ArrayList<>(availableBranches);
    }

    public void setAvailableBranches(List<String> branches) {
        this.availableBranches = branches != null ? new ArrayList<>(branches) : new ArrayList<>();
    }

    public List<String> getAvailableCommits() {
        return new ArrayList<>(availableCommits);
    }

    public void setAvailableCommits(List<String> commits) {
        this.availableCommits = commits != null ? new ArrayList<>(commits) : new ArrayList<>();
    }

    public void clear() {
        initializeDefaults();
    }

    public void loadFromModel(ReviewFormModel model) {
        if (model == null) return;

        title.setValue(model.getTitle());
        author.setValue(model.getAuthor());
        summary.setValue(model.getSummary());
        mode.setValue(model.getMode());
        branchName.setValue(model.getBranchName());
        reviewAgainstBranch.setValue(model.getReviewAgainstBranch());
        commitBranchFilter.setValue(model.getCommitBranchFilter());
        selectedCommits.setValue(new ArrayList<>(model.getSelectedCommits()));
        selectedRepositories.setValue(new ArrayList<>(model.getSelectedRepositories()));
        selectedReviewers.setValue(new ArrayList<>(model.getSelectedReviewers()));

        setAvailableRepositories(model.getAvailableRepositories());
        setAvailableReviewers(model.getAvailableReviewers());
        setAvailableBranches(model.getAvailableBranches());
        setAvailableCommits(model.getAvailableCommits());
    }

    public void saveToModel(ReviewFormModel model) {
        if (model == null) return;

        model.setTitle(title.getValue());
        model.setAuthor(author.getValue());
        model.setSummary(summary.getValue());
        model.setMode(mode.getValue());
        model.setBranchName(branchName.getValue());
        model.setReviewAgainstBranch(reviewAgainstBranch.getValue());
        model.setCommitBranchFilter(commitBranchFilter.getValue());
        model.setSelectedCommits(selectedCommits.getValue());
        model.setSelectedRepositories(selectedRepositories.getValue());
        model.setSelectedReviewers(selectedReviewers.getValue());

        model.setAvailableRepositories(availableRepositories);
        model.setAvailableReviewers(availableReviewers);
        model.setAvailableBranches(availableBranches);
        model.setAvailableCommits(availableCommits);
    }
}

