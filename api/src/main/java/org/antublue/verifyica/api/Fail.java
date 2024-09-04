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

package org.antublue.verifyica.api;

import java.util.function.Supplier;

/** Class to implement Fail */
public class Fail {

    /** Constructor */
    private Fail() {
        // INTENTIONALLY BLANK
    }

    /**
     * Fail
     *
     * @param message message
     */
    public static void fail(String message) {
        throw new AssertionError(message);
    }

    /**
     * Fail
     *
     * @param supplier supplier
     */
    public static void fail(Supplier<String> supplier) {
        String message = supplier != null ? supplier.get() : null;
        throw new AssertionError(message);
    }

    /**
     * Fail
     *
     * @param message message
     * @param throwable throwable
     */
    public static void fail(String message, Throwable throwable) {
        throw new AssertionError(message, throwable);
    }

    /**
     * Fail
     *
     * @param supplier supplier
     * @param throwable throwable
     */
    public static void fail(Supplier<String> supplier, Throwable throwable) {
        String message = supplier != null ? supplier.get() : null;
        throw new AssertionError(message, throwable);
    }

    /**
     * Fail if true
     *
     * @param condition condition
     * @param message message
     */
    public static void failIfTrue(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }

    /**
     * Fail if true
     *
     * @param condition condition
     * @param supplier supplier
     */
    public static void failIfTrue(boolean condition, Supplier<String> supplier) {
        if (condition) {
            String message = supplier != null ? supplier.get() : null;
            throw new AssertionError(message);
        }
    }

    /**
     * Fail if true
     *
     * @param condition condition
     * @param message message
     * @param throwable throwable
     */
    public static void failIfTrue(boolean condition, String message, Throwable throwable) {
        if (condition) {
            throw new AssertionError(message, throwable);
        }
    }

    /**
     * Fail if true
     *
     * @param condition condition
     * @param supplier supplier
     * @param throwable throwable
     */
    public static void failIfTrue(
            boolean condition, Supplier<String> supplier, Throwable throwable) {
        if (condition) {
            String message = supplier != null ? supplier.get() : null;
            throw new AssertionError(message, throwable);
        }
    }

    /**
     * Fail if false
     *
     * @param condition condition
     * @param message message
     */
    public static void failIfFalse(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    /**
     * Fail if false
     *
     * @param condition condition
     * @param supplier supplier
     */
    public static void failIfFalse(boolean condition, Supplier<String> supplier) {
        if (!condition) {
            String message = supplier != null ? supplier.get() : null;
            throw new AssertionError(message);
        }
    }

    /**
     * Fail if false
     *
     * @param condition condition
     * @param message message
     * @param throwable throwable
     */
    public static void failIfFalse(boolean condition, String message, Throwable throwable) {
        if (!condition) {
            throw new AssertionError(message, throwable);
        }
    }

    /**
     * Fail if false
     *
     * @param condition condition
     * @param supplier supplier
     * @param throwable throwable
     */
    public static void failIfFalse(
            boolean condition, Supplier<String> supplier, Throwable throwable) {
        if (!condition) {
            String message = supplier != null ? supplier.get() : null;
            throw new AssertionError(message, throwable);
        }
    }
}
