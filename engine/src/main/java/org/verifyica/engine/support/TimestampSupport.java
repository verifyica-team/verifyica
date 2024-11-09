/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.verifyica.engine.common.Precondition;

/** Class to implement HumanReadableTimeSupport */
// Suppress PMD.UselessParentheses - PMD has bug around UselessParentheses calculating milliseconds
@SuppressWarnings("PMD.UselessParentheses")
public final class TimestampSupport {

    /** Format */
    public enum Format {
        /** Long */
        LONG,
        /** Short */
        SHORT
    }

    /** Constructor */
    private TimestampSupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to convert a duration into a human-readable time
     *
     * @param nanoseconds nanoseconds
     * @return the return value
     */
    public static String toHumanReadable(long nanoseconds) {
        return toHumanReadable(nanoseconds, Format.LONG);
    }

    /**
     * Method to convert a duration into a human-readable time
     *
     * @param nanoseconds nanoseconds
     * @param format format
     * @return the return value
     */
    public static String toHumanReadable(long nanoseconds, Format format) {
        Precondition.notNull(format, "format is null");

        long nanosecondsPositive = nanoseconds > 0 ? nanoseconds : -nanoseconds;
        long millisecondsDuration = (long) ((double) nanosecondsPositive / 1e+6d);
        long hours = TimeUnit.MILLISECONDS.toHours(millisecondsDuration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisecondsDuration) - (hours * 60);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millisecondsDuration) - ((hours * 60 * 60) + (minutes * 60));
        long milliseconds =
                millisecondsDuration - ((hours * 60 * 60 * 1000) + (minutes * 60 * 1000) + (seconds * 1000));

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(hours);

        boolean useShortFormat = format == Format.SHORT;

        if (useShortFormat) {
            stringBuilder.append(" h");
        } else {
            stringBuilder.append(" hour");
            if (hours != 1) {
                stringBuilder.append("s");
            }
        }

        stringBuilder.append(", ");
        stringBuilder.append(minutes);

        if (useShortFormat) {
            stringBuilder.append(" m");
        } else {
            stringBuilder.append(" minute");
            if (minutes != 1) {
                stringBuilder.append("s");
            }
        }

        stringBuilder.append(", ");
        stringBuilder.append(seconds);

        if (useShortFormat) {
            stringBuilder.append(" s");
        } else {
            stringBuilder.append(" second");
            if (seconds != 1) {
                stringBuilder.append("s");
            }
        }

        stringBuilder.append(", ");
        stringBuilder.append(milliseconds);
        stringBuilder.append(" ms");

        String result = stringBuilder.toString();

        if (result.startsWith("0 h, ")) {
            result = result.substring("0 h, ".length());
        }

        if (result.startsWith("0 hours, ")) {
            result = result.substring("0 hours, ".length());
        }

        if (result.startsWith("0 m, ")) {
            result = result.substring("0 m, ".length());
        }

        if (result.startsWith("0 minutes, ")) {
            result = result.substring("0 minutes, ".length());
        }

        if (result.startsWith("0 s, ")) {
            result = result.substring("0 s, ".length());
        }

        if (result.startsWith("0 seconds, ")) {
            result = result.substring("0 seconds, ".length());
        }

        return result;
    }

    /**
     * Method to get the current time as a String
     *
     * @return the return value
     */
    public static String now() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH);

        return simpleDateFormat.format(new Date());
    }

    /**
     * Method to convert a duration to milliseconds.nanoseconds String
     *
     * @param duration duration
     * @return a milliseconds.nanoseconds String
     */
    public static String convertDurationToMillisAndNanoseconds(Duration duration) {
        long millis = duration.toMillis();
        int nanos = duration.getNano() % 1000000;
        return millis + "." + String.format("%06d", nanos);
    }

    /**
     * Method to convert nanoseconds to specific timing unit String
     *
     * @param timingUnit timingUnit
     * @param nanoseconds nanoseconds
     * @return a String representing the converted nanoseconds value
     */
    public static String toTimingUnit(long nanoseconds, String timingUnit) {
        String workingUnit = timingUnit == null || timingUnit.trim().equalsIgnoreCase("")
                ? "milliseconds"
                : timingUnit.trim().toLowerCase(Locale.ENGLISH);

        switch (workingUnit) {
            case "nanoseconds": {
                return nanoseconds + " ns";
            }
            case "microseconds": {
                return (nanoseconds / 1e+3) + " μs";
            }
            case "seconds": {
                return (nanoseconds / 1e+9) + " s";
            }
            case "minutes": {
                return (nanoseconds / 1e+12) + " m";
            }
            case "adaptive": {
                if (nanoseconds >= 1e+12) {
                    return (nanoseconds / 1e+12) + " m";
                } else if (nanoseconds >= 1e+9) {
                    return (nanoseconds / 1e+9) + " s";
                } else if (nanoseconds >= 1e+6) {
                    return (nanoseconds / 1e+6) + " ms";
                } else if (nanoseconds >= 1e+3) {
                    return (nanoseconds / 1e+3) + " μs";
                } else {
                    return nanoseconds + " ns";
                }
            }
            case "milliseconds":
            default: {
                return (nanoseconds / 1e+6) + " ms";
            }
        }
    }
}
