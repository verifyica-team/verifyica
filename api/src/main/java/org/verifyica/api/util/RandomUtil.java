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

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.ZigguratSampler;
import org.apache.commons.rng.simple.RandomSource;

/**
 * Central support class for random value generation.
 *
 * <p>
 * By default, randomness is sourced from a per-thread
 * {@link RandomSource#XO_SHI_RO_256_SS} instance, providing fast,
 * thread-safe, non-deterministic behavior. XoShiRo256** is a 256-bit
 * generator with a period of 2&#x00B2;&#x2075;&#x2076;&#x22121; that passes
 * BigCrush and PractRand.
 * </p>
 *
 * <p>
 * Gaussian values are produced by the Ziggurat algorithm
 * ({@link ZigguratSampler.NormalizedGaussian}), which is faster and
 * statistically superior to the Box-Muller transform used by
 * {@code java.util.Random#nextGaussian()}.
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
 *
 * <h2>Required dependencies (Apache License 2.0, Java 8+)</h2>
 * <pre>
 *   org.apache.commons:commons-rng-simple:1.6
 *   org.apache.commons:commons-rng-sampling:1.6
 * </pre>
 */
public final class RandomUtil {

    /** Alphabetic characters. */
    private static final char[] ALPHA = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    /** Alphanumeric characters. */
    private static final char[] ALPHANUM =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    /**
     * Per-thread default RNG — XoShiRo256** seeded non-deterministically.
     * Created lazily on first access per thread; never removed.
     */
    private static final ThreadLocal<RngHolder> DEFAULT_HOLDER =
            ThreadLocal.withInitial(() -> new RngHolder(RandomSource.XO_SHI_RO_256_SS.create()));

    /**
     * Thread-local deterministic RNG override.
     *
     * <p><strong>Warning:</strong> if a thread is returned to a pool while a seed
     * is still installed (e.g. because an exception bypassed cleanup), subsequent
     * unrelated work on that thread will silently use the deterministic RNG.
     * Prefer {@link #withSeed(long, Runnable)} for safe, scoped seeding.</p>
     */
    private static final ThreadLocal<RngHolder> SEEDED_HOLDER = new ThreadLocal<>();

    // -------------------------------------------------------------------------
    // Prevent instantiation
    // -------------------------------------------------------------------------

    private RandomUtil() {}

    // -------------------------------------------------------------------------
    // Seeding API  (signatures unchanged)
    // -------------------------------------------------------------------------

    /**
     * Installs a deterministic seed for the current thread.
     *
     * <p><strong>Warning:</strong> this method leaves the seed active until
     * {@link #useThreadLocal()} is explicitly called. In thread-pool
     * environments this can cause the seed to bleed into unrelated work.
     * Prefer the scoped variant {@link #withSeed(long, Runnable)} wherever
     * possible.</p>
     *
     * @param seed deterministic seed
     */
    public static void useSeed(long seed) {
        SEEDED_HOLDER.set(new RngHolder(RandomSource.XO_SHI_RO_256_SS.create(seed)));
    }

    /**
     * Clears any deterministic seed for the current thread and restores
     * default non-deterministic behavior.
     */
    public static void useThreadLocal() {
        SEEDED_HOLDER.remove();
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

        RngHolder previous = SEEDED_HOLDER.get();
        try {
            SEEDED_HOLDER.set(new RngHolder(RandomSource.XO_SHI_RO_256_SS.create(seed)));
            r.run();
        } finally {
            if (previous == null) {
                SEEDED_HOLDER.remove();
            } else {
                SEEDED_HOLDER.set(previous);
            }
        }
    }

    /**
     * Returns the active {@link RngHolder} for the current thread.
     *
     * <p>Resolving the holder once per public entry point and passing it
     * directly to any private helpers avoids repeated {@link ThreadLocal}
     * lookups inside loops or delegation chains.</p>
     *
     * @return active RNG holder
     */
    private static RngHolder holder() {
        RngHolder seeded = SEEDED_HOLDER.get();
        return (seeded != null) ? seeded : DEFAULT_HOLDER.get();
    }
    
