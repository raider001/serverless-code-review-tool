package com.kalynx.serverlessreviewtool.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * SettingsManager - Controller for loading and saving application settings
 * Handles JSON persistence with proper error handling and logging
 */
public class SettingsManager {

    private static SettingsManager instance;
    private final Gson gson;
    private final Path settingsFile;
    private AppSettings currentSettings;

    private SettingsManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        // Store settings in user home directory
        String userHome = System.getProperty("user.home");
        Path appDir = Paths.get(userHome, ".serverless-review-tool");
        this.settingsFile = appDir.resolve("settings.json");

        // Ensure directory exists
        try {
            Files.createDirectories(appDir);
        } catch (IOException e) {
            System.err.println("Failed to create settings directory: " + e.getMessage());
        }

        // Load or create default settings
        this.currentSettings = loadSettings();
    }

    /**
     * Get singleton instance
     */
    public static synchronized SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
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
    private AppSettings loadSettings() {
        File file = settingsFile.toFile();

        if (!file.exists()) {
            System.out.println("Settings file not found, using defaults: " + settingsFile);
            return new AppSettings();
        }

        try (FileReader reader = new FileReader(file)) {
            AppSettings settings = gson.fromJson(reader, AppSettings.class);
            System.out.println("Loaded settings from: " + settingsFile);
            return settings != null ? settings : new AppSettings();
        } catch (Exception e) {
            System.err.println("Failed to load settings: " + e.getMessage());
            return new AppSettings();
        }
    }

    /**
     * Save current settings to JSON file
     */
    public void saveSettings() {
        try (FileWriter writer = new FileWriter(settingsFile.toFile())) {
            gson.toJson(currentSettings, writer);
            System.out.println("Saved settings to: " + settingsFile);
        } catch (IOException e) {
            System.err.println("Failed to save settings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update settings and auto-save
     */
    public void updateSettings(AppSettings settings) {
        this.currentSettings = settings;
        saveSettings();
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
    }

    public void updateEnablePolling(boolean enable) {
        currentSettings.setEnablePolling(enable);
        saveSettings();
    }

    public void updateTheme(String theme) {
        currentSettings.setTheme(theme);
        saveSettings();
    }

    public void updateWindowDefaults(int width, int height) {
        currentSettings.getWindow().setDefaultWidth(width);
        currentSettings.getWindow().setDefaultHeight(height);
        saveSettings();
    }


    /**
     * Get settings file path for debugging
     */
    public String getSettingsFilePath() {
        return settingsFile.toString();
    }
}

