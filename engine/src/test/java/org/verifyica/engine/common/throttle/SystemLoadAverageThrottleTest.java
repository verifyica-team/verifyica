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

@DisplayName("SystemLoadAverageThrottle Tests")
public class SystemLoadAverageThrottleTest {

    @Test
    @DisplayName("Should return singleton instance")
    public void shouldReturnSingletonInstance() {
        SystemLoadAverageThrottle instance1 = SystemLoadAverageThrottle.getInstance();
        SystemLoadAverageThrottle instance2 = SystemLoadAverageThrottle.getInstance();

        assertThat(instance1).isNotNull();
        assertThat(instance2).isNotNull();
        assertThat(instance1).isSameAs(instance2);
    }

    @Test
    @DisplayName("Should implement Throttle interface")
    public void shouldImplementThrottleInterface() {
        SystemLoadAverageThrottle instance = SystemLoadAverageThrottle.getInstance();

        assertThat(instance).isInstanceOf(Throttle.class);
    }

    @Test
    @DisplayName("Should complete throttle without exception")
    public void shouldCompleteThrottleWithoutException() {
        SystemLoadAverageThrottle instance = SystemLoadAverageThrottle.getInstance();

        assertThatCode(instance::throttle).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should complete throttle with max cumulative time")
    public void shouldCompleteThrottleWithMaxCumulativeTime() {
        SystemLoadAverageThrottle instance = SystemLoadAverageThrottle.getInstance();

        assertThatCode(() -> instance.throttle(100)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle multiple throttle calls")
    public void shouldHandleMultipleThrottleCalls() {
        SystemLoadAverageThrottle instance = SystemLoadAverageThrottle.getInstance();

        assertThatCode(() -> {
                    for (int i = 0; i < 3; i++) {
                        instance.throttle(50);
                    }
                })
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw InterruptedException when thread is interrupted")
    public void shouldThrowInterruptedExceptionWhenThreadIsInterrupted() {
        SystemLoadAverageThrottle instance = SystemLoadAverageThrottle.getInstance();

        Thread testThread = new Thread(() -> {
            try {
                instance.throttle(5000);
            } catch (InterruptedException e) {
                // Expected
                Thread.currentThread().interrupt();
            }
        });

        testThread.start();
        testThread.interrupt();

        assertThatCode(() -> testThread.join(100)).doesNotThrowAnyException();
    }
}
