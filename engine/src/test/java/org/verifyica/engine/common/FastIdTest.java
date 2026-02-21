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
public class FastIdTest {

    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");

    @Nested
    @DisplayName("Generation Tests")
    public class GenerationTests {

        @Test
        @DisplayName("Should generate valid UUID-style string")
        public void shouldGenerateValidUuidStyleString() {
            final FastId id = FastId.randomFastId();

            final String idString = id.toString();

            assertThat(idString).hasSize(36).containsPattern(UUID_PATTERN);
        }

        @Test
        @DisplayName("Should generate version 4 UUID")
        public void shouldGenerateVersion4Uuid() {
            final FastId id = FastId.randomFastId();

            final String idString = id.toString();

            assertThat(idString.charAt(14)).isEqualTo('4');
        }

        @Test
        @DisplayName("Should generate IETF variant UUID")
        public void shouldGenerateIetfVariantUuid() {
            final FastId id = FastId.randomFastId();

            final String idString = id.toString();
            final char variantChar = idString.charAt(19);

            assertThat(variantChar).isIn('8', '9', 'a', 'b');
        }

        @Test
        @DisplayName("Should generate unique IDs")
        public void shouldGenerateUniqueIds() {
            final FastId id1 = FastId.randomFastId();
            final FastId id2 = FastId.randomFastId();

            assertThat(id1).isNotEqualTo(id2);
            assertThat(id1.toString()).isNotEqualTo(id2.toString());
        }

