package com.kalynx.serverlessreviewtool.managers;

import com.kalynx.serverlessreviewtool.configuration.AppSettings;
import com.kalynx.serverlessreviewtool.configuration.SettingsManager;
import com.kalynx.serverlessreviewtool.git.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.Timer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PollingService manages automatic periodic synchronization of repositories.
 * Each repository can have its own polling interval, controlled by its configuration.
 * When global polling is enabled, individual timers are created for each repository based on their intervals.
 */
public class PollingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PollingService.class);

    private final Git git;
    private final SettingsManager settingsManager;
    private final ReviewItemManager reviewItemManager;
    private final Map<String, Timer> repositoryTimers = new HashMap<>();

    public PollingService(Git git, SettingsManager settingsManager, ReviewItemManager reviewItemManager) {
        this.git = git;
        this.settingsManager = settingsManager;
        this.reviewItemManager = reviewItemManager;
        setupPollingFromSettings();
        setupSettingsListener();
    }

    private void setupSettingsListener() {
        settingsManager.addPollingSettingsListener(() -> {
            stopAllPolling();
            setupPollingFromSettings();
        });
    }

    private void setupPollingFromSettings() {
        AppSettings settings = settingsManager.getSettings();
        if (settings.isEnablePolling()) {
            startPollingForAllRepositories();
        }
    }

    /**
     * Starts polling for all configured repositories using their individual intervals.
     * If a repository doesn't have a specific interval configured, uses the global default.
     */
    public void startPollingForAllRepositories() {
        stopAllPolling();

        List<AppSettings.RepositoryConfig> repositories = settingsManager.getSettings().getRepositories();
        int globalIntervalMinutes = settingsManager.getSettings().getPollingIntervalMinutes();

        for (AppSettings.RepositoryConfig repo : repositories) {
            int intervalMinutes = repo.getPollingIntervalMinutes() > 0
                ? repo.getPollingIntervalMinutes()
                : globalIntervalMinutes;

            startPollingForRepository(repo, intervalMinutes);
        }

        LOGGER.info("Polling started for {} repositories with individual intervals", repositories.size());
    }

    /**
     * Starts polling for a specific repository at the specified interval.
     *
     * @param repo repository configuration
     * @param intervalMinutes polling interval in minutes
     */
    private void startPollingForRepository(AppSettings.RepositoryConfig repo, int intervalMinutes) {
        String repoName = repo.getName();

        int intervalMillis = intervalMinutes * 60 * 1000;
        Timer timer = new Timer(intervalMillis, ignored -> performSyncForRepository(repo));
        timer.setInitialDelay(0);
        timer.start();

        repositoryTimers.put(repoName, timer);
        LOGGER.info("Polling started for repository: {}", repoName);
    }

    /**
     * Stops all polling timers.
     */
    public void stopAllPolling() {
        if (!repositoryTimers.isEmpty()) {
            for (Map.Entry<String, Timer> entry : repositoryTimers.entrySet()) {
                entry.getValue().stop();
                LOGGER.info("Polling stopped for repository: {}", entry.getKey());
            }
            repositoryTimers.clear();
        }
    }

    /**
     * Performs a sync operation for a specific repository.
     *
     * @param repo repository to sync
     */
    private void performSyncForRepository(AppSettings.RepositoryConfig repo) {
        String repoName = repo.getName();
        String operationId = "automatic-repository-sync-" + repoName;

        LOGGER.info("Performing scheduled sync for repository: {}", repoName);

        RepositorySyncManager syncManager = new RepositorySyncManager(git, List.of(repo));

        syncManager.syncAllRepositories(
            message -> LOGGER.info("Poll sync [{}]: {}", repoName, message),
            operationId
        )
            .thenAccept(result -> {
                if (result.success) {
                    LOGGER.info("Poll sync completed successfully for: {}", repoName);
                    reviewItemManager.refresh();
                } else {
                    LOGGER.error("Poll sync completed with errors for {}: {}", repoName, result.message);
                }
            })
            .exceptionally(error -> {
                LOGGER.error("Poll sync failed for {}: {}", repoName, error.getMessage());
                return null;
            });
    }

    /**
     * Manually triggers a sync operation for all repositories outside of the regular polling schedule.
     */
    public void triggerManualSync() {
        List<AppSettings.RepositoryConfig> repositories = settingsManager.getSettings().getRepositories();
        for (AppSettings.RepositoryConfig repo : repositories) {
            performSyncForRepository(repo);
        }
    }
}

