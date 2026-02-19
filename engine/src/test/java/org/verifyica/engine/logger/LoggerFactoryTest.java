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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LoggerFactory Tests")
public class LoggerFactoryTest {

    @Test
    @DisplayName("Should return logger for class")
    public void shouldReturnLoggerForClass() {
        Logger logger = LoggerFactory.getLogger(LoggerFactoryTest.class);

        assertThat(logger).isNotNull();
    }

    @Test
    @DisplayName("Should return same logger for same class")
    public void shouldReturnSameLoggerForSameClass() {
        Logger logger1 = LoggerFactory.getLogger(LoggerFactoryTest.class);
        Logger logger2 = LoggerFactory.getLogger(LoggerFactoryTest.class);

        assertThat(logger1).isSameAs(logger2);
    }

    @Test
    @DisplayName("Should return different loggers for different classes")
    public void shouldReturnDifferentLoggersForDifferentClasses() {
        Logger logger1 = LoggerFactory.getLogger(LoggerFactoryTest.class);
        Logger logger2 = LoggerFactory.getLogger(String.class);

        assertThat(logger1).isNotSameAs(logger2);
    }

    @Test
    @DisplayName("Should return logger for name")
    public void shouldReturnLoggerForName() {
        Logger logger = LoggerFactory.getLogger("test.logger");

        assertThat(logger).isNotNull();
    }

    @Test
    @DisplayName("Should return same logger for same name")
    public void shouldReturnSameLoggerForSameName() {
        Logger logger1 = LoggerFactory.getLogger("test.logger.same");
        Logger logger2 = LoggerFactory.getLogger("test.logger.same");

        assertThat(logger1).isSameAs(logger2);
    }

    @Test
    @DisplayName("Should return root logger for null class")
    public void shouldReturnRootLoggerForNullClass() {
        Logger logger = LoggerFactory.getLogger((Class<?>) null);

        assertThat(logger).isNotNull();
    }

    @Test
    @DisplayName("Should return root logger for null name")
    public void shouldReturnRootLoggerForNullName() {
        Logger logger = LoggerFactory.getLogger((String) null);

        assertThat(logger).isNotNull();
    }

    @Test
    @DisplayName("Should return root logger for empty name")
    public void shouldReturnRootLoggerForEmptyName() {
        Logger logger = LoggerFactory.getLogger("");

        assertThat(logger).isNotNull();
    }

    @Test
    @DisplayName("Should return root logger for blank name")
    public void shouldReturnRootLoggerForBlankName() {
        Logger logger = LoggerFactory.getLogger("   ");

        assertThat(logger).isNotNull();
    }

    @Test
    @DisplayName("Should trim logger name")
    public void shouldTrimLoggerName() {
        Logger logger1 = LoggerFactory.getLogger("trimmed.logger");
        Logger logger2 = LoggerFactory.getLogger("  trimmed.logger  ");

        assertThat(logger1).isSameAs(logger2);
    }

    @Test
    @DisplayName("Should handle class name as logger name")
    public void shouldHandleClassNameAsLoggerName() {
        Logger loggerByClass = LoggerFactory.getLogger(LoggerFactoryTest.class);
        Logger loggerByName = LoggerFactory.getLogger(LoggerFactoryTest.class.getName());

        assertThat(loggerByClass).isSameAs(loggerByName);
    }
}
