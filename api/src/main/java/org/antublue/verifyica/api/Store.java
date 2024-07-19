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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/** Class to implement Store */
@SuppressWarnings({"unchecked", "unused"})
public class Store {

    private final Map<Object, Object> map;
    private final ReadWriteLock readWriteLock;

    /** Constructor */
    public Store() {
        map = new TreeMap<>();
        readWriteLock = new ReentrantReadWriteLock(true);
    }

    /**
     * Constructor
     *
     * @param map map
     */
    public Store(Map<Object, Object> map) {
        notNull(map, "map is null");

        this.map = new TreeMap<>(map);
        this.readWriteLock = new ReentrantReadWriteLock(true);
    }

    /**
     * Returns the number of key-value mappings in this Store.
     *
     * @return the number of key-value mappings
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
     * Replaces each entry's value with the function result
     *
     * @param function the function
     */
    public void replaceAll(BiFunction<? super Object, ? super Object, ?> function) {
        notNull(function, "function is null");

        try {
            getLock().readLock().lock();
            map.replaceAll(function);
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Attempts to compute a mapping for the key and th existing value in this Store
     *
     * @param key key
     * @param remappingFunction remapping function
     * @return the new value for the key if it exists, else null
     * @param <T>
     */
    public <T> T compute(
            Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        notNull(key, "key is null");
        notNull(remappingFunction, "remappingFunction is null");

        try {
            getLock().readLock().lock();
            return (T) map.compute(key, remappingFunction);
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Returns a Collections of values in this Store.
     *
     * @return a Collection of values in this Store
     */
    public Collection<Object> values() {
        try {
            getLock().readLock().lock();
            return map.values();
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Puts a key-value in this Store
     *
     * @param key key
     * @param value value
     * @return the existing value (or null if an existing value doesn't exist)
     * @param <T> the return type
     */
    public <T> T put(Object key, Object value) {
        notNull(key, "key is null");

        try {
            getLock().readLock().lock();
            return (T) map.put(key, value);
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Replaces a value in this Store
     *
     * @param key key
     * @param value value
     * @return the existing value (or null if an existing value doesn't exist)
     * @param <T> the return type
     */
    public <T> T replace(Object key, Object value) {
        notNull(key, "key is null");

        try {
            getLock().readLock().lock();
            return (T) map.replace(key, value);
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Performs the given action on this Store
     *
     * @param action action
     */
    public void forEach(BiConsumer<? super Object, ? super Object> action) {
        notNull(action, "action is null");

        try {
            getLock().readLock().lock();
            map.forEach(action);
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Replaces a value in this store with a new value
     *
     * @param key key
     * @param oldValue old value
     * @param newValue new value
     * @return true if a value was replaced, else false
     */
    public boolean replace(Object key, Object oldValue, Object newValue) {
        notNull(key, "key is null");

        try {
            getLock().readLock().lock();
            return map.replace(key, oldValue, newValue);
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Returns if the key exists in this Store
     *
     * @param key key
     * @return true if the key exists, else false
     */
    public boolean containsKey(Object key) {
        notNull(key, "key is null");

        try {
            getLock().readLock().lock();
            return map.containsKey(key);
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Removes a key-value mapping from this Store
     *
     * @param key key
     * @return the value
     * @param <T> the return type
     */
    public <T> T remove(Object key) {
        notNull(key, "key is null");

        try {
            getLock().readLock().lock();
            return (T) map.remove(key);
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Removes a key-value mapping from this Store
     *
     * @param key key
     * @param type type
     * @return the value
     * @param <T> the return type
     */
    public <T> T remove(Object key, Class<T> type) {
        notNull(key, "key is null");
        notNull(type, "type is null");

        try {
            getLock().readLock().lock();
            return type.cast(map.remove(key));
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Returns if this Store empty
     *
     * @return true if empty, else false
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
     * Return a copy of this Store's entry set
     *
     * @return a copy of this Store's entry set
     */
    public Set<Map.Entry<Object, Object>> entrySet() {
        try {
            getLock().readLock().lock();
            return new LinkedHashSet<>(map.entrySet());
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Removes a key-value mapping from this Store
     *
     * @param key key
     * @param value value
     * @return true if the key-value mapping was removed, else false
     */
    public boolean remove(Object key, Object value) {
        notNull(key, "key is null");

        try {
            getLock().readLock().lock();
            return map.remove(key, value);
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Returns whether this Store contains a value
     *
     * @param value value
     * @return true if the value exists, else false
     */
    public boolean containsValue(Object value) {
        notNull(value, "value is null");

        try {
            getLock().readLock().lock();
            return map.containsValue(value);
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Merges a Map into this Store
     *
     * @param map map
     */
    public void putAll(Map<?, ?> map) {
        notNull(map, "map is null");

        try {
            getLock().writeLock().lock();
            this.map.putAll(map);
        } finally {
            getLock().writeLock().unlock();
        }
    }

    /**
     * Execute a remapping function if this Store contains the key
     *
     * @param key key
     * @param remappingFunction remapping function
     * @return the existing value, or result from the remapping function
     * @param <T> the return type
     */
    public <T> T computeIfPresent(
            Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        notNull(key, "key is null");
        notNull(remappingFunction, "remappingFunction is null");

        try {
            getLock().readLock().lock();
            return (T) map.computeIfPresent(key, remappingFunction);
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Puts a key-value mapping into this Store if it doesn't exist
     *
     * @param key key
     * @param value value
     * @return the existing value
     * @param <T> the return type
     */
    public <T> T putIfAbsent(Object key, Object value) {
        notNull(key, "key is null");

        try {
            getLock().readLock().lock();
            return (T) map.putIfAbsent(key, value);
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Merges a key-value mapping into this Store if the key doesn't exist or the value is null
     *
     * @param key key
     * @param value value
     * @param remappingFunction remapping function
     * @return the new value or null if there is no key-value mapping
     * @param <T> the return type
     */
    public <T> T merge(
            Object key,
            Object value,
            BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        notNull(key, "key is null");
        notNull(remappingFunction, "remappingFunction is null");

        try {
            getLock().readLock().lock();
            return (T) map.merge(key, value, remappingFunction);
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Returns a copy of this Store's key set
     *
     * @return a copy of this Store's key set
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
     * Returns the value from this Store
     *
     * @param key key
     * @return the value if it exists, else null
     * @param <T> the return type
     */
    public <T> T get(Object key) {
        notNull(key, "key is null");

        try {
            getLock().readLock().lock();
            return (T) map.get(key);
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Returns the value from this Store
     *
     * @param key key
     * @param type the return type
     * @return the value if it exists, else null
     * @param <T> the return type
     */
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

    /** Removes all key-value mappings */
    public void clear() {
        try {
            getLock().readLock().lock();
            map.clear();
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Returns the value from this Store
     *
     * @param key key
     * @param defaultValue the default value
     * @return the value if it exists, else the default value
     * @param <T> the return type
     */
    public <T> T getOrDefault(Object key, Object defaultValue) {
        notNull(key, "key is null");

        try {
            getLock().readLock().lock();
            return (T) map.getOrDefault(key, defaultValue);
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Returns the value from this Store
     *
     * @param key key
     * @param type the type
     * @param defaultValue the default value
     * @return the value if it exists, else the default value
     * @param <T> the return type
     */
    public <T> T getOrDefault(Object key, Class<T> type, T defaultValue) {
        notNull(key, "key is null");

        try {
            getLock().readLock().lock();
            return (T) map.getOrDefault(key, defaultValue);
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Puts a value into this Store if a key-value mapping doesn't exist
     *
     * @param key key
     * @param mappingFunction mapping function
     * @return the existing value if it exists, else the result of the mapping function
     * @param <T> the return type
     */
    public <T> T computeIfAbsent(Object key, Function<? super Object, ?> mappingFunction) {
        notNull(key, "key is null");
        notNull(mappingFunction, "mappingFunction is null");

        try {
            getLock().readLock().lock();
            return (T) map.computeIfAbsent(key, mappingFunction);
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Merges another Store into this Store
     *
     * @param store store
     * @return this Store
     */
    public Store merge(Store store) {
        notNull(store, "store is null");

        try {
            store.getLock().readLock().lock();
            getLock().writeLock().lock();
            this.map.putAll(store.map);
        } finally {
            getLock().writeLock().unlock();
            store.getLock().readLock().unlock();
        }

        return this;
    }

    /**
     * Merges a Map into this Store
     *
     * @param map map
     * @return this Store
     */
    public Store merge(Map<?, ?> map) {
        notNull(map, "map is null");

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
     * Merges Properties into the Store
     *
     * @param properties properties
     * @return this Store
     */
    public Store merge(Properties properties) {
        notNull(properties, "properties is null");

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
     * Duplicates this Store
     *
     * @return a duplicate of this Store
     */
    public Store duplicate() {
        try {
            getLock().readLock().lock();
            return new Store(this.map);
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Returns the Store's ReadWriteLock
     *
     * @return the Store's ReadWriteLock
     */
    public ReadWriteLock getLock() {
        return readWriteLock;
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
        return Objects.hashCode(map);
    }

    /**
     * Checks if an Object is not null, throwing an IllegalArgumentException is the Object is null.
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
