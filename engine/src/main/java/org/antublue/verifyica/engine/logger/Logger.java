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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antublue.verifyica.engine.configuration.ConcreteConfiguration;
import org.antublue.verifyica.engine.configuration.Constants;

/** Class to implement Logger */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Logger {

    private static final ConcreteConfiguration CONCRETE_CONFIGURATION =
            ConcreteConfiguration.getInstance();

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
                CONCRETE_CONFIGURATION.get(Constants.LOGGER_LEVEL).orElse(Level.INFO.toString());

        String regex = CONCRETE_CONFIGURATION.get(Constants.LOGGER_REGEX).orElse(".*");

        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                level = Level.toLevel(loggerLevel, Level.INFO);
            }
        } catch (Throwable t) {
            // DO NOTHING
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
        if (level != null) {
            this.level = level;
        }
    }

    /**
     * Method to return if a specific Level is enabled
     *
     * @param level level
     * @return the return value
     */
    public boolean isEnabled(Level level) {
        return this.level.toInt() >= level.toInt();
    }

    /**
     * Method to log a TRACE message
     *
     * @param message message
     */
    public void trace(Object message) {
        if (isTraceEnabled()) {
            log(System.out, createMessage(Level.TRACE, message));
        }
    }

    /**
     * Method to log a TRACE message
     *
     * @param format format
     * @param object object
     */
    public void trace(String format, Object object) {
        if (isTraceEnabled()) {
            trace(format, new Object[] {object});
        }
    }

    /**
     * Method to log a TRACE message
     *
     * @param format format
     * @param objects objects
     */
    public void trace(String format, Object... objects) {
        if (isTraceEnabled()) {
            Objects.requireNonNull(format);
            log(System.out, createMessage(Level.TRACE, format(format, objects)));
        }
    }

    /**
     * Method to log a TRACE message
     *
     * @param message message
     * @param throwable throwable
     */
    public void trace(Object message, Throwable throwable) {
        if (isTraceEnabled()) {
            log(System.out, createMessage(Level.TRACE, createMessage(message, throwable)));
        }
    }

    /**
     * Method to log a DEBUG message
     *
     * @param message message
     */
    public void debug(Object message) {
        if (isDebugEnabled()) {
            log(System.out, createMessage(Level.DEBUG, message));
        }
    }

    /**
     * Method to log a DEBUG message
     *
     * @param format format
     * @param object object
     */
    public void debug(String format, Object object) {
        if (isDebugEnabled()) {
            debug(format, new Object[] {object});
        }
    }

    /**
     * Method to log a DEBUG message
     *
     * @param format format
     * @param objects objects
     */
    public void debug(String format, Object... objects) {
        if (isDebugEnabled()) {
            Objects.requireNonNull(format);
            log(System.out, createMessage(Level.DEBUG, format(format, objects)));
        }
    }

    /**
     * Method to log a DEBUG message
     *
     * @param message message
     * @param throwable throwable
     */
    public void debug(Object message, Throwable throwable) {
        if (isDebugEnabled()) {
            log(System.out, createMessage(Level.DEBUG, createMessage(message, throwable)));
        }
    }

    /**
     * Method to log an INFO message
     *
     * @param message message
     */
    public void info(Object message) {
        if (isInfoEnabled()) {
            log(System.out, createMessage(Level.INFO, message));
        }
    }

    /**
     * Method to log an INFO message
     *
     * @param format format
     * @param object object
     */
    public void info(String format, Object object) {
        if (isInfoEnabled()) {
            info(format, new Object[] {object});
        }
    }

    /**
     * Method to log an INFO message
     *
     * @param format format
     * @param objects objects
     */
    public void info(String format, Object... objects) {
        if (isInfoEnabled()) {
            Objects.requireNonNull(format);
            log(System.out, createMessage(Level.INFO, format(format, objects)));
        }
    }

    /**
     * Method to log an INFO message
     *
     * @param message message
     * @param throwable throwable
     */
    public void info(Object message, Throwable throwable) {
        if (isInfoEnabled()) {
            log(System.out, createMessage(Level.INFO, createMessage(message, throwable)));
        }
    }

    /**
     * Method to log a WARN message
     *
     * @param message message
     */
    public void warn(Object message) {
        if (isWarnEnabled()) {
            log(System.out, createMessage(Level.WARN, message));
        }
    }

    /**
     * Method to log an WARN message
     *
     * @param format format
     * @param object object
     */
    public void warn(String format, Object object) {
        if (isWarnEnabled()) {
            warn(format, new Object[] {object});
        }
    }

    /**
     * Method to log an WARN message
     *
     * @param format format
     * @param objects objects
     */
    public void warn(String format, Object... objects) {
        if (isWarnEnabled()) {
            Objects.requireNonNull(format);
            log(System.out, createMessage(Level.WARN, format(format, objects)));
        }
    }

    /**
     * Method to log an WARN message
     *
     * @param message message
     * @param throwable throwable
     */
    public void warn(Object message, Throwable throwable) {
        if (isWarnEnabled()) {
            log(System.out, createMessage(Level.WARN, createMessage(message, throwable)));
        }
    }

    /**
     * Method to log an ERROR message
     *
     * @param message message
     */
    public void error(Object message) {
        if (isErrorEnabled()) {
            log(System.err, createMessage(Level.ERROR, message));
        }
    }

    /**
     * Method to log an ERROR message
     *
     * @param format format
     * @param object object
     */
    public void error(String format, Object object) {
        if (isErrorEnabled()) {
            error(format, new Object[] {object});
        }
    }

    /**
     * Method to log an ERROR message
     *
     * @param format format
     * @param objects objects
     */
    public void error(String format, Object... objects) {
        if (isErrorEnabled()) {
            Objects.requireNonNull(format);
            log(System.out, createMessage(Level.ERROR, format(format, objects)));
        }
    }

    /**
     * Method to log an ERROR message
     *
     * @param message message
     * @param throwable throwable
     */
    public void error(Object message, Throwable throwable) {
        if (isErrorEnabled()) {
            log(System.out, createMessage(Level.ERROR, createMessage(message, throwable)));
        }
    }

    /**
     * Method to create a log message
     *
     * @param level level
     * @param message message
     * @return the return value
     */
    private String createMessage(Level level, Object message) {
        String dateTime;

        synchronized (SIMPLE_DATE_FORMAT) {
            dateTime = SIMPLE_DATE_FORMAT.format(new Date());
        }

        return dateTime
                + " | "
                + Thread.currentThread().getName()
                + " | "
                + level.toString()
                + " | "
                + name
                + " | "
                + message
                + " ";
    }

    /**
     * Method to create a log message
     *
     * @param message message
     * @param throwable throwable
     * @return the return value
     */
    private String createMessage(Object message, Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
            printWriter.println(message);
            if (throwable != null) {
                throwable.printStackTrace(printWriter);
            }
        }
        return stringWriter.toString();
    }

    /**
     * Method to log to a PrintStream
     *
     * @param printStream printStream
     * @param message message
     */
    private void log(PrintStream printStream, Object message) {
        printStream.println(message);
        printStream.flush();
    }
}
