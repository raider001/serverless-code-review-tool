package com.kalynx.lwdi;

import java.io.Serial;

public abstract class DependencyInjectionException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;
    
    public DependencyInjectionException(String message) {
        super(message);
    }
}