    /**
     * Returns a uniformly distributed boolean.
     *
     * @return random boolean
     */
    public static boolean nextBoolean() {
        return holder().rng.nextBoolean();
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

        return holder().rng.nextInt(bound);
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

        return minInclusive + holder().rng.nextInt(maxExclusive - minInclusive);
    }

    /**
     * Returns a uniformly distributed long in {@code [0, bound)}.
     *
     * <p>Delegates to {@link UniformRandomProvider#nextLong(long)}, which uses
     * a bias-free rejection-sampling algorithm — no hand-rolled loop required.</p>
     *
     * @param bound exclusive upper bound (must be {@code > 0})
     * @return random long
     */
    public static long nextLong(long bound) {
        if (bound <= 0L) {
            throw new IllegalArgumentException("bound must be > 0");
        }

        return holder().rng.nextLong(bound);
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

        // Resolve holder once so the inner call reuses the same RNG reference
        // without a second ThreadLocal lookup.
        return minInclusive + holder().rng.nextLong(maxExclusive - minInclusive);
    }

    /**
     * Returns a uniformly distributed double in {@code [0.0, 1.0)}.
     *
     * @return random double
     */
    public static double nextDouble() {
        return holder().rng.nextDouble();
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
        if (minInclusive >= maxExclusive) {
            throw new IllegalArgumentException("min must be < max");
        }

        return minInclusive + holder().rng.nextDouble() * (maxExclusive - minInclusive);
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

        return holder().rng.nextDouble() < p;
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

        return holder().rng.nextInt(n) == 0;
    }

    /**
     * Returns a standard normal value with mean 0 and standard deviation 1.
     *
     * <p>Uses the Ziggurat algorithm, which is faster and produces better
     * tail behaviour than the Box-Muller transform used by
     * {@code java.util.Random#nextGaussian()}.</p>
     *
     * @return normally distributed value
     */
    public static double standardNormal() {
        return holder().gaussian.sample();
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

        // Resolve holder once; reuse the cached Ziggurat sampler directly.
        return mean + holder().gaussian.sample() * stddev;
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
        if (maxInclusive <= minInclusive) {
            throw new IllegalArgumentException("min must be < max");
        }

        if (stddev < 0.0) {
            throw new IllegalArgumentException("stddev must be >= 0");
        }

        // Resolve holder once; avoids a second lookup inside the Gaussian call.
        RngHolder h = holder();
        double x = mean + h.gaussian.sample() * stddev;

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

        // Resolve holder once; inline the Gaussian to avoid a second lookup
        // through a delegation chain.
        return Math.exp(mu + holder().gaussian.sample() * sigma);
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
        // Resolve holder once before the loop to avoid a ThreadLocal lookup
        // on every iteration.
        UniformRandomProvider rng = holder().rng;

        for (int i = 0; i < length; i++) {
            stringBuilder.append(alphabet[rng.nextInt(alphabet.length)]);
        }

        return stringBuilder.toString();
    }

    /**
     * Bundles a {@link UniformRandomProvider} with its associated
     * {@link ZigguratSampler.NormalizedGaussian}.
     *
     * <p>The Ziggurat sampler holds a reference to the underlying RNG, so a
     * single holder object covers all primitive and Gaussian generation without
     * extra indirection. Creating the sampler is O(1) and involves only a
     * reference assignment.</p>
     */
    private static final class RngHolder {
        final UniformRandomProvider rng;
        final ZigguratSampler.NormalizedGaussian gaussian;

        RngHolder(UniformRandomProvider rng) {
            this.rng = rng;
            this.gaussian = ZigguratSampler.NormalizedGaussian.of(rng);
        }
    }
}
