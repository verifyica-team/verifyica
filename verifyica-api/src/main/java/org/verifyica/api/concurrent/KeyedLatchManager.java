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
     * Create or get a latch with the specified count
     *
     * @param key key
     * @param count the number of times countDown must be invoked before threads can pass through await
     * @return the CountDownLatch
     * @throws IllegalArgumentException if key is null or blank, or count is negative
     * @throws IllegalStateException if a latch already exists for this key
     */
    public static CountDownLatch createLatch(String key, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }

        String trimmedKey = trimAndValidate(key, "key is null", "key is blank");

        LOCK.lock();
        try {
            if (LATCHES.containsKey(trimmedKey)) {
                throw new IllegalStateException(format("Latch for key [%s] already exists", trimmedKey));
            }
            
            CountDownLatch latch = new CountDownLatch(count);
            LATCHES.put(trimmedKey, latch);
            return latch;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Get an existing latch
     *
     * @param key key
     * @return the CountDownLatch, or null if no latch exists for this key
     * @throws IllegalArgumentException if key is null or blank
     */
    public static CountDownLatch getLatch(String key) {
        String trimmedKey = trimAndValidate(key, "key is null", "key is blank");

        LOCK.lock();
        try {
            return LATCHES.get(trimmedKey);
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Wait for the latch to count down to zero
     *
     * @param key key
     * @throws IllegalArgumentException if key is null or blank
     * @throws IllegalStateException if no latch exists for this key
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public static void await(String key) throws InterruptedException {
        String trimmedKey = trimAndValidate(key, "key is null", "key is blank");
        CountDownLatch latch;

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
     * Wait for the latch to count down to zero, with timeout
     *
     * @param key key
     * @param timeout the maximum time to wait
     * @param timeUnit the time unit of the timeout argument
     * @return true if the count reached zero, false if the waiting time elapsed
     * @throws IllegalArgumentException if key is null or blank, or timeUnit is null
     * @throws IllegalStateException if no latch exists for this key
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public static boolean await(String key, long timeout, TimeUnit timeUnit) throws InterruptedException {
        notNull(timeUnit, "timeUnit is null");

        String trimmedKey = trimAndValidate(key, "key is null", "key is blank");
        CountDownLatch latch;

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
     * Count down the latch
     *
     * @param key key
     * @throws IllegalArgumentException if key is null or blank
     * @throws IllegalStateException if no latch exists for this key
     */
    public static void countDown(String key) {
        String trimmedKey = trimAndValidate(key, "key is null", "key is blank");
        CountDownLatch latch;

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
     * Get the current count of the latch
     *
     * @param key key
     * @return the current count
     * @throws IllegalArgumentException if key is null or blank
     * @throws IllegalStateException if no latch exists for this key
     */
    public static long getCount(String key) {
        String trimmedKey = trimAndValidate(key, "key is null", "key is blank");
        CountDownLatch latch;

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
     * Remove a latch
     *
     * @param key key
     * @return the removed CountDownLatch, or null if no latch existed for this key
     * @throws IllegalArgumentException if key is null or blank
     */
    public static CountDownLatch removeLatch(String key) {
        String trimmedKey = trimAndValidate(key, "key is null", "key is blank");

        LOCK.lock();
        try {
            return LATCHES.remove(trimmedKey);
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Check if a latch exists for the key
     *
     * @param key key
     * @return true if a latch exists, false otherwise
     * @throws IllegalArgumentException if key is null or blank
     */
    public static boolean hasLatch(String key) {
        String trimmedKey = trimAndValidate(key, "key is null", "key is blank");

        LOCK.lock();
        try {
            return LATCHES.containsKey(trimmedKey);
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Assert the number of latches
     *
     * @param size size
     */
    static void assertSize(int size) {
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
     * Validate an Object is not null, throwing an IllegalArgumentException if it is null
     *
     * @param object object
     * @param message message
     */
    private static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validate a String is not null and not blank, throwing an IllegalArgumentException
     * if it is null or blank. Returns the trimmed string.
     *
     * @param string string
     * @param nullMessage nullMessage
     * @param blankMessage blankMessage
     * @return the trimmed string
     */
    private static String trimAndValidate(String string, String nullMessage, String blankMessage) {
        if (string == null) {
            throw new IllegalArgumentException(nullMessage);
        }

        String trimmed = string.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(blankMessage);
        }
        return trimmed;
    }
}
