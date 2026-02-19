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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.verifyica.engine.support.TimestampSupport.Format;

@DisplayName("TimestampSupport Tests")
public class TimestampSupportTest {

    @Test
    @DisplayName("Should convert nanoseconds to human readable format")
    public void shouldConvertNanosecondsToHumanReadableFormat() {
        // 1 hour = 3600 seconds = 3,600,000,000,000 nanoseconds
        String result = TimestampSupport.toHumanReadable(3_600_000_000_000L);

        assertThat(result).contains("1 hour");
        assertThat(result).contains("0 minute");
        assertThat(result).contains("0 second");
        assertThat(result).contains("0 ms");
    }

    @Test
    @DisplayName("Should convert nanoseconds with short format")
    public void shouldConvertNanosecondsWithShortFormat() {
        // 1 hour = 3600 seconds = 3,600,000,000,000 nanoseconds
        String result = TimestampSupport.toHumanReadable(Format.SHORT, 3_600_000_000_000L);

        assertThat(result).contains("1 h");
        assertThat(result).contains("0 m");
        assertThat(result).contains("0 s");
        assertThat(result).contains("0 ms");
    }

    @Test
    @DisplayName("Should handle zero nanoseconds")
    public void shouldHandleZeroNanoseconds() {
        String result = TimestampSupport.toHumanReadable(0);

        assertThat(result).isEqualTo("0 ms");
    }

    @Test
    @DisplayName("Should handle negative nanoseconds")
    public void shouldHandleNegativeNanoseconds() {
        String result = TimestampSupport.toHumanReadable(-1_000_000_000L);

        assertThat(result).contains("ms");
    }

    @Test
    @DisplayName("Should throw exception for null format")
    public void shouldThrowExceptionForNullFormat() {
        assertThatThrownBy(() -> TimestampSupport.toHumanReadable(null, 1_000_000_000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("format is null");
    }

    @Test
    @DisplayName("Should return current timestamp")
    public void shouldReturnCurrentTimestamp() {
        String now = TimestampSupport.now();

        assertThat(now).isNotNull();
        assertThat(now).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}");
    }

    @Test
    @DisplayName("Should convert duration to millis and nanoseconds")
    public void shouldConvertDurationToMillisAndNanoseconds() {
        Duration duration = Duration.ofMillis(1234).plusNanos(567890);
        String result = TimestampSupport.convertDurationToMillisAndNanoseconds(duration);

        assertThat(result).matches("\\d+\\.\\d{6}");
    }

    @Test
    @DisplayName("Should convert to timing unit - milliseconds")
    public void shouldConvertToTimingUnitMilliseconds() {
        String result = TimestampSupport.toTimingUnit(1_000_000_000L, "milliseconds");

        assertThat(result).contains("ms");
    }

    @Test
    @DisplayName("Should convert to timing unit - nanoseconds")
    public void shouldConvertToTimingUnitNanoseconds() {
        String result = TimestampSupport.toTimingUnit(1_000_000_000L, "nanoseconds");

        assertThat(result).contains("ns");
    }

    @Test
    @DisplayName("Should convert to timing unit - microseconds")
    public void shouldConvertToTimingUnitMicroseconds() {
        String result = TimestampSupport.toTimingUnit(1_000_000_000L, "microseconds");

        assertThat(result).contains("μs");
    }

    @Test
    @DisplayName("Should convert to timing unit - seconds")
    public void shouldConvertToTimingUnitSeconds() {
        String result = TimestampSupport.toTimingUnit(1_000_000_000L, "seconds");

        assertThat(result).contains("s");
    }

    @Test
    @DisplayName("Should convert to timing unit - minutes")
    public void shouldConvertToTimingUnitMinutes() {
        String result = TimestampSupport.toTimingUnit(60_000_000_000L, "minutes");

        assertThat(result).contains("m");
    }

    @Test
    @DisplayName("Should convert to timing unit - adaptive with nanoseconds")
    public void shouldConvertToTimingUnitAdaptiveWithNanoseconds() {
        String result = TimestampSupport.toTimingUnit(500L, "adaptive");

        assertThat(result).contains("ns");
    }

    @Test
    @DisplayName("Should convert to timing unit - adaptive with microseconds")
    public void shouldConvertToTimingUnitAdaptiveWithMicroseconds() {
        String result = TimestampSupport.toTimingUnit(50_000L, "adaptive");

        assertThat(result).contains("μs");
    }

    @Test
    @DisplayName("Should convert to timing unit - adaptive with milliseconds")
    public void shouldConvertToTimingUnitAdaptiveWithMilliseconds() {
        String result = TimestampSupport.toTimingUnit(50_000_000L, "adaptive");

        assertThat(result).contains("ms");
    }

    @Test
    @DisplayName("Should convert to timing unit - adaptive with seconds")
    public void shouldConvertToTimingUnitAdaptiveWithSeconds() {
        String result = TimestampSupport.toTimingUnit(5_000_000_000L, "adaptive");

        assertThat(result).contains("s");
    }

    @Test
    @DisplayName("Should convert to timing unit - adaptive with minutes")
    public void shouldConvertToTimingUnitAdaptiveWithMinutes() {
        // 1 minute = 60 seconds = 60,000,000,000 nanoseconds
        // Threshold for minutes is 1,000,000,000,000 nanoseconds (1000 seconds ~ 16.6 minutes)
        String result = TimestampSupport.toTimingUnit(1_200_000_000_000L, "adaptive");

        assertThat(result).contains("m");
    }

    @Test
    @DisplayName("Should default to milliseconds for null timing unit")
    public void shouldDefaultToMillisecondsForNullTimingUnit() {
        String result = TimestampSupport.toTimingUnit(1_000_000_000L, null);

        assertThat(result).contains("ms");
    }

    @Test
    @DisplayName("Should default to milliseconds for empty timing unit")
    public void shouldDefaultToMillisecondsForEmptyTimingUnit() {
        String result = TimestampSupport.toTimingUnit(1_000_000_000L, "");

        assertThat(result).contains("ms");
    }

    @Test
    @DisplayName("Should default to milliseconds for blank timing unit")
    public void shouldDefaultToMillisecondsForBlankTimingUnit() {
        String result = TimestampSupport.toTimingUnit(1_000_000_000L, "   ");

        assertThat(result).contains("ms");
    }

    @Test
    @DisplayName("Should default to milliseconds for unknown timing unit")
    public void shouldDefaultToMillisecondsForUnknownTimingUnit() {
        String result = TimestampSupport.toTimingUnit(1_000_000_000L, "unknown");

        assertThat(result).contains("ms");
    }

    @Test
    @DisplayName("Should handle timing unit with whitespace")
    public void shouldHandleTimingUnitWithWhitespace() {
        String result = TimestampSupport.toTimingUnit(1_000_000_000L, "  seconds  ");

        assertThat(result).contains("s");
    }

    @Test
    @DisplayName("Should handle timing unit with different case")
    public void shouldHandleTimingUnitWithDifferentCase() {
        String result = TimestampSupport.toTimingUnit(1_000_000_000L, "SECONDS");

        assertThat(result).contains("s");
    }
}
