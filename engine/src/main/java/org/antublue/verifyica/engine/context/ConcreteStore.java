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

package org.antublue.verifyica.engine.context;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import org.antublue.verifyica.api.Store;
import org.antublue.verifyica.engine.common.Precondition;

/** Class to implement ConcreteStore */
@SuppressWarnings("unchecked")
public class ConcreteStore implements Store {

    private final Map<Object, Object> map;
    private final ReadWriteLock readWriteLock;

    /** Constructor */
    public ConcreteStore() {
        this(new LinkedHashMap<>());
    }

    /**
     * Constructor
     *
     * <p>Copies the map
     *
     * @param map map
     */
    private ConcreteStore(Map<Object, Object> map) {
        Precondition.notNull(map, "map is null");

        this.map = new LinkedHashMap<>(map);
        readWriteLock = new ReentrantReadWriteLock(true);
    }

    @Override
    public Object put(Object key, Object value) {
        Precondition.notNull(key, "key is null");

        getReadWriteLock().writeLock().lock();
        try {
            return map.put(key, value);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public Object get(Object key) {
        Precondition.notNull(key, "key is null");

        getReadWriteLock().readLock().lock();
        try {
            return map.get(key);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public <V> V get(Object key, Class<V> type) {
        Precondition.notNull(key, "key is null");
        Precondition.notNull(type, "type is null");

        getReadWriteLock().readLock().lock();
        try {
            return type.cast(map.get(key));
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public Optional<Object> getOptional(Object key) {
        Precondition.notNull(key, "key is null");

        getReadWriteLock().readLock().lock();
        try {
            return Optional.ofNullable(map.get(key));
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public <V> Optional<V> getOptional(Object key, Class<V> type) {
        Precondition.notNull(key, "key is null");
        Precondition.notNull(type, "type is null");

        getReadWriteLock().readLock().lock();
        try {
            return Optional.ofNullable(type.cast(map.get(key)));
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        Precondition.notNull(key, "key is null");

        Object value = get(key);
        if (value != null) {
            return value;
        } else {
            return defaultValue;
        }
    }

    @Override
    public <V> V getOrDefault(Object key, V defaultValue, Class<V> type) {
        Precondition.notNull(key, "key is null");
        Precondition.notNull(type, "type is null");

        Object value = get(key);
        if (value != null) {
            return (V) value;
        } else {
            return defaultValue;
        }
    }

    @Override
    public Object computeIfAbsent(Object key, Function<Object, Object> mappingFunction) {
        Precondition.notNull(key, "key is null");
        Precondition.notNull(mappingFunction, "mappingFunction is null");

        getReadWriteLock().writeLock().lock();
        try {
            Object value = get(key);
            if (value == null) {
                value = mappingFunction.apply(key);
                if (value != null) {
                    put(key, value);
                }
            }
            return value;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public <V> V computeIfAbsent(Object key, Function<Object, V> mappingFunction, Class<V> type) {
        Precondition.notNull(key, "key is null");
        Precondition.notNull(mappingFunction, "mappingFunction is null");
        Precondition.notNull(type, "type is null");

        getReadWriteLock().writeLock().lock();
        try {
            Object value = get(key);
            if (value == null) {
                value = mappingFunction.apply(key);
                if (value != null) {
                    put(key, value);
                }
            }
            return (V) value;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        Precondition.notNull(key, "key is null");

        getReadWriteLock().readLock().lock();
        try {
            return map.containsKey(key);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public boolean contains(Object key, Object value) {
        Precondition.notNull(key, "key is null");

        getReadWriteLock().readLock().lock();
        try {
            return Objects.equals(map.get(key), value);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public Object remove(Object key) {
        Precondition.notNull(key, "key is null");

        getReadWriteLock().writeLock().lock();
        try {
            return map.remove(key);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public <V> V remove(Object key, Class<V> type) {
        Precondition.notNull(key, "key is null");
        Precondition.notNull(type, "type is null");

        getReadWriteLock().writeLock().lock();
        try {
            return type.cast(map.remove(key));
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public Optional<Object> removeOptional(Object key) {
        Precondition.notNull(key, "key is null");

        getReadWriteLock().writeLock().lock();
        try {
            return Optional.ofNullable(map.remove(key));
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public <V> Optional<V> removeOptional(Object key, Class<V> type) {
        Precondition.notNull(key, "key is null");
        Precondition.notNull(type, "type is null");

        getReadWriteLock().writeLock().lock();
        try {
            return Optional.ofNullable(type.cast(map.remove(key)));
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public int size() {
        getReadWriteLock().readLock().lock();
        try {
            return map.size();
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Store clear() {
        getReadWriteLock().writeLock().lock();
        try {
            map.clear();
            return this;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public Store replace(Map<Object, Object> map) {
        Precondition.notNull(map, "map is null");

        getReadWriteLock().writeLock().lock();
        try {
            map.clear();
            merge(map);
            return this;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public Store replace(Store store) {
        Precondition.notNull(store, "store is null");

        getReadWriteLock().writeLock().lock();
        try {
            store.getReadWriteLock().readLock().lock();
            try {
                map.clear();
                merge(store);
                return this;
            } finally {
                store.getReadWriteLock().readLock().unlock();
            }
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public Store merge(Map<Object, Object> map) {
        Precondition.notNull(map, "map is null");

        if (map.isEmpty()) {
            return this;
        }

        getReadWriteLock().writeLock().lock();
        try {
            this.map.putAll(map);
            return this;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public Store merge(Store store) {
        Precondition.notNull(store, "store is null");

        if (store.isEmpty()) {
            return this;
        }

        getReadWriteLock().writeLock().lock();
        try {
            store.getReadWriteLock().readLock().lock();
            try {
                store.keySet().forEach(key -> put(key, store.get(key)));
                return this;
            } finally {
                store.getReadWriteLock().readLock().unlock();
            }
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public Store duplicate() {
        getReadWriteLock().readLock().lock();
        try {
            return new ConcreteStore(new TreeMap<>(this.map));
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public Set<Object> keySet() {
        getReadWriteLock().readLock().lock();
        try {
            return new HashSet<>(map.keySet());
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public Lock getLock() {
        return getReadWriteLock().writeLock();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    @Override
    public String toString() {
        getReadWriteLock().readLock().lock();
        try {
            return "DefaultStore{map=" + map + '}';
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConcreteStore that = (ConcreteStore) o;
        getReadWriteLock().readLock().lock();
        try {
            return Objects.equals(map, that.map);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public int hashCode() {
        getReadWriteLock().readLock().lock();
        try {
            return Objects.hashCode(map);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }
}
