package com.kalynx.serverlessreviewtool.mockdata.repositories;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class BaseRepository {

    protected static void initGitRepository(Path repoPath) throws IOException, InterruptedException {
        executeGitCommand(repoPath, "git", "init");
        executeGitCommand(repoPath, "git", "config", "user.name", "Mock User");
        executeGitCommand(repoPath, "git", "config", "user.email", "mock@example.com");
        executeGitCommand(repoPath, "git", "config", "commit.gpgsign", "false");
    }

    protected static void executeGitCommand(Path workingDir, String... command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workingDir.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String errorMsg = "Git command failed with exit code " + exitCode + ": " + String.join(" ", command);
            if (output.length() > 0) {
                errorMsg += "\nOutput: " + output.toString();
            }
            throw new IOException(errorMsg);
        }
    }

    protected static void commitFile(Path repoPath, String filePath, String commitMessage) throws IOException, InterruptedException {
        executeGitCommand(repoPath, "git", "add", filePath);

        // Check if there are changes to commit
        ProcessBuilder pb = new ProcessBuilder("git", "diff", "--cached", "--quiet");
        pb.directory(repoPath.toFile());
        Process process = pb.start();
        int exitCode = process.waitFor();

        // If exitCode is 0, there are no changes; if 1, there are changes
        if (exitCode == 1) {
            executeGitCommand(repoPath, "git", "commit", "-m", commitMessage);
        } else {
            System.out.println("    Skipping commit (no changes): " + commitMessage);
        }
    }

    protected static void addGitNote(Path repoPath, String commitRef, String noteRef, String noteContent) throws IOException, InterruptedException {
        Path tempFile = Files.createTempFile("note", ".txt");
        try {
            Files.writeString(tempFile, noteContent);
            executeGitCommand(repoPath, "git", "notes", "--ref=" + noteRef, "add", "-F", tempFile.toString(), commitRef);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    protected static String getLastCommitHash(Path repoPath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("git", "rev-parse", "HEAD");
        pb.directory(repoPath.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Failed to get last commit hash");
        }

        return output.toString().trim();
    }
}

