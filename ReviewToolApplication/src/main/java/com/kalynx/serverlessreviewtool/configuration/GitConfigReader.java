package com.kalynx.serverlessreviewtool.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class GitConfigReader {
    private static final Logger logger = LoggerFactory.getLogger(GitConfigReader.class);

    public static String getUserName() {
        return getGitConfig("user.name");
    }

    public static String getUserEmail() {
        return getGitConfig("user.email");
    }

    private static String getGitConfig(String key) {
        try {
            Process process = new ProcessBuilder("git", "config", "--global", key)
                .redirectErrorStream(true)
                .start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String value = reader.readLine();

                int exitCode = process.waitFor();
                if (exitCode == 0 && value != null) {
                    return value.trim();
                }
            }
        } catch (Exception e) {
            logger.debug("Could not read git config for {}: {}", key, e.getMessage());
        }
        return null;
    }

    public static boolean isGitConfigured() {
        String name = getUserName();
        return name != null && !name.isEmpty();
    }
}

