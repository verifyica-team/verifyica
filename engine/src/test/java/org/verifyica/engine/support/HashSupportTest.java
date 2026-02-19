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

package org.verifyica.engine.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("HashSupport Tests")
public class HashSupportTest {

    @Test
    @DisplayName("Should generate alphanumeric hash of specified length")
    public void shouldGenerateAlphanumericHashOfSpecifiedLength() {
        String hash = HashSupport.alphanumeric(10);

        assertThat(hash).hasSize(10);
    }

    @Test
    @DisplayName("Should generate different hashes on multiple calls")
    public void shouldGenerateDifferentHashesOnMultipleCalls() {
        String hash1 = HashSupport.alphanumeric(10);
        String hash2 = HashSupport.alphanumeric(10);

        // Note: There's a very small probability of collision
        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    @DisplayName("Should generate hash with only alphanumeric characters")
    public void shouldGenerateHashWithOnlyAlphanumericCharacters() {
        String hash = HashSupport.alphanumeric(20);

        assertThat(hash).matches("^[a-zA-Z0-9]+$");
    }

    @Test
    @DisplayName("Should throw exception for zero length")
    public void shouldThrowExceptionForZeroLength() {
        assertThatThrownBy(() -> HashSupport.alphanumeric(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("length is less than 1");
    }

    @Test
    @DisplayName("Should throw exception for negative length")
    public void shouldThrowExceptionForNegativeLength() {
        assertThatThrownBy(() -> HashSupport.alphanumeric(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("length is less than 1");
    }

    @Test
    @DisplayName("Should generate hash of length 1")
    public void shouldGenerateHashOfLength1() {
        String hash = HashSupport.alphanumeric(1);

        assertThat(hash).hasSize(1);
        assertThat(hash).matches("^[a-zA-Z0-9]$");
    }

    @Test
    @DisplayName("Should generate hash of large length")
    public void shouldGenerateHashOfLargeLength() {
        String hash = HashSupport.alphanumeric(100);

        assertThat(hash).hasSize(100);
        assertThat(hash).matches("^[a-zA-Z0-9]+$");
    }

    @Test
    @DisplayName("Should not contain 'fail' substring")
    public void shouldNotContainFailSubstring() {
        // Generate multiple hashes to increase chance of hitting a "fail" case
        for (int i = 0; i < 100; i++) {
            String hash = HashSupport.alphanumeric(10);
            assertThat(hash.toLowerCase()).doesNotContain("fail");
        }
    }
}
