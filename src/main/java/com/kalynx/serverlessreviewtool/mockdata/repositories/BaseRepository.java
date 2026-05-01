package com.kalynx.serverlessreviewtool.mockdata.repositories;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Git command failed with exit code " + exitCode + ": " + String.join(" ", command));
        }
    }

    protected static void commitFile(Path repoPath, String filePath, String commitMessage) throws IOException, InterruptedException {
        executeGitCommand(repoPath, "git", "add", filePath);
        executeGitCommand(repoPath, "git", "commit", "-m", commitMessage);
    }
}

