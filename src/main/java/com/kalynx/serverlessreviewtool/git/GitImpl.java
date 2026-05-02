package com.kalynx.serverlessreviewtool.git;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * GitImpl provides an asynchronous implementation of the Git interface using CompletableFuture.
 * All Git commands are executed via ProcessUtils async API in a separate process.
 * This class is responsible for managing Git repositories, including initializing repositories,
 * fetching and pushing notes, and managing references.
 */
public class GitImpl implements Git {

    public static final String ORIGIN = "origin";
    private final Path gitLocalPath;

    public GitImpl(Path gitLocalPath) {
        this.gitLocalPath = gitLocalPath;
        initialize();
    }

    private void initialize() {
        if (gitLocalPath.toFile().exists()) {
            Path markerFile = gitLocalPath.resolve(".serverlessreviewtool");
            if (!Files.exists(markerFile)) {
                throw new RuntimeException("Directory exists but is not a valid ServerlessReviewTool directory. " +
                        "Missing .serverlessreviewtool marker file in: " + gitLocalPath);
            }
        } else {
            try {
                Files.createDirectories(gitLocalPath);
                Path markerFile = gitLocalPath.resolve(".serverlessreviewtool");
                Files.createFile(markerFile);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create Git local path directory", e);
            }
        }
    }

    @Override
    public CompletableFuture<Void> cloneRepository(String remoteUrl) {
        String repoName = extractRepoName(remoteUrl);
        Path repoPath = gitLocalPath.resolve(repoName);

        return executeAsync(gitLocalPath, "git", "clone", remoteUrl, repoName)
            .thenCompose(ignored -> configureNotesMergeStrategy(repoPath))
            .thenCompose(ignored -> fetchNotes(repoPath));
    }

