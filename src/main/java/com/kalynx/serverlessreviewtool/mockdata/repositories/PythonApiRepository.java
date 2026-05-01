package com.kalynx.serverlessreviewtool.mockdata.repositories;

import com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi.AppFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi.ModelsFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi.AuthFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi.DatabaseFileMock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PythonApiRepository extends BaseRepository {
    private static final String REPO_NAME = "python-api-service";

    public static void create(Path basePath) throws Exception {
        Path repoPath = basePath.resolve(REPO_NAME);
        Files.createDirectories(repoPath);

        initGitRepository(repoPath);
        createInitialStructure(repoPath);

        System.out.println("  Creating app.py with incremental commits...");
        AppFileMock.create(repoPath);

        System.out.println("  Creating models.py with incremental commits...");
        ModelsFileMock.create(repoPath);

        System.out.println("  Creating auth.py with incremental commits...");
        AuthFileMock.create(repoPath);

        System.out.println("  Creating database.py with incremental commits...");
        DatabaseFileMock.create(repoPath);

        System.out.println("  Python API repository created at: " + repoPath);
    }

    private static void createInitialStructure(Path repoPath) throws IOException, InterruptedException {
        Files.createDirectories(repoPath.resolve("app"));
        Files.createDirectories(repoPath.resolve("app/models"));
        Files.createDirectories(repoPath.resolve("app/routes"));
        Files.createDirectories(repoPath.resolve("tests"));

        Path readme = repoPath.resolve("README.md");
        Files.writeString(readme, "# Python API Service\n\nA Flask-based REST API for user management.\n");
        executeGitCommand(repoPath, "git", "add", "README.md");
        executeGitCommand(repoPath, "git", "commit", "-m", "Initial commit: Add README");

        Path requirements = repoPath.resolve("requirements.txt");
        Files.writeString(requirements, "flask==2.3.0\nflask-sqlalchemy==3.0.5\npyjwt==2.8.0\n");
        executeGitCommand(repoPath, "git", "add", "requirements.txt");
        executeGitCommand(repoPath, "git", "commit", "-m", "Add initial dependencies");
    }
}


