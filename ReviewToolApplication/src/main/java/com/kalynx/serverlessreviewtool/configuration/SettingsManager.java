package com.kalynx.serverlessreviewtool.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kalynx.serverlessreviewtool.managers.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * SettingsManager - Controller for loading and saving application settings
 * Handles JSON persistence with proper error handling and logging
 */
public class SettingsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsManager.class);

    private final Gson gson;
    private final Path settingsFile;
    private final RepositoryManager repositoryManager;
    private final AppSettings currentSettings;
    private final Set<Consumer<String>> userNameListeners = new HashSet<>();
    private final Set<Runnable> pollingSettingsListeners = new HashSet<>();
    private final Set<Consumer<List<AppSettings.RepositoryConfig>>> repositoryNameListeners = new HashSet<>();

    public SettingsManager(RepositoryManager repositoryManager) {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.repositoryManager = repositoryManager;


        String userHome = System.getProperty("user.home");
        Path appDir = Paths.get(userHome, ".serverless-review-tool");
        this.settingsFile = appDir.resolve("settings.json");

        try {
            Files.createDirectories(appDir);
        } catch (IOException e) {
            LOGGER.error("Failed to create settings directory: {}", appDir, e);
        }

        // Load or create default settings
        this.currentSettings = loadSettings();
    }


    /**
     * Get current settings
     */
    public AppSettings getSettings() {
        return currentSettings;
    }

    /**
     * Load settings from JSON file
     * Returns default settings if file doesn't exist or on error
     */
    public AppSettings loadSettings() {
        File file = settingsFile.toFile();

        if (!file.exists()) {
            LOGGER.info("Settings file not found, using defaults: {}", settingsFile);
            return new AppSettings();
        }

        try (FileReader reader = new FileReader(file)) {
            AppSettings settings = gson.fromJson(reader, AppSettings.class);
            notifyRepositoryNameListeners();
            LOGGER.info("Loaded settings from: {}", settingsFile);
            return settings;
        } catch (Exception e) {
            LOGGER.warn("Failed to load settings from {}, using defaults", settingsFile, e);
            return new AppSettings();
        }
    }

    /**
     * Save current settings to JSON file
     */
    public void saveSettings() {
        try (FileWriter writer = new FileWriter(settingsFile.toFile())) {
            gson.toJson(currentSettings, writer);
            LOGGER.info("Saved settings to: {}", settingsFile);
        } catch (IOException e) {
            LOGGER.error("Failed to save settings: {}", e.getMessage(), e);
        }
    }

    /**
     * Update specific setting and auto-save
     */
    public void updateNotificationServiceUrl(String url) {
        currentSettings.setNotificationServiceUrl(url);
        saveSettings();
    }

    public void updatePollingInterval(int minutes) {
        currentSettings.setPollingIntervalMinutes(minutes);
        saveSettings();
        notifyPollingSettingsListeners();
    }

    public void updateEnablePolling(boolean enable) {
        currentSettings.setEnablePolling(enable);
        saveSettings();
        notifyPollingSettingsListeners();
    }

    public void updateWindowDefaults(int width, int height) {
        currentSettings.getWindow().setDefaultWidth(width);
        currentSettings.getWindow().setDefaultHeight(height);
        saveSettings();
    }

    /**
     * Stores the currently logged-in user identity used by the application.
     *
     * @param userName the logged-in user name
     * @param userEmail the logged-in user email
     */
    public void loginUser(String userName, String userEmail) {
        currentSettings.setLoggedInUserName(userName != null ? userName.trim() : "");
        currentSettings.setLoggedInUserEmail(userEmail != null ? userEmail.trim() : "");
        saveSettings();
        notifyUserNameListeners();
    }

    /**
     * Clears the current logged-in user identity.
     */
    public void logoutUser() {
        currentSettings.setLoggedInUserName("");
        currentSettings.setLoggedInUserEmail("");
        saveSettings();
        notifyUserNameListeners();
    }

    /**
     * Indicates whether a user is currently logged in.
     *
     * @return true when a logged-in user is present
     */
    public boolean isLoggedIn() {
        return !getLoggedInUserName().isEmpty();
    }

    /**
     * Returns the persisted logged-in user name.
     *
     * @return the logged-in user name, or an empty string
     */
    public String getLoggedInUserName() {
        String loggedInUserName = currentSettings.getLoggedInUserName();
        return loggedInUserName != null ? loggedInUserName : "";
    }

    /**
     * Returns the persisted logged-in user email.
     *
     * @return the logged-in user email, or an empty string
     */
    public String getLoggedInUserEmail() {
        String loggedInUserEmail = currentSettings.getLoggedInUserEmail();
        return loggedInUserEmail != null ? loggedInUserEmail : "";
    }

    public void addRepository(AppSettings.RepositoryConfig config) {
        currentSettings.getRepositories().add(config);
        saveSettings();
        repositoryManager.updateRepositories(currentSettings.getRepositories());
        notifyPollingSettingsListeners();
    }

    public void updateRepository(int index, AppSettings.RepositoryConfig updated) {
        currentSettings.getRepositories().set(index, updated);
        saveSettings();
        repositoryManager.updateRepositories(currentSettings.getRepositories());
        notifyPollingSettingsListeners();
    }

    public void removeRepository(int index) {
        currentSettings.getRepositories().remove(index);
        saveSettings();
        repositoryManager.updateRepositories(currentSettings.getRepositories());
        notifyPollingSettingsListeners();
    }

    public String getCurrentUserName() {
        String loggedInUserName = getLoggedInUserName();
        if (!loggedInUserName.isEmpty()) {
            return loggedInUserName;
        }

        String manualName = currentSettings.getUserName();
        if (manualName != null && !manualName.isEmpty()) {
            return manualName;
        }

        return "Unknown User";
    }

    public String getCurrentUserEmail() {
        String loggedInUserEmail = getLoggedInUserEmail();
        if (!loggedInUserEmail.isEmpty()) {
            return loggedInUserEmail;
        }

        String manualEmail = currentSettings.getUserEmail();
        if (manualEmail != null && !manualEmail.isEmpty()) {
            return manualEmail;
        }

        return "";
    }

    public void addUserNameListener(Consumer<String> listener) {
        userNameListeners.add(listener);
        listener.accept(getCurrentUserName());
    }

    public void removeUserNameListener(Consumer<String> listener) {
        userNameListeners.remove(listener);
    }

    private void notifyUserNameListeners() {
        String currentUserName = getCurrentUserName();
        userNameListeners.forEach(listener -> listener.accept(currentUserName));
    }

    public void addPollingSettingsListener(Runnable listener) {
        pollingSettingsListeners.add(listener);
    }

    public void removePollingSettingsListener(Runnable listener) {
        pollingSettingsListeners.remove(listener);
    }

    private void notifyPollingSettingsListeners() {
        pollingSettingsListeners.forEach(Runnable::run);
    }

    public void addRepositoryNameListener(Consumer<List<AppSettings.RepositoryConfig>> listener) {
        repositoryNameListeners.add(listener);
        if (!currentSettings.getRepositories().isEmpty()) {
            listener.accept(currentSettings.getRepositories());
        }
    }

    public void removeRepositoryNameListener(Consumer<List<AppSettings.RepositoryConfig>> listener) {
        repositoryNameListeners.remove(listener);
    }

    private void notifyRepositoryNameListeners() {
        LOGGER.info("Notifying repository name listeners");
        repositoryNameListeners.forEach(listener -> listener.accept(currentSettings.getRepositories()));
    }
}

