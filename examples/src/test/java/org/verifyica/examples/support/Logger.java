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

package org.verifyica.examples.support;

import static java.lang.String.format;

/**
 * Class to implement Logger
 */
public class Logger {

    private final String name;

    /**
     * Constructor
     *
     * @param name name
     */
    private Logger(String name) {
        this.name = name;
    }

    /**
     * Log an info print
     *
     * @param object object
     */
    public void info(Object object) {
        System.out.printf("%s | %s%n", name, object);
    }

    /**
     * Log an info print
     *
     * @param format format
     * @param objects objects
     */
    public void info(String format, Object... objects) {
        if (format == null) {
            throw new IllegalArgumentException("format is null");
        }

        if (format.trim().isEmpty()) {
            throw new IllegalArgumentException("format is blank");
        }

        info(format(format, objects));
    }

    /**
     * Create a Logger
     *
     * @param clazz clazz
     * @return a Logger
     */
    public static Logger createLogger(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        }

        return new Logger(clazz.getName());
    }
}
