package com.kalynx.serverlessreviewtool.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * AppSettings - POJO representing all application settings
 * Serialized to/from JSON for persistence
 */
public class AppSettings {

    // Window settings
    private WindowSettings window;

    // Notification service settings
    private String notificationServiceUrl;

    // Repository configurations
    private List<RepositoryConfig> repositories;

    // Polling settings
    private int pollingIntervalMinutes;
    private boolean enablePolling;

    // Theme settings
    private String theme; // "Dark" or "Light"

    public AppSettings() {
        // Default values
        this.window = new WindowSettings();
        this.notificationServiceUrl = "";
        this.repositories = new ArrayList<>();
        this.pollingIntervalMinutes = 15;
        this.enablePolling = true;
        this.theme = "Dark";
    }

    // Getters and Setters
    public WindowSettings getWindow() { return window; }
    public void setWindow(WindowSettings window) { this.window = window; }

    public String getNotificationServiceUrl() { return notificationServiceUrl; }
    public void setNotificationServiceUrl(String url) { this.notificationServiceUrl = url; }

    public List<RepositoryConfig> getRepositories() { return repositories; }
    public void setRepositories(List<RepositoryConfig> repositories) { this.repositories = repositories; }

    public int getPollingIntervalMinutes() { return pollingIntervalMinutes; }
    public void setPollingIntervalMinutes(int minutes) { this.pollingIntervalMinutes = minutes; }

    public boolean isEnablePolling() { return enablePolling; }
    public void setEnablePolling(boolean enable) { this.enablePolling = enable; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    /**
     * Window settings - size and position
     */
    public static class WindowSettings {
        private int defaultWidth;
        private int defaultHeight;

        public WindowSettings() {
            // Default window size
            this.defaultWidth = 1000;
            this.defaultHeight = 700;
        }

        public int getDefaultWidth() { return defaultWidth; }
        public void setDefaultWidth(int width) { this.defaultWidth = width; }

        public int getDefaultHeight() { return defaultHeight; }
        public void setDefaultHeight(int height) { this.defaultHeight = height; }
    }

    /**
     * Repository configuration
     */
    public static class RepositoryConfig {
        private String name;
        private String url;
        private int pollingIntervalMinutes;

        public RepositoryConfig() {
            // Default constructor for Gson
        }

        public RepositoryConfig(String name, String url, int pollingIntervalMinutes) {
            this.name = name;
            this.url = url;
            this.pollingIntervalMinutes = pollingIntervalMinutes;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public int getPollingIntervalMinutes() { return pollingIntervalMinutes; }
        public void setPollingIntervalMinutes(int minutes) { this.pollingIntervalMinutes = minutes; }

        @Override
        public String toString() {
            return name + " (" + url + ")";
        }
    }
}

