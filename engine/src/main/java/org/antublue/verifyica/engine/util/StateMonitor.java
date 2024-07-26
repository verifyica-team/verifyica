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

package org.antublue.verifyica.engine.util;

import static java.lang.String.format;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Class to implement StateMonitor */
@SuppressWarnings("PMD.UnusedMethod")
public class StateMonitor<T> {

    private final Map<T, Entry<T>> map;
    private Entry<T> lastEntry;

    /** Constructor */
    public StateMonitor() {
        this(null);
    }

    /** Constructor */
    public StateMonitor(T initial) {
        map = new LinkedHashMap<>();
        if (initial != null) {
            put(initial);
        }
    }

    /**
     * Method to add a state
     *
     * @param state state
     */
    public void put(T state) {
        notNull(state, "state is null");

        if (map.containsKey(state)) {
            throw new IllegalStateException(
                    format("Programmer error, state [%s] already added", state));
        }

        Entry<T> entry = new Entry<>(state);
        map.put(state, entry);
        lastEntry = entry;
    }

    /**
     * Method to add a State and associated Throwable
     *
     * @param state state
     * @param throwable throwable
     */
    public void put(T state, Throwable throwable) {
        notNull(state, "state is null");
        notNull(throwable, "throwable is null");

        if (map.containsKey(state)) {
            throw new IllegalStateException(
                    format("Programmer error, state [%s] already added", state));
        }

        Entry<T> entry = new Entry<>(state, throwable);
        map.put(state, entry);
        lastEntry = entry;
    }

    /**
     * Method to return if a state exists
     *
     * @param state state
     * @return true if the state exists, else false
     */
    public boolean contains(T state) {
        notNull(state, "state is null");
        return map.containsKey(state);
    }

    /**
     * Method to get the last state entry
     *
     * @return the last state entry, possibly null
     */
    public Entry<T> getLastStateEntry() {
        return lastEntry;
    }

    /**
     * Method to get the state containing the first Throwable
     *
     * @return the state containing the first Throwable
     */
    public Entry<T> getFirstStateEntryWithThrowable() {
        for (Map.Entry<T, Entry<T>> mapEntry : map.entrySet()) {
            Entry<T> entry = mapEntry.getValue();
            if (entry.hasThrowable()) {
                return entry;
            }
        }

        return null;
    }

    /**
     * Method to get the Set of entries
     *
     * @return the Set of entries
     */
    public Set<Entry<T>> entrySet() {
        return new LinkedHashSet<>(map.values());
    }

    /**
     * Check if an Object is not null
     *
     * @param object object
     * @param message message
     */
    private static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /** Class to implement Entry */
    public static class Entry<T> {

        private final T state;
        private final Throwable throwable;

        /** Constructor */
        public Entry(T state) {
            this.state = state;
            this.throwable = null;
        }

        /**
         * Constructor
         *
         * @param state state
         * @param throwable throwble
         */
        public Entry(T state, Throwable throwable) {
            this.state = state;
            this.throwable = throwable;
        }

        /**
         * Method to get the state
         *
         * @return the state
         */
        public T getState() {
            return state;
        }

        /**
         * Method to get the Throwable, may be null
         *
         * @return the Throwable, may be null
         */
        public Throwable getThrowable() {
            return throwable;
        }

        /**
         * Method to return if the entry has a Throwable
         *
         * @return true if the entry has a Throwable, else false
         */
        public boolean hasThrowable() {
            return throwable != null;
        }

        @Override
        public String toString() {
            return "Entry{" + "state=" + state + ", throwable=" + throwable + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry<?> entry = (Entry<?>) o;
            return Objects.equals(state, entry.state) && Objects.equals(throwable, entry.throwable);
        }

        @Override
        public int hashCode() {
            return Objects.hash(state, throwable);
        }
    }
}
