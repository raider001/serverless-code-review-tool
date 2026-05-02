package com.kalynx.lwdi;

public abstract class DependencyInjectionException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public DependencyInjectionException(String message) {
        super(message);
    }
}

