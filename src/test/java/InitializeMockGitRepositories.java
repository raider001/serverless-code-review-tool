package com.kalynx.serverlessreviewtool.mockdata;

import com.kalynx.serverlessreviewtool.git.GitException;

import java.io.IOException;

public class InitializeMockGitRepositories {

    public static void main(String[] args) {
        try {
            System.out.println("=================================================");
            System.out.println("  Mock Git Repository Initialization");
            System.out.println("=================================================\n");

            GitRepositoryInitializer initializer = GitRepositoryInitializer.create();
            initializer.initializeAllRepositories();

            System.out.println("\n=================================================");
            System.out.println("  Summary");
            System.out.println("=================================================");
            System.out.println("Created repositories:");
            System.out.println("  1. frontend-app");
            System.out.println("     - Remote: " + initializer.getRemotePath("frontend-app"));
            System.out.println("     - Local:  " + initializer.getLocalPath("frontend-app"));
            System.out.println("     - Commits: 2 (OAuth UI, Callbacks)");
            System.out.println("     - Review Notes: 1 review with comments");
            System.out.println();
            System.out.println("  2. backend-api");
            System.out.println("     - Remote: " + initializer.getRemotePath("backend-api"));
            System.out.println("     - Local:  " + initializer.getLocalPath("backend-api"));
            System.out.println("     - Commits: 3 (OAuth setup, Token validation, Refresh token)");
            System.out.println("     - Review Notes: 1 review with multiple reviewers and comments");
            System.out.println();
            System.out.println("  3. shared-lib");
            System.out.println("     - Remote: " + initializer.getRemotePath("shared-lib"));
            System.out.println("     - Local:  " + initializer.getLocalPath("shared-lib"));
            System.out.println("     - Commits: 2 (Utilities, Validators)");
            System.out.println();
            System.out.println("=================================================");
            System.out.println("  ✓ All repositories initialized successfully!");
            System.out.println("=================================================");

        } catch (GitException | IOException e) {
            System.err.println("Failed to initialize mock repositories:");
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}


