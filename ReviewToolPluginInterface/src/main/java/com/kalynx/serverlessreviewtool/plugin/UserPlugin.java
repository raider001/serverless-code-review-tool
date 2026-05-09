package com.kalynx.serverlessreviewtool.plugin;

/**
 * Base class for user management plugins.
 * Extend this class to provide user lookup and validation from an external service.
 *
 * <p>The tool falls back to raw git identity if no UserPlugin is registered.
 */
public abstract class UserPlugin extends Notifier<String, UserPlugin.NotificationType>
    implements Plugin {

    public enum NotificationType {
        USER_ADDED, USER_REMOVED
    }


    /**
     * Validates a user against a validation string provided by the external service.
     * The meaning of the validation string is determined by the implementing service
     * (e.g. an email address, token, or unique identifier).
     *
     * @param user             the git username to validate
     * @param validationString the value to validate the user against
     * @return true if the user is considered valid, false otherwise
     */
    public abstract boolean validateUser(String user, String validationString);
    
}

 

