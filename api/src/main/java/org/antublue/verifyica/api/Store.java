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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

/** Class to implement Store */
public interface Store {

    /**
     * Put a key-value pair
     *
     * @param key key
     * @param value value
     */
    void put(Object key, Object value);

    /**
     * Get a value
     *
     * @param key key
     * @return the value
     */
    Object get(Object key);

    /**
     * Get a value
     *
     * @param key key
     * @param returnType returnType
     * @return the value
     * @param <V> the return type
     */
    <V> V get(Object key, Class<V> returnType);

    /**
     * Get a value
     *
     * @param key key
     * @param defaultValue defaultValue
     * @return the value
     */
    Object getOrDefault(Object key, Object defaultValue);

    /**
     * Get a value
     *
     * @param key key
     * @param defaultValue defaultValue
     * @param returnType returnType
     * @return the value
     * @param <V> the return type
     */
    <V> V getOrDefault(Object key, V defaultValue, Class<V> returnType);

    /**
     * Get or compute if absent
     *
     * @param key key
     * @param mappingFunction mappingFunction
     * @return the value
     */
    Object computeIfAbsent(Object key, Function<Object, Object> mappingFunction);

    /**
     * Get or computer if absent
     *
     * @param key key
     * @param mappingFunction mappingFunction
     * @param returnType returnType
     * @return the value
     * @param <V> the return type
     */
    <V> V computeIfAbsent(Object key, Function<Object, V> mappingFunction, Class<V> returnType);

    /**
     * Get a value
     *
     * @param key key
     * @return the value
     */
    Optional<Object> getOptional(Object key);

    /**
     * Get a value
     *
     * @param key key
     * @param returnType returnType
     * @return the value
     * @param <V> the return type
     */
    <V> Optional<V> getOptional(Object key, Class<V> returnType);

    /**
     * Return if a key exists
     *
     * @param key key
     * @return true if the key exists, else false
     */
    boolean containsKey(Object key);

    /**
     * Remove a key-value pair
     *
     * @param key key
     * @return the value
     */
    Object remove(Object key);

    /**
     * Remove a key-value pair
     *
     * @param key key
     * @param returnType returnType
     * @return the value
     * @param <V> the return type
     */
    <V> V remove(Object key, Class<V> returnType);

    /**
     * Remove a key-value pair
     *
     * @param key key
     * @return the value
     */
    Optional<Object> removeOptional(Object key);

    /**
     * Remove a key-value pair
     *
     * @param key key
     * @param returnType returnType
     * @return the value
     * @param <V> the return type
     */
    <V> Optional<V> removeOptional(Object key, Class<V> returnType);

    /**
     * Clear
     *
     * @return this
     */
    Store clear();

    /**
     * Return the size
     *
     * @return the size
     */
    int size();

    /**
     * Return whether empty
     *
     * @return true if empty, else false
     */
    boolean isEmpty();

    /**
     * Return a COPY of the keySet
     *
     * @return a COPY of the keySet
     */
    Set<Object> keySet();

    /**
     * Replace the contents
     *
     * @param map map
     * @return this
     */
    Store replace(Map<Object, Object> map);

    /**
     * Replace the contents
     *
     * @param store store
     * @return this
     */
    Store replace(Store store);

    /**
     * Merge a Map
     *
     * @param map map
     * @return this
     */
    Store merge(Map<Object, Object> map);

    /**
     * Merge a Store
     *
     * @param store store
     * @return this
     */
    Store merge(Store store);

    /**
     * Duplicate
     *
     * @return a duplicate Store
     */
    Store duplicate();

    /**
     * Get the Lock
     *
     * <p>Equivalent to getReadWriteLock().writeLock()
     *
     * @return the Lock
     */
    default Lock getLock() {
        return getReadWriteLock().writeLock();
    }

    /**
     * Get the ReadWriteLock
     *
     * @return the ReadWriteLock
     */
    ReadWriteLock getReadWriteLock();
}
