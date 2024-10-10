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

import static java.lang.String.format;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Class to implement Runner
 *
 * <p>Deprecated - @see Trap
 */
@Deprecated
public class Runner {

    private final List<Throwable> throwables;

    /** Constructor */
    public Runner() {
        throwables = new ArrayList<>();
    }

    /**
     * Perform a Task. If a Throwable occurs, collect it
     *
     * @param task task
     * @return this
     */
    public Runner perform(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("task is null");
        }

        try {
            task.perform();
        } catch (Throwable t) {
            throwables.add(t);
        }

        return this;
    }

    /**
     * Perform an array of Tasks. For each task, if a Throwable occurs, collect it
     *
     * @param tasks tasks
     * @return this
     */
    public Runner perform(Task... tasks) {
        if (tasks == null) {
            throw new IllegalArgumentException("tasks is null");
        }

        if (tasks.length == 0) {
            throw new IllegalArgumentException("tasks is empty");
        }

        // Validate the array doesn't have any null Tasks
        for (int i = 0; i < tasks.length; i++) {
            Task task = tasks[i];
            if (task == null) {
                throw new IllegalArgumentException(format("tasks[%d] is null", i));
            }
        }

        for (int i = 0; i < tasks.length; i++) {
            try {
                tasks[i].perform();
            } catch (Throwable t) {
                throwables.add(t);
            }
        }

        return this;
    }

    /**
     * Perform a Collection of Tasks. For each task, if a Throwable occurs, collect it
     *
     * @param tasks tasks
     * @return this
     */
    public Runner perform(Collection<Task> tasks) {
        if (tasks == null) {
            throw new IllegalArgumentException("tasks is null");
        }

        if (tasks.isEmpty()) {
            throw new IllegalArgumentException("tasks is empty");
        }

        // Validate the Collection doesn't have any null Tasks
        int i = 0;
        for (Task task : tasks) {
            if (task == null) {
                throw new IllegalArgumentException(format("tasks[%d] is null", i));
            }
            i++;
        }

        for (Task task : tasks) {
            try {
                task.perform();
            } catch (Throwable t) {
                throwables.add(t);
            }
        }

        return this;
    }

    /**
     * Check if any Task threw a Throwable
     *
     * @return true if no Throwables have been collected, else false
     */
    public boolean isEmpty() {
        return throwables.isEmpty();
    }

    /**
     * Check if any Task threw a Throwable
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
    public List<Throwable> throwables() {
        return throwables;
    }

    /**
     * Get the first Throwable
     *
     * @return if a Throwable was thrown, an Optional that contains the first Throwable, else Optional.empty()
     */
    public Optional<Throwable> firstThrowable() {
        return throwables.isEmpty() ? Optional.empty() : Optional.of(throwables.get(0));
    }

    /**
     * Asserts no Throwables have been collected. If any Throwables have been collected, throw the first Throwable
     *
     * @throws Throwable Throwable
     * @return this
     */
    public Runner assertSuccessful() throws Throwable {
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
    public Runner clear() {
        throwables.clear();
        return this;
    }

    /**
     * Creates a Task to close a Closeable object if not null
     *
     * @param closeable closeable
     * @return a Task
     */
    public static Task closeTask(Closeable closeable) {
        return () -> {
            if (closeable != null) {
                closeable.close();
            }
        };
    }

    /** Interface to implement Task */
    public interface Task {

        /**
         * Method to perform the task
         *
         * @throws Throwable Throwable
         */
        void perform() throws Throwable;
    }
}
