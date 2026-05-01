package com.kalynx.serverlessreviewtool.git;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class GitImpl implements Git {

    @Override
    public void initNotesRepository(Path repoPath, String remote, String remoteUrl) throws GitException {
        try {
            Files.createDirectories(repoPath);
        } catch (IOException e) {
            throw new GitException("Failed to create repository directory", e);
        }

        executeSync(repoPath, "git", "init");
        executeSync(repoPath, "git", "config", "notes.mergeStrategy", "union");
        executeSync(repoPath, "git", "remote", "add", remote, remoteUrl);

        try {
            fetchNotes(repoPath, remote);
        } catch (GitException e) {
            System.out.println("Initial fetch failed (expected for new repositories): " + e.getMessage());
        }
    }

    @Override
    public void removeRepository(Path repoPath) throws GitException {
        try {
            deleteDirectory(repoPath);
        } catch (IOException e) {
            throw new GitException("Failed to remove repository", e);
        }
    }

    @Override
    public void appendToStream(Path repoPath, String streamRef, String entryJson) throws GitException {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("git-notes", ".txt");

            List<String> existing = new ArrayList<>();
            if (refExists(repoPath, streamRef)) {
                existing = readStream(repoPath, streamRef);
            }

            existing.add(entryJson);
            Files.write(tempFile, String.join("\n", existing).getBytes(StandardCharsets.UTF_8));

            executeSync(repoPath, "git", "hash-object", "-w", tempFile.toString());

            String blobHash = executeSync(repoPath, "git", "hash-object", "-w", tempFile.toString()).trim();

            executeSync(repoPath, "git", "update-ref", streamRef, blobHash);

        } catch (IOException e) {
            throw new GitException("Failed to append to stream", e);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public List<String> readStream(Path repoPath, String streamRef) throws GitException {
        if (!refExists(repoPath, streamRef)) {
            return new ArrayList<>();
        }

        String content = executeSync(repoPath, "git", "cat-file", "-p", streamRef);

        return Arrays.stream(content.split("\n"))
                .filter(line -> !line.trim().isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public void fetchNotes(Path repoPath, String remote) throws GitException {
        executeSync(repoPath, "git", "-c", "notes.mergeStrategy=union", "fetch", remote,
                "+refs/notes/reviews/*:refs/notes/reviews/*");
    }

    @Override
    public void pushNotes(Path repoPath, String remote) throws GitException {
        executeSync(repoPath, "git", "push", remote, "refs/notes/reviews/*");
    }

    @Override
    public boolean refExists(Path repoPath, String ref) throws GitException {
        try {
            executeSync(repoPath, "git", "show-ref", "--verify", ref);
            return true;
        } catch (GitException e) {
            if (e.getMessage().contains("not a valid ref")) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public List<String> listReviews(Path repoPath) throws GitException {
        String output = executeSync(repoPath, "git", "for-each-ref", "--format=%(refname)", NotesRef.ROOT);

        return Arrays.stream(output.split("\n"))
                .filter(line -> !line.trim().isEmpty())
                .map(this::extractReviewId)
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    private String extractReviewId(String refPath) {
        if (!refPath.startsWith(NotesRef.ROOT)) {
            return null;
        }

        String remainder = refPath.substring(NotesRef.ROOT.length());
        int slashIndex = remainder.indexOf('/');

        return slashIndex > 0 ? remainder.substring(0, slashIndex) : remainder;
    }

    private String executeSync(Path workingDir, String... command) throws GitException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();

        ProcessUtils.runProcess(command)
                .workingDirectory(workingDir)
                .timeout(Duration.ofSeconds(30))
                .onSuccess(output -> {
                    result.set(output);
                    latch.countDown();
                })
                .onFailure(stderr -> {
                    error.set(stderr);
                    latch.countDown();
                })
                .onTimeout(() -> {
                    error.set("Command timed out: " + String.join(" ", command));
                    latch.countDown();
                })
                .runAsync();

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GitException("Command interrupted", e);
        }

        if (error.get() != null) {
            throw new GitException("Git command failed: " + String.join(" ", command) + "\n" + error.get());
        }

        return result.get();
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



