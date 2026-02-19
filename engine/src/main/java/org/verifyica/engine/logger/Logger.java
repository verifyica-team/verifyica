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

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.verifyica.api.Configuration;
import org.verifyica.engine.common.Precondition;
import org.verifyica.engine.configuration.ConcreteConfiguration;
import org.verifyica.engine.configuration.Constants;

/**
 * Thread-safe logger with lightweight level checks and efficient message assembly.
 *
 * <p>This implementation avoids per-call allocations where practical (e.g., via a
 * {@link ThreadLocal} {@link StringBuilder}), uses a volatile level for fast reads,
 * and caches the configured regex {@link Pattern} to avoid recompilation for each logger.
 */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Logger {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    // Cache integer thresholds once.
    private static final int TRACE_INT = Level.TRACE.toInt();
    private static final int DEBUG_INT = Level.DEBUG.toInt();
    private static final int INFO_INT = Level.INFO.toInt();
    private static final int WARN_INT = Level.WARN.toInt();
    private static final int ERROR_INT = Level.ERROR.toInt();

    // Reuse a StringBuilder per thread to reduce allocations under heavy logging.
    private static final ThreadLocal<StringBuilder> TL_BUILDER = ThreadLocal.withInitial(() -> new StringBuilder(256));

    // Config cache (best-effort). If config changes at runtime, we refresh when values differ.
    private static volatile String cachedRegex;
    private static volatile Pattern cachedPattern;
    private static volatile String cachedLevelText;
    private static volatile Level cachedDecodedLevel;

    private final String name;

    /** Cached integer value of {@link Level} fast comparisons. */
    private volatile int levelInt;

    /**
     * Constructor.
     *
     * @param name logger name
     */
    Logger(final String name) {
        this.name = name;
        this.levelInt = resolveInitialLevel(name).toInt();
    }

    private static Level resolveInitialLevel(final String loggerName) {
        final Configuration configuration = ConcreteConfiguration.getInstance();

        final String levelText =
                configuration.getProperties().getProperty(Constants.ENGINE_LOGGER_LEVEL, Level.INFO.toString());

        final String regex = configuration.getProperties().getProperty(Constants.ENGINE_LOGGER_REGEX, ".*");

        try {
            Pattern pattern = cachedPattern;
            if (pattern == null || cachedRegex == null || !cachedRegex.equals(regex)) {
                pattern = Pattern.compile(regex);
                cachedRegex = regex;
                cachedPattern = pattern;
            }

            Level decoded = cachedDecodedLevel;
            if (decoded == null || cachedLevelText == null || !cachedLevelText.equals(levelText)) {
                decoded = Level.decode(levelText);
                cachedLevelText = levelText;
                cachedDecodedLevel = decoded;
            }

            final Matcher matcher = pattern.matcher(loggerName);
            return matcher.find() ? decoded : Level.INFO;
        } catch (Throwable t) {
            // Intentionally empty (config errors should not break logging)
            return Level.INFO;
        }
    }

    /**
     * Returns whether TRACE logging is enabled.
     *
     * @return true if TRACE is enabled
     */
    public boolean isTraceEnabled() {
        return levelInt >= TRACE_INT;
    }

    /**
     * Returns whether DEBUG logging is enabled.
     *
     * @return true if DEBUG is enabled
     */
    public boolean isDebugEnabled() {
        return levelInt >= DEBUG_INT;
    }

    /**
     * Returns whether INFO logging is enabled.
     *
     * @return true if INFO is enabled
     */
    public boolean isInfoEnabled() {
        return levelInt >= INFO_INT;
    }

    /**
     * Returns whether WARN logging is enabled.
     *
     * @return true if WARN is enabled
     */
    public boolean isWarnEnabled() {
        return levelInt >= WARN_INT;
    }

    /**
     * Returns whether ERROR logging is enabled.
     *
     * @return true if ERROR is enabled
     */
    public boolean isErrorEnabled() {
        return levelInt >= ERROR_INT;
    }

    /**
     * Dynamically changes the logging level.
     *
     * @param level new level
     */
    public void setLevel(final Level level) {
        Precondition.notNull(level, "level is null");

        this.levelInt = level.toInt();
    }

    /**
     * Returns whether a specific level is enabled.
     *
     * @param level level to test
     * @return true if enabled
     */
    public boolean isEnabled(final Level level) {
        Precondition.notNull(level, "level is null");

        return levelInt >= level.toInt();
    }

    /**
     * Logs a TRACE message.
     *
     * @param message message
     */
    public void trace(final String message) {
        if (isTraceEnabled()) {
            logMessage(System.out, Level.TRACE, message);
        }
    }

    /**
     * Logs a TRACE message.
     *
     * @param format format
     * @param objects arguments
     */
    public void trace(final String format, final Object... objects) {
        Precondition.notNullOrBlank(format, "format is null", "format is blank");

        if (isTraceEnabled()) {
            logFormat(System.out, Level.TRACE, format, objects);
        }
    }

    /**
     * Logs a DEBUG message.
     *
     * @param message message
     */
    public void debug(final String message) {
        if (isDebugEnabled()) {
            logMessage(System.out, Level.DEBUG, message);
        }
    }

    /**
     * Logs a DEBUG message.
     *
     * @param format format
     * @param objects arguments
     */
    public void debug(final String format, final Object... objects) {
        Precondition.notNullOrBlank(format, "format is null", "format is blank");

        if (isDebugEnabled()) {
            logFormat(System.out, Level.DEBUG, format, objects);
        }
    }

    /**
     * Logs an INFO message.
     *
     * @param message message
     */
    public void info(final String message) {
        if (isInfoEnabled()) {
            logMessage(System.out, Level.INFO, message);
        }
    }

    /**
     * Logs an INFO message.
     *
     * @param format format
     * @param objects arguments
     */
    public void info(final String format, final Object... objects) {
        Precondition.notNullOrBlank(format, "format is null", "format is blank");

        if (isInfoEnabled()) {
            logFormat(System.out, Level.INFO, format, objects);
        }
    }

    /**
     * Logs a WARN message.
     *
     * @param message message
     */
    public void warn(final String message) {
        if (isWarnEnabled()) {
            logMessage(System.out, Level.WARN, message);
        }
    }

    /**
     * Logs a WARN message.
     *
     * @param format format
     * @param objects arguments
     */
    public void warn(final String format, final Object... objects) {
        Precondition.notNullOrBlank(format, "format is null", "format is blank");

        if (isWarnEnabled()) {
            logFormat(System.out, Level.WARN, format, objects);
        }
    }

    /**
     * Logs an ERROR message.
     *
     * @param message message
     */
    public void error(final String message) {
        if (isErrorEnabled()) {
            logMessage(System.err, Level.ERROR, message);
        }
    }

    /**
     * Logs an ERROR message.
     *
     * @param format format
     * @param objects arguments
     */
    public void error(final String format, final Object... objects) {
        Precondition.notNullOrBlank(format, "format is null", "format is blank");

        if (isErrorEnabled()) {
            logFormat(System.err, Level.ERROR, format, objects);
        }
    }

    /**
     * Flushes both {@code System.out} and {@code System.err}.
     */
    public void flush() {
        System.err.flush();
        System.out.flush();
    }

    private void logMessage(final PrintStream printStream, final Level level, final String message) {
        // Preserve legacy behavior: if caller passes null, StringBuilder.append(null) prints "null".
        final StringBuilder sb = TL_BUILDER.get();
        sb.setLength(0);

        sb.append(LocalDateTime.now().format(DATE_TIME_FORMATTER))
                .append(" | ")
                .append(level)
                .append(" | ")
                .append(Thread.currentThread().getName())
                .append(" | ")
                .append(name)
                .append(" | ")
                .append(message);

        printStream.println(sb);
    }

    private void logFormat(
            final PrintStream printStream, final Level level, final String format, final Object... objects) {
        final StringBuilder sb = TL_BUILDER.get();
        sb.setLength(0);

        sb.append(LocalDateTime.now().format(DATE_TIME_FORMATTER))
                .append(" | ")
                .append(level)
                .append(" | ")
                .append(Thread.currentThread().getName())
                .append(" | ")
                .append(name)
                .append(" | ")
                .append(String.format(format, objects));

        printStream.println(sb);
    }
}
