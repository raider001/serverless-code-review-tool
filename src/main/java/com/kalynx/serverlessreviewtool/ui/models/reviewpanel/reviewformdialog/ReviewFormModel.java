package com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog;

import java.util.ArrayList;
import java.util.List;

public class ReviewFormModel {

    private final List<String> availableRepositories;
    private final List<String> availableReviewers;
    private final List<String> availableBranches;
    private final List<String> availableCommits;

    private String title;
    private String author;
    private String summary;
    private ReviewMode mode;

    private String branchName;
    private String reviewAgainstBranch;

    private String commitBranchFilter;
    private final List<String> selectedCommits;

    private final List<String> selectedRepositories;
    private final List<String> selectedReviewers;

    public ReviewFormModel() {
        this.availableRepositories = new ArrayList<>();
        this.availableReviewers = new ArrayList<>();
        this.availableBranches = new ArrayList<>();
        this.availableCommits = new ArrayList<>();

        this.title = "";
        this.author = "";
        this.summary = "";
        this.mode = ReviewMode.BRANCH;

        this.branchName = "";
        this.reviewAgainstBranch = "main";

        this.commitBranchFilter = "All Branches";
        this.selectedCommits = new ArrayList<>();

        this.selectedRepositories = new ArrayList<>();
        this.selectedReviewers = new ArrayList<>();
    }

    public List<String> getAvailableRepositories() {
        return new ArrayList<>(availableRepositories);
    }

    public void setAvailableRepositories(List<String> repositories) {
        this.availableRepositories.clear();
        if (repositories != null) {
            this.availableRepositories.addAll(repositories);
        }
    }

    public List<String> getAvailableReviewers() {
        return new ArrayList<>(availableReviewers);
    }

    public void setAvailableReviewers(List<String> reviewers) {
        this.availableReviewers.clear();
        if (reviewers != null) {
            this.availableReviewers.addAll(reviewers);
        }
    }

    public List<String> getAvailableBranches() {
        return new ArrayList<>(availableBranches);
    }

    public void setAvailableBranches(List<String> branches) {
        this.availableBranches.clear();
        if (branches != null) {
            this.availableBranches.addAll(branches);
        }
    }

    public List<String> getAvailableCommits() {
        return new ArrayList<>(availableCommits);
    }

    public void setAvailableCommits(List<String> commits) {
        this.availableCommits.clear();
        if (commits != null) {
            this.availableCommits.addAll(commits);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title != null ? title : "";
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author != null ? author : "";
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary != null ? summary : "";
    }

    public ReviewMode getMode() {
        return mode;
    }

    public void setMode(ReviewMode mode) {
        this.mode = mode != null ? mode : ReviewMode.BRANCH;
    }

    public boolean isBranchMode() {
        return mode == ReviewMode.BRANCH;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName != null ? branchName : "";
    }

    public String getReviewAgainstBranch() {
        return reviewAgainstBranch;
    }

    public void setReviewAgainstBranch(String reviewAgainstBranch) {
        this.reviewAgainstBranch = reviewAgainstBranch != null ? reviewAgainstBranch : "main";
    }

    public String getCommitBranchFilter() {
        return commitBranchFilter;
    }

    public void setCommitBranchFilter(String commitBranchFilter) {
        this.commitBranchFilter = commitBranchFilter != null ? commitBranchFilter : "All Branches";
    }

    public List<String> getSelectedCommits() {
        return new ArrayList<>(selectedCommits);
    }

    public void setSelectedCommits(List<String> commits) {
        this.selectedCommits.clear();
        if (commits != null) {
            this.selectedCommits.addAll(commits);
        }
    }

    public void addSelectedCommit(String commit) {
        if (commit != null && !selectedCommits.contains(commit)) {
            selectedCommits.add(commit);
        }
    }

    public void removeSelectedCommit(String commit) {
        selectedCommits.remove(commit);
    }

    public List<String> getSelectedRepositories() {
        return new ArrayList<>(selectedRepositories);
    }

    public void setSelectedRepositories(List<String> repositories) {
        this.selectedRepositories.clear();
        if (repositories != null) {
            this.selectedRepositories.addAll(repositories);
        }
    }

    public void addSelectedRepository(String repository) {
        if (repository != null && !selectedRepositories.contains(repository)) {
            selectedRepositories.add(repository);
        }
    }

    public void removeSelectedRepository(String repository) {
        selectedRepositories.remove(repository);
    }

    public List<String> getSelectedReviewers() {
        return new ArrayList<>(selectedReviewers);
    }

    public void setSelectedReviewers(List<String> reviewers) {
        this.selectedReviewers.clear();
        if (reviewers != null) {
            this.selectedReviewers.addAll(reviewers);
        }
    }

    public void addSelectedReviewer(String reviewer) {
        if (reviewer != null && !selectedReviewers.contains(reviewer)) {
            selectedReviewers.add(reviewer);
        }
    }

    public void removeSelectedReviewer(String reviewer) {
        selectedReviewers.remove(reviewer);
    }

    public boolean hasSelectedRepositories() {
        return !selectedRepositories.isEmpty();
    }

    public boolean hasSelectedReviewers() {
        return !selectedReviewers.isEmpty();
    }

    public boolean hasSelectedCommits() {
        return !selectedCommits.isEmpty();
    }

    public void clear() {
        this.title = "";
        this.author = "";
        this.summary = "";
        this.mode = ReviewMode.BRANCH;
        this.branchName = "";
        this.reviewAgainstBranch = "main";
        this.commitBranchFilter = "All Branches";
        this.selectedCommits.clear();
        this.selectedRepositories.clear();
        this.selectedReviewers.clear();
    }

    public enum ReviewMode {
        BRANCH,
        COMMIT
    }
}

