/*
 * Copyright (C) 2023 The Verifyica project authors
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

package org.antublue.verifyica.maven.plugin.logger;

import static java.lang.String.format;

import org.apache.maven.plugin.logging.Log;

/** Class to implement Logger */
public class Logger {

    private final Log log;

    /**
     * Constructor
     *
     * @param log log
     */
    private Logger(Log log) {
        this.log = log;
    }

    /**
     * Method to log a DEBUG message
     *
     * @param message message
     */
    public void debug(String message) {
        if (log.isDebugEnabled()) {
            log.debug(message);
        }
    }

    /**
     * Method to log a DEBUG message
     *
     * @param format format
     * @param objects object
     */
    public void debug(String format, Object... objects) {
        if (log.isDebugEnabled()) {
            log.debug(format(format, objects));
        }
    }

    /**
     * Method to log an INFO message
     *
     * @param message message
     */
    public void info(String message) {
        if (log.isInfoEnabled()) {
            log.info(message);
        }
    }

    /**
     * Method to log an INFO message
     *
     * @param format format
     * @param objects object
     */
    public void info(String format, Object... objects) {
        if (log.isInfoEnabled()) {
            log.info(format(format, objects));
        }
    }

    /**
     * Method to log an WARN message
     *
     * @param message message
     */
    public void warn(String message) {
        if (log.isWarnEnabled()) {
            log.warn(message);
        }
    }

    /**
     * Method to log a WARN message
     *
     * @param format format
     * @param objects object
     */
    public void warn(String format, Object... objects) {
        if (log.isWarnEnabled()) {
            log.warn(format(format, objects));
        }
    }

    /**
     * Method to log an ERROR message
     *
     * @param message message
     */
    public void error(String message) {
        if (log.isErrorEnabled()) {
            log.error(message);
        }
    }

    /**
     * Method to log an ERROR message
     *
     * @param format format
     * @param objects object
     */
    public void error(String format, Object... objects) {
        if (log.isErrorEnabled()) {
            log.error(format(format, objects));
        }
    }

    /**
     * Method to create a Logger from a Maven Log
     *
     * @param log log
     * @return a Logger
     */
    public static Logger from(Log log) {
        return new Logger(log);
    }
}
