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

/** Class to implement Fail */
public class Fail extends RuntimeException {

    /**
     * Constructor
     *
     * @param message message
     */
    private Fail(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param message message
     * @param throwable throwable
     */
    private Fail(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Fail
     *
     * @param message message
     */
    public static void fail(String message) {
        throw new Fail(message);
    }

    /**
     * Fail
     *
     * @param message message
     * @param throwable throwable
     */
    public static void fail(String message, Throwable throwable) {
        throw new Fail(message, throwable);
    }

    /**
     * Fail if true
     *
     * @param condition condition
     * @param message message
     */
    public static void failIfTrue(boolean condition, String message) {
        if (condition) {
            throw new Fail(message);
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
            throw new Fail(message, throwable);
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
            throw new Fail(message);
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
            throw new Fail(message, throwable);
        }
    }
}
