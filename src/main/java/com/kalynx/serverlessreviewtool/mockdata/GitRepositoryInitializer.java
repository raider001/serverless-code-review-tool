package com.kalynx.serverlessreviewtool.mockdata;

import com.kalynx.serverlessreviewtool.mockdata.repositories.JavaBackendRepository;
import com.kalynx.serverlessreviewtool.mockdata.repositories.PythonApiRepository;
import com.kalynx.serverlessreviewtool.mockdata.repositories.ReactFrontendRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GitRepositoryInitializer {
    private static final Path MOCK_BASE_PATH = Paths.get(System.getProperty("user.home"), ".serverless-review-tool", "mock-repos");

    public static void main(String[] args) {
        try {
            System.out.println("Initializing mock Git repositories...");
            System.out.println("Base path: " + MOCK_BASE_PATH);

            cleanupExistingRepositories();
            createMockRepositories();

            System.out.println("\nMock repositories created successfully!");
            System.out.println("Location: " + MOCK_BASE_PATH);
        } catch (Exception e) {
            System.err.println("Failed to initialize mock repositories: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static Path getBasePath() {
        return MOCK_BASE_PATH;
    }

    private static void cleanupExistingRepositories() throws IOException {
        if (Files.exists(MOCK_BASE_PATH)) {
            System.out.println("\nCleaning up existing mock repositories...");
            deleteDirectory(MOCK_BASE_PATH);
        }
        Files.createDirectories(MOCK_BASE_PATH);
    }

    private static void createMockRepositories() throws Exception {
        System.out.println("\nCreating Java Backend repository...");
        JavaBackendRepository.create(MOCK_BASE_PATH);

        System.out.println("\nCreating Python API repository...");
        PythonApiRepository.create(MOCK_BASE_PATH);

        System.out.println("\nCreating React Frontend repository...");
        ReactFrontendRepository.create(MOCK_BASE_PATH);
    }

    private static void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        System.err.println("Failed to delete: " + p + " - " + e.getMessage());
                    }
                });
        }
    }
}

