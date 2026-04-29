package com.kalynx.serverlessreviewtool.mockdata;

import com.kalynx.serverlessreviewtool.managers.RepositoryManager;
import com.kalynx.serverlessreviewtool.models.Commit;
import com.kalynx.serverlessreviewtool.models.FileChangeType;
import com.kalynx.serverlessreviewtool.models.Repository;
import com.kalynx.serverlessreviewtool.models.ReviewFile;

import java.util.ArrayList;
import java.util.List;

/**
 * RepositoryMockData - Provides mock repository data for development and testing
 * Populates the RepositoryManager with sample repositories
 */
public class RepositoryMockData {

    /**
     * Load mock repository data into the RepositoryManager
     * Call this once at application startup for testing/development
     */
    public static void loadMockData() {
        List<Repository> mockRepositories = createMockRepositories();
        RepositoryManager.getInstance().updateRepositories(mockRepositories);
    }

    /**
     * Create a list of mock repositories
     * @return List of mock Repository objects
     */
    private static List<Repository> createMockRepositories() {
        List<Repository> repositories = new ArrayList<>();

        repositories.add(new Repository(
            "frontend-app",
            "React-based web application frontend",
            "https://github.com/example/frontend-app"
        ));

        repositories.add(new Repository(
            "backend-api",
            "RESTful API server built with Spring Boot",
            "https://github.com/example/backend-api"
        ));

        repositories.add(new Repository(
            "mobile-app",
            "React Native mobile application",
            "https://github.com/example/mobile-app"
        ));

        repositories.add(new Repository(
            "shared-lib",
            "Shared utility library for common functionality",
            "https://github.com/example/shared-lib"
        ));

        return repositories;
    }

    /**
     * Create detailed repositories with commits and files for review contexts
     * @return List of Repository objects with full commit and file data
     */
    public static List<Repository> createDetailedRepositories() {
        List<Repository> repositories = new ArrayList<>();

        Repository backendRepo = new Repository("backend-api", "RESTful API server", "https://github.com/company/backend-api");
        Repository frontendRepo = new Repository("frontend-app", "React web application", "https://github.com/company/frontend-app");

        backendRepo.addCommit(new Commit("abc123", "Initial OAuth2 setup", "John Doe", "2024-04-20"));
        backendRepo.addCommit(new Commit("def456", "Add token validation", "John Doe", "2024-04-21"));
        backendRepo.addCommit(new Commit("ghi789", "Implement refresh token", "John Doe", "2024-04-22"));

        backendRepo.addFile(new ReviewFile("src/auth/OAuthService.java", "backend-api", FileChangeType.MODIFIED));
        backendRepo.addFile(new ReviewFile("src/auth/TokenValidator.java", "backend-api", FileChangeType.ADDED));
        backendRepo.addFile(new ReviewFile("src/config/SecurityConfig.java", "backend-api", FileChangeType.MODIFIED));
        backendRepo.addFile(new ReviewFile("src/models/User.java", "backend-api", FileChangeType.MODIFIED));

        frontendRepo.addCommit(new Commit("jkl012", "Add OAuth login UI", "Jane Smith", "2024-04-21"));
        frontendRepo.addCommit(new Commit("mno345", "Handle OAuth callbacks", "Jane Smith", "2024-04-22"));

        frontendRepo.addFile(new ReviewFile("src/components/LoginForm.tsx", "frontend-app", FileChangeType.MODIFIED));
        frontendRepo.addFile(new ReviewFile("src/services/AuthService.ts", "frontend-app", FileChangeType.ADDED));
        frontendRepo.addFile(new ReviewFile("src/utils/TokenStorage.ts", "frontend-app", FileChangeType.ADDED));

        repositories.add(backendRepo);
        repositories.add(frontendRepo);

        return repositories;
    }

    /**
     * Refresh the mock data (useful for testing refresh functionality)
     */
    public static void refreshMockData() {
        loadMockData();
        System.out.println("Mock repository data refreshed");
    }
}

