/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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

package org.verifyica.examples.support;

import java.util.Random;

public class TestSupport {

    /** Constructor */
    private TestSupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to log a message
     *
     * @param object object
     */
    public static void info(Object object) {
        System.out.println(object);
    }

    /**
     * Method to log a message
     *
     * @param format format
     * @param objects objects
     */
    public static void info(String format, Object... objects) {
        if (format == null) {
            throw new IllegalArgumentException("format is null");
        }

        if (format.trim().isEmpty()) {
            throw new IllegalArgumentException("format is blank");
        }

        System.out.printf(format + "%n", objects);
    }

    /**
     * Method to create a random string
     *
     * @param length length
     * @return a random String
     */
    public static String randomString(int length) {
        return new Random()
                .ints(97, 123 + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
