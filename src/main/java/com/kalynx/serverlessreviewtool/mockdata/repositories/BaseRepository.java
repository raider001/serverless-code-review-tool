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
            if (!output.isEmpty()) {
                errorMsg += "\nOutput: " + output;
            }
            throw new IOException(errorMsg);
        }
    }

    protected static void commitFile(Path repoPath, String filePath, String commitMessage) throws IOException, InterruptedException {
        executeGitCommand(repoPath, "git", "add", filePath);

        ProcessBuilder pb = new ProcessBuilder("git", "diff", "--cached", "--quiet");
        pb.directory(repoPath.toFile());
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode == 1) {
            executeGitCommand(repoPath, "git", "commit", "-m", commitMessage);
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

    protected static String getRootCommitHash(Path repoPath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("git", "rev-list", "--max-parents=0", "HEAD");
        pb.directory(repoPath.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
                break;
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Failed to get root commit hash");
        }

        return output.toString().trim();
    }

    protected static void createBranch(Path repoPath, String branchName) throws IOException, InterruptedException {
        executeGitCommand(repoPath, "git", "branch", branchName);
    }

    protected static void checkoutBranch(Path repoPath, String branchName) throws IOException, InterruptedException {
        executeGitCommand(repoPath, "git", "checkout", branchName);
    }

    protected static void createAndCheckoutBranch(Path repoPath, String branchName) throws IOException, InterruptedException {
        executeGitCommand(repoPath, "git", "checkout", "-b", branchName);
    }

    protected static void checkoutMain(Path repoPath) throws IOException, InterruptedException {
        try {
            executeGitCommand(repoPath, "git", "checkout", "main");
        } catch (IOException e) {
            executeGitCommand(repoPath, "git", "checkout", "master");
        }
    }

    protected static void createInitialStructure(Path repoPath) throws IOException, InterruptedException {
        Path srcDir = repoPath.resolve("src");
        Files.createDirectories(srcDir);

        Path readmePath = repoPath.resolve("README.md");
        Files.writeString(readmePath, "# " + repoPath.getFileName() + "\n\nMock repository for testing.\n");
        commitFile(repoPath, "README.md", "Initial commit");

        executeGitCommand(repoPath, "git", "branch", "-M", "main");
    }
}

