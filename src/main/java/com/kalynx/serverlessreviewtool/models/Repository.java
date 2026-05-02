package com.kalynx.serverlessreviewtool.models;

import java.util.ArrayList;
import java.util.List;

public class Repository {
    private final String name;
    private final String description;
    private final String url;
    private final List<ReviewFile> files;
    private final List<String> branches;

    public Repository(String name, String description, String url) {
        this.name = name;
        this.description = description;
        this.url = url;
        this.files = new ArrayList<>();
        this.branches = new ArrayList<>();
    }

    public void addFile(ReviewFile file) {
        files.add(file);
    }

    public void setBranches(List<String> branches) {
        this.branches.clear();
        this.branches.addAll(branches);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public List<ReviewFile> getFiles() {
        return new ArrayList<>(files);
    }

    public List<String> getBranches() {
        return new ArrayList<>(branches);
    }

    @Override
    public String toString() {
        return name;
    }
}

