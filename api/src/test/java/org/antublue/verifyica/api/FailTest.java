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

package org.antublue.verifyica.api;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class FailTest {

    @Test
    public void testNoExceptions() {
        Fail.failIfTrue(false, "not expected");
        Fail.failIfFalse(true, "not expected");
    }

    @Test
    public void testExceptions() {
        RuntimeException runtimeException = new RuntimeException();

        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> Fail.fail("expected"))
                .withMessage("expected");
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> Fail.fail(() -> "expected"))
                .withMessage("expected");
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> Fail.fail("expected", runtimeException))
                .withMessage("expected")
                .withCause(runtimeException);
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> Fail.fail(() -> "expected", runtimeException))
                .withMessage("expected")
                .withCause(runtimeException);
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> Fail.failIfTrue(true, "expected"))
                .withMessage("expected");
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> Fail.failIfTrue(true, () -> "expected"))
                .withMessage("expected");
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> Fail.failIfFalse(false, "expected"))
                .withMessage("expected");
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> Fail.failIfFalse(false, () -> "expected"))
                .withMessage("expected");

        // TODO all methods
    }
}
