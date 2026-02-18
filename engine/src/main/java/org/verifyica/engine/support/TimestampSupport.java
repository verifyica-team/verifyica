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

package org.verifyica.engine.support;

import static java.lang.String.format;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.verifyica.engine.common.Precondition;

/**
 * Class to implement TimestampSupport
 */
// Suppress PMD.UselessParentheses - PMD has bug around UselessParentheses calculating milliseconds
@SuppressWarnings("PMD.UselessParentheses")
public final class TimestampSupport {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Format
     */
    public enum Format {

        /**
         * Long.
         */
        LONG,

        /**
         * Short.
         */
        SHORT
    }

    /**
     * Constructor
     */
    private TimestampSupport() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Method to convert nanoseconds into a human-readable time
     *
     * @param nanoseconds nanoseconds
     * @return the return value
     */
    public static String toHumanReadable(long nanoseconds) {
        return toHumanReadable(Format.LONG, nanoseconds);
    }

    /**
     * Method to convert nanoseconds into a human-readable time
     *
     * @param format format
     * @param nanoseconds nanoseconds
     * @return the return value
     */
    public static String toHumanReadable(Format format, long nanoseconds) {
        Precondition.notNull(format, "format is null");

        long nanosecondsPositive = nanoseconds > 0 ? nanoseconds : -nanoseconds;
        long millisecondsDuration = nanosecondsPositive / 1_000_000L;
        long hours = TimeUnit.MILLISECONDS.toHours(millisecondsDuration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisecondsDuration) - (hours * 60);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millisecondsDuration) - ((hours * 60 * 60) + (minutes * 60));
        long milliseconds =
                millisecondsDuration - ((hours * 60 * 60 * 1_000) + (minutes * 60 * 1_000) + (seconds * 1_000));

        boolean useShortFormat = format == Format.SHORT;
        StringBuilder stringBuilder = new StringBuilder(64);
        boolean needsSeparator = false;

        // Append hours if non-zero
        if (hours != 0) {
            stringBuilder.append(hours);
            if (useShortFormat) {
                stringBuilder.append(" h");
            } else {
                stringBuilder.append(" hour");
                if (hours != 1) {
                    stringBuilder.append("s");
                }
            }
            needsSeparator = true;
        }

        // Append minutes if non-zero or if hours were appended
        if (minutes != 0 || needsSeparator) {
            if (needsSeparator) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(minutes);
            if (useShortFormat) {
                stringBuilder.append(" m");
            } else {
                stringBuilder.append(" minute");
                if (minutes != 1) {
                    stringBuilder.append("s");
                }
            }
            needsSeparator = true;
        }

        // Append seconds if non-zero or if minutes/hours were appended
        if (seconds != 0 || needsSeparator) {
            if (needsSeparator) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(seconds);
            if (useShortFormat) {
                stringBuilder.append(" s");
            } else {
                stringBuilder.append(" second");
                if (seconds != 1) {
                    stringBuilder.append("s");
                }
            }
            needsSeparator = true;
        }

        // Always append milliseconds
        if (needsSeparator) {
            stringBuilder.append(", ");
        }
        stringBuilder.append(milliseconds);
        stringBuilder.append(" ms");

        return stringBuilder.toString();
    }

    /**
     * Method to get the current time as a String
     *
     * @return the return value
     */
    public static String now() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    /**
     * Method to convert a duration to milliseconds.nanoseconds String
     *
     * @param duration duration
     * @return a milliseconds.nanoseconds String
     */
    public static String convertDurationToMillisAndNanoseconds(Duration duration) {
        return format("%d.%06d", duration.toMillis(), duration.getNano() % 1_000_000);
    }

    /**
     * Method to convert nanoseconds to specific timing unit String
     *
     * @param timingUnit timingUnit
     * @param nanoseconds nanoseconds
     * @return a String representing the converted nanoseconds value
     */
    public static String toTimingUnit(long nanoseconds, String timingUnit) {
        if (timingUnit == null) {
            return nanoseconds / 1_000_000L + " ms";
        }

        String trimmed = timingUnit.trim();
        if (trimmed.isEmpty()) {
            return nanoseconds / 1_000_000L + " ms";
        }

        String workingUnit = trimmed.toLowerCase(Locale.ENGLISH);

        switch (workingUnit) {
            case "nanoseconds": {
                return nanoseconds + " ns";
            }
            case "microseconds": {
                return nanoseconds / 1_000L + " \u03bcs";
            }
            case "seconds": {
                return nanoseconds / 1_000_000_000L + " s";
            }
            case "minutes": {
                return nanoseconds / 60_000_000_000L + " m";
            }
            case "adaptive": {
                if (nanoseconds >= 1_000_000_000_000L) {
                    return nanoseconds / 60_000_000_000L + " m";
                } else if (nanoseconds >= 1_000_000_000L) {
                    return nanoseconds / 1_000_000_000L + " s";
                } else if (nanoseconds >= 1_000_000L) {
                    return nanoseconds / 1_000_000L + " ms";
                } else if (nanoseconds >= 1_000L) {
                    return nanoseconds / 1_000L + " \u03bcs";
                } else {
                    return nanoseconds + " ns";
                }
            }
            case "milliseconds":
            default: {
                return nanoseconds / 1_000_000L + " ms";
            }
        }
    }
}
