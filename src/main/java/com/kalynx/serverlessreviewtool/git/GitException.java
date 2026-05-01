package com.kalynx.serverlessreviewtool.git;

/**
 * GitException indicates a git operation has failed.
 */
public class GitException extends Exception {

    /**
     * Creates a GitException with a message.
     *
     * @param message error description
     */
    public GitException(String message) {
        super(message);
    }

    /**
     * Creates a GitException with a message and cause.
     *
     * @param message error description
     * @param cause   underlying exception
     */
    public GitException(String message, Throwable cause) {
        super(message, cause);
    }
}

