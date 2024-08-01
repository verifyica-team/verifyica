/*
 * Copyright (C) 2024 The Verifyica project authors
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

package org.antublue.verifyica.engine.support;

import static java.lang.String.format;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/** Class to implement RandomSupport */
public class RandomSupport {

    /** Constructor */
    private RandomSupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to get a random boolean
     *
     * @return a random boolean
     */
    public static boolean randomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    /**
     * Method to get a random integer in a range (minimum and maximum are both inclusive)
     *
     * @param minimum minimum
     * @param maximum maximum
     * @return a random integer
     */
    public static int randomInt(int minimum, int maximum) {
        if (minimum == maximum) {
            return minimum;
        }

        int realMinimum = Math.min(minimum, maximum);
        int realMaximum = Math.max(minimum, maximum);
        int random = ThreadLocalRandom.current().nextInt(realMinimum, realMaximum);

        if (ThreadLocalRandom.current().nextBoolean()) {
            random += 1;
        }

        return random;
    }

    /**
     * Method to get a random long in a range (minimum and maximum are both inclusive)
     *
     * @param minimum minimum
     * @param maximum maximum
     * @return a random long
     */
    public static long randomLong(long minimum, long maximum) {
        if (minimum == maximum) {
            return minimum;
        }

        long realMinimum = Math.min(minimum, maximum);
        long realMaximum = Math.max(minimum, maximum);
        long random = ThreadLocalRandom.current().nextLong(realMinimum, realMaximum);

        if (ThreadLocalRandom.current().nextBoolean()) {
            random += 1;
        }

        return random;
    }

    /**
     * Method to get a random float in a range (minimum and maximum are both inclusive)
     *
     * @param minimum minimum
     * @param maximum maximum
     * @return a random float
     */
    public static float randomFloat(float minimum, float maximum) {
        if (minimum == maximum) {
            return minimum;
        }

        float realMinimum = Math.min(minimum, maximum);
        float realMaximum = Math.max(minimum, maximum);

        return randomBigDecimal(BigDecimal.valueOf(realMinimum), BigDecimal.valueOf(realMaximum))
                .floatValue();
    }

    /**
     * Method to get a random double in a range (minimum and maximum are both inclusive)
     *
     * @param minimum minimum
     * @param maximum maximum
     * @return a random double
     */
    public static double randomDouble(double minimum, double maximum) {
        if (minimum == maximum) {
            return minimum;
        }

        double realMinimum = Math.min(minimum, maximum);
        double realMaximum = Math.max(minimum, maximum);

        BigDecimal minimumBigDecimal;
        BigDecimal maximumBigDecimal;

        try {
            minimumBigDecimal = BigDecimal.valueOf(realMinimum);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(format("Invalid minimum [%f]", minimum));
        }

        try {
            maximumBigDecimal = BigDecimal.valueOf(realMaximum);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(format("Invalid maximum [%f]", maximum));
        }

        return randomBigDecimal(minimumBigDecimal, maximumBigDecimal).doubleValue();
    }

    /**
     * Method to get a random BigInteger in a range (minimum and maximum are both inclusive)
     *
     * @param minimum minimum
     * @param maximum maximum
     * @return a random BigInteger
     */
    public static BigInteger randomBigInteger(String minimum, String maximum) {
        ArgumentSupport.notNullOrEmpty(minimum, "minimum is null", "minimum is empty");
        ArgumentSupport.notNullOrEmpty(maximum, "maximum is null", "maximum is empty");

        if (minimum.trim().contains(".")) {
            throw new IllegalArgumentException(format("Invalid minimum [%s] ", minimum));
        }

        if (maximum.trim().contains(".")) {
            throw new IllegalArgumentException(format("Invalid maximum [%s]", maximum));
        }

        BigInteger minimumBigInteger;
        BigInteger maximumBigInteger;

        try {
            minimumBigInteger = new BigInteger(minimum.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(format("Invalid minimum [%s]", minimum));
        }

        try {
            maximumBigInteger = new BigInteger(maximum.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(format("Invalid maximum [%s]", maximum));
        }

        if (minimumBigInteger.equals(maximumBigInteger)) {
            return new BigInteger(minimum);
        }

        if (maximumBigInteger.subtract(minimumBigInteger).abs().equals(BigInteger.ONE)) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                return new BigInteger(minimum);
            } else {
                return new BigInteger(maximum);
            }
        }

        if (minimumBigInteger.compareTo(maximumBigInteger) > 0) {
            BigInteger temp = maximumBigInteger;
            maximumBigInteger = minimumBigInteger;
            minimumBigInteger = temp;
        }

        Random random = ThreadLocalRandom.current();

        BigInteger range =
                maximumBigInteger
                        .subtract(minimumBigInteger)
                        .add(BigInteger.ONE); // Add 1 because upper bound is inclusive
        BigInteger generated = new BigInteger(range.bitLength(), random);
        while (generated.compareTo(range) >= 0) {
            generated = new BigInteger(range.bitLength(), random);
        }

        return generated.add(minimumBigInteger);
    }

    /**
     * Method to get a BigDecimal in a range (minimum and maximum are both inclusive)
     *
     * @param minimum minimum
     * @param maximum maximum
     * @return a random BigDecimal
     */
    public static BigDecimal randomBigDecimal(String minimum, String maximum) {
        ArgumentSupport.notNullOrEmpty(minimum, "minimum is null", "minimum is empty");
        ArgumentSupport.notNullOrEmpty(maximum, "maximum is null", "maximum is empty");

        BigDecimal minimumBigDecimal;
        BigDecimal maximummBigDecimal;

        try {
            minimumBigDecimal = new BigDecimal(minimum.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(format("Invalid minimum [%s]", minimum));
        }

        try {
            maximummBigDecimal = new BigDecimal(maximum.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(format("Invalid maximum [%s]", maximum));
        }

        return randomBigDecimal(minimumBigDecimal, maximummBigDecimal);
    }

    /**
     * Method to get a random BigDecimal in a range (minimum and maximum are both inclusive)
     *
     * @param minimum minimum
     * @param maximum maximum
     * @return a random BigDecimal
     */
    private static BigDecimal randomBigDecimal(BigDecimal minimum, BigDecimal maximum) {
        ArgumentSupport.notNull(minimum, "minimum is null");
        ArgumentSupport.notNull(maximum, "maximum is null");

        if (minimum.equals(maximum)) {
            return minimum;
        }

        BigDecimal realMinimum = minimum;
        BigDecimal realMaximum = maximum;

        if (realMinimum.compareTo(realMaximum) < 0) {
            BigDecimal temp = realMinimum;
            realMinimum = realMaximum;
            realMaximum = temp;
        }

        int digitCount = Math.max(realMinimum.precision(), realMaximum.precision()) + 10;
        int bitCount = (int) (digitCount / Math.log10(2.0));

        BigDecimal alpha =
                new BigDecimal(new BigInteger(bitCount, ThreadLocalRandom.current()))
                        .movePointLeft(digitCount);

        return minimum.add(maximum.subtract(minimum).multiply(alpha, new MathContext(digitCount)));
    }
}
