package com.kalynx.serverlessreviewtool.ui.review.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository - Represents a repository in the review
 */
public class Repository {
    private final String name;
    private final String url;
    private final List<Commit> commits;
    private final List<ReviewFile> files;

    public Repository(String name, String url) {
        this.name = name;
        this.url = url;
        this.commits = new ArrayList<>();
        this.files = new ArrayList<>();
    }

    public void addCommit(Commit commit) {
        commits.add(commit);
    }

    public void addFile(ReviewFile file) {
        files.add(file);
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public List<Commit> getCommits() {
        return new ArrayList<>(commits);
    }

    public List<ReviewFile> getFiles() {
        return new ArrayList<>(files);
    }

    @Override
    public String toString() {
        return name;
    }
}

