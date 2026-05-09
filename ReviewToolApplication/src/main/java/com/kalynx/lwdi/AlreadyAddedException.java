package com.kalynx.lwdi;

public class AlreadyAddedException extends DependencyInjectionException {

    public AlreadyAddedException(Class<?> offendingClass) {
        super(offendingClass.getName() + " has already been added to the framework.");
    }
}

