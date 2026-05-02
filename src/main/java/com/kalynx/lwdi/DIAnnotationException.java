package com.kalynx.lwdi;

import java.io.Serial;
public class DIAnnotationException extends DependencyInjectionException {
    @Serial
    private static final long serialVersionUID = 1L;

    public DIAnnotationException(Class<?> offendingClass) {
        super(offendingClass.getName() + " must have one @DI annotation.");
    }
}
