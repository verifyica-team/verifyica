/*
 * Copyright (C) Verifyica project authors and contributors
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
 * Class to implement Execution
 */
public class Execution {

    /**
     * Constructor
     */
    private Execution() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Throws an ExecutionSkippedException if condition
     *
     * @param condition condition
     * @throws ExecutionSkippedException ExecutionSkippedException
     */
    public static void skipIfCondition(boolean condition) throws ExecutionSkippedException {
        skipIfCondition(condition, null);
    }

    /**
     * Throws an ExecutionSkippedException if condition
     *
     * @param condition condition
     * @param message message
     * @throws ExecutionSkippedException ExecutionSkippedException
     */
    public static void skipIfCondition(boolean condition, String message) throws ExecutionSkippedException {
        if (condition) {
            if (message == null || message.trim().isEmpty()) {
                throw new ExecutionSkippedException();
            } else {
                throw new ExecutionSkippedException(message);
            }
        }
    }

    /**
     * Throws an ExecutionSkippedException if condition
     *
     * @param supplier supplier
     * @throws ExecutionSkippedException ExecutionSkippedException
     */
    public static void skipIfCondition(BooleanSupplier supplier) throws ExecutionSkippedException {
        if (supplier == null) {
            throw new IllegalArgumentException("supplier is null");
        }

        skipIfCondition(supplier.getAsBoolean(), null);
    }

    /**
     * Throws an ExecutionSkippedException if condition
     *
     * @param supplier supplier
     * @param message message
     * @throws ExecutionSkippedException ExecutionSkippedException
     */
    public static void skipIfCondition(BooleanSupplier supplier, String message) throws ExecutionSkippedException {
        if (supplier == null) {
            throw new IllegalArgumentException("supplier is null");
        }

        skipIfCondition(supplier.getAsBoolean(), message);
    }

    /**
     * Throws an ExecutionSkippedException if not condition
     *
     * @param condition condition
     * @throws ExecutionSkippedException ExecutionSkippedException
     */
    public static void skipIfNotCondition(boolean condition) throws ExecutionSkippedException {
        skipIfNotCondition(condition, null);
    }

    /**
     * Throws an ExecutionSkippedException if not condition
     *
     * @param condition condition
     * @param message message
     * @throws ExecutionSkippedException ExecutionSkippedException
     */
    public static void skipIfNotCondition(boolean condition, String message) throws ExecutionSkippedException {
        if (!condition) {
            if (message == null || message.trim().isEmpty()) {
                throw new ExecutionSkippedException();
            } else {
                throw new ExecutionSkippedException(message);
            }
        }
    }

    /**
     * Throws an ExecutionSkippedException if not condition
     *
     * @param supplier supplier
     * @throws ExecutionSkippedException ExecutionSkippedException
     */
    public static void skipIfNotCondition(BooleanSupplier supplier) throws ExecutionSkippedException {
        if (supplier == null) {
            throw new IllegalArgumentException("supplier is null");
        }

        skipIfNotCondition(supplier.getAsBoolean(), null);
    }

    /**
     * Throws an ExecutionSkippedException if not condition
     *
     * @param supplier supplier
     * @param message message
     * @throws ExecutionSkippedException ExecutionSkippedException
     */
    public static void skipIfNotCondition(BooleanSupplier supplier, String message) throws ExecutionSkippedException {
        if (supplier == null) {
            throw new IllegalArgumentException("supplier is null");
        }

        skipIfNotCondition(supplier.getAsBoolean(), message);
    }

    /** Class to implement ExecutionSkippedException */
    public static class ExecutionSkippedException extends RuntimeException {

        /**
         * Constructor
         */
        public ExecutionSkippedException() {
            super();
        }

        /**
         * Constructor
         *
         * @param message message
         */
        public ExecutionSkippedException(String message) {
            super(message);
        }
    }
}
