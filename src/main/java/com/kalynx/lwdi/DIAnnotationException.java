package com.kalynx.lwdi;
public class DIAnnotationException extends DependencyInjectionException {
    public DIAnnotationException(Class<?> offendingClass) {
        super(offendingClass.getName() + " must have one @DI annotation.");
    }
}
