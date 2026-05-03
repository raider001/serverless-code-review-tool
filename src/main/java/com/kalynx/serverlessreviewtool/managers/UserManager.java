package com.kalynx.serverlessreviewtool.managers;

import com.kalynx.serverlessreviewtool.models.User;
import com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog.ReviewFormModels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * UserManager - Manages user data
 * Manages the list of users/reviewers and notifies listeners of changes
 */
public class UserManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserManager.class);

    private List<User> users = new ArrayList<>();

    private final Set<Consumer<List<User>>> listeners = new HashSet<>();

    public UserManager() {
    }

    public void updateUsers(List<User> users) {
        this.users = users;
        notifyListeners();
    }

    public List<User> getUsers() {
        return List.copyOf(users);
    }

    public void addListener(Consumer<List<User>> listener) {

        listeners.add(listener);
        listener.accept(List.copyOf(users));
    }

    public void removeListener(Consumer<List<User>> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        LOGGER.info("Notifying listeners of user list update: {} users", users.size());
        listeners.forEach(listener -> listener.accept(List.copyOf(users)));
    }
}

