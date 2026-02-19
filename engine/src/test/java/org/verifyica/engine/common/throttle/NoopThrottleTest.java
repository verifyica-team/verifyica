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

@DisplayName("NoopThrottle Tests")
public class NoopThrottleTest {

    @Test
    @DisplayName("Should return singleton instance")
    public void shouldReturnSingletonInstance() {
        NoopThrottle instance1 = NoopThrottle.getInstance();
        NoopThrottle instance2 = NoopThrottle.getInstance();

        assertThat(instance1).isNotNull();
        assertThat(instance2).isNotNull();
        assertThat(instance1).isSameAs(instance2);
    }

    @Test
    @DisplayName("Should implement Throttle interface")
    public void shouldImplementThrottleInterface() {
        NoopThrottle instance = NoopThrottle.getInstance();

        assertThat(instance).isInstanceOf(Throttle.class);
    }

    @Test
    @DisplayName("Should not throw exception when throttling")
    public void shouldNotThrowExceptionWhenThrottling() {
        NoopThrottle instance = NoopThrottle.getInstance();

        assertThatCode(instance::throttle).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should complete throttle immediately")
    public void shouldCompleteThrottleImmediately() {
        NoopThrottle instance = NoopThrottle.getInstance();

        long startTime = System.currentTimeMillis();

        assertThatCode(() -> instance.throttle()).doesNotThrowAnyException();

        long endTime = System.currentTimeMillis();

        // Should complete in less than 10ms (essentially immediate)
        assertThat(endTime - startTime).isLessThan(10L);
    }

    @Test
    @DisplayName("Should handle multiple throttle calls")
    public void shouldHandleMultipleThrottleCalls() {
        NoopThrottle instance = NoopThrottle.getInstance();

        assertThatCode(() -> {
                    for (int i = 0; i < 100; i++) {
                        instance.throttle();
                    }
                })
                .doesNotThrowAnyException();
    }
}
