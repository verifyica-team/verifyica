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
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

/** Class to implement Store */
public interface Store {

    /**
     * Put a key-value pair
     *
     * @param key key
     * @param value value
     * @return the existing value, or null
     * @param <T>
     */
    <T> T put(Object key, Object value);

    /**
     * Replace the contents
     *
     * @param store store
     * @return this
     */
    Store replace(Store store);

    /**
     * Get the value or use the function to create a value
     *
     * @param key key
     * @param function function
     * @return the value
     * @param <T> type
     */
    <T> T computeIfAbsent(Object key, Function<Object, Object> function);

    /**
     * Merge a Store
     *
     * @param store store
     * @return this
     */
    Store merge(Store store);

    /**
     * Merge a Map
     *
     * @param map map
     * @return this
     */
    Store merge(Map<Object, Object> map);

    /**
     * Get a value
     *
     * @param key key
     * @return the value
     * @param <T> the return type
     */
    <T> T get(Object key);

    /**
     * Get a value
     *
     * @param key key
     * @param type the return type
     * @return the value
     * @param <T> the return type
     */
    <T> T get(Object key, Class<T> type);

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
     * @param <T> the return type
     */
    <T> T remove(Object key);

    /**
     * Remove a key-value pair
     *
     * @param key key
     * @param type type
     * @return the value
     * @param <T> the return type
     */
    <T> T remove(Object key, Class<T> type);

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
     * Duplicate
     *
     * @return a duplicate Store
     */
    Store duplicate();

    /**
     * Get the ReadWriteLock
     *
     * @return the ReadWriteLock
     */
    ReadWriteLock getLock();
}
