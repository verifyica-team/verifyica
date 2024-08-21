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
public class StateTracker<T> {

    private final Map<T, StateEntry<T>> map;
    private StateEntry<T> currentStateEntry;

    /** Constructor */
    public StateTracker() {
        map = new LinkedHashMap<>();
    }

    /**
     * Method to add a state
     *
     * @param state state
     */
    public void setState(T state) {
        Precondition.notNull(state, "state is null");

        if (map.containsKey(state)) {
            throw new IllegalStateException(
                    format("Application error, state [%s] already encountered", state));
        }

        currentStateEntry = new StateEntry<>(state);
        map.put(state, currentStateEntry);
    }

    /**
     * Method to add a State and associated Throwable
     *
     * @param state state
     * @param throwable throwable
     */
    public void setState(T state, Throwable throwable) {
        Precondition.notNull(state, "state is null");
        Precondition.notNull(throwable, "throwable is null");

        if (map.containsKey(state)) {
            throw new IllegalStateException(
                    format("Application error, state [%s] already encountered", state));
        }

        currentStateEntry = new StateEntry<>(state, throwable);
        map.put(state, currentStateEntry);
    }

    /**
     * Method to return if a state exists
     *
     * @param state state
     * @return true if the state exists, else false
     */
    public boolean containsState(T state) {
        Precondition.notNull(state, "state is null");
        return map.containsKey(state);
    }

    /**
     * Method to return the current state
     *
     * @return the last state
     */
    public T getState() {
        return currentStateEntry.getState();
    }

    /**
     * Method to return if the last state matches a state
     *
     * @param state state
     * @return true if the last state matches, else false
     */
    public boolean isState(T state) {
        if (state == null && currentStateEntry != null) {
            return false;
        }

        return currentStateEntry != null && state.equals(currentStateEntry.getState());
    }

    /** Method to clear all states */
    public void clear() {
        map.clear();
    }

    /**
     * Method to get the state containing the first Throwable
     *
     * @return the state containing the first Throwable
     */
    public Optional<StateEntry<T>> getStateWithThrowable() {
        for (Map.Entry<T, StateEntry<T>> mapEntry : map.entrySet()) {
            StateEntry<T> stateEntry = mapEntry.getValue();
            if (stateEntry.hasThrowable()) {
                return Optional.of(stateEntry);
            }
        }

        return Optional.empty();
    }

    /**
     * Method to get a Collection of states being observed
     *
     * @return a Collection of states observed tracked
     */
    public Collection<StateEntry<T>> getObservedStates() {
        return new LinkedHashSet<>(map.values());
    }

    @Override
    public String toString() {
        return "StateTracker{" + "map=" + map + ", lastStateEntry=" + currentStateEntry + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateTracker<?> that = (StateTracker<?>) o;
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
