package com.kalynx.serverlessreviewtool.plugins.defaults;

import com.kalynx.serverlessreviewtool.plugin.UserPlugin;

/**
 * Basic default user plugin that performs no external integration.
 * Validation succeeds when both values are non-empty and equal ignoring case.
 */
public class DefaultUserPlugin extends UserPlugin {

    @Override
    public boolean validateUser(String user, String validationString) {
        return user != null
            && validationString != null
            && !user.isBlank()
            && user.equalsIgnoreCase(validationString);
    }

    @Override
    public void initialize() {

    }
}

