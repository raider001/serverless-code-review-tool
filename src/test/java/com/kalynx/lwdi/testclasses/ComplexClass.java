package com.kalynx.lwdi.testclasses;

import com.kalynx.lwdi.DI;

public class ComplexClass {

    @DI
    public ComplexClass(SimpleClassWithAnnotation clz1, SimpleClassWithoutAnnotation clz2) {

    }
}

