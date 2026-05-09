package com.kalynx.serverlessreviewtool.managers;

import com.kalynx.serverlessreviewtool.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * UserManager - Manages user data
 * Manages the list of users/reviewers and notifies listeners of changes
 */
public class UserManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserManager.class);

    private final Map<String, User> users = new LinkedHashMap<>();

    private final Set<Consumer<List<User>>> listeners = new HashSet<>();

    /** Constructs a UserManager with an empty user list. */
    public UserManager() {
    }

    /**
     * Adds users to the managed list, ignoring any whose username already exists.
     * Notifies listeners if any users were actually added.
     *
     * @param users the users to add
     */
    public void addUsers(List<User> users) {
        boolean changed = false;
        for (User user : users) {
            if (this.users.putIfAbsent(user.getUsername(), user) == null) {
                changed = true;
            }
        }
        if (changed) notifyListeners();
    }

    /**
     * Removes users by username and notifies listeners if any were removed.
     *
     * @param usernames the usernames to remove
     */
    public void removeUsers(String... usernames) {
        boolean changed = false;
        for (String username : usernames) {
            if (this.users.remove(username) != null) {
                changed = true;
            }
        }
        if (changed) notifyListeners();
    }

    /**
     * Removes users from the managed list and notifies listeners.
     *
     * @param users the users to remove
     */
    public void removeUsers(List<User> users) {
        removeUsers(users.stream().map(User::getUsername).toArray(String[]::new));
    }

    /**
     * Returns an unmodifiable copy of the current user list.
     *
     * @return the list of users
     */
    public List<User> getUsers() {
        return List.copyOf(users.values());
    }

    /**
     * Registers a listener that is notified whenever the user list changes.
     * The listener is immediately invoked with the current list upon registration.
     *
     * @param listener the listener to add
     */
    public void addListener(Consumer<List<User>> listener) {

        listeners.add(listener);
        listener.accept(List.copyOf(users.values()));
    }

    /**
     * Removes a previously registered listener.
     *
     * @param listener the listener to remove
     */
    public void removeListener(Consumer<List<User>> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        LOGGER.info("Notifying listeners of user list update: {} users", users.size());
        listeners.forEach(listener -> listener.accept(List.copyOf(users.values())));
    }
}
