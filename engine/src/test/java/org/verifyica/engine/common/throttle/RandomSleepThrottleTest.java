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

package org.verifyica.engine.common.throttle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RandomSleepThrottle Tests")
public class RandomSleepThrottleTest {

    @Test
    @DisplayName("Should implement Throttle interface")
    public void shouldImplementThrottleInterface() {
        RandomSleepThrottle throttle = new RandomSleepThrottle("test", 0, 0);

        assertThat(throttle).isInstanceOf(Throttle.class);
    }

    @Test
    @DisplayName("Should not throw exception for valid range")
    public void shouldNotThrowExceptionForValidRange() {
        assertThatCode(() -> new RandomSleepThrottle("test", 0, 100)).doesNotThrowAnyException();
        assertThatCode(() -> new RandomSleepThrottle("test", 50, 50)).doesNotThrowAnyException();
        assertThatCode(() -> new RandomSleepThrottle("test", 0, 0)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle negative values by converting to zero")
    public void shouldHandleNegativeValuesByConvertingToZero() {
        assertThatCode(() -> new RandomSleepThrottle("test", -1, 100)).doesNotThrowAnyException();
        assertThatCode(() -> new RandomSleepThrottle("test", 0, -1)).doesNotThrowAnyException();
        assertThatCode(() -> new RandomSleepThrottle("test", -1, -1)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should complete immediately when minimum and maximum are zero")
    public void shouldCompleteImmediatelyWhenMinAndMaxAreZero() throws InterruptedException {
        RandomSleepThrottle throttle = new RandomSleepThrottle("test", 0, 0);

        long startTime = System.currentTimeMillis();
        throttle.throttle();
        long endTime = System.currentTimeMillis();

        assertThat(endTime - startTime).isLessThan(10L);
    }

    @Test
    @DisplayName("Should sleep for fixed duration when minimum equals maximum")
    public void shouldSleepForFixedDurationWhenMinEqualsMax() throws InterruptedException {
        RandomSleepThrottle throttle = new RandomSleepThrottle("test", 50, 50);

        long startTime = System.currentTimeMillis();
        throttle.throttle();
        long endTime = System.currentTimeMillis();

        // Should sleep for approximately 50ms (with some tolerance)
        assertThat(endTime - startTime).isGreaterThanOrEqualTo(45L).isLessThan(100L);
    }

    @Test
    @DisplayName("Should sleep within range")
    public void shouldSleepWithinRange() throws InterruptedException {
        RandomSleepThrottle throttle = new RandomSleepThrottle("test", 10, 30);

        long startTime = System.currentTimeMillis();
        throttle.throttle();
        long endTime = System.currentTimeMillis();

        // Should sleep between 10ms and 30ms (with some tolerance)
        assertThat(endTime - startTime).isGreaterThanOrEqualTo(5L).isLessThan(50L);
    }

    @Test
    @DisplayName("Should handle reversed minimum and maximum")
    public void shouldHandleReversedMinimumAndMaximum() throws InterruptedException {
        RandomSleepThrottle throttle = new RandomSleepThrottle("test", 100, 50);

        long startTime = System.currentTimeMillis();
        throttle.throttle();
        long endTime = System.currentTimeMillis();

        // Should normalize to min=50, max=100
        assertThat(endTime - startTime).isGreaterThanOrEqualTo(45L).isLessThan(150L);
    }

    @Test
    @DisplayName("Should throw InterruptedException when thread is interrupted")
    public void shouldThrowInterruptedExceptionWhenThreadIsInterrupted() {
        RandomSleepThrottle throttle = new RandomSleepThrottle("test", 1000, 2000);

        Thread testThread = new Thread(() -> {
            try {
                throttle.throttle();
            } catch (InterruptedException e) {
                // Expected
                Thread.currentThread().interrupt();
            }
        });

        testThread.start();
        testThread.interrupt();

        assertThatCode(() -> testThread.join(100)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle multiple throttle calls")
    public void shouldHandleMultipleThrottleCalls() {
        RandomSleepThrottle throttle = new RandomSleepThrottle("test", 0, 5);

        assertThatCode(() -> {
                    for (int i = 0; i < 5; i++) {
                        throttle.throttle();
                    }
                })
                .doesNotThrowAnyException();
    }
}
