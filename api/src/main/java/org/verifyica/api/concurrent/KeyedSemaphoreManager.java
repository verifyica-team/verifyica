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
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * KeyedSemaphoreManager manages semaphores by string keys, allowing control over the number of threads
 * that can access a particular resource or pool of resources concurrently.
 */
public class KeyedSemaphoreManager {

    /** The lock used for thread-safe access to the semaphores map. */
    private static final Lock LOCK = new ReentrantLock(true);

    /** The map of semaphores keyed by string identifiers. */
    private static final Map<String, Semaphore> SEMAPHORES = new HashMap<>();

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private KeyedSemaphoreManager() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Create or get a semaphore with the specified number of permits
     *
     * @param key key
     * @param permits the initial number of permits available
     * @return the Semaphore
     * @throws IllegalArgumentException if key is null or blank, or permits is negative
     * @throws IllegalStateException if a semaphore already exists for this key
     */
    public static Semaphore createSemaphore(String key, int permits) {
        notBlank(key, "key is null", "key is blank");
        if (permits < 0) {
            throw new IllegalArgumentException("permits cannot be negative");
        }

        String trimmedKey = key.trim();

        LOCK.lock();
        try {
            if (SEMAPHORES.containsKey(trimmedKey)) {
                throw new IllegalStateException(format("Semaphore for key [%s] already exists", trimmedKey));
            }

            Semaphore semaphore = new Semaphore(permits, true);
            SEMAPHORES.put(trimmedKey, semaphore);
            return semaphore;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Create or get a semaphore with the specified number of permits and fairness policy
     *
     * @param key key
     * @param permits the initial number of permits available
     * @param fair if true, the semaphore uses a fair ordering policy
     * @return the Semaphore
     * @throws IllegalArgumentException if key is null or blank, or permits is negative
     * @throws IllegalStateException if a semaphore already exists for this key
     */
    public static Semaphore createSemaphore(String key, int permits, boolean fair) {
        notBlank(key, "key is null", "key is blank");
        if (permits < 0) {
            throw new IllegalArgumentException("permits cannot be negative");
        }

        String trimmedKey = key.trim();

        LOCK.lock();
        try {
            if (SEMAPHORES.containsKey(trimmedKey)) {
                throw new IllegalStateException(format("Semaphore for key [%s] already exists", trimmedKey));
            }

            Semaphore semaphore = new Semaphore(permits, fair);
            SEMAPHORES.put(trimmedKey, semaphore);
            return semaphore;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Get an existing semaphore
     *
     * @param key key
     * @return the Semaphore, or null if no semaphore exists for this key
     * @throws IllegalArgumentException if key is null or blank
     */
    public static Semaphore getSemaphore(String key) {
        notBlank(key, "key is null", "key is blank");

        String trimmedKey = key.trim();

        LOCK.lock();
        try {
            return SEMAPHORES.get(trimmedKey);
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Acquire a permit from the semaphore
     *
     * @param key key
     * @throws IllegalArgumentException if key is null or blank
     * @throws IllegalStateException if no semaphore exists for this key
     * @throws InterruptedException if the current thread is interrupted
     */
    public static void acquire(String key) throws InterruptedException {
        notBlank(key, "key is null", "key is blank");

        String trimmedKey = key.trim();
        Semaphore semaphore;

        LOCK.lock();
        try {
            semaphore = SEMAPHORES.get(trimmedKey);
            if (semaphore == null) {
                throw new IllegalStateException(format("No semaphore exists for key [%s]", trimmedKey));
            }
        } finally {
            LOCK.unlock();
        }

        semaphore.acquire();
    }

    /**
     * Acquire the given number of permits from the semaphore
     *
     * @param key key
     * @param permits the number of permits to acquire
     * @throws IllegalArgumentException if key is null or blank, or permits is negative
     * @throws IllegalStateException if no semaphore exists for this key
     * @throws InterruptedException if the current thread is interrupted
     */
    public static void acquire(String key, int permits) throws InterruptedException {
        notBlank(key, "key is null", "key is blank");
        if (permits < 0) {
            throw new IllegalArgumentException("permits cannot be negative");
        }

        String trimmedKey = key.trim();
        Semaphore semaphore;

        LOCK.lock();
        try {
            semaphore = SEMAPHORES.get(trimmedKey);
            if (semaphore == null) {
                throw new IllegalStateException(format("No semaphore exists for key [%s]", trimmedKey));
            }
        } finally {
            LOCK.unlock();
        }

        semaphore.acquire(permits);
    }

    /**
     * Acquire a permit if one is immediately available
     *
     * @param key key
     * @return true if a permit was acquired, false otherwise
     * @throws IllegalArgumentException if key is null or blank
     * @throws IllegalStateException if no semaphore exists for this key
     */
    public static boolean tryAcquire(String key) {
        notBlank(key, "key is null", "key is blank");

        String trimmedKey = key.trim();
        Semaphore semaphore;

        LOCK.lock();
        try {
            semaphore = SEMAPHORES.get(trimmedKey);
            if (semaphore == null) {
                throw new IllegalStateException(format("No semaphore exists for key [%s]", trimmedKey));
            }
        } finally {
            LOCK.unlock();
        }

        return semaphore.tryAcquire();
    }

    /**
     * Acquire a permit, waiting up to the specified timeout
     *
     * @param key key
     * @param timeout the maximum time to wait for a permit
     * @param timeUnit the time unit of the timeout argument
     * @return true if a permit was acquired, false if the timeout elapsed
     * @throws IllegalArgumentException if key is null or blank, or timeUnit is null
     * @throws IllegalStateException if no semaphore exists for this key
     * @throws InterruptedException if the current thread is interrupted
     */
    public static boolean tryAcquire(String key, long timeout, TimeUnit timeUnit) throws InterruptedException {
        notBlank(key, "key is null", "key is blank");
        notNull(timeUnit, "timeUnit is null");

        String trimmedKey = key.trim();
        Semaphore semaphore;

        LOCK.lock();
        try {
            semaphore = SEMAPHORES.get(trimmedKey);
            if (semaphore == null) {
                throw new IllegalStateException(format("No semaphore exists for key [%s]", trimmedKey));
            }
        } finally {
            LOCK.unlock();
        }

        return semaphore.tryAcquire(timeout, timeUnit);
    }

    /**
     * Release a permit
     *
     * @param key key
     * @throws IllegalArgumentException if key is null or blank
     * @throws IllegalStateException if no semaphore exists for this key
     */
    public static void release(String key) {
        notBlank(key, "key is null", "key is blank");

        String trimmedKey = key.trim();
        Semaphore semaphore;

        LOCK.lock();
        try {
            semaphore = SEMAPHORES.get(trimmedKey);
            if (semaphore == null) {
                throw new IllegalStateException(format("No semaphore exists for key [%s]", trimmedKey));
            }
        } finally {
            LOCK.unlock();
        }

        semaphore.release();
    }

    /**
     * Release the given number of permits
     *
     * @param key key
     * @param permits the number of permits to release
     * @throws IllegalArgumentException if key is null or blank, or permits is negative
     * @throws IllegalStateException if no semaphore exists for this key
     */
    public static void release(String key, int permits) {
        notBlank(key, "key is null", "key is blank");
        if (permits < 0) {
            throw new IllegalArgumentException("permits cannot be negative");
        }

        String trimmedKey = key.trim();
        Semaphore semaphore;

        LOCK.lock();
        try {
            semaphore = SEMAPHORES.get(trimmedKey);
            if (semaphore == null) {
                throw new IllegalStateException(format("No semaphore exists for key [%s]", trimmedKey));
            }
        } finally {
            LOCK.unlock();
        }

        semaphore.release(permits);
    }

    /**
     * Get the current number of available permits
     *
     * @param key key
     * @return the number of available permits
     * @throws IllegalArgumentException if key is null or blank
     * @throws IllegalStateException if no semaphore exists for this key
     */
    public static int availablePermits(String key) {
        notBlank(key, "key is null", "key is blank");

        String trimmedKey = key.trim();
        Semaphore semaphore;

        LOCK.lock();
        try {
            semaphore = SEMAPHORES.get(trimmedKey);
            if (semaphore == null) {
                throw new IllegalStateException(format("No semaphore exists for key [%s]", trimmedKey));
            }
        } finally {
            LOCK.unlock();
        }

        return semaphore.availablePermits();
    }

    /**
     * Drain all available permits
     *
     * @param key key
     * @return the number of permits drained
     * @throws IllegalArgumentException if key is null or blank
     * @throws IllegalStateException if no semaphore exists for this key
     */
    public static int drainPermits(String key) {
        notBlank(key, "key is null", "key is blank");

        String trimmedKey = key.trim();
        Semaphore semaphore;

        LOCK.lock();
        try {
            semaphore = SEMAPHORES.get(trimmedKey);
            if (semaphore == null) {
                throw new IllegalStateException(format("No semaphore exists for key [%s]", trimmedKey));
            }
        } finally {
            LOCK.unlock();
        }

        return semaphore.drainPermits();
    }

    /**
     * Remove a semaphore
     *
     * @param key key
     * @return the removed Semaphore, or null if no semaphore existed for this key
     * @throws IllegalArgumentException if key is null or blank
     */
    public static Semaphore removeSemaphore(String key) {
        notBlank(key, "key is null", "key is blank");

        String trimmedKey = key.trim();

        LOCK.lock();
        try {
            return SEMAPHORES.remove(trimmedKey);
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Check if a semaphore exists for the key
     *
     * @param key key
     * @return true if a semaphore exists, false otherwise
     * @throws IllegalArgumentException if key is null or blank
     */
    public static boolean hasSemaphore(String key) {
        notBlank(key, "key is null", "key is blank");

        String trimmedKey = key.trim();

        LOCK.lock();
        try {
            return SEMAPHORES.containsKey(trimmedKey);
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Asserts that the number of semaphores matches the expected size.
     *
     * @param size the expected number of semaphores.
     */
    static void assertSize(int size) {
        LOCK.lock();
        try {
            if (SEMAPHORES.size() != size) {
                throw new IllegalStateException("semaphores size is incorrect");
            }
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Validates that an object is not null, throwing an IllegalArgumentException if it is null.
     *
     * @param object the object to validate.
     * @param message the exception message.
     */
    private static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates that a string is not null and not blank, throwing an IllegalArgumentException
     * if it is null or blank.
     *
     * @param string the string to validate.
     * @param nullMessage the exception message if the string is null.
     * @param blankMessage the exception message if the string is blank.
     */
    private static void notBlank(String string, String nullMessage, String blankMessage) {
        if (string == null) {
            throw new IllegalArgumentException(nullMessage);
        }

        if (string.trim().isEmpty()) {
            throw new IllegalArgumentException(blankMessage);
        }
    }
}
