package com.kalynx.serverlessreviewtool.mockdata.repositories;

import com.kalynx.serverlessreviewtool.mockdata.repositories.javabackend.UserServiceFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.javabackend.AuthControllerFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.javabackend.DatabaseConfigFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.javabackend.UserRepositoryFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.javabackend.branches.FeatureAuthBranch;
import com.kalynx.serverlessreviewtool.mockdata.repositories.javabackend.branches.BugfixSecurityBranch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JavaBackendRepository extends BaseRepository {
    private static final String REPO_NAME = "java-backend-service";

    public static void create(Path basePath) throws Exception {
        Path repoPath = basePath.resolve(REPO_NAME);
        Files.createDirectories(repoPath);

        initGitRepository(repoPath);
        createInitialStructure(repoPath);

        System.out.println("  Creating UserService.java with incremental commits...");
        UserServiceFileMock.create(repoPath);

        System.out.println("  Creating AuthController.java with incremental commits...");
        AuthControllerFileMock.create(repoPath);

        System.out.println("  Creating DatabaseConfig.java with incremental commits...");
        DatabaseConfigFileMock.create(repoPath);

        System.out.println("  Creating UserRepository.java with incremental commits...");
        UserRepositoryFileMock.create(repoPath);

        System.out.println("  Adding review notes...");
        addReviewNotes(repoPath);

        System.out.println("  Creating feature branches...");
        FeatureAuthBranch.create(repoPath);
        BugfixSecurityBranch.create(repoPath);

        System.out.println("  Java Backend repository created at: " + repoPath);
    }

    private static void addReviewNotes(Path repoPath) throws IOException, InterruptedException {
        String commitHash = getRootCommitHash(repoPath);

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-001/metadata/title",
            "{\"id\":\"01933ea1-8f47-7a3c-b8f1-d5e2c3a4b5c6\",\"timestamp\":\"2026-01-15T10:00:00Z\",\"editor\":\"john.doe\",\"data\":\"Refactor user service layer\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-001/metadata/description",
            "{\"id\":\"01933ea1-8f47-7a3c-b8f1-d5e2c3a4b5c9\",\"timestamp\":\"2026-01-15T10:00:00Z\",\"editor\":\"john.doe\",\"data\":\"Refactored the user service to improve maintainability and add proper error handling. Split monolithic functions into smaller, testable components.\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-001/metadata/author",
            "{\"id\":\"01933ea1-8f47-7a3c-b8f1-d5e2c3a4b5c7\",\"timestamp\":\"2026-01-15T10:00:00Z\",\"editor\":\"john.doe\",\"data\":\"john.doe\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-001/metadata/status",
            "{\"id\":\"01933ea1-8f47-7a3c-b8f1-d5e2c3a4b5c8\",\"timestamp\":\"2026-01-15T10:00:00Z\",\"editor\":\"john.doe\",\"data\":\"OPEN\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-002/metadata/title",
            "{\"id\":\"01933ea2-9f57-8b4d-c9g2-e6f3d4b5c6d7\",\"timestamp\":\"2026-01-16T14:30:00Z\",\"editor\":\"jane.smith\",\"data\":\"Security fixes for auth controller\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-002/metadata/description",
            "{\"id\":\"01933ea2-9f57-8b4d-c9g2-e6f3d4b5c6da\",\"timestamp\":\"2026-01-16T14:30:00Z\",\"editor\":\"jane.smith\",\"data\":\"Fixed authentication bypass vulnerability and added rate limiting to prevent brute force attacks. Updated input validation for user credentials.\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-002/metadata/author",
            "{\"id\":\"01933ea2-9f57-8b4d-c9g2-e6f3d4b5c6d8\",\"timestamp\":\"2026-01-16T14:30:00Z\",\"editor\":\"jane.smith\",\"data\":\"jane.smith\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-002/metadata/status",
            "{\"id\":\"01933ea2-9f57-8b4d-c9g2-e6f3d4b5c6d9\",\"timestamp\":\"2026-01-16T14:30:00Z\",\"editor\":\"jane.smith\",\"data\":\"IN_REVIEW\"}");
    }

    protected static void createInitialStructure(Path repoPath) throws IOException, InterruptedException {
        Files.createDirectories(repoPath.resolve("src/main/java/com/example/service"));
        Files.createDirectories(repoPath.resolve("src/main/java/com/example/controller"));
        Files.createDirectories(repoPath.resolve("src/main/java/com/example/config"));
        Files.createDirectories(repoPath.resolve("src/main/java/com/example/repository"));

        Path readme = repoPath.resolve("README.md");
        Files.writeString(readme, "# Java Backend Service\n\nA Spring Boot microservice for user management.\n");
        executeGitCommand(repoPath, "git", "add", "README.md");
        executeGitCommand(repoPath, "git", "commit", "-m", "Initial commit: Add README");
    }
}

