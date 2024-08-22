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

package org.antublue.verifyica.engine.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.UUID;
import org.junit.jupiter.api.Test;

public class ThreadNameRunnableTest {

    private final String[] validThreadNames = new String[] {
            "",
            " ",
            UUID.randomUUID().toString()
    };

    @Test
    public void testValidThreadNames() {
        Thread thread = Thread.currentThread();
        String originalThreadName = thread.getName();

        for (String validThreadName : validThreadNames) {
            new ThreadNameRunnable(
                    validThreadName,
                    () -> {
                        String threadName = Thread.currentThread().getName();
                        assertThat(threadName).isNotNull();
                        assertThat(threadName).isEqualTo(validThreadName);
                    })
                    .run();

            assertThat(thread.getName()).isEqualTo(originalThreadName);
        }
    }

    @Test
    public void testInvalidArguments() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new ThreadNameRunnable("test", null));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new ThreadNameRunnable(null, () -> {}));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new ThreadNameRunnable(null, null));
    }
}
