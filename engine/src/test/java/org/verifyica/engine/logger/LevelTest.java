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

@DisplayName("Level Tests")
public class LevelTest {

    @Test
    @DisplayName("Should have predefined levels")
    public void shouldHavePredefinedLevels() {
        assertThat(Level.ERROR).isNotNull();
        assertThat(Level.WARN).isNotNull();
        assertThat(Level.INFO).isNotNull();
        assertThat(Level.DEBUG).isNotNull();
        assertThat(Level.TRACE).isNotNull();
        assertThat(Level.ALL).isNotNull();
    }

    @Test
    @DisplayName("Should have correct level values")
    public void shouldHaveCorrectLevelValues() {
        assertThat(Level.ERROR.toInt()).isEqualTo(100);
        assertThat(Level.WARN.toInt()).isEqualTo(200);
        assertThat(Level.INFO.toInt()).isEqualTo(300);
        assertThat(Level.DEBUG.toInt()).isEqualTo(400);
        assertThat(Level.TRACE.toInt()).isEqualTo(500);
        assertThat(Level.ALL.toInt()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("Should have correct level names")
    public void shouldHaveCorrectLevelNames() {
        assertThat(Level.ERROR.toString()).isEqualTo("ERROR");
        assertThat(Level.WARN.toString()).isEqualTo("WARN");
        assertThat(Level.INFO.toString()).isEqualTo("INFO");
        assertThat(Level.DEBUG.toString()).isEqualTo("DEBUG");
        assertThat(Level.TRACE.toString()).isEqualTo("TRACE");
        assertThat(Level.ALL.toString()).isEqualTo("ALL");
    }

    @Test
    @DisplayName("Should decode valid level strings")
    public void shouldDecodeValidLevelStrings() {
        assertThat(Level.decode("ERROR")).isEqualTo(Level.ERROR);
        assertThat(Level.decode("WARN")).isEqualTo(Level.WARN);
        assertThat(Level.decode("INFO")).isEqualTo(Level.INFO);
        assertThat(Level.decode("DEBUG")).isEqualTo(Level.DEBUG);
        assertThat(Level.decode("TRACE")).isEqualTo(Level.TRACE);
        assertThat(Level.decode("ALL")).isEqualTo(Level.ALL);
    }

    @Test
    @DisplayName("Should decode level strings with whitespace")
    public void shouldDecodeLevelStringsWithWhitespace() {
        assertThat(Level.decode("  ERROR  ")).isEqualTo(Level.ERROR);
        assertThat(Level.decode("  INFO  ")).isEqualTo(Level.INFO);
    }

    @Test
    @DisplayName("Should return INFO for null input")
    public void shouldReturnInfoForNullInput() {
        assertThat(Level.decode(null)).isEqualTo(Level.INFO);
    }

    @Test
    @DisplayName("Should return INFO for empty string")
    public void shouldReturnInfoForEmptyString() {
        assertThat(Level.decode("")).isEqualTo(Level.INFO);
    }

    @Test
    @DisplayName("Should return INFO for blank string")
    public void shouldReturnInfoForBlankString() {
        assertThat(Level.decode("   ")).isEqualTo(Level.INFO);
    }

    @Test
    @DisplayName("Should return INFO for invalid level string")
    public void shouldReturnInfoForInvalidLevelString() {
        assertThat(Level.decode("INVALID")).isEqualTo(Level.INFO);
        assertThat(Level.decode("unknown")).isEqualTo(Level.INFO);
    }

    @Test
    @DisplayName("Should implement equals correctly")
    public void shouldImplementEqualsCorrectly() {
        assertThat(Level.ERROR).isEqualTo(Level.ERROR);
        assertThat(Level.ERROR).isNotEqualTo(Level.WARN);
        assertThat(Level.ERROR).isNotEqualTo(null);
        assertThat(Level.ERROR).isNotEqualTo("ERROR");
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    public void shouldImplementHashCodeCorrectly() {
        assertThat(Level.ERROR.hashCode()).isEqualTo(Level.ERROR.hashCode());
        assertThat(Level.ERROR.hashCode()).isNotEqualTo(Level.WARN.hashCode());
    }

    @Test
    @DisplayName("Should maintain level ordering")
    public void shouldMaintainLevelOrdering() {
        assertThat(Level.ERROR.toInt()).isLessThan(Level.WARN.toInt());
        assertThat(Level.WARN.toInt()).isLessThan(Level.INFO.toInt());
        assertThat(Level.INFO.toInt()).isLessThan(Level.DEBUG.toInt());
        assertThat(Level.DEBUG.toInt()).isLessThan(Level.TRACE.toInt());
        assertThat(Level.TRACE.toInt()).isLessThan(Level.ALL.toInt());
    }
}
