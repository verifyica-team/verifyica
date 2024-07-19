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

package org.antublue.verifyica.api;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import org.junit.platform.commons.util.Preconditions;

/** Class to implement Store */
@SuppressWarnings("unchecked")
public class Store {

    private final ReadWriteLock readWriteLock;
    private final Map<Object, Object> map;

    /** Constructor */
    public Store() {
        readWriteLock = new ReentrantReadWriteLock(true);
        map = new TreeMap<>();
    }

    /**
     * Method to put a value into the Store
     *
     * @param key key
     * @param value value
     * @return the Store
     */
    public Store put(Object key, Object value) {
        Preconditions.notNull(key, "key is null");

        try {
            getLock().writeLock().lock();

            if (value == null) {
                map.remove(key);
            } else {
                map.put(key, value);
            }
        } finally {
            getLock().writeLock().unlock();
        }

        return this;
    }

    /**
     * Method to get a value from the Store
     *
     * @param key key
     * @return the value
     * @param <T> type
     */
    public <T> T get(Object key) {
        Preconditions.notNull(key, "key is null");

        try {
            getLock().readLock().lock();
            return (T) map.get(key);
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Method to get value from the Store
     *
     * @param key key
     * @param type type
     * @return the value
     * @param <T> type
     */
    public <T> T get(Object key, Class<T> type) {
        Preconditions.notNull(key, "key is null");
        Preconditions.notNull(type, "type is null");

        try {
            getLock().readLock().lock();
            return type.cast(map.get(key));
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Method to get a value from the Store, throwing an Exception if null
     *
     * @param key key
     * @param supplier supplier
     * @return the Object
     * @param <T> type
     */
    @Deprecated
    public <T> T getOrThrow(Object key, Supplier<? extends RuntimeException> supplier) {
        Preconditions.notNull(key, "key is null");
        Preconditions.notNull(supplier, "supplier is null");

        T t = get(key);
        if (t == null) {
            throw supplier.get();
        } else {
            return t;
        }
    }

    /**
     * Method to get a value from the Store, throwing an Exception if null
     *
     * @param key key
     * @param type type
     * @param supplier supplier
     * @return the Object
     * @param <T> type
     */
    @Deprecated
    public <T> T getOrThrow(
            Object key, Class<T> type, Supplier<? extends RuntimeException> supplier) {
        Preconditions.notNull(key, "key is null");
        Preconditions.notNull(type, "type is null");
        Preconditions.notNull(supplier, "supplier is null");

        T t = get(key, type);
        if (t == null) {
            throw supplier.get();
        } else {
            return t;
        }
    }

    /**
     * Method to remove value from the Store
     *
     * @param key key
     * @return the existing Object
     * @param <T> type
     */
    public <T> T remove(Object key) {
        Preconditions.notNull(key, "key is null");

        try {
            getLock().writeLock().lock();
            return (T) map.remove(key);
        } finally {
            getLock().writeLock().unlock();
        }
    }

    /**
     * Method to remove a value from the Store
     *
     * @param key key
     * @param type type
     * @return the existing Object
     * @param <T> type
     */
    public <T> T remove(Object key, Class<T> type) {
        Preconditions.notNull(key, "key is null");
        Preconditions.notNull(type, "type is null");

        try {
            getLock().writeLock().lock();
            return type.cast(map.remove(key));
        } finally {
            getLock().writeLock().unlock();
        }
    }

    /**
     * Method to clear the Store
     *
     * @return the Store
     */
    public Store clear() {
        try {
            getLock().writeLock().lock();

            for (Object value : map.values()) {
                if (value instanceof AutoCloseable) {
                    try {
                        ((AutoCloseable) value).close();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
            map.clear();
        } finally {
            getLock().writeLock().unlock();
        }

        return this;
    }

    /**
     * Method to return if the Store contains the key
     *
     * @param key key
     * @return true if the Store contains the key, else false
     */
    public boolean containsKey(Object key) {
        Preconditions.notNull(key, "key is null");

        try {
            getLock().writeLock().lock();
            return map.containsKey(key);
        } finally {
            getLock().writeLock().unlock();
        }
    }

    /**
     * Method to return whether the Store is empty
     *
     * @return true of the store is empty, else false
     */
    public boolean isEmpty() {
        try {
            getLock().readLock().lock();
            return map.isEmpty();
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Method to get the Store size
     *
     * @return the size
     */
    public int size() {
        try {
            getLock().readLock().lock();
            return map.size();
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Method to get the Store's key set
     *
     * @return the Store's key set
     */
    public Set<Object> keySet() {
        try {
            getLock().readLock().lock();
            return new LinkedHashSet<>(map.keySet());
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Method to merge a Store into the Store. Locks the Store
     *
     * @param store store
     * @return this
     */
    public Store merge(Store store) {
        Preconditions.notNull(store, "store is null");

        if (store.size() > 0) {
            try {
                store.getLock().readLock().lock();
                getLock().writeLock().lock();
                map.putAll(store.map);
            } finally {
                getLock().writeLock().unlock();
                store.getLock().readLock().unlock();
            }
        }

        return this;
    }

    /**
     * Method to merge a Map into the Store. Locks the Store
     *
     * @param map map
     * @return this
     */
    public Store merge(Map<String, String> map) {
        Preconditions.notNull(map, "map is null");

        if (!map.isEmpty()) {
            try {
                getLock().writeLock().lock();
                this.map.putAll(map);
            } finally {
                getLock().writeLock().unlock();
            }
        }

        return this;
    }

    /**
     * Method to merge Properties into the Store. Locks the Store
     *
     * @param properties properties
     * @return this
     */
    public Store merge(Properties properties) {
        Preconditions.notNull(properties, "properties is null");

        if (!properties.isEmpty()) {
            try {
                getLock().writeLock().lock();
                Set<Map.Entry<Object, Object>> entrySet = new HashSet<>(properties.entrySet());
                entrySet.forEach(entry -> map.put(entry.getKey(), entry.getValue()));
            } finally {
                getLock().writeLock().unlock();
            }
        }

        return this;
    }

    /**
     * Method to duplicate the Store
     *
     * @return a duplicate Store
     */
    public Store duplicate() {
        try {
            getLock().readLock().lock();

            Store store = new Store();
            store.map.putAll(this.map);
            return store;
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Method to get the Store lock
     *
     * @return the Store lock
     */
    public ReadWriteLock getLock() {
        return readWriteLock;
    }

    @Override
    public String toString() {
        try {
            getLock().readLock().lock();
            return map.toString();
        } finally {
            getLock().readLock().unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Store store = (Store) o;
        return Objects.equals(map, store.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }
}
