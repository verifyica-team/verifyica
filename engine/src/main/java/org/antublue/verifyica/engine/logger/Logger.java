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

import static java.lang.String.format;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antublue.verifyica.api.Configuration;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.configuration.DefaultConfiguration;
import org.antublue.verifyica.engine.support.ArgumentSupport;

/** Class to implement Logger */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Logger {

    private static final Configuration CONFIGURATION = DefaultConfiguration.getInstance();

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    private final String name;
    private Level level;

    /**
     * Constructor
     *
     * @param name name
     */
    Logger(String name) {
        this.name = name;
        this.level = Level.INFO;

        String loggerLevel =
                CONFIGURATION
                        .getOptional(Constants.ENGINE_LOGGER_LEVEL)
                        .orElse(Level.INFO.toString());

        String regex = CONFIGURATION.getOptional(Constants.ENGINE_LOGGER_REGEX).orElse(".*");

        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                level = Level.decode(loggerLevel);
            }
        } catch (Throwable t) {
            // INTENTIONALLY BLANK
        }
    }

    /**
     * Method to return if TRACE logging is enabled
     *
     * @return the return value
     */
    public boolean isTraceEnabled() {
        return level.toInt() >= Level.TRACE.toInt();
    }

    /**
     * Method to return if DEBUG logging is enabled
     *
     * @return the return value
     */
    public boolean isDebugEnabled() {
        return level.toInt() >= Level.DEBUG.toInt();
    }

    /**
     * Method to return if INFO logging is enabled
     *
     * @return the return value
     */
    public boolean isInfoEnabled() {
        return level.toInt() >= Level.INFO.toInt();
    }

    /**
     * Method to return if WARNING logging is enabled
     *
     * @return the return value
     */
    public boolean isWarnEnabled() {
        return level.toInt() >= Level.WARN.toInt();
    }

    /**
     * Method to return if ERROR logging is enabled
     *
     * @return the return value
     */
    public boolean isErrorEnabled() {
        return level.toInt() >= Level.ERROR.toInt();
    }

    /**
     * Method to dynamically change the logging level
     *
     * @param level level
     */
    public void setLevel(Level level) {
        ArgumentSupport.notNull(name, "level is null");
        this.level = level;
    }

    /**
     * Method to return if a specific Level is enabled
     *
     * @param level level
     * @return the return value
     */
    public boolean isEnabled(Level level) {
        ArgumentSupport.notNull(name, "level is null");
        return this.level.toInt() >= level.toInt();
    }

    /**
     * Method to log a TRACE message
     *
     * @param message message
     */
    public void trace(String message) {
        if (isTraceEnabled()) {
            log(System.out, Level.TRACE, "%s", message);
        }
    }

    /**
     * Method to log a TRACE message
     *
     * @param format format
     * @param objects objects
     */
    public void trace(String format, Object... objects) {
        ArgumentSupport.notNullOrEmpty(format, "format is null", "format is empty");

        if (isTraceEnabled()) {
            log(System.out, Level.TRACE, format, objects);
        }
    }

    /**
     * Method to log a DEBUG message
     *
     * @param message message
     */
    public void debug(String message) {
        if (isDebugEnabled()) {
            log(System.out, Level.DEBUG, "%s", message);
        }
    }

    /**
     * Method to log a DEBUG message
     *
     * @param format format
     * @param objects objects
     */
    public void debug(String format, Object... objects) {
        ArgumentSupport.notNullOrEmpty(format, "format is null", "format is empty");

        if (isDebugEnabled()) {
            log(System.out, Level.DEBUG, format, objects);
        }
    }

    /**
     * Method to log an INFO message
     *
     * @param message message
     */
    public void info(String message) {

        if (isInfoEnabled()) {
            log(System.out, Level.INFO, "%s", message);
        }
    }

    /**
     * Method to log an INFO message
     *
     * @param format format
     * @param objects objects
     */
    public void info(String format, Object... objects) {
        ArgumentSupport.notNullOrEmpty(format, "format is null", "format is empty");

        if (isInfoEnabled()) {
            log(System.out, Level.INFO, format, objects);
        }
    }

    /**
     * Method to log a WARN message
     *
     * @param message message
     */
    public void warn(String message) {
        if (isWarnEnabled()) {
            log(System.out, Level.WARN, "%s", message);
        }
    }

    /**
     * Method to log an WARN message
     *
     * @param format format
     * @param objects objects
     */
    public void warn(String format, Object... objects) {
        ArgumentSupport.notNullOrEmpty(format, "format is null", "format is empty");

        if (isWarnEnabled()) {
            log(System.out, Level.WARN, format, objects);
        }
    }

    /**
     * Method to log an ERROR message
     *
     * @param message message
     */
    public void error(String message) {
        if (isErrorEnabled()) {
            log(System.err, Level.ERROR, "%s", message);
        }
    }

    /**
     * Method to log an ERROR message
     *
     * @param format format
     * @param objects objects
     */
    public void error(String format, Object... objects) {
        ArgumentSupport.notNullOrEmpty(format, "format is null", "format is empty");

        if (isErrorEnabled()) {
            log(System.err, Level.ERROR, format, objects);
        }
    }

    /**
     * Method to log a message
     *
     * @param printStream printStream
     * @param level level
     * @param format format
     * @param objects objects
     */
    private void log(PrintStream printStream, Level level, String format, Object... objects) {
        String dateTime;

        synchronized (SIMPLE_DATE_FORMAT) {
            dateTime = SIMPLE_DATE_FORMAT.format(new Date());
        }

        printStream.println(
                dateTime
                        + " | "
                        + Thread.currentThread().getName()
                        + " | "
                        + level.toString()
                        + " | "
                        + name
                        + " | "
                        + format(format, objects));

        printStream.flush();
    }
}
