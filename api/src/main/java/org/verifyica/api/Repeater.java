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

import java.util.concurrent.ThreadLocalRandom;

/** Class to implement Repeater */
@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
public class Repeater {

    private final int iterations;
    private Throttle throttle;
    private ThrowableRunnable before;
    private ThrowableRunnable test;
    private ThrowableRunnable after;
    private ThrowableConsumer throwableConsumer;

    /**
     * Constructor
     *
     * @param iterations the number of iterations to run the test
     */
    public Repeater(int iterations) {
        if (iterations < 1) {
            throw new IllegalArgumentException(format("iterations [%d] is less than 1", iterations));
        }

        this.iterations = iterations;
    }

    /**
     * Method to set a fixed throttle time between tests
     *
     * @param milliseconds fixed throttle time in milliseconds
     * @return the Repeater
     */
    public Repeater throttle(long milliseconds) {
        if (milliseconds >= 0) {
            throttle = new FixedThrottle(milliseconds);
        }

        return this;
    }

    /**
     * Method to set a Throwable to throttle time between tests
     *
     * @param throttle the throttle
     * @return the Repeater
     */
    public Repeater throttle(Throttle throttle) {
        if (throttle == null) {
            throw new IllegalArgumentException("throttle is null");
        }

        return this;
    }

    /**
     * Method to run code before a test
     *
     * @param throwableRunnable throwableRunnable
     * @return the Repeater
     */
    public Repeater before(ThrowableRunnable throwableRunnable) {
        if (throwableRunnable == null) {
            throw new IllegalArgumentException("throwableRunnable is null");
        }

        before = throwableRunnable;

        return this;
    }

    /**
     * Method to run the test. If code ran before the test fails, the test will be skipped.
     *
     * @param throwableRunnable throwableRunnable
     * @return the Repeater
     */
    public Repeater test(ThrowableRunnable throwableRunnable) {
        if (throwableRunnable == null) {
            throw new IllegalArgumentException("throwableRunnable is null");
        }

        test = throwableRunnable;

        return this;
    }

    /**
     * Method to run code after the test. Always runs.
     *
     * @param throwableRunnable throwableRunnable
     * @return the Repeater
     */
    public Repeater after(ThrowableRunnable throwableRunnable) {
        if (throwableRunnable == null) {
            throw new IllegalArgumentException("throwableRunnable is null");
        }

        after = throwableRunnable;

        return this;
    }

    /**
     * Method to set a consumer
     *
     * @param throwableConsumer throwableConsumer
     * @return the Repeater
     */
    public Repeater accept(ThrowableConsumer throwableConsumer) {
        if (throwableConsumer == null) {
            throw new IllegalArgumentException("throwableConsumer is null");
        }

        this.throwableConsumer = throwableConsumer;

        return this;
    }

    /**
     * Method to execute the repeater
     *
     * @throws Throwable Throwable
     */
    public void execute() throws Throwable {
        if (test == null) {
            throw new IllegalArgumentException("test is null");
        }

        Throwable throwable = null;

        // Run the test
        for (int i = 1; i <= iterations; i++) {
            try {
                // If we have a before, run it
                if (before != null) {
                    before.run();
                }

                // Run the test
                test.run();
            } catch (Throwable t) {
                throwable = t;
            }

            // If we have an after, run it
            if (after != null) {
                try {
                    after.run();
                } catch (Throwable t) {
                    throwable = throwable != null ? throwable : t;
                }
            }

            // If we have a Throwable consumer, call it
            if (throwableConsumer != null) {
                try {
                    throwableConsumer.accept(i, throwable);
                } catch (RuntimeException e) {
                    if ("4dbf35e7-415c-4e66-a61e-f6a7057e382e".equals(e.getMessage())) {
                        // break
                        return;
                    } else {
                        throw e;
                    }
                }
            } else if (throwable != null) {
                throw throwable;
            }

            // If we have a throttle, throttle the test
            if (throttle != null && i < iterations) {
                throttle.throttle();
            }
        }

        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Method to abort a test
     */
    public static void abort() {
        throw new RuntimeException("4dbf35e7-415c-4e66-a61e-f6a7057e382e");
    }

    /**
     * Method to rethrow a Throwable is not null
     *
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    public static void rethrow(Throwable throwable) throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Interface to define code
     */
    public interface ThrowableRunnable {

        /**
         * Method to run the code
         *
         * @throws Throwable Throwable
         */
        void run() throws Throwable;
    }

    /**
     * Interface to consume the result
     */
    public interface ThrowableConsumer {

        /**
         * Method to accept and process a Throwable
         *
         * @param counter the counter
         * @param throwable the throwable
         * @throws Throwable Throwable
         */
        void accept(int counter, Throwable throwable) throws Throwable;
    }

    /**
     * Interface to implement a Throttle
     */
    public interface Throttle {

        /**
         * Method to throttle the test
         */
        void throttle();
    }

    /**
     * Class to implement a fixed throttle
     */
    public static class FixedThrottle implements Throttle {

        private final long milliseconds;

        /**
         * Constructor
         *
         * @param milliseconds milliseconds
         */
        public FixedThrottle(long milliseconds) {
            this.milliseconds = milliseconds;
        }

        @Override
        public void throttle() {
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Class to implement a random throttle
     */
    public static class RandomThrottle implements Throttle {

        private final long minMilliseconds;
        private final long maxMilliseconds;

        /**
         * Constructor
         *
         * @param minMilliseconds the minimum number of milliseconds to throttle
         * @param maxMilliseconds the maximum number of milliseconds to throttle
         */
        public RandomThrottle(long minMilliseconds, long maxMilliseconds) {
            if (minMilliseconds < 0) {
                throw new IllegalArgumentException("minMilliseconds must be >= 0");
            }

            if (maxMilliseconds < minMilliseconds) {
                throw new IllegalArgumentException("maxMilliseconds must be >= minMilliseconds");
            }

            this.minMilliseconds = minMilliseconds;
            this.maxMilliseconds = maxMilliseconds;
        }

        @Override
        public void throttle() {
            long sleep = (minMilliseconds == maxMilliseconds)
                    ? minMilliseconds
                    : ThreadLocalRandom.current().nextLong(minMilliseconds, maxMilliseconds + 1);
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Class to implement an exponential backoff throttle
     */
    public static class ExponentialBackoffThrottle implements Throttle {

        private final long maxMilliseconds;
        private long currentBackoff;

        /**
         * Constructor
         *
         * @param maxMilliseconds the maximum number of milliseconds to back off
         */
        public ExponentialBackoffThrottle(long maxMilliseconds) {
            this.maxMilliseconds = maxMilliseconds;
            this.currentBackoff = 1;
        }

        @Override
        public void throttle() {
            try {
                Thread.sleep(currentBackoff);
                currentBackoff = Math.min(currentBackoff * 2, maxMilliseconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
