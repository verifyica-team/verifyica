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

    private final List<CleanupTask> cleanupTasks = new ArrayList<>();
    private final List<Throwable> throwables = new ArrayList<>();
    private boolean hasRun = false;

    /**
     * Adds a cleanup task
     *
     * @param cleanupTask the cleanup task to add
     * @return this CleanupExecutor
     */
    public CleanupExecutor addTask(CleanupTask cleanupTask) {
        if (cleanupTask == null) {
            throw new IllegalArgumentException("cleanupAction is null");
        }

        cleanupTasks.add(cleanupTask);

        return this;
    }

    /**
     * Adds a cleanup task that is executed only if the supplied value is present (non-null)
     *
     * @param supplier      the supplier of the value
     * @param cleanupAction the cleanup task to add
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
     * Adds multiple cleanup tasks
     *
     * @param cleanupActions the cleanup tasks to add
     * @return this CleanupExecutor
     */
    public CleanupExecutor addCleanupTasks(List<? extends CleanupTask> cleanupActions) {
        if (cleanupActions == null) {
            throw new IllegalArgumentException("cleanupActions is null");
        }

        for (CleanupTask cleanupTask : cleanupActions) {
            addTask(cleanupTask);
        }

        return this;
    }

    /**
     * Gets the list of added cleanup tasks
     *
     * @return the list of added cleanup tasks
     */
    public List<CleanupTask> cleanupActions() {
        return Collections.unmodifiableList(cleanupTasks);
    }

    /**
     * Gets the list of failures occurred during execution. A failure is represented by a Throwable.
     * For any cleanup task that throws a Throwable during execution,
     * the Throwable is collected in this list, else null is added.
     *
     * @return the list of failures
     */
    public List<Throwable> throwables() {
        return Collections.unmodifiableList(throwables);
    }

    /**
     * Runs all added cleanup tasks in order, collecting any thrown throwables.
     *
     * @return this CleanupExecutor
     */
    public CleanupExecutor execute() {
        if (hasRun) {
            throw new IllegalStateException("CleanupExecutor has already been run");
        }

        hasRun = true;
        throwables.clear();

        for (CleanupTask cleanupTask : cleanupTasks) {
            try {
                cleanupTask.run();
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
