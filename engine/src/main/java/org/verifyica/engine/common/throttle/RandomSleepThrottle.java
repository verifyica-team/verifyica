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

package org.verifyica.engine.common.throttle;

import java.util.concurrent.ThreadLocalRandom;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;

/** Class to implement RandomSleepThrottle */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class RandomSleepThrottle implements Throttle {

    private static final Logger LOGGER = LoggerFactory.getLogger(Throttle.class);

    private final String name;
    private final long minimum;
    private final long maximum;

    /**
     * Constructor
     *
     * @param name name
     * @param minimum minimum
     * @param maximum maximum
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
     * Method to create a random long that is inclusive of minimum and maximum.
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
