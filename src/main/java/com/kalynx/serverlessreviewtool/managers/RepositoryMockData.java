package com.kalynx.serverlessreviewtool.managers;

import com.kalynx.serverlessreviewtool.models.Repository;

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
     * Refresh the mock data (useful for testing refresh functionality)
     */
    public static void refreshMockData() {
        loadMockData();
        System.out.println("Mock repository data refreshed");
    }
}

