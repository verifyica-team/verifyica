/*
 * Copyright (C) Verifyica project authors and contributors
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

import static java.lang.String.format;

import java.util.Random;

/**
 * Utility class for generating random numbers within a specified range.
 */
public class RandomSupport {

    private static final Random RANDOM = new Random();

    /**
     * Constructor
     */
    private RandomSupport() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Generates a random long value between the specified minimum and maximum values, inclusive.
     *
     * @param minimum the minimum value (inclusive)
     * @param maximum the maximum value (inclusive)
     * @return a random long value within the specified range
     * @throws IllegalArgumentException if minimum is greater than maximum
     */
    public static long randomLong(long minimum, long maximum) {
        if (minimum == maximum) {
            return minimum;
        }

        if (minimum > maximum) {
            throw new IllegalArgumentException(format("Minimum [%d] is greater than maximum [%d]", minimum, maximum));
        }

        return (long) (RANDOM.nextDouble() * (maximum - minimum + 1)) + minimum;
    }
}