        @Test
        @DisplayName("Should generate many unique IDs")
        public void shouldGenerateManyUniqueIds() {
            final int count = 10000;
            final Set<String> ids = new HashSet<>();

            for (int i = 0; i < count; i++) {
                ids.add(FastId.randomFastId().toString());
            }

            assertThat(ids).hasSize(count);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    public class ToStringTests {

        @Test
        @DisplayName("Should format with hyphens at correct positions")
        public void shouldFormatWithHyphensAtCorrectPositions() {
            final FastId id = FastId.randomFastId();

            final String idString = id.toString();

            assertThat(idString.charAt(8)).isEqualTo('-');
            assertThat(idString.charAt(13)).isEqualTo('-');
            assertThat(idString.charAt(18)).isEqualTo('-');
            assertThat(idString.charAt(23)).isEqualTo('-');
        }

        @Test
        @DisplayName("Should return same string on multiple calls")
        public void shouldReturnSameStringOnMultipleCalls() {
            final FastId id = FastId.randomFastId();

            final String str1 = id.toString();
            final String str2 = id.toString();

            assertThat(str1).isEqualTo(str2);
        }

        @Test
        @DisplayName("Should contain only lowercase hex characters")
        public void shouldContainOnlyLowercaseHexCharacters() {
            final FastId id = FastId.randomFastId();

            final String idString = id.toString().replace("-", "");

            assertThat(idString).matches("[0-9a-f]+");
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    public class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        public void shouldBeEqualToItself() {
            final FastId id = FastId.randomFastId();

            assertThat(id).isEqualTo(id);
        }

        @Test
        @DisplayName("Should not be equal to different ID")
        public void shouldNotBeEqualToDifferentId() {
            final FastId id1 = FastId.randomFastId();
            final FastId id2 = FastId.randomFastId();

            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        public void shouldNotBeEqualToNull() {
            final FastId id = FastId.randomFastId();

            assertThat(id).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        public void shouldNotBeEqualToDifferentType() {
            final FastId id = FastId.randomFastId();

            assertThat(id).isNotEqualTo(id.toString());
        }

        @Test
        @DisplayName("Should have consistent hashCode")
        public void shouldHaveConsistentHashCode() {
            final FastId id = FastId.randomFastId();

            final int hash1 = id.hashCode();
            final int hash2 = id.hashCode();

            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("Should have different hashCodes for different IDs")
        public void shouldHaveDifferentHashCodesForDifferentIds() {
            final int count = 1000;
            final Set<Integer> hashCodes = new HashSet<>();

            for (int i = 0; i < count; i++) {
                hashCodes.add(FastId.randomFastId().hashCode());
            }

            // Allow for some collisions, but should have good distribution
            assertThat(hashCodes.size()).isGreaterThan((int) (count * 0.95));
        }
    }

    @Nested
    @DisplayName("From Factory Tests")
    public class FromFactoryTests {

        @Test
        @DisplayName("Should create FastId from raw bits")
        public void shouldCreateFastIdFromRawBits() {
            final long msb = 0x123456789ABCDEF0L;
            final long lsb = 0x0FEDCBA987654321L;

            final FastId id = FastId.from(msb, lsb);

            assertThat(id.getMostSignificantBits()).isEqualTo(msb);
            assertThat(id.getLeastSignificantBits()).isEqualTo(lsb);
        }

        @Test
        @DisplayName("Should create FastId with zero bits")
        public void shouldCreateFastIdWithZeroBits() {
            final FastId id = FastId.from(0L, 0L);

            assertThat(id.getMostSignificantBits()).isEqualTo(0L);
            assertThat(id.getLeastSignificantBits()).isEqualTo(0L);
            assertThat(id.toString()).isEqualTo("00000000-0000-0000-0000-000000000000");
        }

        @Test
        @DisplayName("Should create FastId with maximum bits")
        public void shouldCreateFastIdWithMaximumBits() {
            final FastId id = FastId.from(-1L, -1L);

            assertThat(id.getMostSignificantBits()).isEqualTo(-1L);
            assertThat(id.getLeastSignificantBits()).isEqualTo(-1L);
            assertThat(id.toString()).isEqualTo("ffffffff-ffff-ffff-ffff-ffffffffffff");
        }

        @Test
        @DisplayName("Should create equal FastIds from same bits")
        public void shouldCreateEqualFastIdsFromSameBits() {
            final long msb = 0x123456789ABCDEF0L;
            final long lsb = 0x0FEDCBA987654321L;

            final FastId id1 = FastId.from(msb, lsb);
            final FastId id2 = FastId.from(msb, lsb);

            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("Should create different FastIds from different bits")
        public void shouldCreateDifferentFastIdsFromDifferentBits() {
            final FastId id1 = FastId.from(0x123456789ABCDEF0L, 0L);
            final FastId id2 = FastId.from(0x0FEDCBA987654321L, 0L);

            assertThat(id1).isNotEqualTo(id2);
        }
    }

    @Nested
    @DisplayName("Getters Tests")
    public class GettersTests {

        @Test
        @DisplayName("Should return correct most significant bits")
        public void shouldReturnCorrectMostSignificantBits() {
            final long msb = 0x123456789ABCDEF0L;
            final long lsb = 0x0FEDCBA987654321L;

            final FastId id = FastId.from(msb, lsb);

            assertThat(id.getMostSignificantBits()).isEqualTo(msb);
        }

        @Test
        @DisplayName("Should return correct least significant bits")
        public void shouldReturnCorrectLeastSignificantBits() {
            final long msb = 0x123456789ABCDEF0L;
            final long lsb = 0x0FEDCBA987654321L;

            final FastId id = FastId.from(msb, lsb);

            assertThat(id.getLeastSignificantBits()).isEqualTo(lsb);
        }

        @Test
        @DisplayName("Should return bits consistent with toString")
        public void shouldReturnBitsConsistentWithToString() {
            final FastId id = FastId.from(0x12345678L, 0x9ABCDEF0L);

            final String str = id.toString();
            final String expectedMsb = str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18);
            final String expectedLsb = str.substring(19, 23) + str.substring(24);

            assertThat(Long.parseUnsignedLong(expectedMsb, 16)).isEqualTo(id.getMostSignificantBits());
            assertThat(Long.parseUnsignedLong(expectedLsb, 16)).isEqualTo(id.getLeastSignificantBits());
        }
    }

    @Nested
    @DisplayName("Comparable Tests")
    public class ComparableTests {

        @Test
        @DisplayName("Should compare equal IDs as zero")
        public void shouldCompareEqualIdsAsZero() {
            final FastId id = FastId.randomFastId();

            assertThat(id.compareTo(id)).isZero();
        }

        @Test
        @DisplayName("Should throw when comparing to null")
        public void shouldThrowWhenComparingToNull() {
            final FastId id = FastId.randomFastId();

            assertThatThrownBy(() -> id.compareTo(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("other is null");
        }

        @Test
        @DisplayName("Should have consistent ordering")
        public void shouldHaveConsistentOrdering() {
            final FastId id1 = FastId.randomFastId();
            final FastId id2 = FastId.randomFastId();

            final int comparison1 = id1.compareTo(id2);
            final int comparison2 = id2.compareTo(id1);

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
        public void shouldMaintainTransitivity() {
            final FastId id1 = FastId.randomFastId();
            final FastId id2 = FastId.randomFastId();
            final FastId id3 = FastId.randomFastId();

            if (id1.compareTo(id2) <= 0 && id2.compareTo(id3) <= 0) {
                assertThat(id1.compareTo(id3)).isLessThanOrEqualTo(0);
            }
        }

        @Test
        @DisplayName("Should compare correctly when msb differs")
        public void shouldCompareCorrectlyWhenMsbDiffers() {
            final FastId id1 = FastId.from(0x1000000000000000L, 0L);
            final FastId id2 = FastId.from(0x2000000000000000L, 0L);

            assertThat(id1.compareTo(id2)).isNegative();
            assertThat(id2.compareTo(id1)).isPositive();
        }

        @Test
        @DisplayName("Should compare correctly when msb same but lsb differs")
        public void shouldCompareCorrectlyWhenMsbSameButLsbDiffers() {
            final FastId id1 = FastId.from(0x123456789ABCDEF0L, 0x1000000000000000L);
            final FastId id2 = FastId.from(0x123456789ABCDEF0L, 0x2000000000000000L);

            assertThat(id1.compareTo(id2)).isNegative();
            assertThat(id2.compareTo(id1)).isPositive();
        }

        @Test
        @DisplayName("Should compare correctly with negative values")
        public void shouldCompareCorrectlyWithNegativeValues() {
            // Note: FastId uses signed long comparison, so -1L < 0L
            final FastId id1 = FastId.from(-1L, 0L); // All bits set in MSB (signed: -1)
            final FastId id2 = FastId.from(0L, 0L); // All bits clear in MSB (signed: 0)

            assertThat(id1.compareTo(id2)).isNegative();
            assertThat(id2.compareTo(id1)).isPositive();
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    public class ThreadSafetyTests {

        @Test
        @DisplayName("Should generate unique IDs from multiple threads")
        public void shouldGenerateUniqueIdsFromMultipleThreads() throws InterruptedException {
            final int threadCount = 10;
            final int idsPerThread = 1000;
            final Set<String> allIds = new HashSet<>();
            final Thread[] threads = new Thread[threadCount];

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

            for (final Thread thread : threads) {
                thread.join();
            }

            assertThat(allIds).hasSize(threadCount * idsPerThread);
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    public class PerformanceTests {

        @Test
        @DisplayName("Should generate IDs quickly")
        public void shouldGenerateIdsQuickly() {
            final int count = 100000;
            final long startTime = System.currentTimeMillis();

            for (int i = 0; i < count; i++) {
                FastId.randomFastId();
            }

            final long duration = System.currentTimeMillis() - startTime;

            // Should generate 100k IDs in less than 1 second
            assertThat(duration).isLessThan(1000);
        }

        @Test
        @DisplayName("Should convert to string quickly")
        public void shouldConvertToStringQuickly() {
            final int count = 100000;
            final FastId[] ids = new FastId[count];
            for (int i = 0; i < count; i++) {
                ids[i] = FastId.randomFastId();
            }

            final long startTime = System.currentTimeMillis();

            for (final FastId id : ids) {
                id.toString();
            }

            final long duration = System.currentTimeMillis() - startTime;

            // Should convert 100k IDs to strings in less than 1 second
            assertThat(duration).isLessThan(1000);
        }
    }
}
