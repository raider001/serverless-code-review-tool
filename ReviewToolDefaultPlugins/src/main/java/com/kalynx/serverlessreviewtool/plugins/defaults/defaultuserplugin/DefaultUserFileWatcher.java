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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Watches a newline-delimited user file and emits added/removed user deltas.
 */
public class DefaultUserFileWatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUserFileWatcher.class);

    private final Path usersFilePath;
    private final BiConsumer<Set<String>, Set<String>> onUsersChanged;

    private final Set<String> knownUsers = new LinkedHashSet<>();

    private WatchService watchService;

    /**
     * Creates a file watcher for the given users file.
     *
     * @param usersFilePath   path to the users file
     * @param onUsersChanged  callback receiving added and removed users
     */
    public DefaultUserFileWatcher(Path usersFilePath, BiConsumer<Set<String>, Set<String>> onUsersChanged) {
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
        Set<String> latestUsers = readUsers(usersFilePath);

        Set<String> added = new LinkedHashSet<>(latestUsers);
        added.removeAll(knownUsers);

        Set<String> removed = new LinkedHashSet<>(knownUsers);
        removed.removeAll(latestUsers);

        if (!added.isEmpty() || !removed.isEmpty()) {
            onUsersChanged.accept(Set.copyOf(added), Set.copyOf(removed));
        }

        knownUsers.clear();
        knownUsers.addAll(latestUsers);
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

    private Set<String> readUsers(Path filePath) {
        if (!Files.exists(filePath)) {
            return Set.of();
        }
        try {
            return Files.readAllLines(filePath, StandardCharsets.UTF_8)
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        } catch (IOException e) {
            LOGGER.error("Failed reading users file: {}", filePath, e);
            return Set.of();
        }
    }
}

