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

package org.verifyica.engine.common;

import static org.assertj.core.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.*;

@DisplayName("FastId Tests")
class FastIdTest {

    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");

    @Nested
    @DisplayName("Generation Tests")
    class GenerationTests {

        @Test
        @DisplayName("Should generate valid UUID-style string")
        void shouldGenerateValidUuidStyleString() {
            FastId id = FastId.randomFastId();

            String idString = id.toString();

            assertThat(idString).hasSize(36).containsPattern(UUID_PATTERN);
        }

        @Test
        @DisplayName("Should generate version 4 UUID")
        void shouldGenerateVersion4Uuid() {
            FastId id = FastId.randomFastId();

            String idString = id.toString();

            assertThat(idString.charAt(14)).isEqualTo('4');
        }

        @Test
        @DisplayName("Should generate IETF variant UUID")
        void shouldGenerateIetfVariantUuid() {
            FastId id = FastId.randomFastId();

            String idString = id.toString();
            char variantChar = idString.charAt(19);

            assertThat(variantChar).isIn('8', '9', 'a', 'b');
        }

        @Test
        @DisplayName("Should generate unique IDs")
        void shouldGenerateUniqueIds() {
            FastId id1 = FastId.randomFastId();
            FastId id2 = FastId.randomFastId();

            assertThat(id1).isNotEqualTo(id2);
            assertThat(id1.toString()).isNotEqualTo(id2.toString());
        }

        @Test
        @DisplayName("Should generate many unique IDs")
        void shouldGenerateManyUniqueIds() {
            int count = 10000;
            Set<String> ids = new HashSet<>();

            for (int i = 0; i < count; i++) {
                ids.add(FastId.randomFastId().toString());
            }

            assertThat(ids).hasSize(count);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should format with hyphens at correct positions")
        void shouldFormatWithHyphensAtCorrectPositions() {
            FastId id = FastId.randomFastId();

            String idString = id.toString();

            assertThat(idString.charAt(8)).isEqualTo('-');
            assertThat(idString.charAt(13)).isEqualTo('-');
            assertThat(idString.charAt(18)).isEqualTo('-');
            assertThat(idString.charAt(23)).isEqualTo('-');
        }

        @Test
        @DisplayName("Should return same string on multiple calls")
        void shouldReturnSameStringOnMultipleCalls() {
            FastId id = FastId.randomFastId();

            String str1 = id.toString();
            String str2 = id.toString();

            assertThat(str1).isEqualTo(str2);
        }

        @Test
        @DisplayName("Should contain only lowercase hex characters")
        void shouldContainOnlyLowercaseHexCharacters() {
            FastId id = FastId.randomFastId();

            String idString = id.toString().replace("-", "");

            assertThat(idString).matches("[0-9a-f]+");
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            FastId id = FastId.randomFastId();

            assertThat(id).isEqualTo(id);
        }

        @Test
        @DisplayName("Should not be equal to different ID")
        void shouldNotBeEqualToDifferentId() {
            FastId id1 = FastId.randomFastId();
            FastId id2 = FastId.randomFastId();

            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            FastId id = FastId.randomFastId();

            assertThat(id).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            FastId id = FastId.randomFastId();

            assertThat(id).isNotEqualTo(id.toString());
        }

        @Test
        @DisplayName("Should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            FastId id = FastId.randomFastId();

            int hash1 = id.hashCode();
            int hash2 = id.hashCode();

            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("Should have different hashCodes for different IDs")
        void shouldHaveDifferentHashCodesForDifferentIds() {
            int count = 1000;
            Set<Integer> hashCodes = new HashSet<>();

            for (int i = 0; i < count; i++) {
                hashCodes.add(FastId.randomFastId().hashCode());
            }

            // Allow for some collisions, but should have good distribution
            assertThat(hashCodes.size()).isGreaterThan((int) (count * 0.95));
        }
    }

    @Nested
    @DisplayName("Comparable Tests")
    class ComparableTests {

        @Test
        @DisplayName("Should compare equal IDs as zero")
        void shouldCompareEqualIdsAsZero() {
            FastId id = FastId.randomFastId();

            assertThat(id.compareTo(id)).isZero();
        }

        @Test
        @DisplayName("Should have consistent ordering")
        void shouldHaveConsistentOrdering() {
            FastId id1 = FastId.randomFastId();
            FastId id2 = FastId.randomFastId();

            int comparison1 = id1.compareTo(id2);
            int comparison2 = id2.compareTo(id1);

            if (comparison1 > 0) {
                assertThat(comparison2).isNegative();
            } else if (comparison1 < 0) {
                assertThat(comparison2).isPositive();
            } else {
                assertThat(comparison2).isZero();
            }
        }

        @Test
        @DisplayName("Should maintain transitivity")
        void shouldMaintainTransitivity() {
            FastId id1 = FastId.randomFastId();
            FastId id2 = FastId.randomFastId();
            FastId id3 = FastId.randomFastId();

            if (id1.compareTo(id2) <= 0 && id2.compareTo(id3) <= 0) {
                assertThat(id1.compareTo(id3)).isLessThanOrEqualTo(0);
            }
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Should generate unique IDs from multiple threads")
        void shouldGenerateUniqueIdsFromMultipleThreads() throws InterruptedException {
            int threadCount = 10;
            int idsPerThread = 1000;
            Set<String> allIds = new HashSet<>();
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < idsPerThread; j++) {
                        synchronized (allIds) {
                            allIds.add(FastId.randomFastId().toString());
                        }
                    }
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            assertThat(allIds).hasSize(threadCount * idsPerThread);
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should generate IDs quickly")
        void shouldGenerateIdsQuickly() {
            int count = 100000;
            long startTime = System.currentTimeMillis();

            for (int i = 0; i < count; i++) {
                FastId.randomFastId();
            }

            long duration = System.currentTimeMillis() - startTime;

            // Should generate 100k IDs in less than 1 second
            assertThat(duration).isLessThan(1000);
        }

        @Test
        @DisplayName("Should convert to string quickly")
        void shouldConvertToStringQuickly() {
            int count = 100000;
            FastId[] ids = new FastId[count];
            for (int i = 0; i < count; i++) {
                ids[i] = FastId.randomFastId();
            }

            long startTime = System.currentTimeMillis();

            for (FastId id : ids) {
                id.toString();
            }

            long duration = System.currentTimeMillis() - startTime;

            // Should convert 100k IDs to strings in less than 1 second
            assertThat(duration).isLessThan(1000);
        }
    }
}
