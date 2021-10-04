package com.etherblood.etherworld.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityDatabase implements EntityData {

    private final Map<Class<?>, ComponentTable<?>> world = new HashMap<>();
    private int nextEntity = 1;

    @Override
    public int createEntity() {
        return nextEntity++;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(int entity, Class<T> key) {
        ComponentTable<?> table = world.get(key);
        if (table == null) {
            return null;
        }
        return (T) table.get(entity);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> void set(int entity, Class<T> key, T value) {
        if (value != null) {
            ComponentTable components = world.computeIfAbsent(key, x -> new ComponentTable<>());
            components.set(entity, value);
        }
    }

    @Override
    public void remove(int entity, Class<?> component) {
        ComponentTable<?> table = world.get(component);
        if (table != null) {
            table.remove(entity);
        }
    }

    @Override
    public List<Integer> list(Class<?> component) {
        ComponentTable<?> table = world.get(component);
        if (table == null) {
            return Collections.emptyList();
        }
        return table.list();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public List<Integer> findByValue(Object value) {
        ComponentTable table = world.get(value.getClass());
        if (table == null) {
            return Collections.emptyList();
        }
        return table.findByValue(value);
    }
}