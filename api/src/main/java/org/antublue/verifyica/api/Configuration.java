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
import org.antublue.verifyica.api.concurrency.locks.LockProvider;
import org.antublue.verifyica.api.concurrency.locks.ReadWriteLockProvider;

/** Interface to implement Configuration */
public interface Configuration extends ReadWriteLockProvider, LockProvider {

    /**
     * Put a key-value pair
     *
     * @param key key
     * @param value value
     * @return an Optional containing the existing value, or an empty Optional
     */
    Optional<String> put(String key, String value);

    /**
     * Get the value or use the function to create a value
     *
     * @param key key
     * @param transformer transformer
     * @return an Optional containing the existing value, or an Optional of the value returned by
     *     the transformer
     */
    Optional<String> computeIfAbsent(String key, Function<String, String> transformer);

    /**
     * Get a value
     *
     * @param key key
     * @return the value
     */
    String get(String key);

    /**
     * Get a value
     *
     * @param key key
     * @return an Optional containing the value, or an empty Optional
     */
    Optional<String> getOptional(String key);

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
     * Remove a key-value pair
     *
     * @param key key
     * @return an Optional containing the value, or an empty Optional
     */
    Optional<String> removeOptional(String key);

    /**
     * Clear
     *
     * @return this
     */
    Configuration clear();

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
    Set<String> keySet();

    /**
     * Replace the contents
     *
     * @param map map
     * @return this
     */
    Configuration replace(Map<String, String> map);

    /**
     * Replace the contents
     *
     * @param Configuration Configuration
     * @return this
     */
    Configuration replace(Configuration Configuration);

    /**
     * Merge a Map
     *
     * @param map map
     * @return this
     */
    Configuration merge(Map<String, String> map);

    /**
     * Merge a Configuration
     *
     * @param Configuration Configuration
     * @return this
     */
    Configuration merge(Configuration Configuration);

    /**
     * Duplicate the Configuration
     *
     * @return a duplicate Configuration
     */
    Configuration duplicate();

    /**
     * Get the Lock
     *
     * @return the Lock
     */
    @Override
    default Lock getLock() {
        return getReadWriteLock().writeLock();
    }

    /**
     * Get the ReadWriteLock
     *
     * @return the ReadWriteLock
     */
    @Override
    ReadWriteLock getReadWriteLock();
}
