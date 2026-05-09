package com.kalynx.serverlessreviewtool.managers;

import com.kalynx.serverlessreviewtool.plugin.PluginRegistry;
import com.kalynx.serverlessreviewtool.plugin.SyntaxHighlighterPlugin;
import com.kalynx.serverlessreviewtool.plugin.UserPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * PluginManager - Application facade for plugin discovery and access.
 * Loads plugins at startup and provides typed access to registered plugins.
 */
public class PluginManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginManager.class);

    private final PluginRegistry pluginRegistry = new PluginRegistry();
    private boolean initialized;

    private final List<Runnable> pendingUserListeners = new ArrayList<>();

    /**
     * Loads plugins from the configured plugins directory.
     * Applies any pre-registered listeners before calling plugin initialize(),
     * ensuring plugins fire initial events to already-attached listeners.
     * Safe to call multiple times; only first call performs loading.
     */
    public synchronized void initialize() {
        if (initialized) {
            return;
        }
        pluginRegistry.load();
        pendingUserListeners.forEach(Runnable::run);
        pendingUserListeners.clear();
        pluginRegistry.initializePlugins();
        initialized = true;
        LOGGER.info("PluginManager initialized");
    }

    /**
     * Registers a listener for user plugin events.
     * If called before {@link #initialize()}, the listener is queued and applied
     * after plugins are discovered but before they are initialized.
     *
     * @param type     the notification type to listen for
     * @param listener the listener to register
     */
    public void addListenerToUserPlugins(UserPlugin.NotificationType type, Consumer<String[]> listener) {
        if (!initialized) {
            pendingUserListeners.add(() ->
                pluginRegistry.getPlugins(UserPlugin.class).forEach(plugin -> plugin.addListener(type, listener))
            );
        } else {
            pluginRegistry.getPlugins(UserPlugin.class).forEach(plugin -> plugin.addListener(type, listener));
        }
    }

    /**
     * Indicates whether any user plugins are currently registered.
     *
     * @return true when at least one user plugin is available
     */
    public boolean hasUserPlugins() {
        return !pluginRegistry.getPlugins(UserPlugin.class).isEmpty();
    }

    /**
     * Validates a user against the registered user plugins.
     *
     * @param user the user to validate
     * @param validationString the validation value supplied by the user
     * @return true when any registered user plugin accepts the identity
     */
    public boolean validateUser(String user, String validationString) {
        return pluginRegistry.getPlugins(UserPlugin.class).stream()
            .anyMatch(plugin -> plugin.validateUser(user, validationString));
    }



    /**
     * Returns the registered syntax highlighter plugin for the given file extension, if any.
     * Each plugin handles exactly one extension, so the match is precise.
     *
     * @param fileExtension lower-case file extension without dot (e.g. {@code "java"})
     * @return optional containing the plugin for this extension, or empty if none registered
     */
    public Optional<SyntaxHighlighterPlugin> getSyntaxHighlighterFor(String fileExtension) {
        return pluginRegistry.getPlugins(SyntaxHighlighterPlugin.class).stream()
            .filter(plugin -> fileExtension.equalsIgnoreCase(plugin.getFileExtension()))
            .findFirst();
    }

    /**
     * Releases plugin classloader resources.
     */
    public synchronized void shutdown() {
        if (!initialized) {
            return;
        }
        pluginRegistry.close();
        initialized = false;
        LOGGER.info("PluginManager shut down");
    }
}

