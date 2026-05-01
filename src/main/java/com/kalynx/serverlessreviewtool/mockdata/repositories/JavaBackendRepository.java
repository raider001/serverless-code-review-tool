package com.kalynx.serverlessreviewtool.mockdata.repositories;

import com.kalynx.serverlessreviewtool.mockdata.repositories.javabackend.UserServiceFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.javabackend.AuthControllerFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.javabackend.DatabaseConfigFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.javabackend.UserRepositoryFileMock;

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

        System.out.println("  Java Backend repository created at: " + repoPath);
    }

    private static void createInitialStructure(Path repoPath) throws IOException, InterruptedException {
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

