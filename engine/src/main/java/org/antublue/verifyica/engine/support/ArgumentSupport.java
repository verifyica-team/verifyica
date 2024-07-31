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

package org.antublue.verifyica.engine.support;

/** Class to implement ArgumentSupport */
public class ArgumentSupport {

    /** Constructor */
    private ArgumentSupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to validate an Object is not nul, throwing an IllegalArgumentException if it's null
     *
     * @param object object
     * @param message message
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Method to validate a String is not null and not empty, throwing an IllegalArgumentException
     * if it's null or empty
     *
     * @param string string
     * @param nullMessage nullMessage
     * @param emptyMessage emptyMessage
     */
    public static void notNullOrEmpty(String string, String nullMessage, String emptyMessage) {
        if (string == null) {
            throw new IllegalArgumentException(nullMessage);
        }

        if (string.trim().isEmpty()) {
            throw new IllegalArgumentException(emptyMessage);
        }
    }
}
