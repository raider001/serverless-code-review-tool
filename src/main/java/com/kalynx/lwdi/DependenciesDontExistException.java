package com.kalynx.lwdi;
public class DependenciesDontExistException extends DependencyInjectionException {
    public DependenciesDontExistException(Class<?> offendingClass, Class<?> requiredDependency) {
        super(offendingClass.getName() + " requires " + requiredDependency.getName() + " to be injected first.");
    }
}
