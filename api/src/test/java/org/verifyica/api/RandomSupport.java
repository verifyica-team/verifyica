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

import java.util.concurrent.ThreadLocalRandom;

public class RandomSupport {

    private RandomSupport() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Method to create a random string
     *
     * @param length length
     * @return a random String
     */
    public static String randomString(int length) {
        return ThreadLocalRandom.current()
                .ints(97, 123 + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    /**
     * Method to create a random long that is inclusive of minimum and maximum.
     *
     * @param minimum minimum value (inclusive)
     * @param maximum maximum value (inclusive)
     * @return a random long between minimum and maximum, inclusive
     */
    public static long randomLong(long minimum, long maximum) {
        if (minimum >= maximum) {
            throw new IllegalArgumentException("maximum must be greater than minimum");
        }

        return ThreadLocalRandom.current().nextLong(minimum, maximum + 1);
    }
}
