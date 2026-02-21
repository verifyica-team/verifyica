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

package org.verifyica.engine.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UncheckedClassNotFoundException Tests")
public class UncheckedClassNotFoundExceptionTest {

    @Test
    @DisplayName("Should extend RuntimeException")
    public void shouldExtendRuntimeException() {
        final ClassNotFoundException cause = new ClassNotFoundException("Class not found");
        final UncheckedClassNotFoundException exception = new UncheckedClassNotFoundException("message", cause);

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should store message and cause")
    public void shouldStoreMessageAndCause() {
        final String message = "Class not found error";
        final ClassNotFoundException cause = new ClassNotFoundException("Original error");
        final UncheckedClassNotFoundException exception = new UncheckedClassNotFoundException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Should throw itself when throwUnchecked is called")
    public void shouldThrowItselfWhenThrowUncheckedIsCalled() {
        final ClassNotFoundException cause = new ClassNotFoundException("Original error");
        final UncheckedClassNotFoundException exception = new UncheckedClassNotFoundException("message", cause);

        assertThatThrownBy(exception::throwUnchecked)
                .isInstanceOf(UncheckedClassNotFoundException.class)
                .hasMessage("message")
                .hasCause(cause);
    }

    @Test
    @DisplayName("Should create and throw using static throwUnchecked method")
    public void shouldCreateAndThrowUsingStaticThrowUncheckedMethod() {
        final ClassNotFoundException cause = new ClassNotFoundException("Original error");

        assertThatThrownBy(() -> UncheckedClassNotFoundException.throwUnchecked(cause))
                .isInstanceOf(UncheckedClassNotFoundException.class)
                .hasMessage("Original error")
                .hasCause(cause);
    }

    @Test
    @DisplayName("Should preserve stack trace")
    public void shouldPreserveStackTrace() {
        final ClassNotFoundException cause = new ClassNotFoundException("Original error");
        final UncheckedClassNotFoundException exception = new UncheckedClassNotFoundException("message", cause);

        assertThat(exception.getStackTrace()).isNotNull();
        assertThat(exception.getStackTrace().length).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should handle null message")
    public void shouldHandleNullMessage() {
        final ClassNotFoundException cause = new ClassNotFoundException("Original error");
        final UncheckedClassNotFoundException exception = new UncheckedClassNotFoundException(null, cause);

        assertThat(exception.getMessage()).isNull();
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Should handle null cause")
    public void shouldHandleNullCause() {
        final UncheckedClassNotFoundException exception = new UncheckedClassNotFoundException("message", null);

        assertThat(exception.getMessage()).isEqualTo("message");
        assertThat(exception.getCause()).isNull();
    }
}
