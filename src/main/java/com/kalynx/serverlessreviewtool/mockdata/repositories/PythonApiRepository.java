package com.kalynx.serverlessreviewtool.mockdata.repositories;

import com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi.AppFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi.ModelsFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi.AuthFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi.DatabaseFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi.branches.FeatureLoggingBranch;
import com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi.branches.RefactorDatabaseBranch;

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

        System.out.println("  Adding review notes...");
        addReviewNotes(repoPath);

        System.out.println("  Creating feature branches...");
        FeatureLoggingBranch.create(repoPath);
        RefactorDatabaseBranch.create(repoPath);

        System.out.println("  Python API repository created at: " + repoPath);
    }

    private static void addReviewNotes(Path repoPath) throws IOException, InterruptedException {
        String commitHash = getRootCommitHash(repoPath);

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-201/metadata/title",
            "{\"id\":\"01933ea4-bf77-ad6f-ebg4-h8i5f6d7e8f9\",\"timestamp\":\"2026-01-18T11:30:00Z\",\"editor\":\"sarah.chen\",\"data\":\"Database connection pooling\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-201/metadata/author",
            "{\"id\":\"01933ea4-bf77-ad6f-ebg4-h8i5f6d7e8g0\",\"timestamp\":\"2026-01-18T11:30:00Z\",\"editor\":\"sarah.chen\",\"data\":\"sarah.chen\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-201/metadata/status",
            "{\"id\":\"01933ea4-bf77-ad6f-ebg4-h8i5f6d7e8g1\",\"timestamp\":\"2026-01-18T11:30:00Z\",\"editor\":\"sarah.chen\",\"data\":\"CHANGES_REQUESTED\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-202/metadata/title",
            "{\"id\":\"01933ea5-cf87-be7g-fch5-i9j6g7e8f9g0\",\"timestamp\":\"2026-01-19T14:45:00Z\",\"editor\":\"alex.kumar\",\"data\":\"API authentication enhancements\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-202/metadata/author",
            "{\"id\":\"01933ea5-cf87-be7g-fch5-i9j6g7e8f9g1\",\"timestamp\":\"2026-01-19T14:45:00Z\",\"editor\":\"alex.kumar\",\"data\":\"alex.kumar\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-202/metadata/status",
            "{\"id\":\"01933ea5-cf87-be7g-fch5-i9j6g7e8f9g2\",\"timestamp\":\"2026-01-19T14:45:00Z\",\"editor\":\"alex.kumar\",\"data\":\"IN_REVIEW\"}");
    }

    protected static void createInitialStructure(Path repoPath) throws IOException, InterruptedException {
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


