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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.junit.platform.commons.util.Preconditions;

@SuppressWarnings("unchecked")
public class Store {

    private final Map<Object, Object> map;

    /** Constructor */
    public Store() {
        map = new ConcurrentHashMap<>();
    }

    /**
     * Method to put an object into the Store
     *
     * @param key key
     * @param value value
     * @return the Store
     */
    public Store put(Object key, Object value) {
        Preconditions.notNull(key, "key is null");

        if (value == null) {
            map.remove(key);
        } else {
            map.put(key, value);
        }

        return this;
    }

    /**
     * Method to get an Object from the Store
     *
     * @param key key
     * @return the Object
     * @param <T> type
     */
    public <T> T get(Object key) {
        Preconditions.notNull(key, "key is null");
        return (T) map.get(key);
    }

    /**
     * Method to get an Object from the Store
     *
     * @param key key
     * @param type type
     * @return the Object
     * @param <T> type
     */
    public <T> T get(Object key, Class<T> type) {
        Preconditions.notNull(key, "key is null");
        Preconditions.notNull(type, "type is null");

        return type.cast(map.get(key));
    }

    /**
     * Method to get an Object from the Store, throwing an Exception if null
     *
     * @param key key
     * @param supplier supplier
     * @return the Object
     * @param <T> type
     */
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
     * Method to get an Object from the Store, throwing an Exception if null
     *
     * @param key key
     * @param supplier supplier
     * @return the Object
     * @param <T> type
     */
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
     * Method to remove an Object from the Store, return the existing Object
     *
     * @param key key
     * @return the existing Object
     * @param <T> type
     */
    public <T> T remove(Object key) {
        Preconditions.notNull(key, "key is null");

        return (T) map.remove(key);
    }

    /**
     * Method to remove an Object from the Store, return the existing Object
     *
     * @param key key
     * @param type type
     * @return the existing Object
     * @param <T> type
     */
    public <T> T remove(Object key, Class<T> type) {
        Preconditions.notNull(key, "key is null");
        Preconditions.notNull(type, "type is null");

        return type.cast(map.remove(key));
    }

    /**
     * Method to clear the store
     *
     * @return the Store
     */
    public Store clear() {
        synchronized (map) {
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
        }

        return this;
    }
}
