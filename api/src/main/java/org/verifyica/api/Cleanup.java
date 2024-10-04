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

package org.verifyica.api;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Class to implement Cleanup */
public class Cleanup {

    private final List<Throwable> throwables;

    /** Constructor */
    public Cleanup() {
        throwables = new ArrayList<>();
    }

    /**
     * Performs a Cleanup.Action, collecting the Throwable if thrown
     *
     * @param action executable
     * @return this
     */
    public Cleanup perform(Action action) {
        try {
            action.perform();
        } catch (Throwable t) {
            throwables.add(t);
        }

        return this;
    }

    /**
     * Performs an array of Cleanup.Actions, collecting the Throwables if thrown
     *
     * @param actions executables
     * @return this
     */
    public Cleanup perform(Action... actions) {
        for (Action action : actions) {
            perform(action);
        }

        return this;
    }

    /**
     * Close a Closable if not null
     *
     * @param closeable closable
     * @return this
     */
    public Cleanup close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable t) {
                throwables.add(t);
            }
        }

        return this;
    }

    /**
     * Close an array of Closeables
     *
     * @param closeables closeables
     * @return this
     */
    public Cleanup close(Closeable... closeables) {
        if (closeables != null) {
            for (Closeable closeable : closeables) {
                close(closeable);
            }
        }

        return this;
    }

    /**
     * Returns if the Cleanup has collected any Throwables
     *
     * @return true if no Throwables have been collected, else false
     */
    public boolean isEmpty() {
        return throwables.isEmpty();
    }

    /**
     * Returns if the Cleanup has not collected any Throwables
     *
     * @return true if no Throwables have been collected, else false
     */
    public boolean isNotEmpty() {
        return !isEmpty();
    }

    /**
     * Get the List of Throwables
     *
     * @return a List of Throwables
     */
    public List<Throwable> getThrowables() {
        return throwables;
    }

    /**
     * Get the first Throwable
     *
     * @return if a Throwable was thrown, an Optional that contains the first Throwable, else Optional.empty()
     */
    public Optional<Throwable> getFirst() {
        return throwables.isEmpty() ? Optional.empty() : Optional.of(throwables.get(0));
    }

    /**
     * Throws the first Throwable if not empty
     *
     * @throws Throwable Throwable
     */
    public Cleanup assertEmpty() throws Throwable {
        if (isNotEmpty()) {
            throw throwables.get(0);
        }

        return this;
    }

    /**
     * Removes any collected Throwables
     *
     * @return this
     */
    public Cleanup clear() {
        throwables.clear();
        return this;
    }

    /** Interface to implement Action */
    public interface Action {

        /**
         * Perform method
         *
         * @throws Throwable Throwable
         */
        void perform() throws Throwable;
    }
}
