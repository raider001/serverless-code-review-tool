package com.kalynx.serverlessreviewtool.mockdata;

import com.kalynx.serverlessreviewtool.managers.UserManager;
import com.kalynx.serverlessreviewtool.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserMockData {

    public static void loadMockData(UserManager userManager) {
        List<User> mockUsers = createMockUsers();
        userManager.updateUsers(mockUsers);
    }

    private static List<User> createMockUsers() {
        List<User> users = new ArrayList<>();

        users.add(new User("John Doe", "john.doe@example.com", "jdoe"));
        users.add(new User("Jane Smith", "jane.smith@example.com", "jsmith"));
        users.add(new User("Bob Johnson", "bob.johnson@example.com", "bjohnson"));
        users.add(new User("Eve Anderson", "eve.anderson@example.com", "eanderson"));
        users.add(new User("Michael Scott", "michael.scott@example.com", "mscott"));
        users.add(new User("Sarah Connor", "sarah.connor@example.com", "sconnor"));
        users.add(new User("James Bond", "james.bond@example.com", "jbond"));
        users.add(new User("Tony Stark", "tony.stark@example.com", "tstark"));

        return users;
    }

    public static void refreshMockData(UserManager userManager) {
        loadMockData(userManager);
        System.out.println("Mock user data refreshed");
    }
}

