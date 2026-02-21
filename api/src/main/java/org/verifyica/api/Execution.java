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

import java.util.function.BooleanSupplier;

/**
 * Utility class providing methods to skip test execution based on conditions.
 */
public class Execution {

    /**
     * Constructor.
     */
    private Execution() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Skips execution if the condition is true.
     *
     * @param condition the condition to check
     * @throws ExecutionSkippedException if the condition is true
     */
    public static void skipIfCondition(final boolean condition) throws ExecutionSkippedException {
        skipIfCondition(condition, null);
    }

    /**
     * Skips execution if the condition is true.
     *
     * @param condition the condition to check
     * @param message the skip message
     * @throws ExecutionSkippedException if the condition is true
     */
    public static void skipIfCondition(final boolean condition, final String message) throws ExecutionSkippedException {
        if (condition) {
            if (message == null || message.trim().isEmpty()) {
                throw new ExecutionSkippedException();
            } else {
                throw new ExecutionSkippedException(message);
            }
        }
    }

    /**
     * Skips execution if the supplier returns true.
     *
     * @param supplier the boolean supplier
     * @throws ExecutionSkippedException if the supplier returns true
     * @throws IllegalArgumentException if supplier is null
     */
    public static void skipIfCondition(final BooleanSupplier supplier) throws ExecutionSkippedException {
        if (supplier == null) {
            throw new IllegalArgumentException("supplier is null");
        }

        skipIfCondition(supplier.getAsBoolean(), null);
    }

    /**
     * Skips execution if the supplier returns true.
     *
     * @param supplier the boolean supplier
     * @param message the skip message
     * @throws ExecutionSkippedException if the supplier returns true
     * @throws IllegalArgumentException if supplier is null
     */
    public static void skipIfCondition(final BooleanSupplier supplier, final String message)
            throws ExecutionSkippedException {
        if (supplier == null) {
            throw new IllegalArgumentException("supplier is null");
        }

        skipIfCondition(supplier.getAsBoolean(), message);
    }

    /**
     * Skips execution if the condition is false.
     *
     * @param condition the condition to check
     * @throws ExecutionSkippedException if the condition is false
     */
    public static void skipIfNotCondition(final boolean condition) throws ExecutionSkippedException {
        skipIfNotCondition(condition, null);
    }

    /**
     * Skips execution if the condition is false.
     *
     * @param condition the condition to check
     * @param message the skip message
     * @throws ExecutionSkippedException if the condition is false
     */
    public static void skipIfNotCondition(final boolean condition, final String message)
            throws ExecutionSkippedException {
        if (!condition) {
            if (message == null || message.trim().isEmpty()) {
                throw new ExecutionSkippedException();
            } else {
                throw new ExecutionSkippedException(message);
            }
        }
    }

    /**
     * Skips execution if the supplier returns false.
     *
     * @param supplier the boolean supplier
     * @throws ExecutionSkippedException if the supplier returns false
     * @throws IllegalArgumentException if supplier is null
     */
    public static void skipIfNotCondition(final BooleanSupplier supplier) throws ExecutionSkippedException {
        if (supplier == null) {
            throw new IllegalArgumentException("supplier is null");
        }

        skipIfNotCondition(supplier.getAsBoolean(), null);
    }

    /**
     * Skips execution if the supplier returns false.
     *
     * @param supplier the boolean supplier
     * @param message the skip message
     * @throws ExecutionSkippedException if the supplier returns false
     * @throws IllegalArgumentException if supplier is null
     */
    public static void skipIfNotCondition(final BooleanSupplier supplier, final String message)
            throws ExecutionSkippedException {
        if (supplier == null) {
            throw new IllegalArgumentException("supplier is null");
        }

        skipIfNotCondition(supplier.getAsBoolean(), message);
    }

    /**
     * Exception thrown to indicate that test execution should be skipped.
     */
    public static class ExecutionSkippedException extends RuntimeException {

        /**
         * Creates a new ExecutionSkippedException with no message.
         */
        public ExecutionSkippedException() {
            super();
        }

        /**
         * Creates a new ExecutionSkippedException with the specified message.
         *
         * @param message the exception message
         */
        public ExecutionSkippedException(final String message) {
            super(message);
        }
    }
}
