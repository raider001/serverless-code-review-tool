package com.kalynx.serverlessreviewtool.mockdata.repositories;

import com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi.AppFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi.ModelsFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi.AuthFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi.DatabaseFileMock;
import com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi.branches.FeatureLoggingBranch;
import com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi.branches.FeatureOAuthIntegrationBranch;
import com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi.branches.RefactorDatabaseBranch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PythonApiRepository extends BaseRepository {
    private static final String REPO_NAME = "python-api-service";
    private static final Logger LOGGER = LoggerFactory.getLogger(PythonApiRepository.class);

    public static void create(Path basePath) throws Exception {
        Path repoPath = basePath.resolve(REPO_NAME);
        Files.createDirectories(repoPath);

        initGitRepository(repoPath);
        createInitialStructure(repoPath);

        LOGGER.info("Creating app.py with incremental commits");
        AppFileMock.create(repoPath);

        LOGGER.info("Creating models.py with incremental commits");
        ModelsFileMock.create(repoPath);

        LOGGER.info("Creating auth.py with incremental commits");
        AuthFileMock.create(repoPath);

        LOGGER.info("Creating database.py with incremental commits");
        DatabaseFileMock.create(repoPath);

        LOGGER.info("Adding review notes");
        addReviewNotes(repoPath);

        LOGGER.info("Creating feature branches");
        FeatureLoggingBranch.create(repoPath);
        FeatureOAuthIntegrationBranch.create(repoPath);
        RefactorDatabaseBranch.create(repoPath);

        LOGGER.info("Python API repository created at: {}", repoPath);
    }

    private static void addReviewNotes(Path repoPath) throws IOException, InterruptedException {
        String commitHash = getRootCommitHash(repoPath);

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-201/metadata/title",
            "{\"id\":\"01933ea4-bf77-ad6f-ebg4-h8i5f6d7e8f9\",\"timestamp\":\"2026-01-18T11:30:00Z\",\"editor\":\"sarah.chen\",\"data\":\"Database connection pooling\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-201/metadata/description",
            "{\"id\":\"01933ea4-bf77-ad6f-ebg4-h8i5f6d7e8g2\",\"timestamp\":\"2026-01-18T11:30:00Z\",\"editor\":\"sarah.chen\",\"data\":\"Implemented connection pooling for database to improve performance and reduce connection overhead. Added configuration options for pool size and timeout settings.\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-201/metadata/author",
            "{\"id\":\"01933ea4-bf77-ad6f-ebg4-h8i5f6d7e8g0\",\"timestamp\":\"2026-01-18T11:30:00Z\",\"editor\":\"sarah.chen\",\"data\":\"sarah.chen\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-201/metadata/status",
            "{\"id\":\"01933ea4-bf77-ad6f-ebg4-h8i5f6d7e8g1\",\"timestamp\":\"2026-01-18T11:30:00Z\",\"editor\":\"sarah.chen\",\"data\":\"CHANGES_REQUESTED\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-201/metadata/primaryRepository",
            "{\"id\":\"01933ea4-bf77-ad6f-ebg4-h8i5f6d7e8g3\",\"timestamp\":\"2026-01-18T11:30:00Z\",\"editor\":\"sarah.chen\",\"data\":\"true\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-201/metadata/branch",
            "{\"id\":\"01933ea4-bf77-ad6f-ebg4-h8i5f6d7e8g4\",\"timestamp\":\"2026-01-18T11:30:00Z\",\"editor\":\"sarah.chen\",\"data\":\"feature/structured-logging\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-201/metadata/baseBranch",
            "{\"id\":\"01933ea4-bf77-ad6f-ebg4-h8i5f6d7e8g5\",\"timestamp\":\"2026-01-18T11:30:00Z\",\"editor\":\"sarah.chen\",\"data\":\"master\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-202/metadata/title",
            "{\"id\":\"01933ea5-cf87-be7g-fch5-i9j6g7e8f9g0\",\"timestamp\":\"2026-01-19T14:45:00Z\",\"editor\":\"alex.kumar\",\"data\":\"API authentication enhancements\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-202/metadata/description",
            "{\"id\":\"01933ea5-cf87-be7g-fch5-i9j6g7e8f9g3\",\"timestamp\":\"2026-01-19T14:45:00Z\",\"editor\":\"alex.kumar\",\"data\":\"Enhanced JWT-based authentication with refresh token support and improved session management. Added rate limiting and IP-based access control.\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-202/metadata/author",
            "{\"id\":\"01933ea5-cf87-be7g-fch5-i9j6g7e8f9g1\",\"timestamp\":\"2026-01-19T14:45:00Z\",\"editor\":\"alex.kumar\",\"data\":\"alex.kumar\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-202/metadata/status",
            "{\"id\":\"01933ea5-cf87-be7g-fch5-i9j6g7e8f9g2\",\"timestamp\":\"2026-01-19T14:45:00Z\",\"editor\":\"alex.kumar\",\"data\":\"IN_REVIEW\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-202/metadata/primaryRepository",
            "{\"id\":\"01933ea5-cf87-be7g-fch5-i9j6g7e8f9g4\",\"timestamp\":\"2026-01-19T14:45:00Z\",\"editor\":\"alex.kumar\",\"data\":\"true\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-202/metadata/branch",
            "{\"id\":\"01933ea5-cf87-be7g-fch5-i9j6g7e8f9g5\",\"timestamp\":\"2026-01-19T14:45:00Z\",\"editor\":\"alex.kumar\",\"data\":\"refactor/async-database\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-202/metadata/baseBranch",
            "{\"id\":\"01933ea5-cf87-be7g-fch5-i9j6g7e8f9g6\",\"timestamp\":\"2026-01-19T14:45:00Z\",\"editor\":\"alex.kumar\",\"data\":\"master\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-300/metadata/title",
            "{\"id\":\"01933ea6-df97-cf8h-gdi6-j0k7h8f9g0h1\",\"timestamp\":\"2026-01-20T16:00:00Z\",\"editor\":\"system.admin\",\"data\":\"Cross-service authentication integration\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-300/metadata/description",
            "{\"id\":\"01933ea6-df97-cf8h-gdi6-j0k7h8f9g0h4\",\"timestamp\":\"2026-01-20T16:00:00Z\",\"editor\":\"system.admin\",\"data\":\"Comprehensive authentication system spanning Java backend and Python API services. Implements unified session management, token validation, and cross-service authorization checks.\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-300/metadata/author",
            "{\"id\":\"01933ea6-df97-cf8h-gdi6-j0k7h8f9g0h2\",\"timestamp\":\"2026-01-20T16:00:00Z\",\"editor\":\"system.admin\",\"data\":\"system.admin\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-300/metadata/status",
            "{\"id\":\"01933ea6-df97-cf8h-gdi6-j0k7h8f9g0h3\",\"timestamp\":\"2026-01-20T16:00:00Z\",\"editor\":\"system.admin\",\"data\":\"OPEN\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-300/metadata/primaryRepository",
            "{\"id\":\"01933ea6-df97-cf8h-gdi6-j0k7h8f9g0h5\",\"timestamp\":\"2026-01-20T16:00:00Z\",\"editor\":\"system.admin\",\"data\":\"false\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-300/metadata/branch",
            "{\"id\":\"01933ea6-df97-cf8h-gdi6-j0k7h8f9g0h6\",\"timestamp\":\"2026-01-20T16:00:00Z\",\"editor\":\"system.admin\",\"data\":\"feature/oauth-integration\"}");

        addGitNote(repoPath, commitHash, "refs/notes/reviews/review-300/metadata/baseBranch",
            "{\"id\":\"01933ea6-df97-cf8h-gdi6-j0k7h8f9g0h7\",\"timestamp\":\"2026-01-20T16:00:00Z\",\"editor\":\"system.admin\",\"data\":\"master\"}");
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