    @Override
    public CompletableFuture<Void> removeRepository(String repo) {
        Path repoPath = gitLocalPath.resolve(repo);
        return CompletableFuture.runAsync(() -> {
            try {
                deleteDirectory(repoPath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete repository: " + repoPath, e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> pull(String repository) {
        Path repoPath = gitLocalPath.resolve(repository);
        return executeAsync(repoPath, "git", "pull", ORIGIN)
            .thenCompose(ignored -> fetchNotes(repoPath));
    }

    public CompletableFuture<Void> fetch(String repository) {
        Path repoPath = gitLocalPath.resolve(repository);
        return executeAsync(repoPath, "git", "fetch", ORIGIN)
            .thenCompose(ignored -> fetchNotes(repoPath));
    }

    @Override
    public CompletableFuture<Void> pushNotes(String repository, List<String> notes) {
        Path repoPath = gitLocalPath.resolve(repository);
        if (notes == null || notes.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        List<CompletableFuture<String>> pushFutures = notes.stream()
            .map(noteRef -> executeAsync(repoPath, "git", "push", ORIGIN, noteRef))
            .toList();

        return CompletableFuture.allOf(pushFutures.toArray(new CompletableFuture[0]))
            .thenApply(ignored -> null);
    }

    @Override
    public CompletableFuture<Void> appendToNotes(String repository, String note, String data) {
        Path repoPath = gitLocalPath.resolve(repository);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return Files.createTempFile("git-notes", ".txt");
            } catch (IOException e) {
                throw new RuntimeException("Failed to create temp file", e);
            }
        }).thenCompose(tempFile -> {
            CompletableFuture<Void> future = refExists(repoPath, note)
                .thenCompose(exists -> {
                    if (exists) {
                        return readNoteContent(repoPath, note);
                    } else {
                        return CompletableFuture.completedFuture(new ArrayList<>());
                    }
                })
                .thenCompose(existing -> {
                    existing.add(data);
                    try {
                        Files.writeString(tempFile, String.join("\n", existing));
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to write temp file", e);
                    }
                    return executeAsync(repoPath, "git", "hash-object", "-w", tempFile.toString());
                })
                .thenCompose(blobHash ->
                    executeAsync(repoPath, "git", "update-ref", note, blobHash.trim())
                )
                .thenApply(ignored -> null);

            return future.whenComplete((ignored1, ignored2) -> {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            });
        });
    }

    private CompletableFuture<Boolean> refExists(Path repoPath, String ref) {
        return executeAsync(repoPath, "git", "show-ref", "--verify", ref)
            .thenApply(ignored -> true)
            .exceptionally(ex -> {
                if (ex.getMessage() != null && ex.getMessage().contains("not a valid ref")) {
                    return false;
                }
                throw new RuntimeException(ex);
            });
    }

    private CompletableFuture<List<String>> readNoteContent(Path repoPath, String noteRef) {
        return executeAsync(repoPath, "git", "cat-file", "-p", noteRef)
            .thenApply(content ->
                Arrays.stream(content.split("\n"))
                    .filter(line -> !line.trim().isEmpty())
                    .collect(Collectors.toList())
            );
    }

    private CompletableFuture<Void> configureNotesMergeStrategy(Path repository) {
        return executeAsync(repository, "git", "config", "notes.mergeStrategy", "union")
            .thenApply(ignored -> null);
    }

    private CompletableFuture<Void> fetchNotes(Path repository) {
        return executeAsync(repository, "git", "fetch", ORIGIN, "refs/notes/*:refs/notes/*")
            .thenCompose(ignored -> mergeNotes(repository))
            .exceptionally(ex -> {
                if (ex.getMessage() != null && ex.getMessage().contains("Couldn't find remote ref")) {
                    return null;
                }
                throw new RuntimeException(ex);
            });
    }

    private CompletableFuture<Void> mergeNotes(Path repository) {
        return executeAsync(repository, "git", "notes", "merge", "-s", "union", "refs/notes/commits")
            .thenApply(ignored -> (Void) null)
            .exceptionally(ex -> {
                if (ex.getMessage() != null && 
                    (ex.getMessage().contains("No notes to merge") || 
                     ex.getMessage().contains("Already up to date"))) {
                    return null;
                }
                return null;
            });
    }

    private String extractRepoName(String remoteUrl) {
        String url = remoteUrl;
        if (url.endsWith(".git")) {
            url = url.substring(0, url.length() - 4);
        }

        // Handle both forward slashes (URLs) and backslashes (Windows paths)
        url = url.replace("\\", "/");
        int lastSlash = url.lastIndexOf('/');

        if (lastSlash >= 0) {
            return url.substring(lastSlash + 1);
        }
        return url;
    }



    private CompletableFuture<String> executeAsync(Path workingDir, String... command) {
        CompletableFuture<String> future = new CompletableFuture<>();

        ProcessUtils.runProcess(command)
                .workingDirectory(workingDir)
                .timeout(Duration.ofSeconds(30))
                .onSuccess(future::complete)
                .onFailure(error -> future.completeExceptionally(
                    new RuntimeException("Git command failed: " + String.join(" ", command) + "\n" + error)))
                .onTimeout(() -> future.completeExceptionally(
                    new RuntimeException("Command timed out: " + String.join(" ", command))))
                .runAsync();

        return future;
    }

    private void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        if (Files.isDirectory(path.resolve(".git"))) {
            try {
                ProcessBuilder pb = new ProcessBuilder("git", "gc", "--prune=now");
                pb.directory(path.toFile());
                pb.redirectErrorStream(true);
                Process process = pb.start();
                process.waitFor();
                Thread.sleep(100);
            } catch (Exception ignored) {
            }
        }

        try (Stream<Path> stream = Files.walk(path)) {
            stream.sorted(java.util.Comparator.reverseOrder())
                .forEach(this::deleteWithRetry);
        }
    }

    private void deleteWithRetry(Path path) {
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                if (Files.exists(path) && !Files.isDirectory(path)) {
                    if (!path.toFile().setWritable(true)) {
                        System.err.println("Warning: Could not set file writable: " + path);
                    }
                }
                Files.delete(path);
                return;
            } catch (IOException e) {
                if (i == maxRetries - 1) {
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
}

