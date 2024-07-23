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

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import org.antublue.verifyica.api.Store;

/** Class to implement DefaultStore */
@SuppressWarnings("unchecked")
public class DefaultStore implements Store {

    private final TreeMap<Object, Object> map;
    private final ReadWriteLock readWriteLock;

    /** Constructor */
    public DefaultStore() {
        this(new TreeMap<>());
    }

    /**
     * Constructor
     *
     * @param map map
     */
    private DefaultStore(TreeMap<Object, Object> map) {
        notNull(map, "map is null");

        this.map = map;
        readWriteLock = new ReentrantReadWriteLock(true);
    }

    @Override
    public <T> T put(Object key, Object value) {
        notNull(key, "key is null");

        try {
            getLock().writeLock().lock();
            return (T) map.put(key, value);
        } finally {
            getLock().writeLock().unlock();
        }
    }

    @Override
    public Store replace(Store store) {
        notNull(store, "store is null");

        try {
            store.getLock().readLock().lock();
            getLock().writeLock().lock();
            clear();
            merge(store);
            return this;
        } finally {
            getLock().writeLock().unlock();
            store.getLock().readLock().unlock();
        }
    }

    @Override
    public <T> T computeIfAbsent(Object key, Function<Object, Object> function) {
        notNull(key, "key is null");
        notNull(function, "function is null");

        try {
            getLock().writeLock().lock();
            return (T) map.computeIfAbsent(key, function);
        } finally {
            getLock().writeLock().unlock();
        }
    }

    @Override
    public Store merge(Store store) {
        notNull(store, "store is null");

        try {
            store.getLock().readLock().lock();
            getLock().writeLock().lock();

            store.keySet().forEach(key -> put(key, store.get(key)));

            return this;
        } finally {
            getLock().writeLock().unlock();
            store.getLock().readLock().unlock();
        }
    }

    @Override
    public Store merge(Map<Object, Object> map) {
        notNull(map, "map is null");

        try {
            getLock().writeLock().lock();
            this.map.putAll(map);
            return this;
        } finally {
            getLock().writeLock().unlock();
        }
    }

    @Override
    public <T> T get(Object key) {
        notNull(key, "key is null");

        try {
            getLock().readLock().lock();
            return (T) map.get(key);
        } finally {
            getLock().readLock().unlock();
        }
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        notNull(key, "key is null");
        notNull(type, "type is null");

        try {
            getLock().readLock().lock();
            return type.cast(map.get(key));
        } finally {
            getLock().readLock().unlock();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        notNull(key, "key is null");

        try {
            getLock().readLock().lock();
            return map.containsKey(key);
        } finally {
            getLock().readLock().unlock();
        }
    }

    @Override
    public <T> T remove(Object key) {
        notNull(key, "key is null");

        try {
            getLock().writeLock().lock();
            return (T) map.remove(key);
        } finally {
            getLock().writeLock().unlock();
        }
    }

    @Override
    public <T> T remove(Object key, Class<T> type) {
        notNull(key, "key is null");
        notNull(type, "type is null");

        try {
            getLock().writeLock().lock();
            return type.cast(map.remove(key));
        } finally {
            getLock().writeLock().unlock();
        }
    }

    @Override
    public int size() {
        try {
            getLock().readLock().lock();
            return map.size();
        } finally {
            getLock().readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Store clear() {
        try {
            getLock().writeLock().lock();
            map.clear();
            return this;
        } finally {
            getLock().writeLock().unlock();
        }
    }

    @Override
    public Store duplicate() {
        try {
            getLock().readLock().lock();
            return new DefaultStore(this.map);
        } finally {
            getLock().readLock().unlock();
        }
    }

    @Override
    public Set<Object> keySet() {
        try {
            getLock().readLock().lock();
            return new TreeSet<>(map.keySet());
        } finally {
            getLock().readLock().unlock();
        }
    }

    @Override
    public ReadWriteLock getLock() {
        return readWriteLock;
    }

    @Override
    public String toString() {
        return "Store{map=" + map + '}';
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

    /**
     * Check if an Object is not null
     *
     * @param object object
     * @param message message
     */
    private static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
