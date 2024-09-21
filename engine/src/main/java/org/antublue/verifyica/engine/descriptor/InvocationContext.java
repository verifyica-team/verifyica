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

package org.antublue.verifyica.engine.descriptor;

import java.util.HashMap;
import java.util.Map;
import org.antublue.verifyica.engine.common.Precondition;

/** Class to implement InvocationContext */
@SuppressWarnings("unchecked")
public class InvocationContext {

    private final Map<String, Object> map;

    /** Constructor */
    public InvocationContext() {
        map = new HashMap<>();
    }

    /**
     * Constructor
     *
     * @param map map
     */
    private InvocationContext(Map<String, Object> map) {
        this.map = map;
    }

    /**
     * Method to set a key-value pair
     *
     * @param clazz clazz
     * @param value value
     * @return this
     */
    public InvocationContext set(Class<?> clazz, Object value) {
        return set(clazz.getName(), value);
    }

    /**
     * Method to set a key-value pair
     *
     * @param key key
     * @param value value
     * @return this
     */
    public InvocationContext set(String key, Object value) {
        Precondition.notBlank(key, "key is null", "key is blank");
        Precondition.notNull(value, "value is null");

        map.put(key, value);

        return this;
    }

    /**
     * Method to get a value
     *
     * @param key key
     * @return a value
     * @param <V> type
     */
    public <V> V get(String key) {
        Precondition.notBlank(key, "key is blank", "key is null");

        return (V) map.get(key);
    }

    /**
     * Method to get a value
     *
     * @param clazz clazz
     * @return a value
     * @param <V> type
     */
    public <V> V get(Class<V> clazz) {
        Precondition.notNull(clazz, "clazz is null");

        return clazz.cast(map.get(clazz.getName()));
    }

    /**
     * Method to get a value
     *
     * @param key key
     * @param type type
     * @return a value
     * @param <V> type
     */
    public <V> V get(String key, Class<V> type) {
        Precondition.notBlank(key, "key is null", "key is blank");
        Precondition.notNull(type, "type is null");

        return type.cast(map.get(key));
    }

    /**
     * Method to copy
     *
     * @return an EngineExecutionContext
     */
    public InvocationContext copy() {
        return new InvocationContext(new HashMap<>(this.map));
    }
}
