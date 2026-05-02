package com.kalynx.lwdi;
public class NotAInterfaceException extends DependencyInjectionException {
    public NotAInterfaceException(Class<?> offendingClass) {
        super(offendingClass.getName() + " is not an interface.");
    }
}
