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

package org.antublue.verifyica.test.support;

/** Class to implement Repeater */
@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
public class Repeater {

    private final int count;
    private Throttle throttle;
    private ThrowableRunnable beforeEach;
    private ThrowableRunnable test;
    private ThrowableRunnable afterEach;
    private ThrowableConsumer throwableConsumer;

    /**
     * Constructor
     *
     * @param count count
     */
    public Repeater(int count) {
        if (count < 1) {
            throw new IllegalStateException("count must be greater than 0");
        }
        this.count = count;
    }

    /**
     * Method to set a throttle time between tests
     *
     * @param milliseconds milliseconds
     * @return this
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
     * @param throttle throttle
     * @return this
     */
    public Repeater throttle(Throttle throttle) {
        if (throttle != null) {
            this.throttle = throttle;
        }
        return this;
    }

    /**
     * Method to run code before a test
     *
     * @param throwableRunnable throwableRunnable
     * @return this
     */
    public Repeater before(ThrowableRunnable throwableRunnable) {
        beforeEach = throwableRunnable;
        return this;
    }

    /**
     * Method to run the test. If code ran before the test fails, the test will be skipped.
     *
     * @param throwableRunnable throwableRunnable
     * @return this
     */
    public Repeater execute(ThrowableRunnable throwableRunnable) {
        test = throwableRunnable;
        return this;
    }

    /**
     * Method to run code after the test. Always runs.
     *
     * @param throwableRunnable throwableRunnable
     * @return this
     */
    public Repeater after(ThrowableRunnable throwableRunnable) {
        afterEach = throwableRunnable;
        return this;
    }

    /**
     * Method to set a consumer
     *
     * @param throwableConsumer throwableConsumer
     * @throws Throwable Throwable
     * @return this
     */
    public Repeater accept(ThrowableConsumer throwableConsumer) throws Throwable {
        this.throwableConsumer = throwableConsumer;
        return this;
    }

    public void execute() throws Throwable {
        Throwable throwable = null;

        for (int i = 1; i <= count; i++) {
            try {
                beforeEach.run();
                test.run();
            } catch (Throwable t) {
                throwable = t;
            } finally {
                try {
                    afterEach.run();
                } catch (Throwable t) {
                    throwable = throwable != null ? throwable : t;
                }
            }

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

            if (i < count && throttle != null) {
                throttle.throttle();
            }
        }

        if (throwable != null) {
            throw throwable;
        }
    }

    /** Method to abort a test */
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

    /** Interface to define code */
    public interface ThrowableRunnable {

        void run() throws Throwable;
    }

    /** Interface to consume the result */
    public interface ThrowableConsumer {

        void accept(int counter, Throwable throwable) throws Throwable;
    }

    /** Interface to implement a Throttle */
    public interface Throttle {

        void throttle();
    }

    /** Class to implement a fix throttle */
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

    /** Class to implement an exponential backoff throttle */
    public static class ExponentialBackoffThrottle implements Throttle {

        private final long maxMilliseconds;
        private long currentBackoff;

        /**
         * Constructor
         *
         * @param maxMilliseconds maxMilliseconds
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
