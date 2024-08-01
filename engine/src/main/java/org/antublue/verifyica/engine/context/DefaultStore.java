/*
 * Copyright (C) 2024 The Verifyica project authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.antublue.verifyica.engine.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import org.antublue.verifyica.api.Store;
import org.antublue.verifyica.engine.support.ArgumentSupport;

/** Class to implement DefaultStore */
@SuppressWarnings("unchecked")
public class DefaultStore implements Store {

    private final Map<Object, Object> map;
    private final ReadWriteLock readWriteLock;

    /** Constructor */
    public DefaultStore() {
        this(new HashMap<>());
    }

    /**
     * Constructor
     *
     * @param map map
     */
    private DefaultStore(Map<Object, Object> map) {
        ArgumentSupport.notNull(map, "map is null");

        this.map = map;
        readWriteLock = new ReentrantReadWriteLock(true);
    }

    @Override
    public void put(Object key, Object value) {
        ArgumentSupport.notNull(key, "key is null");

        try {
            getReadWriteLock().writeLock().lock();
            map.put(key, value);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public Object get(Object key) {
        ArgumentSupport.notNull(key, "key is null");

        try {
            getReadWriteLock().readLock().lock();
            return map.get(key);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public <V> V get(Object key, Class<V> type) {
        ArgumentSupport.notNull(key, "key is null");
        ArgumentSupport.notNull(type, "type is null");

        try {
            getReadWriteLock().readLock().lock();
            return type.cast(map.get(key));
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public Optional<Object> getOptional(Object key) {
        ArgumentSupport.notNull(key, "key is null");

        try {
            getReadWriteLock().readLock().lock();
            return Optional.ofNullable(map.get(key));
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public <V> Optional<V> getOptional(Object key, Class<V> type) {
        ArgumentSupport.notNull(key, "key is null");
        ArgumentSupport.notNull(type, "type is null");

        try {
            getReadWriteLock().readLock().lock();
            return Optional.ofNullable(type.cast(map.get(key)));
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        Object value = get(key);
        if (value != null) {
            return value;
        } else {
            return defaultValue;
        }
    }

    @Override
    public <V> V getOrDefault(Object key, V defaultValue, Class<V> type) {
        Object value = get(key);
        if (value != null) {
            return (V) value;
        } else {
            return defaultValue;
        }
    }

    @Override
    public Object computeIfAbsent(Object key, Function<Object, Object> function) {
        try {
            getReadWriteLock().writeLock().lock();
            Object value = get(key);
            if (value == null) {
                value = function.apply(key);
            }
            return value;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public <V> V computeIfAbsent(Object key, Function<Object, V> function, Class<V> type) {
        try {
            getReadWriteLock().writeLock().lock();
            Object value = get(key);
            if (value == null) {
                value = function.apply(key);
            }
            return (V) value;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        ArgumentSupport.notNull(key, "key is null");

        try {
            getReadWriteLock().readLock().lock();
            return map.containsKey(key);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public Object remove(Object key) {
        ArgumentSupport.notNull(key, "key is null");

        try {
            getReadWriteLock().writeLock().lock();
            return map.remove(key);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public <V> V remove(Object key, Class<V> type) {
        ArgumentSupport.notNull(key, "key is null");
        ArgumentSupport.notNull(type, "type is null");

        try {
            getReadWriteLock().writeLock().lock();
            return type.cast(map.remove(key));
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public Optional<Object> removeOptional(Object key) {
        ArgumentSupport.notNull(key, "key is null");

        try {
            getReadWriteLock().writeLock().lock();
            return Optional.ofNullable(map.remove(key));
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public <V> Optional<V> removeOptional(Object key, Class<V> type) {
        ArgumentSupport.notNull(key, "key is null");
        ArgumentSupport.notNull(type, "type is null");

        try {
            getReadWriteLock().writeLock().lock();
            return Optional.ofNullable(type.cast(map.remove(key)));
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public int size() {
        try {
            getReadWriteLock().readLock().lock();
            return map.size();
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Store clear() {
        try {
            getReadWriteLock().writeLock().lock();
            map.clear();
            return this;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public Store replace(Map<Object, Object> map) {
        ArgumentSupport.notNull(map, "map is null");

        try {
            getReadWriteLock().writeLock().lock();
            clear();
            merge(map);
            return this;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public Store replace(Store store) {
        ArgumentSupport.notNull(store, "store is null");

        try {
            store.getReadWriteLock().readLock().lock();
            getReadWriteLock().writeLock().lock();
            clear();
            merge(store);
            return this;
        } finally {
            getReadWriteLock().writeLock().unlock();
            store.getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public Store merge(Map<Object, Object> map) {
        ArgumentSupport.notNull(map, "map is null");

        try {
            getReadWriteLock().writeLock().lock();
            this.map.putAll(map);
            return this;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public Store merge(Store store) {
        ArgumentSupport.notNull(store, "store is null");

        try {
            store.getReadWriteLock().readLock().lock();
            getReadWriteLock().writeLock().lock();
            store.keySet().forEach(key -> put(key, store.get(key)));
            return this;
        } finally {
            getReadWriteLock().writeLock().unlock();
            store.getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public Store duplicate() {
        try {
            getReadWriteLock().readLock().lock();
            return new DefaultStore(new TreeMap<>(this.map));
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public Set<Object> keySet() {
        try {
            getReadWriteLock().readLock().lock();
            return new TreeSet<>(map.keySet());
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    @Override
    public String toString() {
        return "DefaultStore{map=" + map + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultStore that = (DefaultStore) o;
        return Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(map);
    }
}
