package com.kalynx.serverlessreviewtool.git;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GitFactory {

    private static Git instance;
    private static final Path DEFAULT_GIT_PATH = Paths.get(System.getProperty("user.home"), ".serverless-review-tool", "git");

    public static Git getInstance() {
        if (instance == null) {
            instance = new GitImpl(DEFAULT_GIT_PATH);
        }
        return instance;
    }

    public static void setInstance(Git git) {
        instance = git;
    }

    public static Git createNew() {
        return new GitImpl(DEFAULT_GIT_PATH);
    }

    public static Git createNew(Path gitLocalPath) {
        return new GitImpl(gitLocalPath);
    }
}

