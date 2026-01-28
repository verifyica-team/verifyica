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

package org.verifyica.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Class to implement CleanupPlan
 */
public final class CleanupPlan {

    private final List<CleanupAction> cleanupActions = new ArrayList<>();
    private final List<Throwable> throwables = new ArrayList<>();
    private boolean hasRun = false;

    /**
     * Adds a cleanup action to the plan
     *
     * @param cleanupAction the cleanup action to add
     * @return this CleanupPlan
     */
    public CleanupPlan addAction(CleanupAction cleanupAction) {
        if (cleanupAction == null) {
            throw new IllegalArgumentException("cleanupAction is null");
        }

        cleanupActions.add(cleanupAction);

        return this;
    }

    /**
     * Adds a cleanup action to the plan that is executed only if the supplied value is present (non-null)
     *
     * @param supplier      the supplier of the value
     * @param cleanupAction the cleanup action to add
     * @param <T>           the type of the value
     * @return this CleanupPlan
     */
    public <T> CleanupPlan addActionIfPresent(Supplier<? extends T> supplier, Consumer<? super T> cleanupAction) {
        if (supplier == null) {
            throw new IllegalArgumentException("supplier is null");
        }
        if (cleanupAction == null) {
            throw new IllegalArgumentException("cleanupAction is null");
        }

        return addAction(() -> Optional.ofNullable(supplier.get()).ifPresent(cleanupAction));
    }

    /**
     * Adds multiple cleanup actions to the plan
     *
     * @param cleanupActions the cleanup actions to add
     * @return this CleanupPlan
     */
    public CleanupPlan addCleanupActions(List<? extends CleanupAction> cleanupActions) {
        if (cleanupActions == null) {
            throw new IllegalArgumentException("cleanupActions is null");
        }

        for (CleanupAction cleanupAction : cleanupActions) {
            addAction(cleanupAction);
        }

        return this;
    }

    /**
     * Gets the list of added cleanup actions
     *
     * @return the list of added cleanup actions
     */
    public List<CleanupAction> cleanupActions() {
        return Collections.unmodifiableList(cleanupActions);
    }

    /**
     * Gets the list of failures occurred during execution. A failure is represented by a Throwable.
     * For any cleanup action that throws a Throwable during execution,
     * the Throwable is collected in this list, else null is added.
     *
     * @return the list of failures
     */
    public List<Throwable> throwables() {
        return Collections.unmodifiableList(throwables);
    }

    /**
     * Runs all added cleanup actions in order, collecting any thrown throwables.
     *
     * @return this CleanupPlan
     */
    public CleanupPlan run() {
        if (hasRun) {
            throw new IllegalStateException("CleanupPlan has already been run");
        }

        hasRun = true;
        throwables.clear();

        for (CleanupAction cleanupAction : cleanupActions) {
            try {
                cleanupAction.run();
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
    public void verify() throws Throwable {
        if (!hasRun) {
            run();
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
