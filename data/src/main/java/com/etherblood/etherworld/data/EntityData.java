package com.etherblood.etherworld.data;

import java.util.List;

public interface EntityData {
    int createEntity();

    <T> T get(int entity, Class<T> key);

    <T> void set(int entity, Class<T> key, T value);

    @SuppressWarnings("unchecked")
    default <T> void set(int entity, T value) {
        set(entity, (Class<T>) value.getClass(), value);
    }

    void remove(int entity, Class<?> component);

    List<Integer> list(Class<?> component);

    default boolean has(int entity, Class<?> key) {
        return get(entity, key) != null;
    }

    List<Integer> findByValue(Object value);
}
