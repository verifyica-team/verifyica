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

package org.verifyica.engine.logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A factory for creating and managing {@link Logger} instances.
 *
 * <p>This class provides a centralized way to obtain logger instances. Loggers are
 * cached and reused for the same name, making it efficient to obtain loggers from
 * multiple locations in the codebase.
 *
 * <p>The factory uses a singleton pattern with lazy initialization to ensure only
 * one instance exists throughout the application lifecycle.
 *
 * @see Logger
 */
@SuppressWarnings("PMD.EmptyCatchBlock")
public final class LoggerFactory {

    private static final Logger ROOT_LOGGER = new Logger("<ROOT>");

    private final Map<String, Logger> loggers = new ConcurrentHashMap<>();

    /**
     * Private constructor to prevent instantiation since this is a factory class.
     */
    private LoggerFactory() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Gets or creates a logger with the specified name.
     *
     * @param name the name for the logger
     * @return the logger instance
     */
    private Logger getOrCreateLogger(String name) {
        return loggers.computeIfAbsent(name, Logger::new);
    }

    /**
     * Gets a logger for the specified class.
     *
     * @param clazz the class for which to get the logger (may be null)
     * @return a Logger instance
     */
    public static Logger getLogger(Class<?> clazz) {
        return clazz != null ? getLogger(clazz.getName()) : ROOT_LOGGER;
    }

    /**
     * Gets a logger with the specified name.
     *
     * @param name the name for the logger (may be null or blank)
     * @return a Logger instance
     */
    public static Logger getLogger(String name) {
        if (name == null) {
            return ROOT_LOGGER;
        }

        final String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return ROOT_LOGGER;
        }

        return SingletonHolder.SINGLETON.getOrCreateLogger(trimmed);
    }

    /**
     * Holds the singleton instance of LoggerFactory using lazy initialization.
     */
    private static final class SingletonHolder {

        /**
         * The singleton instance
         */
        private static final LoggerFactory SINGLETON = new LoggerFactory();
    }
}
