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

@DisplayName("Throttle Interface Tests")
public class ThrottleTest {

    @Test
    @DisplayName("Should be able to implement Throttle interface")
    public void shouldBeAbleToImplementThrottleInterface() {
        final Throttle customThrottle = () -> {
            // Custom implementation
        };

        assertThat(customThrottle).isInstanceOf(Throttle.class);
    }

    @Test
    @DisplayName("Should execute throttle method")
    public void shouldExecuteThrottleMethod() {
        final boolean[] executed = {false};

        final Throttle customThrottle = () -> executed[0] = true;

        assertThatCode(customThrottle::throttle).doesNotThrowAnyException();
        assertThat(executed[0]).isTrue();
    }

    @Test
    @DisplayName("Should propagate InterruptedException")
    public void shouldPropagateInterruptedException() {
        final Throttle customThrottle = () -> {
            throw new InterruptedException("Test interruption");
        };

        assertThatCode(() -> {
                    try {
                        customThrottle.throttle();
                    } catch (final InterruptedException e) {
                        // Expected
                        assertThat(e.getMessage()).isEqualTo("Test interruption");
                    }
                })
                .doesNotThrowAnyException();
    }
}
