package com.kalynx.serverlessreviewtool.mockdata;

import com.kalynx.serverlessreviewtool.mockdata.repositories.JavaBackendRepository;
import com.kalynx.serverlessreviewtool.mockdata.repositories.PythonApiRepository;
import com.kalynx.serverlessreviewtool.mockdata.repositories.ReactFrontendRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Initializes and manages the mock Git repositories used by the application.
 */
public class GitRepositoryInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitRepositoryInitializer.class);
    private static final Path MOCK_BASE_PATH = Paths.get(System.getProperty("user.home"), ".serverless-review-tool", "mock-repos");
    private static final Path LOCAL_REPOSITORIES_PATH = Paths.get(System.getProperty("user.home"), ".serverless-review-tool", "repositories");
    private static final List<String> EXPECTED_REPOSITORIES = List.of(
        "java-backend-service",
        "python-api-service",
        "react-frontend-app"
    );

    /**
     * Initializes the mock repositories.
     */
    public static void main() {
        try {
            LOGGER.info("Initializing mock Git repositories...");
            LOGGER.info("Base path: {}", MOCK_BASE_PATH);

            long startTime = System.currentTimeMillis();
            cleanupExistingRepositories();
            createMockRepositories();
            cleanupLocalMockRepositoryClones();

            long elapsedTime = System.currentTimeMillis() - startTime;
            LOGGER.info("Mock repositories created successfully!");
            LOGGER.info("Location: {}", MOCK_BASE_PATH);
            LOGGER.info("Time taken: {} seconds", elapsedTime / 1000.0);
        } catch (Exception e) {
            LOGGER.error("Failed to initialize mock repositories", e);
            System.exit(1);
        }
    }

    /**
     * Returns the base path used for mock repositories.
     *
     * @return the mock repository base path
     */
    public static Path getBasePath() {
        return MOCK_BASE_PATH;
    }

    /**
     * Ensures that the mock repositories exist and are ready for use.
     *
     * @throws Exception if repository cleanup or creation fails
     */
    public static synchronized void ensureMockRepositoriesExist() throws Exception {
        if (areMockRepositoriesReady()) {
            return;
        }

        cleanupExistingRepositories();
        createMockRepositories();
        cleanupLocalMockRepositoryClones();
    }

    /**
     * Checks whether all expected mock repositories exist and are initialized.
     *
     * @return {@code true} if all mock repositories are ready; otherwise {@code false}
     */
    public static boolean areMockRepositoriesReady() {
        if (!Files.isDirectory(MOCK_BASE_PATH)) {
            return false;
        }

        return EXPECTED_REPOSITORIES.stream().allMatch(GitRepositoryInitializer::isRepositoryReady);
    }

    private static boolean isRepositoryReady(String repositoryName) {
        Path repositoryPath = MOCK_BASE_PATH.resolve(repositoryName);
        Path gitDirectory = repositoryPath.resolve(".git");
        return Files.isDirectory(repositoryPath) && Files.isDirectory(gitDirectory);
    }

    private static void cleanupExistingRepositories() throws IOException {
        if (Files.exists(MOCK_BASE_PATH)) {
            LOGGER.info("Cleaning up existing mock repositories...");
            deleteDirectory();
        }
        Files.createDirectories(MOCK_BASE_PATH);
    }

    private static void createMockRepositories() throws Exception {
        try (ExecutorService executor = Executors.newFixedThreadPool(3)) {
            List<Future<String>> futures = new ArrayList<>();

            LOGGER.info("Creating repositories in parallel...");

            futures.add(executor.submit(() -> {
                LOGGER.info("Creating Java Backend repository...");
                JavaBackendRepository.create(MOCK_BASE_PATH);
                return "Java Backend";
            }));

            futures.add(executor.submit(() -> {
                LOGGER.info("Creating Python API repository...");
                PythonApiRepository.create(MOCK_BASE_PATH);
                return "Python API";
            }));

            futures.add(executor.submit(() -> {
                LOGGER.info("Creating React Frontend repository...");
                ReactFrontendRepository.create(MOCK_BASE_PATH);
                return "React Frontend";
            }));

            executor.shutdown();

            try {
                for (Future<String> future : futures) {
                    String repositoryName = future.get();
                    LOGGER.info("✓ {} repository completed", repositoryName);
                }

                if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                    LOGGER.error("Repository creation timed out");
                    executor.shutdownNow();
                }
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Error during parallel repository creation", e);
                executor.shutdownNow();
                throw new Exception("Failed to create repositories", e);
            }
        }
    }

    private static void deleteDirectory() throws IOException {
        if (!Files.exists(GitRepositoryInitializer.MOCK_BASE_PATH)) {
            return;
        }

        try {
            Path[] repositoryDirectories;
            try (var stream = Files.list(GitRepositoryInitializer.MOCK_BASE_PATH)) {
                repositoryDirectories = stream
                    .filter(Files::isDirectory)
                    .toArray(Path[]::new);
            }

            for (Path repositoryDirectory : repositoryDirectories) {
                Path gitDirectory = repositoryDirectory.resolve(".git");
                if (Files.exists(gitDirectory)) {
                    try {
                        ProcessBuilder processBuilder = new ProcessBuilder("git", "gc", "--prune=now");
                        processBuilder.directory(repositoryDirectory.toFile());
                        processBuilder.redirectErrorStream(true);
                        Process process = processBuilder.start();
                        process.waitFor();
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Exception ignored) {
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }

        try (var stream = Files.walk(GitRepositoryInitializer.MOCK_BASE_PATH)) {
            stream.sorted(Comparator.reverseOrder())
                .forEach(GitRepositoryInitializer::deleteWithRetry);
        }
    }

    private static void deleteWithRetry(Path path) {
        for (int i = 0; i < 3; i++) {
            try {
                if (Files.exists(path) && !Files.isDirectory(path)) {
                    boolean writable = path.toFile().setWritable(true);
                    if (!writable && !Files.isWritable(path)) {
                        LOGGER.debug("Unable to mark path as writable before deletion: {}", path);
                    }
                }
                Files.delete(path);
                return;
            } catch (IOException e) {
                if (i == 3 - 1) {
                    LOGGER.warn("Failed to delete after {} attempts: {}", 3, path);
                    path.toFile().deleteOnExit();
                } else {
                    try {
                        Thread.sleep(50L * (i + 1));
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    }

    private static void cleanupLocalMockRepositoryClones() {
        if (!Files.isDirectory(LOCAL_REPOSITORIES_PATH)) {
            return;
        }

        for (String repositoryName : EXPECTED_REPOSITORIES) {
            Path localRepositoryPath = LOCAL_REPOSITORIES_PATH.resolve(repositoryName);
            if (!Files.exists(localRepositoryPath)) {
                continue;
            }

            try {
                try (var stream = Files.walk(localRepositoryPath)) {
                    stream.sorted(Comparator.reverseOrder())
                        .forEach(GitRepositoryInitializer::deleteWithRetry);
                }
                LOGGER.info("Removed stale local mock repository clone: {}", localRepositoryPath);
            } catch (IOException e) {
                LOGGER.warn("Failed to remove local mock repository clone: {}", localRepositoryPath, e);
            }
        }
    }
}
