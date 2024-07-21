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

public interface Configuration {

    /**
     * Put a key-value mapping
     *
     * @param key key
     * @param value value
     * @return the existing value, or null
     */
    String put(String key, String value);

    /**
     * Get a value
     *
     * @param key value
     * @return the value
     */
    String get(String key);

    /**
     * Get a value
     *
     * @param key key
     * @param defaultValue defaultValue
     * @return the value if it exists, else the default value
     */
    String getOrDefault(String key, String defaultValue);

    /**
     * Get the value or use the function to create a value
     *
     * @param key key
     * @param function function
     * @return the value
     */
    String computeIfAbsent(String key, Function<String, String> function);

    /**
     * Merge a Map
     *
     * @param map map
     * @return this
     */
    Configuration merge(Map<String, String> map);

    /**
     * Return if a key exists
     *
     * @param key key
     * @return true if the key exists, else false
     */
    boolean containsKey(String key);

    /**
     * Remove a key-value pair
     *
     * @param key key
     * @return the value
     */
    String remove(String key);

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

    /** Clear */
    Configuration clear();

    /**
     * Return a COPY of the keySet
     *
     * @return a COPY of the keySet
     */
    Set<String> keySet();

    /**
     * Return the ReadWriteLock
     *
     * @return the ReadWriteLock
     */
    ReadWriteLock getLock();
}
