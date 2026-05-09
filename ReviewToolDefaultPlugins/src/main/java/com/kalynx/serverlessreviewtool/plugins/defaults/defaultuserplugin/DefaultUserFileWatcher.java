package com.kalynx.serverlessreviewtool.plugins.defaults.defaultuserplugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Watches a newline-delimited user file and emits added/removed user deltas.
 *
 * <p>Each line must follow the format {@code username,password}. Whitespace around
 * either value is trimmed. Lines that are blank or contain only whitespace are ignored.
 * Lines with no comma are treated as a username with an empty password.
 */
public class DefaultUserFileWatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUserFileWatcher.class);

    private final Path usersFilePath;
    private final BiConsumer<Map<String, String>, Set<String>> onUsersChanged;

    private final Map<String, String> knownUsers = new LinkedHashMap<>();

    private WatchService watchService;

    /**
     * Creates a file watcher for the given users file.
     *
     * @param usersFilePath  path to the users file
     * @param onUsersChanged callback receiving a map of added username-password entries
     *                       and a set of removed usernames
     */
    public DefaultUserFileWatcher(Path usersFilePath, BiConsumer<Map<String, String>, Set<String>> onUsersChanged) {
        this.usersFilePath = usersFilePath;
        this.onUsersChanged = onUsersChanged;
    }

    /**
     * Starts watching the users file for modifications.
     */
    public void start() {
        ensureUsersFileExists(usersFilePath);
        knownUsers.clear();
        processUserFileChange();

        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path parent = usersFilePath.toAbsolutePath().getParent();
            if (parent == null) {
                LOGGER.warn("Cannot watch user file without parent directory: {}", usersFilePath);
                return;
            }

            parent.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE
            );

            Thread watcherThread = new Thread(this::watchLoop, "default-user-plugin-watcher");
            watcherThread.setDaemon(true);
            watcherThread.start();
        } catch (IOException e) {
            LOGGER.error("Failed to start user file watcher for {}", usersFilePath, e);
        }
    }

    private void watchLoop() {
        Path watchedFileName = usersFilePath.getFileName();
        if (watchedFileName == null) {
            return;
        }

        while (!Thread.currentThread().isInterrupted()) {
            try {
                WatchKey key = watchService.take();
                boolean relevantChange = false;

                for (WatchEvent<?> event : key.pollEvents()) {
                    Object context = event.context();
                    if (!(context instanceof Path changed)) {
                        continue;
                    }
                    if (changed.equals(watchedFileName)) {
                        relevantChange = true;
                    }
                }

                key.reset();

                if (relevantChange) {
                    processUserFileChange();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                LOGGER.error("Error while processing user file watch event", e);
            }
        }
    }

    private synchronized void processUserFileChange() {
        Map<String, String> latestUsers = readUsers(usersFilePath);

        Map<String, String> added = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : latestUsers.entrySet()) {
            if (!knownUsers.containsKey(entry.getKey())) {
                added.put(entry.getKey(), entry.getValue());
            } else if (!knownUsers.get(entry.getKey()).equals(entry.getValue())) {
                added.put(entry.getKey(), entry.getValue());
            }
        }

        Set<String> removed = new LinkedHashSet<>(knownUsers.keySet());
        removed.removeAll(latestUsers.keySet());

        if (!added.isEmpty() || !removed.isEmpty()) {
            onUsersChanged.accept(Map.copyOf(added), Set.copyOf(removed));
        }

        knownUsers.clear();
        knownUsers.putAll(latestUsers);
    }

    private void ensureUsersFileExists(Path filePath) {
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(filePath)) {
                Files.writeString(filePath, "", StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize users file: " + filePath, e);
        }
    }

    private Map<String, String> readUsers(Path filePath) {
        if (!Files.exists(filePath)) {
            return Map.of();
        }
        try {
            Map<String, String> users = new LinkedHashMap<>();
            for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",", 2);
                String username = parts[0].trim();
                String password = parts.length > 1 ? parts[1].trim() : "";
                if (!username.isEmpty()) {
                    users.put(username, password);
                }
            }
            return users;
        } catch (IOException e) {
            LOGGER.error("Failed reading users file: {}", filePath, e);
            return Map.of();
        }
    }
}
