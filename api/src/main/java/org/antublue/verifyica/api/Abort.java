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

/** Class to implement Return */
public class Abort extends RuntimeException {

    /**
     * Constructor
     *
     * @param message message
     */
    private Abort(String message) {
        super(message);
    }

    /**
     * Return with a message
     *
     * @param message message
     */
    public static void execute(String message) {
        throw new Abort(message);
    }

    /** Return without a message */
    public static void execute() {
        throw new Abort("returned");
    }

    /**
     * Return without a message if condition is true
     *
     * @param condition condition
     */
    public static void ifTrue(boolean condition) {
        if (condition) {
            throw new Abort("condition != true");
        }
    }

    /**
     * Return with a message if condition is true
     *
     * @param condition condition
     */
    public static void ifTrue(boolean condition, String message) {
        if (condition) {
            throw new Abort(message);
        }
    }

    /**
     * Return without a message if condition is false
     *
     * @param condition condition
     */
    public static void ifFalse(boolean condition) {
        if (!condition) {
            throw new Abort("condition != false");
        }
    }

    /**
     * Return with a message if condition is false
     *
     * @param condition condition
     */
    public static void ifFalse(boolean condition, String message) {
        if (!condition) {
            throw new Abort(message);
        }
    }
}
