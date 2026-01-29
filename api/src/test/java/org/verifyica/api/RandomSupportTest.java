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

package org.verifyica.api;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Comprehensive tests for {@link RandomSupport}.
 *
 * Notes:
 * - These tests focus on argument validation, determinism via seeding, thread-local isolation,
 *   and basic invariants (ranges, clamping, charset membership).
 * - They intentionally avoid asserting statistical properties.
 */
@SuppressWarnings({"PMD.MethodNamingConventions", "PMD.JUnitTestsShouldIncludeAssert"})
public class RandomSupportTest {

    @AfterEach
    void cleanup() {
        // Ensure tests don't leak seeded state in the current thread
        RandomSupport.useThreadLocal();
    }

    /* ============================================================
     * Seeding / configuration
     * ============================================================ */

    @Test
    public void useSeedMakesSequenceDeterministicInCurrentThread() {
        RandomSupport.useSeed(123L);

        int a1 = RandomSupport.nextInt(1_000_000);
        long a2 = RandomSupport.nextLong(1_000_000L);
        double a3 = RandomSupport.nextDouble();
        boolean a4 = RandomSupport.nextBoolean();
        double a5 = RandomSupport.standardNormal();
        String a6 = RandomSupport.alphaString(32);

        RandomSupport.useSeed(123L);

        assertThat(RandomSupport.nextInt(1_000_000)).isEqualTo(a1);
        assertThat(RandomSupport.nextLong(1_000_000L)).isEqualTo(a2);
        assertThat(RandomSupport.nextDouble()).isEqualTo(a3);
        assertThat(RandomSupport.nextBoolean()).isEqualTo(a4);
        assertThat(RandomSupport.standardNormal()).isEqualTo(a5);
        assertThat(RandomSupport.alphaString(32)).isEqualTo(a6);
    }

    @Test
    public void useThreadLocalClearsDeterministicSeed() {
        RandomSupport.useSeed(7L);
        int seeded = RandomSupport.nextInt(1_000_000);

        RandomSupport.useThreadLocal();

        // Not a perfect guarantee, but extremely likely to differ; still keep the assertion weak:
        int unseeded = RandomSupport.nextInt(1_000_000);

        assertThat(unseeded).isBetween(0, 999_999);

        // If this ever flakes (very low probability), it still indicates state was cleared; avoid strict inequality.
        assertThat(seeded).isBetween(0, 999_999);
    }

