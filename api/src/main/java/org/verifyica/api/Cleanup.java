/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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

import static java.lang.String.format;

import java.io.Closeable;
import java.util.List;
import java.util.Optional;

/**
 * Class to implement Cleanup
 *
 * <p>Deprecated - @see Trap
 */
@Deprecated
public class Cleanup {

    private final Runner runner;

    /** Constructor */
    public Cleanup() {
        runner = new Runner();
    }

    /**
     * Performs a Cleanup.Action, collecting the Throwable if thrown
     *
     * @param action executable
     * @return this
     */
    public Cleanup perform(Action action) {
        if (action == null) {
            throw new IllegalArgumentException("action is null");
        }

        runner.perform(action::perform);

        return this;
    }

    /**
     * Performs an array of Cleanup.Actions, collecting the Throwables if thrown
     *
     * @param actions executables
     * @return this
     */
    public Cleanup perform(Action... actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions is null");
        }

        if (actions.length == 0) {
            throw new IllegalArgumentException("actions is empty");
        }

        int i = 0;
        for (Action action : actions) {
            if (action == null) {
                throw new IllegalArgumentException(format("action[%d] is null", i));
            }
            perform(action);
            i++;
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
        runner.perform(Runner.closeTask(closeable));

        return this;
    }

    /**
     * Close an array of Closeables
     *
     * @param closeables closeables
     * @return this
     */
    public Cleanup close(Closeable... closeables) {
        if (closeables == null) {
            throw new IllegalArgumentException("closeables is null");
        }

        if (closeables.length == 0) {
            throw new IllegalArgumentException("closeables is empty");
        }

        for (Closeable closeable : closeables) {
            close(closeable);
        }

        return this;
    }

    /**
     * Returns if the Cleanup has collected any Throwables
     *
     * @return true if no Throwables have been collected, else false
     */
    public boolean isEmpty() {
        return runner.isEmpty();
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
        return runner.throwables();
    }

    /**
     * Get the first Throwable
     *
     * @return if a Throwable was thrown, an Optional that contains the first Throwable, else Optional.empty()
     */
    public Optional<Throwable> getFirst() {
        return runner.firstThrowable();
    }

    /**
     * Throws the first Throwable if not empty
     *
     * @throws Throwable Throwable
     * @return this
     */
    public Cleanup assertEmpty() throws Throwable {
        runner.assertSuccessful();
        return this;
    }

    /**
     * Removes any collected Throwables
     *
     * @return this
     */
    public Cleanup clear() {
        runner.clear();
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
