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

package org.verifyica.engine.common;

/**
 * Utility class for precondition validation.
 */
public class Precondition {

    /**
     * Constructor
     */
    private Precondition() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Validates an Object is not null, throwing an IllegalArgumentException if it is null.
     *
     * @param object the object to validate
     * @param message the exception message
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates a String is not null and not blank, throwing an IllegalArgumentException
     * if it is null or blank.
     *
     * @param string the string to validate
     * @param nullMessage the exception message if the string is null
     * @param blankMessage the exception message if the string is blank
     */
    public static void notNullOrBlank(String string, String nullMessage, String blankMessage) {
        if (string == null) {
            throw new IllegalArgumentException(nullMessage);
        }

        if (isBlank(string)) {
            throw new IllegalArgumentException(blankMessage);
        }
    }

    /**
     * Checks if a String is blank (empty or contains only whitespace).
     *
     * @param string the string to check
     * @return true if the string is blank, false otherwise
     */
    private static boolean isBlank(String string) {
        int length = string.length();
        if (length == 0) {
            return true;
        }

        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(string.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validates a condition is true, throwing an IllegalArgumentException if it is false.
     *
     * @param condition the condition to validate
     * @param message the exception message
     */
    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates a condition is false, throwing an IllegalArgumentException if it is true.
     *
     * @param condition the condition to validate
     * @param message the exception message
     */
    public static void isFalse(boolean condition, String message) {
        if (condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
