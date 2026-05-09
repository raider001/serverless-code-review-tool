package com.kalynx.serverlessreviewtool.plugins.defaults;

import com.kalynx.serverlessreviewtool.plugin.UserPlugin;
import com.kalynx.serverlessreviewtool.plugins.defaults.defaultuserplugin.DefaultUserFileWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Set;

/**
 * Default user plugin backed by a text file where each line is a username.
 * This plugin watches the file for changes and notifies listeners when users
 * are added or removed.
 */
public class DefaultUserPlugin extends UserPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUserPlugin.class);
    private static final String USERS_FILE_PROPERTY = "srt.default.users.file";
    private static final String DEFAULT_USERS_FILE = "users.txt";

    @Override
    public boolean validateUser(String user, String validationString) {
        return true;
    }

    @Override
    public void initialize() {
        Path usersFilePath = resolveUsersFilePath();
        DefaultUserFileWatcher userFileWatcher = new DefaultUserFileWatcher(usersFilePath, this::onUsersChanged);
        userFileWatcher.start();
        LOGGER.info("DefaultUserPlugin initialized using user file: {}", usersFilePath);
    }

    private void onUsersChanged(Set<String> added, Set<String> removed) {
        if (!added.isEmpty()) {
            notifyListeners(NotificationType.USER_ADDED, added.toArray(String[]::new));
            LOGGER.info("Detected added users: {}", added);
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
