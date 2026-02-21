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

package org.verifyica.api.concurrent;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class to implement KeyedLatchManager
 *
 * <p>Manages CountDownLatches by string keys, allowing thread synchronization
 * where one or more threads can wait for a set of operations to complete.</p>
 */
public class KeyedLatchManager {

    private static final Lock LOCK = new ReentrantLock(true);
    private static final Map<String, CountDownLatch> LATCHES = new HashMap<>();

    /**
     * Constructor
     */
    private KeyedLatchManager() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Creates a latch with the specified count.
     *
     * @param key the key to identify the latch
     * @param count the number of times countDown must be invoked before threads can pass through await
     * @return the CountDownLatch
     * @throws IllegalArgumentException if key is null or blank, or count is negative
     * @throws IllegalStateException if a latch already exists for this key
     */
    public static CountDownLatch createLatch(final String key, final int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }

        final String trimmedKey = trimAndValidate(key, "key is null", "key is blank");

        LOCK.lock();
        try {
            if (LATCHES.containsKey(trimmedKey)) {
                throw new IllegalStateException(format("Latch for key [%s] already exists", trimmedKey));
            }

            final CountDownLatch latch = new CountDownLatch(count);
            LATCHES.put(trimmedKey, latch);
            return latch;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Gets an existing latch.
     *
     * @param key the key to identify the latch
     * @return the CountDownLatch, or null if no latch exists for this key
     * @throws IllegalArgumentException if key is null or blank
     */
    public static CountDownLatch getLatch(final String key) {
        final String trimmedKey = trimAndValidate(key, "key is null", "key is blank");

        LOCK.lock();
        try {
            return LATCHES.get(trimmedKey);
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Waits for the latch to count down to zero.
     *
     * @param key the key to identify the latch
     * @throws IllegalArgumentException if key is null or blank
     * @throws IllegalStateException if no latch exists for this key
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public static void await(final String key) throws InterruptedException {
        final String trimmedKey = trimAndValidate(key, "key is null", "key is blank");
        final CountDownLatch latch;

        LOCK.lock();
        try {
            latch = LATCHES.get(trimmedKey);
            if (latch == null) {
                throw new IllegalStateException(format("No latch exists for key [%s]", trimmedKey));
            }
        } finally {
            LOCK.unlock();
        }

        latch.await();
    }

    /**
     * Waits for the latch to count down to zero, with timeout.
     *
     * @param key the key to identify the latch
     * @param timeout the maximum time to wait
     * @param timeUnit the time unit of the timeout argument
     * @return true if the count reached zero, false if the waiting time elapsed
     * @throws IllegalArgumentException if key is null or blank, or timeUnit is null
     * @throws IllegalStateException if no latch exists for this key
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public static boolean await(final String key, final long timeout, final TimeUnit timeUnit)
            throws InterruptedException {
        notNull(timeUnit, "timeUnit is null");

        final String trimmedKey = trimAndValidate(key, "key is null", "key is blank");
        final CountDownLatch latch;

        LOCK.lock();
        try {
            latch = LATCHES.get(trimmedKey);
            if (latch == null) {
                throw new IllegalStateException(format("No latch exists for key [%s]", trimmedKey));
            }
        } finally {
            LOCK.unlock();
        }

        return latch.await(timeout, timeUnit);
    }

    /**
     * Counts down the latch.
     *
     * @param key the key to identify the latch
     * @throws IllegalArgumentException if key is null or blank
     * @throws IllegalStateException if no latch exists for this key
     */
    public static void countDown(final String key) {
        final String trimmedKey = trimAndValidate(key, "key is null", "key is blank");
        final CountDownLatch latch;

        LOCK.lock();
        try {
            latch = LATCHES.get(trimmedKey);
            if (latch == null) {
                throw new IllegalStateException(format("No latch exists for key [%s]", trimmedKey));
            }
        } finally {
            LOCK.unlock();
        }

        latch.countDown();
    }

    /**
     * Gets the current count of the latch.
     *
     * @param key the key to identify the latch
     * @return the current count
     * @throws IllegalArgumentException if key is null or blank
     * @throws IllegalStateException if no latch exists for this key
     */
    public static long getCount(final String key) {
        final String trimmedKey = trimAndValidate(key, "key is null", "key is blank");
        final CountDownLatch latch;

        LOCK.lock();
        try {
            latch = LATCHES.get(trimmedKey);
            if (latch == null) {
                throw new IllegalStateException(format("No latch exists for key [%s]", trimmedKey));
            }
        } finally {
            LOCK.unlock();
        }

        return latch.getCount();
    }

    /**
     * Removes a latch.
     *
     * @param key the key to identify the latch
     * @return the removed CountDownLatch, or null if no latch existed for this key
     * @throws IllegalArgumentException if key is null or blank
     */
    public static CountDownLatch removeLatch(final String key) {
        final String trimmedKey = trimAndValidate(key, "key is null", "key is blank");

        LOCK.lock();
        try {
            return LATCHES.remove(trimmedKey);
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Checks if a latch exists for the key.
     *
     * @param key the key to identify the latch
     * @return true if a latch exists, false otherwise
     * @throws IllegalArgumentException if key is null or blank
     */
    public static boolean hasLatch(final String key) {
        final String trimmedKey = trimAndValidate(key, "key is null", "key is blank");

        LOCK.lock();
        try {
            return LATCHES.containsKey(trimmedKey);
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Asserts the number of latches.
     *
     * @param size the expected number of latches
     */
    static void assertSize(final int size) {
        LOCK.lock();
        try {
            if (LATCHES.size() != size) {
                throw new IllegalStateException("latches size is incorrect");
            }
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Validates an Object is not null, throwing an IllegalArgumentException if it is null.
     *
     * @param object the object to validate
     * @param message the exception message
     */
    private static void notNull(final Object object, final String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates a String is not null and not blank, throwing an IllegalArgumentException
     * if it is null or blank. Returns the trimmed string.
     *
     * @param string the string to validate
     * @param nullMessage the exception message if the string is null
     * @param blankMessage the exception message if the string is blank
     * @return the trimmed string
     */
    private static String trimAndValidate(final String string, final String nullMessage, final String blankMessage) {
        if (string == null) {
            throw new IllegalArgumentException(nullMessage);
        }

        final String trimmed = string.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(blankMessage);
        }
        return trimmed;
    }
}
