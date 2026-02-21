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

package org.verifyica.api.util;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Central support class for random value generation.
 *
 * <p>
 * By default, randomness is sourced from {@link ThreadLocalRandom}, providing
 * fast, thread-safe, non-deterministic behavior.
 * </p>
 *
 * <p>
 * For deterministic behavior (typically in tests), a thread-local seed may be
 * installed via {@link #useSeed(long)} or {@link #withSeed(long, Runnable)}.
 * Seeding is isolated per thread and will not affect other tests.
 * </p>
 *
 * <p>
 * All methods validate arguments explicitly and throw
 * {@link IllegalArgumentException} on invalid input.
 * </p>
 */
public final class RandomUtil {

    /** Alphabetic characters. */
    private static final char[] ALPHA = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    /** Alphanumeric characters. */
    private static final char[] ALPHANUM =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    /**
     * Thread-local deterministic RNG override.
     * When absent, {@link ThreadLocalRandom} is used.
     *
     * <p><strong>Warning:</strong> if a thread is returned to a pool while a seed
     * is still installed (e.g. because an exception bypassed cleanup), subsequent
     * unrelated work on that thread will silently use the deterministic RNG.
     * Prefer {@link #withSeed(long, Runnable)} for safe, scoped seeding.</p>
     */
    private static final ThreadLocal<Random> SEEDED = new ThreadLocal<>();

    /**
     * Prevent instantiation.
     */
    private RandomUtil() {}

    /**
     * Installs a deterministic seed for the current thread.
     *
     * <p><strong>Warning:</strong> this method leaves the seed active until
     * {@link #useThreadLocal()} is explicitly called. In thread-pool environments
     * this can cause the seed to bleed into unrelated work. Prefer the
     * scoped variant {@link #withSeed(long, Runnable)} wherever possible.</p>
     *
     * @param seed deterministic seed
     */
    public static void useSeed(long seed) {
        SEEDED.set(new Random(seed));
    }

    /**
     * Clears any deterministic seed for the current thread and restores
     * default non-deterministic behavior.
     */
    public static void useThreadLocal() {
        SEEDED.remove();
    }

    /**
     * Executes the given runnable using a deterministic seed for the
     * current thread, restoring the previous state afterward.
     *
     * @param seed deterministic seed
     * @param r    code to execute; must not be {@code null}
     * @throws NullPointerException if {@code r} is null
     */
    public static void withSeed(long seed, Runnable r) {
        if (r == null) {
            throw new IllegalArgumentException("r is null");
        }

        Random previous = SEEDED.get();
        try {
            SEEDED.set(new Random(seed));
            r.run();
        } finally {
            if (previous == null) {
                SEEDED.remove();
            } else {
                SEEDED.set(previous);
            }
        }
    }

    /**
     * Returns the active {@link Random} for the current thread.
     * Resolves the ThreadLocal exactly once per public call site so that
     * delegation helpers can accept the resolved instance directly.
     *
     * @return active random generator
     */
    private static Random random() {
        Random r = SEEDED.get();
        return (r != null) ? r : ThreadLocalRandom.current();
    }

    /**
     * Returns a uniformly distributed boolean.
     *
     * @return random boolean
     */
    public static boolean nextBoolean() {
        return random().nextBoolean();
    }

    /**
     * Returns a uniformly distributed integer in {@code [0, bound)}.
     *
     * @param bound exclusive upper bound (must be {@code > 0})
     * @return random integer
     */
    public static int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be > 0");
        }

        return random().nextInt(bound);
    }

    /**
     * Returns a uniformly distributed integer in
     * {@code [minInclusive, maxExclusive)}.
     *
     * @param minInclusive inclusive lower bound
     * @param maxExclusive exclusive upper bound
     * @return random integer
     */
    public static int nextInt(int minInclusive, int maxExclusive) {
        if (minInclusive >= maxExclusive) {
            throw new IllegalArgumentException("min must be < max");
        }

        return minInclusive + random().nextInt(maxExclusive - minInclusive);
    }

    /**
     * Returns a uniformly distributed long in {@code [0, bound)}.
     *
     * <p>Uses rejection sampling for non-power-of-2 bounds to guarantee
     * uniform distribution. The power-of-2 fast path via bit-masking is
     * correct for all power-of-2 bounds because {@link Random#nextLong()}
     * produces 64 independently-distributed bits (two independent 32-bit
     * words combined), so masking any number of low-order bits yields a
     * uniform result.</p>
     *
     * @param bound exclusive upper bound (must be {@code > 0})
     * @return random long
     */
    public static long nextLong(long bound) {
        if (bound <= 0L) {
            throw new IllegalArgumentException("bound must be > 0");
        }

        // FIX #4: resolve random() once and pass it to the private helper so that
        // callers of nextLong(min, max) — which delegate here — do not incur a
        // second ThreadLocal lookup (or repeated lookups inside the rejection loop).
        return nextLong(random(), bound);
    }

    /**
     * Returns a uniformly distributed long in
     * {@code [minInclusive, maxExclusive)}.
     *
     * @param minInclusive inclusive lower bound
     * @param maxExclusive exclusive upper bound
     * @return random long
     */
    public static long nextLong(long minInclusive, long maxExclusive) {
        if (minInclusive >= maxExclusive) {
            throw new IllegalArgumentException("min must be < max");
        }

        // FIX #4: resolve random() once and share it with the inner helper so the
        // rejection-sampling loop does not re-hit the ThreadLocal on every iteration.
        return minInclusive + nextLong(random(), maxExclusive - minInclusive);
    }

    /**
     * Core long generation helper. Accepts a pre-resolved {@link Random} so
     * that callers can avoid redundant {@link ThreadLocal} lookups, including
     * inside the rejection-sampling loop.
     *
     * @param rng   pre-resolved random generator
     * @param bound exclusive upper bound (must be {@code > 0}, caller-validated)
     * @return uniform random long in {@code [0, bound)}
     */
    private static long nextLong(Random rng, long bound) {
        long m = bound - 1L;

        if ((bound & m) == 0L) {
            // Power-of-2 fast path: bit-mask is safe and uniform.
            return rng.nextLong() & m;
        }

        long r;
        do {
            r = rng.nextLong() >>> 1;
        } while (r + m - (r % bound) < 0L);

        return r % bound;
    }

    /**
     * Returns a uniformly distributed double in {@code [0.0, 1.0)}.
     *
     * @return random double
     */
    public static double nextDouble() {
        return random().nextDouble();
    }

    /**
     * Returns a uniformly distributed double in
     * {@code [minInclusive, maxExclusive)}.
     *
     * @param minInclusive inclusive lower bound
     * @param maxExclusive exclusive upper bound
     * @return random double
     */
    public static double nextDouble(double minInclusive, double maxExclusive) {
        // FIX #1: was ">" which allowed min == max to silently return min;
        // changed to ">=" to match the contract of all other bounded methods.
        if (minInclusive >= maxExclusive) {
            throw new IllegalArgumentException("min must be < max");
        }

        return minInclusive + random().nextDouble() * (maxExclusive - minInclusive);
    }

    /**
     * Returns {@code true} with probability {@code p}.
     *
     * @param p probability in {@code [0,1]}
     * @return outcome
     */
    public static boolean chance(double p) {
        if (p <= 0.0) {
            return false;
        }

        if (p >= 1.0) {
            return true;
        }

        return random().nextDouble() < p;
    }

    /**
     * Returns {@code true} with probability {@code 1/n}.
     *
     * @param n denominator (must be {@code > 0})
     * @return outcome
     */
    public static boolean oneIn(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be > 0");
        }

        return random().nextInt(n) == 0;
    }

    /**
     * Returns a standard normal value with mean 0 and standard deviation 1.
     *
     * @return normally distributed value
     */
    public static double standardNormal() {
        return random().nextGaussian();
    }

    /**
     * Returns a normally distributed value with the given mean and standard deviation.
     *
     * @param mean   mean of the distribution
     * @param stddev standard deviation (must be {@code >= 0})
     * @return normally distributed value
     */
    public static double gaussian(double mean, double stddev) {
        if (stddev < 0.0) {
            throw new IllegalArgumentException("stddev must be >= 0");
        }

        return mean + random().nextGaussian() * stddev;
    }

    /**
     * Returns a normally distributed value clamped to the given range.
     *
     * @param mean         mean of the distribution
     * @param stddev       standard deviation (must be {@code >= 0})
     * @param minInclusive minimum allowed value
     * @param maxInclusive maximum allowed value
     * @return bounded normal value
     */
    public static double boundedGaussian(double mean, double stddev, double minInclusive, double maxInclusive) {
        // FIX #2: was "min must be <= max" which described the passing condition,
        // not the error; corrected to "min must be < max".
        if (maxInclusive <= minInclusive) {
            throw new IllegalArgumentException("min must be < max");
        }

        if (stddev < 0.0) {
            throw new IllegalArgumentException("stddev must be > 0");
        }

        // FIX #4: resolve random() once and delegate directly to avoid a second
        // ThreadLocal lookup inside gaussian(), which is called from here.
        Random rng = random();
        double x = mean + rng.nextGaussian() * stddev;

        if (x < minInclusive) {
            return minInclusive;
        }

        if (x > maxInclusive) {
            return maxInclusive;
        }

        return x;
    }

    /**
     * Returns a log-normal distributed value.
     *
     * @param mu    mean of the underlying normal distribution
     * @param sigma standard deviation of the underlying normal distribution
     *              (must be {@code >= 0})
     * @return log-normal value
     */
    public static double logNormal(double mu, double sigma) {
        if (sigma < 0.0) {
            throw new IllegalArgumentException("sigma must be >= 0");
        }

        // FIX #4: resolve random() once and inline the gaussian computation to
        // avoid a second ThreadLocal lookup inside the gaussian() delegation chain.
        return Math.exp(mu + random().nextGaussian() * sigma);
    }

    /**
     * Returns a random alphabetic string.
     *
     * @param length number of characters (must be {@code >= 0})
     * @return random string
     */
    public static String alphaString(int length) {
        return stringFrom(ALPHA, length);
    }

    /**
     * Returns a random alphanumeric string.
     *
     * @param length number of characters (must be {@code >= 0})
     * @return random string
     */
    public static String alphaNumericString(int length) {
        return stringFrom(ALPHANUM, length);
    }

    /**
     * Returns a random string built from the given alphabet.
     *
     * @param alphabet character set to draw from (must not be {@code null} or empty)
     * @param length   number of characters (must be {@code >= 0})
     * @return random string
     */
    public static String stringFrom(char[] alphabet, int length) {
        if (alphabet == null) {
            throw new IllegalArgumentException("alphabet is null");
        }

        if (alphabet.length == 0) {
            throw new IllegalArgumentException("alphabet is empty");
        }

        if (length < 0) {
            throw new IllegalArgumentException("length must be >= 0");
        }

        StringBuilder stringBuilder = new StringBuilder(length);
        Random rng = random();

        for (int i = 0; i < length; i++) {
            stringBuilder.append(alphabet[rng.nextInt(alphabet.length)]);
        }

        return stringBuilder.toString();
    }
}
