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

package org.antublue.verifyica.engine.logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.antublue.verifyica.engine.configuration.ConcreteConfiguration;

/** Class to implement LoggerFactory */
@SuppressWarnings("PMD.EmptyCatchBlock")
public final class LoggerFactory {

    private final Map<String, Logger> loggerMap = new ConcurrentHashMap<>();

    /** Constructor */
    private LoggerFactory() {
        // DO NOTHING
    }

    /**
     * Method to create a Logger
     *
     * @param name name
     * @return the return value
     */
    private Logger createLogger(String name) {
        return loggerMap.computeIfAbsent(name, Logger::new);
    }

    /**
     * Method to get a Logger by Class name
     *
     * @param clazz clazz
     * @return the return value
     */
    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    /**
     * Method to get a Logger by name
     *
     * @param name name
     * @return the return value
     */
    public static Logger getLogger(String name) {
        synchronized (ConcreteConfiguration.getInstance()) {
            return SingletonHolder.SINGLETON.createLogger(name);
        }
    }

    /** Class to hold the singleton instance */
    private static final class SingletonHolder {

        /** The singleton instance */
        private static final LoggerFactory SINGLETON = new LoggerFactory();
    }
}
