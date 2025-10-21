/*
 * Copyright (C) Verifyica project authors and contributors
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

package org.verifyica.test.logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to implement LoggerFactory
 */
@SuppressWarnings("PMD.EmptyCatchBlock")
public final class LoggerFactory {

    private static final Logger ROOT_LOGGER = new Logger("<ROOT>");

    private final Map<String, Logger> loggers = new ConcurrentHashMap<>();

    /**
     * Constructor
     */
    private LoggerFactory() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Method to get or create a Logger
     *
     * @param name name
     * @return a Logger
     */
    private Logger getOrCreateLogger(String name) {
        return loggers.computeIfAbsent(name, Logger::new);
    }

    /**
     * Method to get a Logger for a Class
     *
     * @param clazz clazz
     * @return a Logger
     */
    public static Logger getLogger(Class<?> clazz) {
        return clazz != null ? getLogger(clazz.getName()) : ROOT_LOGGER;
    }

    /**
     * Method to get a Logger by name
     *
     * @param name name
     * @return a Logger
     */
    public static Logger getLogger(String name) {
        Logger logger = null;

        if (name != null && !name.trim().isEmpty()) {
            logger = SingletonHolder.SINGLETON.getOrCreateLogger(name.trim());
        }

        return logger != null ? logger : ROOT_LOGGER;
    }

    /**
     * Class to hold the singleton instance
     */
    private static final class SingletonHolder {

        /**
         * The singleton instance
         */
        private static final LoggerFactory SINGLETON = new LoggerFactory();
    }
}
