/*
 * Copyright (C) Verifyica project authors and contributors
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

package org.verifyica.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to implement ExtendedMap
 *
 * @param <K> key type
 * @param <V> value type
 */
@SuppressWarnings("unchecked")
public class ExtendedMap<K, V> extends ConcurrentHashMap<K, V> {

    /**
     * Constructor
     */
    public ExtendedMap() {
        super();
    }

    /**
     * Constructor
     *
     * @param initialCapacity the initial capacity
     */
    public ExtendedMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructor
     *
     * @param map the map to copy
     */
    public ExtendedMap(Map<? extends K, ? extends V> map) {
        super(map);
    }

    /**
     * Constructor
     *
     * @param initialCapacity the initial capacity
     * @param loadFactor the load factor
     */
    public ExtendedMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Constructor
     *
     * @param initialCapacity the initial capacity
     * @param loadFactor the load factor
     * @param concurrencyLevel the concurrency level
     */
    public ExtendedMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        super(initialCapacity, loadFactor, concurrencyLevel);
    }

    /**
     * Returns the value to which the specified key is mapped cast to the assigned type, or null if this map contains no mapping for the key.
     *
     * @param key the key
     * @return the value to which the specified key is mapped cast to the assigned type, or null if this map contains no mapping for the key
     * @param <T> the assigned type
     */
    public <T> T getAs(K key) {
        return (T) get(key);
    }

    /**
     * Returns the value to which the specified key is mapped cast to the requested type, or null if this map contains no mapping for the key.
     *
     * @param key the key
     * @param type the requested type
     * @return the value to which the specified key is mapped cast to the requested type, or null if this map contains no mapping for the key
     * @param <T> the requested type
     */
    public <T> T getAs(K key, Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }

        return type.cast(get(key));
    }

    /**
     * Removes the mapping for a key from this map if it is present, casting to the assigned type.
     *
     * @param key the key
     * @return the previous value associated with key cast to the assigned type, or null if there was no mapping for key
     * @param <T> the assigned type
     */
    public <T> T removeAs(K key) {
        return (T) remove(key);
    }

    /**
     * Removes the mapping for a key from this map if it is present, casting to the requested type.
     *
     * @param key the key
     * @param type the requested type
     * @return the previous value associated with key cast to the requested type, or null if there was no mapping for key
     * @param <T> the assigned type
     */
    public <T> T removeAs(K key, Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }

        return type.cast(remove(key));
    }
}
