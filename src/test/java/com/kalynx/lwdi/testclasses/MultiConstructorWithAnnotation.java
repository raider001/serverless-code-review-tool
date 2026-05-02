package com.kalynx.lwdi.testclasses;

import com.kalynx.lwdi.DI;

public class MultiConstructorWithAnnotation {

    @DI
    public MultiConstructorWithAnnotation(SimpleClassWithAnnotation ignored) {

    }

    public MultiConstructorWithAnnotation(SimpleClassWithoutAnnotation ignored) {

    }
}

