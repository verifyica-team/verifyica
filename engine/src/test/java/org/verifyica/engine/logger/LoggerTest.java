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

package org.verifyica.engine.logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Logger Tests")
public class LoggerTest {

    @Test
    @DisplayName("Should create logger with name")
    public void shouldCreateLoggerWithName() {
        Logger logger = LoggerFactory.getLogger("test.logger");

        assertThat(logger).isNotNull();
    }

    @Test
    @DisplayName("Should return correct initial level")
    public void shouldReturnCorrectInitialLevel() {
        Logger logger = LoggerFactory.getLogger("test.level");

        // Default level is INFO
        assertThat(logger.isErrorEnabled()).isTrue();
        assertThat(logger.isWarnEnabled()).isTrue();
        assertThat(logger.isInfoEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should set and get level")
    public void shouldSetAndGetLevel() {
        Logger logger = LoggerFactory.getLogger("test.set.level");

        logger.setLevel(Level.DEBUG);

        assertThat(logger.isDebugEnabled()).isTrue();
        assertThat(logger.isTraceEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should throw exception for null level")
    public void shouldThrowExceptionForNullLevel() {
        Logger logger = LoggerFactory.getLogger("test.null.level");

        assertThatThrownBy(() -> logger.setLevel(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("level is null");
    }

    @Test
    @DisplayName("Should check isEnabled correctly")
    public void shouldCheckIsEnabledCorrectly() {
        Logger logger = LoggerFactory.getLogger("test.is.enabled");
        logger.setLevel(Level.WARN);

        assertThat(logger.isEnabled(Level.ERROR)).isTrue();
        assertThat(logger.isEnabled(Level.WARN)).isTrue();
        assertThat(logger.isEnabled(Level.INFO)).isFalse();
        assertThat(logger.isEnabled(Level.DEBUG)).isFalse();
        assertThat(logger.isEnabled(Level.TRACE)).isFalse();
    }

    @Test
    @DisplayName("Should throw exception for null level in isEnabled")
    public void shouldThrowExceptionForNullLevelInIsEnabled() {
        Logger logger = LoggerFactory.getLogger("test.null.level.enabled");

        assertThatThrownBy(() -> logger.isEnabled(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("level is null");
    }

    @Test
    @DisplayName("Should log trace message without exception")
    public void shouldLogTraceMessageWithoutException() {
        Logger logger = LoggerFactory.getLogger("test.trace");
        logger.setLevel(Level.TRACE);

        assertThatCode(() -> logger.trace("Test trace message")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should log trace with format without exception")
    public void shouldLogTraceWithFormatWithoutException() {
        Logger logger = LoggerFactory.getLogger("test.trace.format");
        logger.setLevel(Level.TRACE);

        assertThatCode(() -> logger.trace("Test %s message", "trace")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception for null format in trace")
    public void shouldThrowExceptionForNullFormatInTrace() {
        Logger logger = LoggerFactory.getLogger("test.trace.null");
        logger.setLevel(Level.TRACE);

        assertThatThrownBy(() -> logger.trace(null, "arg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("format is null");
    }

    @Test
    @DisplayName("Should throw exception for blank format in trace")
    public void shouldThrowExceptionForBlankFormatInTrace() {
        Logger logger = LoggerFactory.getLogger("test.trace.blank");
        logger.setLevel(Level.TRACE);

        assertThatThrownBy(() -> logger.trace("   ", "arg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("format is blank");
    }

    @Test
    @DisplayName("Should log debug message without exception")
    public void shouldLogDebugMessageWithoutException() {
        Logger logger = LoggerFactory.getLogger("test.debug");
        logger.setLevel(Level.DEBUG);

        assertThatCode(() -> logger.debug("Test debug message")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should log debug with format without exception")
    public void shouldLogDebugWithFormatWithoutException() {
        Logger logger = LoggerFactory.getLogger("test.debug.format");
        logger.setLevel(Level.DEBUG);

        assertThatCode(() -> logger.debug("Test %s message", "debug")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should log info message without exception")
    public void shouldLogInfoMessageWithoutException() {
        Logger logger = LoggerFactory.getLogger("test.info");

        assertThatCode(() -> logger.info("Test info message")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should log info with format without exception")
    public void shouldLogInfoWithFormatWithoutException() {
        Logger logger = LoggerFactory.getLogger("test.info.format");

        assertThatCode(() -> logger.info("Test %s message", "info")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should log warn message without exception")
    public void shouldLogWarnMessageWithoutException() {
        Logger logger = LoggerFactory.getLogger("test.warn");

        assertThatCode(() -> logger.warn("Test warn message")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should log warn with format without exception")
    public void shouldLogWarnWithFormatWithoutException() {
        Logger logger = LoggerFactory.getLogger("test.warn.format");

        assertThatCode(() -> logger.warn("Test %s message", "warn")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should log error message without exception")
    public void shouldLogErrorMessageWithoutException() {
        Logger logger = LoggerFactory.getLogger("test.error");

        assertThatCode(() -> logger.error("Test error message")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should log error with format without exception")
    public void shouldLogErrorWithFormatWithoutException() {
        Logger logger = LoggerFactory.getLogger("test.error.format");

        assertThatCode(() -> logger.error("Test %s message", "error")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should flush without exception")
    public void shouldFlushWithoutException() {
        Logger logger = LoggerFactory.getLogger("test.flush");

        assertThatCode(() -> logger.flush()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should not log when level is disabled")
    public void shouldNotLogWhenLevelIsDisabled() {
        Logger logger = LoggerFactory.getLogger("test.disabled");
        logger.setLevel(Level.ERROR);

        // These should not throw and should not log (since level is ERROR)
        assertThatCode(() -> {
                    logger.trace("trace");
                    logger.debug("debug");
                    logger.info("info");
                    logger.warn("warn");
                })
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle multiple arguments in format")
    public void shouldHandleMultipleArgumentsInFormat() {
        Logger logger = LoggerFactory.getLogger("test.multi.args");

        assertThatCode(() -> logger.info("Test %s %s %s", "a", "b", "c")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle null arguments in format")
    public void shouldHandleNullArgumentsInFormat() {
        Logger logger = LoggerFactory.getLogger("test.null.args");

        assertThatCode(() -> logger.info("Test %s", (Object) null)).doesNotThrowAnyException();
    }
}
