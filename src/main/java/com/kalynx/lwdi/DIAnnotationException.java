package com.kalynx.lwdi;
public class DIAnnotationException extends DependencyInjectionException {
    private static final long serialVersionUID = 1L;

    public DIAnnotationException(Class<?> offendingClass) {
        super(offendingClass.getName() + " must have one @DI annotation.");
    }
}
