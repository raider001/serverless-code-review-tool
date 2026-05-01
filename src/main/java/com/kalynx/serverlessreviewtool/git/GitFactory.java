package com.kalynx.serverlessreviewtool.git;

public class GitFactory {

    private static Git instance;

    public static Git getInstance() {
        if (instance == null) {
            instance = new GitImpl();
        }
        return instance;
    }

    public static void setInstance(Git git) {
        instance = git;
    }

    public static Git createNew() {
        return new GitImpl();
    }
}

