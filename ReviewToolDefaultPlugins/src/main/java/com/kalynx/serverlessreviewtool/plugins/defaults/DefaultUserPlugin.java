package com.kalynx.serverlessreviewtool.plugins.defaults;

import com.kalynx.serverlessreviewtool.plugin.UserPlugin;
import com.kalynx.serverlessreviewtool.plugins.defaults.defaultuserplugin.DefaultUserFileWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default user plugin backed by a text file where each line is {@code username,password}.
 * This plugin watches the file for changes and notifies listeners when users
 * are added or removed. Validation checks both the username and password.
 */
public class DefaultUserPlugin extends UserPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUserPlugin.class);
    private static final String USERS_FILE_PROPERTY = "srt.default.users.file";
    private static final String DEFAULT_USERS_FILE = "users.txt";

    private final Map<String, String> knownUsers = new ConcurrentHashMap<>();

    @Override
    public boolean validateUser(String user, String validationString) {
        if (user == null) return false;
        String storedPassword = knownUsers.get(user.trim());
        if (storedPassword == null) return false;
        String provided = validationString != null ? validationString : "";
        return storedPassword.equals(provided);
    }

    @Override
    public void initialize() {
        Path usersFilePath = resolveUsersFilePath();
        DefaultUserFileWatcher userFileWatcher = new DefaultUserFileWatcher(usersFilePath, this::onUsersChanged);
        userFileWatcher.start();
        LOGGER.info("DefaultUserPlugin initialized using user file: {}", usersFilePath);
    }

    private void onUsersChanged(Map<String, String> added, Set<String> removed) {
        knownUsers.putAll(added);
        removed.forEach(knownUsers::remove);
        if (!added.isEmpty()) {
            notifyListeners(NotificationType.USER_ADDED, added.keySet().toArray(String[]::new));
            LOGGER.info("Detected added users: {}", added.keySet());
        }
        if (!removed.isEmpty()) {
            notifyListeners(NotificationType.USER_REMOVED, removed.toArray(String[]::new));
            LOGGER.info("Detected removed users: {}", removed);
        }
    }

    private Path resolveUsersFilePath() {
        String configured = System.getProperty(USERS_FILE_PROPERTY, DEFAULT_USERS_FILE);
        return Path.of(configured).toAbsolutePath().normalize();
    }
}
