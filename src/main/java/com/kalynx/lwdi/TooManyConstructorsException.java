package com.kalynx.lwdi;

public class TooManyConstructorsException extends DependencyInjectionException {

    public TooManyConstructorsException(Class<?> offendingClass) {
        super(offendingClass.getName() + " has too many constructors. Use @DI annotation to determine which constructor to use.");
    }
}

