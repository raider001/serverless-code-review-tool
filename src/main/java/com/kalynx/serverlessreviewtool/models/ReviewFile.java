package com.kalynx.serverlessreviewtool.models;

/**
 * ReviewFile - Represents a file in the review
 */
public class ReviewFile {
    private final String path;
    private final String repository;
    private final FileChangeType changeType;
    private final String baseBranch;
    private final String reviewBranch;

    public ReviewFile(String path, String repository, FileChangeType changeType) {
        this(path, repository, changeType, null, null);
    }

    public ReviewFile(String path, String repository, FileChangeType changeType,
                      String baseBranch, String reviewBranch) {
        this.path = path;
        this.repository = repository;
        this.changeType = changeType;
        this.baseBranch = baseBranch;
        this.reviewBranch = reviewBranch;
    }

    public String getPath() {
        return path;
    }

    public String getRepository() {
        return repository;
    }

    public FileChangeType getChangeType() {
        return changeType;
    }

    public String getFileName() {
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    public String getDirectory() {
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(0, lastSlash) : "";
    }

    public String getBaseBranch() {
        return baseBranch;
    }

    public String getReviewBranch() {
        return reviewBranch;
    }

    public boolean hasBranchInfo() {
        return baseBranch != null && reviewBranch != null;
    }

    @Override
    public String toString() {
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ReviewFile)) return false;
        ReviewFile other = (ReviewFile) obj;
        return path.equals(other.path) && repository.equals(other.repository);
    }

    @Override
    public int hashCode() {
        return path.hashCode() + repository.hashCode();
    }
}

