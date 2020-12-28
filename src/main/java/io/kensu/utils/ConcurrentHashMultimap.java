package io.kensu.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMultimap<T> extends ConcurrentHashMap<String, HashSet<T>> {
    public void addEntry(String keyId, T newEntry) {
        this.compute(keyId, (key, existingObjects) -> {
            HashSet<T> newObjects = (existingObjects != null) ?  existingObjects : new HashSet<>();
            newObjects.add(newEntry);
            return newObjects;
        });
    }

    /**
     * Creates a new, empty map with the default initial table size (16).
     */
    public ConcurrentHashMultimap() {
        super();
    }

    public ConcurrentHashMultimap(Map<String, HashSet<T>> m) {
        super(m);
    }
}
