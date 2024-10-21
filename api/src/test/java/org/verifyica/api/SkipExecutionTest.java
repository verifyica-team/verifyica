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

package org.verifyica.api;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

public class SkipExecutionTest {

    @Test
    public void testIfTrue() {
        assertThatExceptionOfType(SkipExecution.class).isThrownBy(() -> SkipExecution.ifTrue(true));
        assertThatExceptionOfType(SkipExecution.class).isThrownBy(() -> SkipExecution.ifTrue(() -> true));

        SkipExecution.ifTrue(false);
        SkipExecution.ifTrue(() -> false);
    }

    @Test
    public void testIfFalse() {
        assertThatExceptionOfType(SkipExecution.class).isThrownBy(() -> SkipExecution.ifFalse(false));
        assertThatExceptionOfType(SkipExecution.class).isThrownBy(() -> SkipExecution.ifFalse(() -> false));

        SkipExecution.ifFalse(true);
        SkipExecution.ifFalse(() -> true);
    }

    @Test
    public void testIllegalArguments() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> SkipExecution.ifTrue(null));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> SkipExecution.ifFalse(null));
    }
}
