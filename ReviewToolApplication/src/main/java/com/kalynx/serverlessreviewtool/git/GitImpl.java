package com.kalynx.serverlessreviewtool.git;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(GitImpl.class);

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

        return isValidGitRepository(repoPath)
            .thenCompose(isValid -> {
                if (isValid) {
                    return ensureRemoteConfigured(repoPath, remoteUrl)
                        .thenCompose(ignored -> detachHead(repoPath));
                } else {
                    return prepareRepositoryPathForClone(repoPath)
                        .thenCompose(ignored -> executeAsync(gitLocalPath, "git", "clone", remoteUrl, repoName))
                        .thenCompose(ignored -> detachHead(repoPath));
                }
            })
            .thenCompose(ignored -> configureNotesMergeStrategy(repoPath))
            .thenCompose(ignored -> fetchAllBranches(repoPath))
            .thenCompose(ignored -> fetchNotes(repoPath));
    }

    private CompletableFuture<Void> prepareRepositoryPathForClone(Path repoPath) {
        return CompletableFuture.runAsync(() -> {
            if (!Files.exists(repoPath)) {
                return;
            }

            try {
                deleteDirectory(repoPath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to remove invalid repository directory: " + repoPath, e);
            }
        });
    }

    private CompletableFuture<Void> detachHead(Path repoPath) {
        return executeAsync(repoPath, "git", "checkout", "--detach")
            .exceptionally(ex -> {
                logger.warn("Failed to detach HEAD: {}", ex.getMessage());
                return "";
            })
            .thenApply(ignored -> null);
    }

    private CompletableFuture<Void> fetchAllBranches(Path repoPath) {
        return executeAsync(repoPath, "git", "fetch", ORIGIN, "+refs/heads/*:refs/heads/*")
            .exceptionally(ex -> {
                if (ex.getMessage() != null && ex.getMessage().contains("refusing to fetch into branch")) {
                    return "NEEDS_DETACH";
                }
                logger.warn("Failed to fetch all branches: {}", ex.getMessage());
                return "";
            })
            .thenCompose(result -> {
                if ("NEEDS_DETACH".equals(result)) {
                    return detachHead(repoPath)
                        .thenCompose(ignored -> executeAsync(repoPath, "git", "fetch", ORIGIN, "+refs/heads/*:refs/heads/*"))
                        .exceptionally(ex -> {
                            logger.warn("Failed to fetch all branches after detach: {}", ex.getMessage());
                            return "";
                        });
                }
                return CompletableFuture.completedFuture(result);
            })
            .thenApply(ignored -> null);
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
        return fetchAllBranches(repoPath)
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

    private CompletableFuture<Void> ensureRemoteConfigured(Path repoPath, String remoteUrl) {
        return executeAsync(repoPath, "git", "remote", "get-url", ORIGIN)
            .handle((currentUrl, ex) -> {
                if (ex != null) {
                    if (ex.getMessage() != null && ex.getMessage().contains("No such remote")) {
                        return executeAsync(repoPath, "git", "remote", "add", ORIGIN, remoteUrl);
                    }
                    throw new RuntimeException(ex);
                } else {
                    if (currentUrl.trim().equals(remoteUrl)) {
                        return CompletableFuture.<String>completedFuture(null);
                    } else {
                        return executeAsync(repoPath, "git", "remote", "set-url", ORIGIN, remoteUrl);
                    }
                }
            })
            .thenCompose(future -> future)
            .thenApply(ignored -> null);
    }

    private CompletableFuture<Boolean> isValidGitRepository(Path repoPath) {
        if (!Files.exists(repoPath) || !Files.isDirectory(repoPath)) {
            return CompletableFuture.completedFuture(false);
        }

        Path gitDir = repoPath.resolve(".git");
        if (!Files.exists(gitDir) || !Files.isDirectory(gitDir)) {
            return CompletableFuture.completedFuture(false);
        }

        return executeAsync(repoPath, "git", "rev-parse", "--git-dir")
            .thenApply(output -> !output.trim().isEmpty())
            .exceptionally(_ -> false);
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
            .thenCompose(ignored -> mergeAllNotes(repository))
            .exceptionally(ex -> {
                if (ex.getMessage() != null && ex.getMessage().contains("Couldn't find remote ref")) {
                    return null;
                }
                logger.warn("Failed to fetch notes: {}", ex.getMessage());
                return null;
            });
    }

    private CompletableFuture<Void> mergeAllNotes(Path repository) {
        return executeAsync(repository, "git", "for-each-ref", "--format=%(refname)", "refs/notes/")
            .thenCompose(output -> {
                List<String> noteRefs = Arrays.stream(output.split("\n"))
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .toList();

                if (noteRefs.isEmpty()) {
                    return CompletableFuture.completedFuture(null);
                }

                List<CompletableFuture<Void>> mergeFutures = noteRefs.stream()
                    .map(ref -> executeAsync(repository, "git", "notes", "--ref=" + ref, "merge", "-s", "union", "origin/" + ref)
                        .thenApply(ignored -> (Void) null)
                        .exceptionally(mergeEx -> {
                            String msg = mergeEx.getMessage();
                            if (msg != null && (msg.contains("No notes to merge") ||
                                               msg.contains("Already up to date") ||
                                               msg.contains("not found"))) {
                                return null;
                            }
                            logger.warn("Failed to merge notes for {}: {}", ref, msg);
                            return null;
                        }))
                    .toList();

                if (mergeFutures.isEmpty()) {
                    return CompletableFuture.completedFuture(null);
                }

                return CompletableFuture.allOf(mergeFutures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> {});
            })
            .exceptionally(ex -> {
                logger.warn("Failed to list note refs: {}", ex.getMessage());
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



    @Override
    public CompletableFuture<String> executeAsync(String repository, String... args) {
        Path repoPath = gitLocalPath.resolve(repository);

        if (!Files.exists(repoPath)) {
            return CompletableFuture.failedFuture(
                new RuntimeException("Repository not found: " + repository)
            );
        }

        String[] command = new String[args.length + 1];
        command[0] = "git";
        System.arraycopy(args, 0, command, 1, args.length);

        return executeAsync(repoPath, command);
    }

    private CompletableFuture<String> executeAsync(Path workingDir, String... command) {
        if (!Files.exists(workingDir) || !Files.isDirectory(workingDir)) {
            return CompletableFuture.failedFuture(
                new RuntimeException("Working directory not found: " + workingDir)
            );
        }

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
                        logger.warn("Could not set file writable: {}", path);
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

    @Override
    public CompletableFuture<List<String>> listBranches(String repository) {
        Path repoPath = gitLocalPath.resolve(repository);
        return executeAsync(repoPath, "git", "branch", "-r", "--format=%(refname:short)")
            .thenApply(output -> Arrays.stream(output.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .map(line -> line.replace("origin/", ""))
                .distinct()
                .collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<String> getDefaultBranch(String repository) {
        Path repoPath = gitLocalPath.resolve(repository);
        return executeAsync(repoPath, "git", "symbolic-ref", "refs/remotes/origin/HEAD")
            .thenApply(output -> {
                String ref = output.trim();
                if (ref.startsWith("refs/remotes/origin/")) {
                    return ref.substring("refs/remotes/origin/".length());
                }
                return ref;
            })
            .exceptionally(_ -> executeAsync(repoPath, "git", "rev-parse", "--abbrev-ref", "origin/HEAD")
                .thenApply(output -> {
                    String ref = output.trim();
                    if (ref.startsWith("origin/")) {
                        return ref.substring("origin/".length());
                    }
                    return ref;
                })
                .exceptionally(_ -> "main")
                .join());
    }

    @Override
    public CompletableFuture<List<String>> listCommits(String repository, String ref, int maxCount) {
        Path repoPath = gitLocalPath.resolve(repository);
        String format = "%H|%an|%ai|%s";
        return executeAsync(repoPath, "git", "log", ref, "--format=" + format, "-n", String.valueOf(maxCount))
            .thenApply(output -> Arrays.stream(output.split("\n"))
                .filter(line -> !line.trim().isEmpty())
                .collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<List<String>> listChangedFiles(String repository, String fromCommit, String toCommit) {
        Path repoPath = gitLocalPath.resolve(repository);
        return executeAsync(repoPath, "git", "diff", "--name-status", fromCommit, toCommit)
            .thenApply(output -> Arrays.stream(output.split("\n"))
                .filter(line -> !line.trim().isEmpty())
                .collect(Collectors.toList()));
    }
}

