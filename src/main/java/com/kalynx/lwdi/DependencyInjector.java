package com.kalynx.lwdi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class DependencyInjector {
    private static final Logger logger = LoggerFactory.getLogger(DependencyInjector.class);
    private final Map<Class<?>, Object> registeredClasses = new HashMap<>();

    public <T> T add(T obj) throws AlreadyAddedException {
        if(registeredClasses.containsKey(obj.getClass())) throw new AlreadyAddedException(obj.getClass());
        registeredClasses.putIfAbsent(obj.getClass(), obj);
        return obj;
    }

    public <T> T add(Class<T> clzInterface, T object) throws DependencyInjectionException {

        if(registeredClasses.get(clzInterface) != null)  throw new AlreadyAddedException(clzInterface);

        if(!clzInterface.isInterface()) {
            throw new NotAInterfaceException(clzInterface);
        }

        registeredClasses.put(clzInterface, object);

        return object;
    }

    public <T, V extends T> T inject(Class<T> clzInterface, Class<V> actual) throws DependencyInjectionException {

        if(registeredClasses.get(clzInterface) != null)  throw new AlreadyAddedException(clzInterface);

        if(!clzInterface.isInterface()) {
            throw new NotAInterfaceException(clzInterface);
        }

        T newObject =  objectBuilder(actual);
        registeredClasses.put(clzInterface,newObject);

        return newObject;
    }

    public <T> T inject(Class<T> clz) throws DependencyInjectionException {
        if(registeredClasses.get(clz) != null)  throw new AlreadyAddedException(clz);
        T newObject =  objectBuilder(clz);
        registeredClasses.put(clz,newObject);
        return newObject;

    }

    private <T> T objectBuilder(Class<T> clz) throws DependencyInjectionException {
        @SuppressWarnings("unchecked")
        List<Constructor<T>> constructors  = Arrays.stream((Constructor<T>[])clz.getConstructors()).filter(ctr -> ctr.getAnnotation(DI.class) != null).collect(Collectors.toList());


        if(constructors.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<Constructor<T>> allConstructors = Arrays.stream((Constructor<T>[]) clz.getConstructors()).toList();
            constructors = allConstructors;
            if(constructors.size() > 1) throw new TooManyConstructorsException(clz);
        }

        if(constructors.size() != 1) throw new DIAnnotationException(clz);

        Constructor<T> selectedCtor = constructors.getFirst();
        Object[] params = new Object[selectedCtor.getParameterTypes().length];

        for(int i = 0; i < selectedCtor.getParameterTypes().length; i++) {
            Class<?> currentParam = selectedCtor.getParameterTypes()[i];
            params[i] = registeredClasses.get(currentParam);

            if(params[i] == null) throw new DependenciesDontExistException(clz, selectedCtor.getParameterTypes()[i]);

        }
        try {
            return selectedCtor.newInstance(params);

        } catch (InstantiationException|IllegalAccessException|InvocationTargetException e) {
            logger.error("Failed to instantiate class {}: {}", clz.getName(), e.getMessage(), e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getDependency(Class<T> dependency) {
        return (T) registeredClasses.get(dependency);
    }

}

