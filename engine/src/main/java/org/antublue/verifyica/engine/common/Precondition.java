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

package org.antublue.verifyica.engine.common;

/** Class to implement Precondition */
public class Precondition {

    /** Constructor */
    private Precondition() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to validate an Object is not null, throwing an IllegalArgumentException if it is null
     *
     * @param object object
     * @param message message
     * @return the Object
     */
    public static Object notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }

        return object;
    }

    /**
     * Method to validate a String is not null and not blank, throwing an IllegalArgumentException
     * if it is null or blank
     *
     * @param string string
     * @param nullMessage nullMessage
     * @param blankMessage blankMessage
     * @return the String trimmed
     */
    public static String notNullOrBlank(String string, String nullMessage, String blankMessage) {
        if (string == null) {
            throw new IllegalArgumentException(nullMessage);
        }

        String trimmed = string.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(blankMessage);
        }

        return trimmed;
    }

    /**
     * Method to validate a condition is true, throwing an IllegalArgumentException if it is false
     *
     * @param condition condition
     * @param message message
     */
    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Method to validate a condition is false, throwing an IllegalArgumentException if it is true
     *
     * @param condition condition
     * @param message message
     */
    public static void isFalse(boolean condition, String message) {
        if (condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
