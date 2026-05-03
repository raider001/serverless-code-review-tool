package com.kalynx.serverlessreviewtool.mockdata.repositories;

import com.kalynx.serverlessreviewtool.mockdata.repositories.reactfrontend.AppFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.reactfrontend.LoginFormFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.reactfrontend.UserListFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.reactfrontend.ApiFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.reactfrontend.branches.FeatureThemingBranch;
import com.kalynx.serverlessreviewtool.mockdata.repositories.reactfrontend.branches.PerformanceOptimizationBranch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ReactFrontendRepository extends BaseRepository {
    private static final String REPO_NAME = "react-frontend-app";

    public static void create(Path basePath) throws Exception {
        Path repoPath = basePath.resolve(REPO_NAME);
        Files.createDirectories(repoPath);

        initGitRepository(repoPath);
        createInitialStructure(repoPath);

        System.out.println("  Creating App.tsx with incremental commits...");
        AppFileMock.create(repoPath);

        System.out.println("  Creating LoginForm.tsx with incremental commits...");
        LoginFormFileMock.create(repoPath);

        System.out.println("  Creating UserList.tsx with incremental commits...");
        UserListFileMock.create(repoPath);

        System.out.println("  Creating api.ts with incremental commits...");
        ApiFileMock.create(repoPath);

        System.out.println("  Adding review notes...");
        addReviewNotes(repoPath);

        System.out.println("  Creating feature branches...");
        FeatureThemingBranch.create(repoPath);
        PerformanceOptimizationBranch.create(repoPath);

        System.out.println("  React Frontend repository created at: " + repoPath);
    }

    private static void addReviewNotes(Path repoPath) throws IOException, InterruptedException {
        String commitHash = getRootCommitHash(repoPath);

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-101/metadata/title",
            "{\"id\":\"01933ea3-af67-9c5e-daf3-g7h4e5c6d7e8\",\"timestamp\":\"2026-01-17T09:15:00Z\",\"editor\":\"mike.wilson\",\"data\":\"UI component refactoring\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-101/metadata/description",
            "{\"id\":\"01933ea3-af67-9c5e-daf3-g7h4e5c6d7f1\",\"timestamp\":\"2026-01-17T09:15:00Z\",\"editor\":\"mike.wilson\",\"data\":\"Refactored React components to use hooks and functional components. Improved type safety with TypeScript interfaces and reduced prop drilling with context API.\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-101/metadata/author",
            "{\"id\":\"01933ea3-af67-9c5e-daf3-g7h4e5c6d7e9\",\"timestamp\":\"2026-01-17T09:15:00Z\",\"editor\":\"mike.wilson\",\"data\":\"mike.wilson\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-101/metadata/status",
            "{\"id\":\"01933ea3-af67-9c5e-daf3-g7h4e5c6d7f0\",\"timestamp\":\"2026-01-17T09:15:00Z\",\"editor\":\"mike.wilson\",\"data\":\"APPROVED\"}");
    }

    protected static void createInitialStructure(Path repoPath) throws IOException, InterruptedException {
        Files.createDirectories(repoPath.resolve("src/components"));
        Files.createDirectories(repoPath.resolve("src/services"));
        Files.createDirectories(repoPath.resolve("src/utils"));
        Files.createDirectories(repoPath.resolve("public"));

        Path readme = repoPath.resolve("README.md");
        Files.writeString(readme, "# React Frontend App\n\nA modern React application with TypeScript.\n");
        executeGitCommand(repoPath, "git", "add", "README.md");
        executeGitCommand(repoPath, "git", "commit", "-m", "Initial commit: Add README");

        Path packageJson = repoPath.resolve("package.json");
        String packageContent = """
            {
              "name": "react-frontend-app",
              "version": "1.0.0",
              "dependencies": {
                "react": "^18.2.0",
                "react-dom": "^18.2.0",
                "typescript": "^5.0.0"
              }
            }
            """;
        Files.writeString(packageJson, packageContent);
        executeGitCommand(repoPath, "git", "add", "package.json");
        executeGitCommand(repoPath, "git", "commit", "-m", "Add package.json with dependencies");
    }
}


