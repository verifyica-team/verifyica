/*
 * Copyright (C) Verifyica project authors and contributors. All rights reserved.
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

package org.verifyica.api.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Class to implement CleanupExecutor
 */
public final class CleanupExecutor {

    private final List<ThrowableTask> throwableTasks = new ArrayList<>();
    private final List<Throwable> throwables = new ArrayList<>();
    private boolean hasRun = false;

    /**
     * Creates a new instance of CleanupExecutor
     */
    public CleanupExecutor() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Adds a throwable task
     *
     * @param throwableTask the throwable task to add
     * @return this CleanupExecutor
     */
    public CleanupExecutor addTask(ThrowableTask throwableTask) {
        if (throwableTask == null) {
            throw new IllegalArgumentException("cleanupAction is null");
        }

        throwableTasks.add(throwableTask);

        return this;
    }

    /**
     * Adds a throwable task that is executed only if the supplied value is present (non-null)
     *
     * @param supplier      the supplier of the value
     * @param cleanupAction the throwable task to add
     * @param <T>           the type of the value
     * @return this CleanupExecutor
     */
    public <T> CleanupExecutor addTaskIfPresent(Supplier<? extends T> supplier, Consumer<? super T> cleanupAction) {
        if (supplier == null) {
            throw new IllegalArgumentException("supplier is null");
        }
        if (cleanupAction == null) {
            throw new IllegalArgumentException("cleanupAction is null");
        }

        return addTask(() -> Optional.ofNullable(supplier.get()).ifPresent(cleanupAction));
    }

    /**
     * Adds multiple throwable tasks
     *
     * @param cleanupActions the throwable tasks to add
     * @return this CleanupExecutor
     */
    public CleanupExecutor addTasks(List<? extends ThrowableTask> cleanupActions) {
        if (cleanupActions == null) {
            throw new IllegalArgumentException("cleanupActions is null");
        }

        for (ThrowableTask throwableTask : cleanupActions) {
            addTask(throwableTask);
        }

        return this;
    }

    /**
     * Gets the list of added throwable tasks
     *
     * @return the list of added throwable tasks
     */
    public List<ThrowableTask> cleanupActions() {
        return Collections.unmodifiableList(throwableTasks);
    }

    /**
     * Gets the list of failures occurred during execution. A failure is represented by a Throwable.
     * For any throwable task that throws a Throwable during execution,
     * the Throwable is collected in this list, else null is added.
     *
     * @return the list of failures
     */
    public List<Throwable> throwables() {
        return Collections.unmodifiableList(throwables);
    }

    /**
     * Runs all added throwable tasks in order, collecting any thrown throwables.
     *
     * @return this CleanupExecutor
     */
    public CleanupExecutor execute() {
        if (hasRun) {
            throw new IllegalStateException("CleanupExecutor has already run");
        }

        hasRun = true;
        throwables.clear();

        for (ThrowableTask throwableTask : throwableTasks) {
            try {
                throwableTask.run();
                throwables.add(null);
            } catch (Throwable t) {
                throwables.add(t);
            }
        }

        return this;
    }

    /**
     * Checks if there are any throwables collected during execution
     *
     * @return true if there are throwables, false otherwise
     */
    public boolean hasThrowables() {
        return !throwables.isEmpty();
    }

    /**
     * Throws the first throwable in the list of failures, if any.
     *
     * @throws Throwable the first throwable in the list of failures
     */
    public void throwIfFailed() throws Throwable {
        if (!hasRun) {
            execute();
        }

        Throwable firstThrowable = null;

        for (Throwable throwable : throwables) {
            if (throwable == null) {
                continue;
            }

            if (firstThrowable == null) {
                firstThrowable = throwable;
            } else {
                firstThrowable.addSuppressed(throwable);
            }
        }

        if (firstThrowable != null) {
            throw firstThrowable;
        }
    }
}
