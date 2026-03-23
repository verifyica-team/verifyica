/*
 * Copyright 2024-present Verifyica project authors and contributors. All rights reserved.
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

import static java.lang.String.format;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.verifyica.api.Configuration;
import org.verifyica.engine.common.Precondition;
import org.verifyica.engine.configuration.ConcreteConfiguration;
import org.verifyica.engine.configuration.Constants;

/**
 * A simple logger for test output.
 */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Logger {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    private final String name;
    private final AtomicReference<Level> level;

    /**
     * Constructs a Logger with the specified name.
     *
     * @param name the logger name
     */
    Logger(String name) {
        this.name = name;
        this.level = new AtomicReference<>(Level.INFO);

        Configuration configuration = ConcreteConfiguration.getInstance();

        String loggerLevel =
                configuration.getProperties().getProperty(Constants.ENGINE_LOGGER_LEVEL, Level.INFO.toString());
        String regex = configuration.getProperties().getProperty(Constants.ENGINE_LOGGER_REGEX, ".*");

        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                level.set(Level.decode(loggerLevel));
            }
        } catch (Throwable t) {
            // INTENTIONALLY EMPTY
        }
    }

    /**
     * Returns whether TRACE logging is enabled.
     *
     * @return true if TRACE is enabled, otherwise false
     */
    public boolean isTraceEnabled() {
        return level.get().toInt() >= Level.TRACE.toInt();
    }

    /**
     * Returns whether DEBUG logging is enabled.
     *
     * @return true if DEBUG is enabled, otherwise false
     */
    public boolean isDebugEnabled() {
        return level.get().toInt() >= Level.DEBUG.toInt();
    }

    /**
     * Returns whether INFO logging is enabled.
     *
     * @return true if INFO is enabled, otherwise false
     */
    public boolean isInfoEnabled() {
        return level.get().toInt() >= Level.INFO.toInt();
    }

    /**
     * Returns whether WARN logging is enabled.
     *
     * @return true if WARN is enabled, otherwise false
     */
    public boolean isWarnEnabled() {
        return level.get().toInt() >= Level.WARN.toInt();
    }

    /**
     * Returns whether ERROR logging is enabled.
     *
     * @return true if ERROR is enabled, otherwise false
     */
    public boolean isErrorEnabled() {
        return level.get().toInt() >= Level.ERROR.toInt();
    }

    /**
     * Sets the logging level.
     *
     * @param level the logging level to set
     */
    public void setLevel(Level level) {
        Precondition.notNull(level, "level is null");

        this.level.set(level);
    }

    /**
     * Returns whether the specified logging level is enabled.
     *
     * @param level the level to check
     * @return true if the level is enabled, otherwise false
     */
    public boolean isEnabled(Level level) {
        Precondition.notNull(level, "level is null");

        return this.level.get().toInt() >= level.toInt();
    }

    /**
     * Logs a TRACE message.
     *
     * @param message the message to log
     */
    public void trace(String message) {
        if (isTraceEnabled()) {
            log(System.out, Level.TRACE, "%s", message);
        }
    }

    /**
     * Logs a formatted TRACE message.
     *
     * @param format the format string
     * @param objects the arguments to format
     */
    public void trace(String format, Object... objects) {
        Precondition.notNullOrBlank(format, "format is null", "format is blank");

        if (isTraceEnabled()) {
            log(System.out, Level.TRACE, format, objects);
        }
    }

    /**
     * Logs a DEBUG message.
     *
     * @param message the message to log
     */
    public void debug(String message) {
        if (isDebugEnabled()) {
            log(System.out, Level.DEBUG, "%s", message);
        }
    }

    /**
     * Logs a formatted DEBUG message.
     *
     * @param format the format string
     * @param objects the arguments to format
     */
    public void debug(String format, Object... objects) {
        Precondition.notNullOrBlank(format, "format is null", "format is blank");

        if (isDebugEnabled()) {
            log(System.out, Level.DEBUG, format, objects);
        }
    }

    /**
     * Logs an INFO message.
     *
     * @param message the message to log
     */
    public void info(String message) {
        if (isInfoEnabled()) {
            log(System.out, Level.INFO, "%s", message);
        }
    }

    /**
     * Logs a formatted INFO message.
     *
     * @param format the format string
     * @param objects the arguments to format
     */
    public void info(String format, Object... objects) {
        Precondition.notNullOrBlank(format, "format is null", "format is blank");

        if (isInfoEnabled()) {
            log(System.out, Level.INFO, format, objects);
        }
    }

    /**
     * Logs a WARN message.
     *
     * @param message the message to log
     */
    public void warn(String message) {
        if (isWarnEnabled()) {
            log(System.out, Level.WARN, "%s", message);
        }
    }

    /**
     * Logs a formatted WARN message.
     *
     * @param format the format string
     * @param objects the arguments to format
     */
    public void warn(String format, Object... objects) {
        Precondition.notNullOrBlank(format, "format is null", "format is blank");

        if (isWarnEnabled()) {
            log(System.out, Level.WARN, format, objects);
        }
    }

    /**
     * Logs an ERROR message.
     *
     * @param message the message to log
     */
    public void error(String message) {
        if (isErrorEnabled()) {
            log(System.err, Level.ERROR, "%s", message);
        }
    }

    /**
     * Logs a formatted ERROR message.
     *
     * @param format the format string
     * @param objects the arguments to format
     */
    public void error(String format, Object... objects) {
        Precondition.notNullOrBlank(format, "format is null", "format is blank");

        if (isErrorEnabled()) {
            log(System.err, Level.ERROR, format, objects);
        }
    }

    /**
     * Flushes the output streams.
     */
    public void flush() {
        System.err.flush();
        System.out.flush();
    }

    /**
     * Logs a message to the specified PrintStream.
     *
     * @param printStream the print stream to log to
     * @param level the logging level
     * @param format the format string
     * @param objects the arguments to format
     */
    private void log(PrintStream printStream, Level level, String format, Object... objects) {
        printStream.println(LocalDateTime.now().format(DATE_TIME_FORMATTER)
                + " | "
                + Thread.currentThread().getName()
                + " | "
                + level.toString()
                + " | "
                + name
                + " | "
                + format(format, objects));
    }
}
