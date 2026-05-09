package com.kalynx.serverlessreviewtool.git;

import java.io.Serial;

/**
 * GitException indicates a git operation has failed.
 */
public class GitException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

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

