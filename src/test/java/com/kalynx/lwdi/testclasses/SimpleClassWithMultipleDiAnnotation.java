package com.kalynx.lwdi.testclasses;

import com.kalynx.lwdi.DI;

public class SimpleClassWithMultipleDiAnnotation {

    @DI
    public SimpleClassWithMultipleDiAnnotation() {

    }

    @DI
    public SimpleClassWithMultipleDiAnnotation(SimpleClassWithoutAnnotation a) {

    }
}

