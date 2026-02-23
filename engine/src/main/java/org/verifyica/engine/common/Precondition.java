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
 * A utility class that provides static methods for validating preconditions and arguments.
 *
 * <p>This class is used throughout the engine to ensure that method arguments and
 * preconditions are met. All validation methods throw {@link IllegalArgumentException}
 * when validation fails, providing clear error messages for debugging.
 *
 * @see IllegalArgumentException
 */
public class Precondition {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Precondition() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Validates that an object is not null, throwing an {@link IllegalArgumentException} if it is null.
     *
     * @param object the object to validate
     * @param message the exception message to use if validation fails
     * @throws IllegalArgumentException if object is null
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates that a string is not null and not blank (empty or whitespace-only),
     * throwing an {@link IllegalArgumentException} if validation fails.
     *
     * @param string the string to validate
     * @param nullMessage the exception message to use if the string is null
     * @param blankMessage the exception message to use if the string is blank
     * @throws IllegalArgumentException if string is null or blank
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
     * Checks if a string is blank (null, empty, or contains only whitespace characters).
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
     * Validates that a condition is true, throwing an {@link IllegalArgumentException} if it is false.
     *
     * @param condition the condition to validate
     * @param message the exception message to use if validation fails
     * @throws IllegalArgumentException if condition is false
     */
    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates that a condition is false, throwing an {@link IllegalArgumentException} if it is true.
     *
     * @param condition the condition to validate
     * @param message the exception message to use if validation fails
     * @throws IllegalArgumentException if condition is true
     */
    public static void isFalse(boolean condition, String message) {
        if (condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
