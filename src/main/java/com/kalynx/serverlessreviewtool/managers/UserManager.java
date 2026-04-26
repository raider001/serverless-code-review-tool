package com.kalynx.serverlessreviewtool.managers;

import com.kalynx.serverlessreviewtool.models.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * UserManager - Singleton manager for user data
 * Manages the list of users/reviewers and notifies listeners of changes
 */
public class UserManager {

    private static UserManager INSTANCE;

    private List<User> users = new ArrayList<>();

    private final Set<Consumer<List<User>>> listeners = new HashSet<>();

    public static synchronized UserManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UserManager();
        }
        return INSTANCE;
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
    }

    public void removeListener(Consumer<List<User>> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        listeners.forEach(listener -> listener.accept(List.copyOf(users)));
    }
}

