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

package org.antublue.verifyica.engine.configuration;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import org.antublue.verifyica.api.Configuration;

public class DefaultConfiguration implements Configuration {

    private final Map<String, String> map;
    private final ReadWriteLock readWriteLock;

    public DefaultConfiguration() {
        map = new TreeMap<>();
        readWriteLock = new ReentrantReadWriteLock(true);
    }

    @Override
    public String put(String key, String value) {
        notNullOrEmpty(key, "key is null", "key is empty");

        try {
            getLock().writeLock().lock();
            return map.put(key, value);
        } finally {
            getLock().writeLock().unlock();
        }
    }

    @Override
    public String get(String key) {
        notNullOrEmpty(key, "key is null", "key is empty");

        try {
            getLock().readLock().lock();
            return map.get(key);
        } finally {
            getLock().readLock().unlock();
        }
    }

    @Override
    public String getOrDefault(String key, String defaultValue) {
        notNullOrEmpty(key, "key is null", "key is empty");

        try {
            getLock().readLock().lock();
            return map.getOrDefault(key, defaultValue);
        } finally {
            getLock().readLock().unlock();
        }
    }

    @Override
    public String computeIfAbsent(String key, Function<String, String> function) {
        notNullOrEmpty(key, "key is null", "key is empty");
        notNull(function, "functio is null");

        try {
            getLock().writeLock().lock();
            return map.computeIfAbsent(key, function);
        } finally {
            getLock().writeLock().unlock();
        }
    }

    @Override
    public boolean containsKey(String key) {
        notNullOrEmpty(key, "key is null", "key is empty");

        try {
            getLock().readLock().lock();
            return map.containsKey(key);
        } finally {
            getLock().readLock().unlock();
        }
    }

    @Override
    public String remove(String key) {
        notNullOrEmpty(key, "key is null", "key is empty");

        try {
            getLock().writeLock().lock();
            return map.remove(key);
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
    public Configuration clear() {
        try {
            getLock().writeLock().lock();
            map.clear();
            return this;
        } finally {
            getLock().writeLock().unlock();
        }
    }

    @Override
    public Set<String> keySet() {
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
        return "DefaultConfiguration{" + "map=" + map + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultConfiguration that = (DefaultConfiguration) o;
        return Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(map);
    }

    /**
     * Check if a Object is not null
     *
     * @param object object
     * @param message message
     */
    private static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Check if a String is not null and not blank
     *
     * @param string object
     * @param nullMessage nullMessage
     * @param emptyMessage emptyMessage
     */
    private static void notNullOrEmpty(String string, String nullMessage, String emptyMessage) {
        if (string == null) {
            throw new IllegalArgumentException(nullMessage);
        }

        if (string.trim().isEmpty()) {
            throw new IllegalArgumentException(emptyMessage);
        }
    }
}
