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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EngineConfigurationException Tests")
public class EngineConfigurationExceptionTest {

    @Test
    @DisplayName("Should extend EngineException")
    public void shouldExtendEngineException() {
        EngineConfigurationException exception = new EngineConfigurationException("test");

        assertThat(exception).isInstanceOf(EngineException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should store message with message constructor")
    public void shouldStoreMessageWithMessageConstructor() {
        String message = "Configuration error";
        EngineConfigurationException exception = new EngineConfigurationException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Should store message and cause with two-argument constructor")
    public void shouldStoreMessageAndCauseWithTwoArgumentConstructor() {
        String message = "Configuration error";
        Throwable cause = new IllegalStateException("Invalid state");
        EngineConfigurationException exception = new EngineConfigurationException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Should handle null message")
    public void shouldHandleNullMessage() {
        EngineConfigurationException exception = new EngineConfigurationException(null);

        assertThat(exception.getMessage()).isNull();
    }

    @Test
    @DisplayName("Should handle null cause")
    public void shouldHandleNullCause() {
        EngineConfigurationException exception = new EngineConfigurationException("message", null);

        assertThat(exception.getMessage()).isEqualTo("message");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Should preserve stack trace")
    public void shouldPreserveStackTrace() {
        EngineConfigurationException exception = new EngineConfigurationException("test");

        assertThat(exception.getStackTrace()).isNotNull();
        assertThat(exception.getStackTrace().length).isGreaterThan(0);
    }
}
