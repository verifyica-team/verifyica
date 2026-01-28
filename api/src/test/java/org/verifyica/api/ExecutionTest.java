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

package org.verifyica.api;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.verifyica.api.Execution.skipIfCondition;
import static org.verifyica.api.Execution.skipIfNotCondition;

import org.junit.jupiter.api.Test;

public class ExecutionTest {

    @Test
    public void testSkipIfCondition() {
        assertThatExceptionOfType(Execution.ExecutionSkippedException.class).isThrownBy(() -> skipIfCondition(true));
        assertThatExceptionOfType(Execution.ExecutionSkippedException.class)
                .isThrownBy(() -> skipIfCondition(true, "skipped"));
        assertThatExceptionOfType(Execution.ExecutionSkippedException.class)
                .isThrownBy(() -> skipIfCondition(() -> true));
        assertThatExceptionOfType(Execution.ExecutionSkippedException.class)
                .isThrownBy(() -> skipIfCondition(() -> true, "skipped"));

        skipIfCondition(false);
        skipIfCondition(false, "skipped");
        skipIfCondition(() -> false);
        skipIfCondition(() -> false, "skipped");
    }

    @Test
    public void testSkipIfNotCondition() {
        assertThatExceptionOfType(Execution.ExecutionSkippedException.class)
                .isThrownBy(() -> skipIfNotCondition(false));
        assertThatExceptionOfType(Execution.ExecutionSkippedException.class)
                .isThrownBy(() -> skipIfNotCondition(false, "skipped"));
        assertThatExceptionOfType(Execution.ExecutionSkippedException.class)
                .isThrownBy(() -> skipIfNotCondition(() -> false));
        assertThatExceptionOfType(Execution.ExecutionSkippedException.class)
                .isThrownBy(() -> skipIfNotCondition(() -> false, "skipped"));

        skipIfNotCondition(true);
        skipIfNotCondition(true, "skipped");
        skipIfNotCondition(() -> true);
        skipIfNotCondition(() -> true, "skipped");
    }

    @Test
    public void testIllegalArguments() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> skipIfCondition(null));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> skipIfCondition(null, "skipped"));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> skipIfNotCondition(null));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> skipIfNotCondition(null, "skipped"));
    }
}
