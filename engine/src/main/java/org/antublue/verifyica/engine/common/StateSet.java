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

package org.antublue.verifyica.engine.common;

import static java.lang.String.format;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Class to implement StateTracker
 *
 * @param <T> the state type
 */
@SuppressWarnings("PMD.UnusedMethod")
public class StateSet<T> {

    private final Map<T, StateEntry<T>> map;
    private StateEntry<T> currentStateEntry;

    /** Constructor */
    public StateSet() {
        map = new LinkedHashMap<>();
    }

    /**
     * Method to set the current State
     *
     * @param state state
     */
    public void setCurrentState(T state) {
        Precondition.notNull(state, "state is null");

        if (map.containsKey(state)) {
            throw new IllegalStateException(format("State [%s] already encountered", state));
        }

        currentStateEntry = new StateEntry<>(state);
        map.put(state, currentStateEntry);
    }

    /**
     * Method to set the current State and associated Throwable
     *
     * @param state state
     * @param throwable throwable
     */
    public void setCurrentState(T state, Throwable throwable) {
        Precondition.notNull(state, "state is null");
        Precondition.notNull(throwable, "throwable is null");

        if (map.containsKey(state)) {
            throw new IllegalStateException(format("State [%s] already encountered", state));
        }

        currentStateEntry = new StateEntry<>(state, throwable);
        map.put(state, currentStateEntry);
    }

    /**
     * Method to return if a State has been observed
     *
     * @param state state
     * @return true if the state exists, else false
     */
    public boolean hasObservedState(T state) {
        Precondition.notNull(state, "state is null");
        return map.containsKey(state);
    }

    /**
     * Method to get the current State
     *
     * @return the last state
     */
    public T getCurrentState() {
        return currentStateEntry.getState();
    }

    /**
     * Method to return if the current State matches a State
     *
     * @param state state
     * @return true if the current State machines a State, else false
     */
    public boolean isCurrentState(T state) {
        if (state == null && currentStateEntry != null) {
            return false;
        }

        return currentStateEntry != null && state.equals(currentStateEntry.getState());
    }

    /**
     * Method to return if any States have been observed
     *
     * @return true no States have been observed, else false
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Method to get a Collection of observed State entries
     *
     * @return a Collection of observed State entries
     */
    public Collection<StateEntry<T>> getObservedStateEntries() {
        return new LinkedHashSet<>(map.values());
    }

    /**
     * Method to get the first State entry with a Throwable
     *
     * @return the first State entry with a Throwable, or an empty Optional
     */
    public Optional<StateEntry<T>> getFirstStateEntryWithThrowable() {
        for (Map.Entry<T, StateEntry<T>> mapEntry : map.entrySet()) {
            StateEntry<T> stateEntry = mapEntry.getValue();
            if (stateEntry.hasThrowable()) {
                return Optional.of(stateEntry);
            }
        }

        return Optional.empty();
    }

    /** Method to clear all observed States */
    public void clear() {
        map.clear();
    }

    @Override
    public String toString() {
        return "StateTracker{" + "map=" + map + ", currentStateEntry=" + currentStateEntry + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateSet<?> that = (StateSet<?>) o;
        return Objects.equals(map, that.map)
                && Objects.equals(currentStateEntry, that.currentStateEntry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map, currentStateEntry);
    }

    /**
     * Class to implement Entry
     *
     * @param <T> the type
     */
    public static class StateEntry<T> {

        private final T state;
        private final Throwable throwable;

        /**
         * Constructor
         *
         * @param state state
         */
        public StateEntry(T state) {
            this.state = state;
            this.throwable = null;
        }

        /**
         * Constructor
         *
         * @param state state
         * @param throwable throwable
         */
        public StateEntry(T state, Throwable throwable) {
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
            StateEntry<?> stateEntry = (StateEntry<?>) o;
            return Objects.equals(state, stateEntry.state)
                    && Objects.equals(throwable, stateEntry.throwable);
        }

        @Override
        public int hashCode() {
            return Objects.hash(state, throwable);
        }
    }
}
