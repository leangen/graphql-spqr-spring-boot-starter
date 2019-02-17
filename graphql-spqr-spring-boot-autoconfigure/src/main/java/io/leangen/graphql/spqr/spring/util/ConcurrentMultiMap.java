package io.leangen.graphql.spqr.spring.util;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("WeakerAccess")
public class ConcurrentMultiMap<K, V> {

    private final ConcurrentMap<K, Set<V>> map = new ConcurrentHashMap<>();

    public synchronized void add(K key, V value) {
        Set<V> set = map.get(key);
        if (set != null) {
            set.add(value);
        } else {
            map.put(key, createConcurrentSet(value));
        }
    }

    public synchronized void remove(K key, V value) {
        Set<V> set = map.get(key);
        if (set != null) {
            set.remove(value);
            if (set.isEmpty()) {
                map.remove(key);
            }
        }
    }

    public boolean contains(K key, V value) {
        return get(key).contains(value);
    }

    public Set<V> get(K key) {
        Set<V> set = map.get(key);
        return set == null ? Collections.emptySet() : set;
    }

    protected Set<V> createConcurrentSet(V value) {
        Set<V> set = ConcurrentHashMap.newKeySet();
        set.add(value);
        return set;
    }
}
