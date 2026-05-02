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

    @Override
    public CompletableFuture<Void> initNotesRepository(Path repoPath, String remote, String remoteUrl) {
        return CompletableFuture.runAsync(() -> {
            try {
                Files.createDirectories(repoPath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create repository directory", e);
            }
        }).thenCompose(__ -> executeAsync(repoPath, "git", "init").thenApply(___ -> null))
          .thenCompose(__ -> executeAsync(repoPath, "git", "config", "notes.mergeStrategy", "union").thenApply(___ -> null))
          .thenCompose(__ -> executeAsync(repoPath, "git", "remote", "add", remote, remoteUrl).thenApply(___ -> null))
          .thenCompose(__ -> fetchNotes(repoPath, remote)
              .exceptionally(ex -> {
                  System.out.println("Initial fetch failed (expected for new repositories): " + ex.getMessage());
                  return null;
              }));
    }

    @Override
    public CompletableFuture<Void> removeRepository(Path repoPath) {
        return CompletableFuture.runAsync(() -> {
            try {
                deleteDirectory(repoPath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to remove repository", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> appendToStream(Path repoPath, String streamRef, String entryJson) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Files.createTempFile("git-notes", ".txt");
            } catch (IOException e) {
                throw new RuntimeException("Failed to create temp file", e);
            }
        }).thenCompose(tempFile -> {
            CompletableFuture<Void> future = refExists(repoPath, streamRef)
                .thenCompose(exists -> {
                    if (exists) {
                        return readStream(repoPath, streamRef);
                    } else {
                        return CompletableFuture.completedFuture(new ArrayList<>());
                    }
                })
                .thenCompose(existing -> {
                    existing.add(entryJson);
                    try {
                        Files.writeString(tempFile, String.join("\n", existing));
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to write temp file", e);
                    }
                    return executeAsync(repoPath, "git", "hash-object", "-w", tempFile.toString());
                })
                .thenCompose(blobHash ->
                    executeAsync(repoPath, "git", "update-ref", streamRef, blobHash.trim())
                )
                .thenApply(__ -> null);

            return future.whenComplete((__, ___) -> {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            });
        });
    }

    @Override
    public CompletableFuture<List<String>> readStream(Path repoPath, String streamRef) {
        return refExists(repoPath, streamRef)
            .thenCompose(exists -> {
                if (!exists) {
                    return CompletableFuture.completedFuture(new ArrayList<>());
                }
                return executeAsync(repoPath, "git", "cat-file", "-p", streamRef)
                    .thenApply(content ->
                        Arrays.stream(content.split("\n"))
                            .filter(line -> !line.trim().isEmpty())
                            .collect(Collectors.toList())
                    );
            });
    }

    @Override
    public CompletableFuture<Void> fetchNotes(Path repoPath, String remote) {
        return executeAsync(repoPath, "git", "-c", "notes.mergeStrategy=union", "fetch", remote,
                "+refs/notes/reviews/*:refs/notes/reviews/*")
                .thenApply(__ -> null);
    }

    @Override
    public CompletableFuture<Void> pushNotes(Path repoPath, String remote) {
        return executeAsync(repoPath, "git", "push", remote, "refs/notes/reviews/*")
                .thenApply(__ -> null);
    }

    @Override
    public CompletableFuture<Boolean> refExists(Path repoPath, String ref) {
        return executeAsync(repoPath, "git", "show-ref", "--verify", ref)
            .thenApply(__ -> true)
            .exceptionally(ex -> {
                if (ex.getMessage() != null && ex.getMessage().contains("not a valid ref")) {
                    return false;
                }
                throw new RuntimeException(ex);
            });
    }

    @Override
    public CompletableFuture<List<String>> listReviews(Path repoPath) {
        return executeAsync(repoPath, "git", "for-each-ref", "--format=%(refname)", NotesRef.ROOT)
            .thenApply(output ->
                Arrays.stream(output.split("\n"))
                    .filter(line -> !line.trim().isEmpty())
                    .map(this::extractReviewId)
                    .filter(id -> id != null && !id.isEmpty())
                    .distinct()
                    .collect(Collectors.toList())
            );
    }

    private String extractReviewId(String refPath) {
        if (!refPath.startsWith(NotesRef.ROOT)) {
            return null;
        }

        String remainder = refPath.substring(NotesRef.ROOT.length());
        int slashIndex = remainder.indexOf('/');

        return slashIndex > 0 ? remainder.substring(0, slashIndex) : remainder;
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

        if (Files.isDirectory(path)) {
            try (Stream<Path> stream = Files.list(path)) {
                stream.forEach(child -> {
                    try {
                        deleteDirectory(child);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }

        Files.delete(path);
    }
}

