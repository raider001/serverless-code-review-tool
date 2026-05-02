package com.kalynx.serverlessreviewtool.mockdata;

import com.kalynx.serverlessreviewtool.mockdata.repositories.JavaBackendRepository;
import com.kalynx.serverlessreviewtool.mockdata.repositories.PythonApiRepository;
import com.kalynx.serverlessreviewtool.mockdata.repositories.ReactFrontendRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class GitRepositoryInitializer {
    private static final Path MOCK_BASE_PATH = Paths.get(System.getProperty("user.home"), ".serverless-review-tool", "mock-repos");

    public static void main(String[] args) {
        try {
            System.out.println("Initializing mock Git repositories...");
            System.out.println("Base path: " + MOCK_BASE_PATH);

            long startTime = System.currentTimeMillis();

            cleanupExistingRepositories();
            createMockRepositories();

            long elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println("\nMock repositories created successfully!");
            System.out.println("Location: " + MOCK_BASE_PATH);
            System.out.println("Time taken: " + (elapsedTime / 1000.0) + " seconds");
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
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<String>> futures = new ArrayList<>();

        System.out.println("\nCreating repositories in parallel...");

        futures.add(executor.submit(() -> {
            System.out.println("Creating Java Backend repository...");
            JavaBackendRepository.create(MOCK_BASE_PATH);
            return "Java Backend";
        }));

        futures.add(executor.submit(() -> {
            System.out.println("Creating Python API repository...");
            PythonApiRepository.create(MOCK_BASE_PATH);
            return "Python API";
        }));

        futures.add(executor.submit(() -> {
            System.out.println("Creating React Frontend repository...");
            ReactFrontendRepository.create(MOCK_BASE_PATH);
            return "React Frontend";
        }));

        executor.shutdown();

        try {
            for (Future<String> future : futures) {
                String repoName = future.get();
                System.out.println("  ✓ " + repoName + " repository completed");
            }

            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                System.err.println("Repository creation timed out");
                executor.shutdownNow();
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error during parallel repository creation: " + e.getMessage());
            executor.shutdownNow();
            throw new Exception("Failed to create repositories", e);
        }
    }

    private static void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        // Run git gc to close file handles before deletion
        try {
            Path[] repoDirs = Files.list(path)
                .filter(Files::isDirectory)
                .toArray(Path[]::new);

            for (Path repoDir : repoDirs) {
                Path gitDir = repoDir.resolve(".git");
                if (Files.exists(gitDir)) {
                    try {
                        // Try to clean up git processes
                        ProcessBuilder pb = new ProcessBuilder("git", "gc", "--prune=now");
                        pb.directory(repoDir.toFile());
                        pb.redirectErrorStream(true);
                        Process process = pb.start();
                        process.waitFor();
                    } catch (Exception ignored) {
                        // Git gc is optional, continue if it fails
                    }
                }
            }
        } catch (Exception ignored) {
            // Continue with deletion even if gc fails
        }

        // Give Windows a moment to release file handles
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }

        // Delete directory with retry logic
        try (var stream = Files.walk(path)) {
            stream.sorted((a, b) -> b.compareTo(a))
                .forEach(p -> deleteWithRetry(p, 3));
        }
    }

    private static void deleteWithRetry(Path path, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                // Make file writable before deletion (Windows)
                if (Files.exists(path) && !Files.isDirectory(path)) {
                    path.toFile().setWritable(true);
                }
                Files.delete(path);
                return; // Success
            } catch (IOException e) {
                if (i == maxRetries - 1) {
                    // Last retry failed
                    System.err.println("Failed to delete after " + maxRetries + " attempts: " + path);
                    // Try to mark for deletion on exit as last resort
                    path.toFile().deleteOnExit();
                } else {
                    // Wait before retry
                    try {
                        Thread.sleep(50L * (i + 1)); // Increasing delay
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    }
}

