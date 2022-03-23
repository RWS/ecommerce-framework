package com.sdl.ecommerce.service;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class ListHelper {

    /**
     * Create wrapper list.
     * Is used to create REST wrappers to the different E-Commerce models so they get serialized in a correct way.
     * @param list
     * @param sourceClass
     * @param targetClass
     * @param <S>
     * @param <T>
     * @return
     */
    static public <S, T> List<T> createWrapperList(List<S> list, Class<S> sourceClass, Class<T> targetClass) {
        ArrayList<T> restList = new ArrayList<>();
        if ( list != null ) {
            list.forEach(item -> restList.add(createInstance(targetClass, sourceClass, item)));
        }
        return restList;
    }

    static private <S,T> T createInstance(Class<T> clazz, Class<S> argumentClass, S argument) {
        try {
            Constructor constructor = clazz.getDeclaredConstructor(argumentClass);
            return (T) constructor.newInstance(argument);
        }
        catch ( Exception e ) {
            throw new RuntimeException("Could not create instance of type:" + argumentClass);
        }
    }
}