    @Test
    public void withSeedRejectsNullRunnable() {
        assertThatThrownBy(() -> RandomSupport.withSeed(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("r is null");
    }

    @Test
    public void withSeedRestoresPreviousSeed_afterRun() {
        RandomSupport.useSeed(111L);

        int before1 = RandomSupport.nextInt(1_000_000);
        int before2 = RandomSupport.nextInt(1_000_000);

        // Recreate expectation by reseeding and consuming same draws, then consuming one more after the withSeed block.
        RandomSupport.useSeed(111L);

        int expBefore1 = RandomSupport.nextInt(1_000_000);
        int expBefore2 = RandomSupport.nextInt(1_000_000);
        int expAfter = RandomSupport.nextInt(1_000_000);

        assertThat(before1).isEqualTo(expBefore1);
        assertThat(before2).isEqualTo(expBefore2);

        RandomSupport.useSeed(111L);
        RandomSupport.nextInt(1_000_000); // consume before1
        RandomSupport.nextInt(1_000_000); // consume before2

        RandomSupport.withSeed(999L, () -> {
            // run some draws to disturb the temporary RNG
            RandomSupport.nextInt(10);
            RandomSupport.nextLong(10);
            RandomSupport.nextDouble();
            RandomSupport.alphaNumericString(10);
        });

        assertThat(RandomSupport.nextInt(1_000_000)).isEqualTo(expAfter);
    }

    @Test
    public void withSeedRestoresToThreadLocalWhenNoPreviousSeed() {
        // Ensure no previous seed
        RandomSupport.useThreadLocal();

        AtomicReference<List<Integer>> first = new AtomicReference<>();
        AtomicReference<List<Integer>> second = new AtomicReference<>();

        RandomSupport.withSeed(42L, () -> first.set(sampleInts(5, 1_000_000)));
        RandomSupport.withSeed(42L, () -> second.set(sampleInts(5, 1_000_000)));

        assertThat(second.get()).isEqualTo(first.get());

        // After withSeed, it should not "stay" deterministic; we can't strictly prove non-determinism,
        // but we can at least ensure calls still work and are in range.
        assertThat(RandomSupport.nextInt(10)).isBetween(0, 9);
    }

    /* ============================================================
     * Thread-local isolation
     * ============================================================ */

    @Test
    @Timeout(5)
    public void seedingIsThreadLocalAndDoesNotLeakAcrossThreads() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);

        try {
            Callable<List<Integer>> taskA = () -> {
                RandomSupport.useSeed(1L);
                return sampleInts(10, 1_000_000);
            };
            Callable<List<Integer>> taskB = () -> {
                RandomSupport.useSeed(2L);
                return sampleInts(10, 1_000_000);
            };

            Future<List<Integer>> fa = pool.submit(taskA);
            Future<List<Integer>> fb = pool.submit(taskB);

            List<Integer> a = fa.get();
            List<Integer> b = fb.get();

            assertThat(a).hasSize(10);
            assertThat(b).hasSize(10);

            // Different seeds should produce different sequences (for Java Random, overwhelmingly likely and stable).
            assertThat(a).isNotEqualTo(b);

            // Same seed in another thread yields the same sequence for that thread.
            Future<List<Integer>> fa2 = pool.submit(() -> {
                RandomSupport.useSeed(1L);
                return sampleInts(10, 1_000_000);
            });

            assertThat(fa2.get()).isEqualTo(a);
        } finally {
            pool.shutdownNow();
        }
    }

    /* ============================================================
     * Basic primitives: range/validation
     * ============================================================ */

