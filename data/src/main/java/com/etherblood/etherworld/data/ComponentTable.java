package com.etherblood.etherworld.data;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ComponentTable<T> {
    private final Map<Integer, T> table = new LinkedHashMap<>();
    private transient Map<T, Set<Integer>> index;

    public T get(int entity) {
        return table.get(entity);
    }

    public void set(int entity, T value) {
        T removed = table.put(entity, value);
        if (index != null) {
            applyRemove(entity, removed);
            index.computeIfAbsent(value, x -> new HashSet<>()).add(entity);
        }
    }

    public void remove(int entity) {
        T removed = table.remove(entity);
        if (index != null) {
            applyRemove(entity, removed);
        }
    }

    private void applyRemove(int entity, T removed) {
        if (removed != null) {
            Set<Integer> entities = index.get(removed);
            entities.remove(entity);
            if (entities.isEmpty()) {
                index.remove(removed);
            }
        }
    }

    public List<Integer> list() {
        return new ArrayList<>(table.keySet());
    }

    public List<Integer> findByValue(T value) {
        if (index == null) {
            index = new HashMap<>();
            for (Map.Entry<Integer, T> entry : table.entrySet()) {
                index.computeIfAbsent(entry.getValue(), x -> new HashSet<>()).add(entry.getKey());
            }
        }
        Set<Integer> entitiesSet = index.get(value);
        if (entitiesSet == null) {
            return Collections.emptyList();
        }
        ArrayList<Integer> entities = new ArrayList<>(entitiesSet);
        Collections.sort(entities);// sort to make result deterministic
        return entities;
    }
}
