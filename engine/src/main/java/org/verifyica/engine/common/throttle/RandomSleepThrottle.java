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

package org.verifyica.engine.common.throttle;

import java.util.concurrent.ThreadLocalRandom;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;

/**
 * RandomSleepThrottle provides a throttle that sleeps for a random duration.
 */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class RandomSleepThrottle implements Throttle {

    /**
     * Logger instance for this class..
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Throttle.class);

    /**
     * The name of this throttle.
     */
    private final String name;

    /**
     * The minimum sleep duration in milliseconds.
     */
    private final long minimum;

    /**
     * The maximum sleep duration in milliseconds.
     */
    private final long maximum;

    /**
     * Constructs a new RandomSleepThrottle with the specified parameters.
     *
     * @param name the name of this throttle
     * @param minimum the minimum sleep duration in milliseconds
     * @param maximum the maximum sleep duration in milliseconds
     */
    public RandomSleepThrottle(String name, long minimum, long maximum) {
        this.name = name;
        this.minimum = minimum >= 0 ? Math.min(minimum, maximum) : 0;
        this.maximum = maximum >= 0 ? Math.max(minimum, maximum) : 0;
    }

    @Override
    public void throttle() throws InterruptedException {
        if (minimum == maximum && minimum == 0) {
            return;
        }

        long throttle;

        if (minimum == maximum) {
            throttle = minimum;
        } else {
            throttle = randomLong(minimum, maximum);
        }

        LOGGER.trace("name [%s] throttle [%d] ms", name, throttle);

        Thread.sleep(throttle);
    }

    /**
     * Creates a random long that is inclusive of minimum and maximum.
     *
     * @param minimum minimum value (inclusive)
     * @param maximum maximum value (inclusive)
     * @return a random long between minimum and maximum, inclusive
     */
    private static long randomLong(long minimum, long maximum) {
        if (minimum >= maximum) {
            throw new IllegalArgumentException("maximum must be greater than minimum");
        }

        return ThreadLocalRandom.current().nextLong(minimum, maximum + 1);
    }
}