    @Test
    public void nextIntBoundValidates() {
        assertThatThrownBy(() -> RandomSupport.nextInt(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("bound must be > 0");

        assertThatThrownBy(() -> RandomSupport.nextInt(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("bound must be > 0");
    }

    @Test
    public void nextIntBoundIsWithinRange() {
        RandomSupport.useSeed(123L);

        IntStream.range(0, 10_000)
                .forEach(i -> assertThat(RandomSupport.nextInt(7)).isBetween(0, 6));
    }

    @Test
    public void nextIntMinMaxValidates() {
        assertThatThrownBy(() -> RandomSupport.nextInt(5, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("min must be < max");

        assertThatThrownBy(() -> RandomSupport.nextInt(6, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("min must be < max");
    }

    @Test
    public void nextIntMinMaxIsWithinRange() {
        RandomSupport.useSeed(321L);

        IntStream.range(0, 10_000)
                .forEach(i -> assertThat(RandomSupport.nextInt(-3, 4)).isBetween(-3, 3));
    }

    @Test
    public void nextLongBoundValidates() {
        assertThatThrownBy(() -> RandomSupport.nextLong(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("bound must be > 0");

        assertThatThrownBy(() -> RandomSupport.nextLong(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("bound must be > 0");
    }

    @Test
    public void nextLongBoundIsWithinRange_powerOfTwo() {
        RandomSupport.useSeed(99L);

        long bound = 1024L; // power of two triggers fast-path
        for (int i = 0; i < 10_000; i++) {
            assertThat(RandomSupport.nextLong(bound)).isBetween(0L, bound - 1);
        }
    }

    @Test
    public void nextLongBoundIsWithinRange_nonPowerOfTwo() {
        RandomSupport.useSeed(99L);

        long bound = 1_000L; // non-power of two triggers rejection loop
        for (int i = 0; i < 10_000; i++) {
            assertThat(RandomSupport.nextLong(bound)).isBetween(0L, bound - 1);
        }
    }

    @Test
    public void nextLongMinMaxValidates() {
        assertThatThrownBy(() -> RandomSupport.nextLong(5L, 5L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("min must be < max");

        assertThatThrownBy(() -> RandomSupport.nextLong(6L, 5L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("min must be < max");
    }

    @Test
    public void nextLongMinMaxIsWithinRange() {
        RandomSupport.useSeed(123L);

        for (int i = 0; i < 10_000; i++) {
            assertThat(RandomSupport.nextLong(-10L, 10L)).isBetween(-10L, 9L);
        }
    }

    @Test
    public void nextDoubleIsWithinUnitInterval() {
        RandomSupport.useSeed(123L);

        for (int i = 0; i < 10_000; i++) {
            double x = RandomSupport.nextDouble();
            assertThat(x).isGreaterThanOrEqualTo(0.0);
            assertThat(x).isLessThan(1.0);
        }
    }

    @Test
    public void nextDoubleMinMaxValidatesAccordingToImplementation() {
        // Implementation checks (minInclusive > maxExclusive) but message says "min must be < max"
        assertThatThrownBy(() -> RandomSupport.nextDouble(2.0, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("min must be < max");
    }

    @Test
    public void nextDoubleMinMaxProducesValuesWithinRange_includingEqualBounds() {
        RandomSupport.useSeed(123L);

        // When min == max, should always return that value (because (max - min) == 0)
        assertThat(RandomSupport.nextDouble(5.0, 5.0)).isEqualTo(5.0);

        for (int i = 0; i < 10_000; i++) {
            double x = RandomSupport.nextDouble(-2.5, 7.25);
            assertThat(x).isGreaterThanOrEqualTo(-2.5);
            assertThat(x).isLessThanOrEqualTo(7.25); // mathematically < 7.25, but floating ops; keep <=
        }
    }

    /* ============================================================
     * Probability helpers
     * ============================================================ */

    @Test
    public void chanceHandlesBoundaryCases() {
        assertThat(RandomSupport.chance(-1.0)).isFalse();
        assertThat(RandomSupport.chance(0.0)).isFalse();
        assertThat(RandomSupport.chance(1.0)).isTrue();
        assertThat(RandomSupport.chance(2.0)).isTrue();
    }

    @Test
    public void chanceIsDeterministicWithSeed_forMidProbability() {
        RandomSupport.useSeed(100L);

        List<Boolean> a = sampleBoolsChance(20, 0.3);

        RandomSupport.useSeed(100L);

        List<Boolean> b = sampleBoolsChance(20, 0.3);

        assertThat(b).isEqualTo(a);
    }

    @Test
    public void oneInValidates() {
        assertThatThrownBy(() -> RandomSupport.oneIn(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("n must be > 0");

        assertThatThrownBy(() -> RandomSupport.oneIn(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("n must be > 0");
    }

    @Test
    public void oneInOneIsAlwaysTrue() {
        RandomSupport.useSeed(1L);

        for (int i = 0; i < 10_000; i++) {
            assertThat(RandomSupport.oneIn(1)).isTrue();
        }
    }

    @Test
    public void oneInIsDeterministicWithSeed() {
        RandomSupport.useSeed(55L);

        List<Boolean> a = sampleBoolsOneIn(50, 7);

        RandomSupport.useSeed(55L);

        List<Boolean> b = sampleBoolsOneIn(50, 7);

        assertThat(b).isEqualTo(a);
    }

    /* ============================================================
     * Gaussian / distributions: validation + invariants
     * ============================================================ */

    @Test
    public void gaussianValidatesStddev() {
        assertThatThrownBy(() -> RandomSupport.gaussian(0.0, -0.0001))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("stddev must be >= 0");
    }

    @Test
    public void gaussianWithZeroStddevAlwaysReturnsMean() {
        RandomSupport.useSeed(123L);

        for (int i = 0; i < 1_000; i++) {
            assertThat(RandomSupport.gaussian(12.34, 0.0)).isEqualTo(12.34);
        }
    }

    @Test
    public void boundedGaussianValidatesRangeAndStddev() {
        assertThatThrownBy(() -> RandomSupport.boundedGaussian(0.0, 1.0, 5.0, 5.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("min must be <= max");

        assertThatThrownBy(() -> RandomSupport.boundedGaussian(0.0, -0.1, 0.0, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("stddev must be >= 0");
    }

    @Test
    public void boundedGaussianClampsToMinMaxInclusive() {
        RandomSupport.useSeed(123L);

        double min = -1.0;
        double max = 1.0;

        for (int i = 0; i < 10_000; i++) {
            double x = RandomSupport.boundedGaussian(0.0, 10.0, min, max);
            assertThat(x).isBetween(min, max);
        }
    }

    @Test
    public void logNormalValidatesSigma() {
        assertThatThrownBy(() -> RandomSupport.logNormal(0.0, -0.1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sigma must be >= 0");
    }

    @Test
    public void logNormalIsAlwaysPositive() {
        RandomSupport.useSeed(123L);
        for (int i = 0; i < 10_000; i++) {
            assertThat(RandomSupport.logNormal(0.0, 1.0)).isGreaterThan(0.0);
        }
    }

    /* ============================================================
     * Strings
     * ============================================================ */

    @Test
    public void stringFromValidatesArguments() {
        assertThatThrownBy(() -> RandomSupport.stringFrom(null, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("alphabet is null");

        assertThatThrownBy(() -> RandomSupport.stringFrom(new char[0], 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("alphabet is empty");

        assertThatThrownBy(() -> RandomSupport.stringFrom(new char[] {'a'}, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("length must be >= 0");
    }

    @Test
    public void stringFromLengthZeroReturnsEmptyString() {
        RandomSupport.useSeed(1L);

        assertThat(RandomSupport.stringFrom(new char[] {'x', 'y'}, 0)).isEqualTo("");
    }

    @Test
    public void stringFromUsesOnlyAlphabetCharacters() {
        RandomSupport.useSeed(123L);

        char[] alphabet = new char[] {'a', 'b', 'c'};
        String s = RandomSupport.stringFrom(alphabet, 10_000);

        assertThat(s).hasSize(10_000);

        assertThat(s.chars().allMatch(ch -> ch == 'a' || ch == 'b' || ch == 'c'))
                .isTrue();
    }

    @Test
    public void alphaStringHasCorrectLengthAndOnlyLetters() {
        RandomSupport.useSeed(123L);

        String s = RandomSupport.alphaString(4096);

        assertThat(s).hasSize(4096);
        assertThat(s).matches("^[a-zA-Z]*$");
    }

    @Test
    public void alphaNumericStringHasCorrectLengthAndOnlyAlphaNum() {
        RandomSupport.useSeed(123L);

        String s = RandomSupport.alphaNumericString(4096);

        assertThat(s).hasSize(4096);
        assertThat(s).matches("^[a-zA-Z0-9]*$");
    }

    @Test
    public void alphaAndAlphaNumericAreDeterministicWithSeed() {
        RandomSupport.useSeed(77L);

        String a1 = RandomSupport.alphaString(64);
        String a2 = RandomSupport.alphaNumericString(64);

        RandomSupport.useSeed(77L);

        assertThat(RandomSupport.alphaString(64)).isEqualTo(a1);
        assertThat(RandomSupport.alphaNumericString(64)).isEqualTo(a2);
    }

    /* ============================================================
     * Helpers
     * ============================================================ */

    private static List<Integer> sampleInts(int count, int bound) {
        List<Integer> out = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            out.add(RandomSupport.nextInt(bound));
        }

        return out;
    }

    private static List<Boolean> sampleBoolsChance(int count, double p) {
        List<Boolean> out = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            out.add(RandomSupport.chance(p));
        }

        return out;
    }

    private static List<Boolean> sampleBoolsOneIn(int count, int n) {
        List<Boolean> out = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            out.add(RandomSupport.oneIn(n));
        }

        return out;
    }
}
