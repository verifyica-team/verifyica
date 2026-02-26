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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages mutexes (locks) by string keys, allowing thread-safe synchronization
 * across different parts of an application using the same key.
 */
public class KeyedMutexManager {

    private static final Lock LOCK = new ReentrantLock(true);
    private static final Map<String, MutexReference> MUTEX_REFERENCES = new HashMap<>();

    /**
     * Private constructor to prevent instantiation.
     */
    private KeyedMutexManager() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Attempts to acquire the lock for the specified key.
     *
     * @param key the key to lock
     * @return true if the lock was free or locked by the current Thread, otherwise false
     */
    public static boolean tryLock(String key) {
        String trimmedKey = trimAndValidate(key, "key is null", "key is blank");
        MutexReference mutexReference;
        boolean isReentrant = false;

        LOCK.lock();
        try {
            mutexReference = MUTEX_REFERENCES.get(trimmedKey);
            if (mutexReference == null) {
                mutexReference = new MutexReference();
                MUTEX_REFERENCES.put(trimmedKey, mutexReference);
            } else {
                isReentrant = mutexReference.getLock().isHeldByCurrentThread();
            }

            if (!isReentrant) {
                mutexReference.addThread();
            }
        } finally {
            LOCK.unlock();
        }

        boolean acquired = mutexReference.getLock().tryLock();

        if (!acquired && !isReentrant) {
            LOCK.lock();
            try {
                mutexReference.removeThread();
                if (mutexReference.getThreadCount() == 0) {
                    MUTEX_REFERENCES.remove(trimmedKey);
                }
            } finally {
                LOCK.unlock();
            }
        }

        return acquired;
    }

    /**
     * Attempts to acquire the lock for the specified key, with timeout.
     *
     * @param key the key to lock
     * @param timeout the maximum time to wait for the lock
     * @param timeUnit the time unit of the timeout argument
     * @return true if the lock was free or locked by the current Thread, otherwise false
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public static boolean tryLock(String key, long timeout, TimeUnit timeUnit) throws InterruptedException {
        notNull(timeUnit, "timeUnit is null");

        String trimmedKey = trimAndValidate(key, "key is null", "key is blank");
        MutexReference mutexReference;
        boolean isReentrant = false;

        LOCK.lock();
        try {
            mutexReference = MUTEX_REFERENCES.get(trimmedKey);
            if (mutexReference == null) {
                mutexReference = new MutexReference();
                MUTEX_REFERENCES.put(trimmedKey, mutexReference);
            } else {
                isReentrant = mutexReference.getLock().isHeldByCurrentThread();
            }

            if (!isReentrant) {
                mutexReference.addThread();
            }
        } finally {
            LOCK.unlock();
        }

        boolean acquired = mutexReference.getLock().tryLock(timeout, timeUnit);

        if (!acquired && !isReentrant) {
            LOCK.lock();
            try {
                mutexReference.removeThread();
                if (mutexReference.getThreadCount() == 0) {
                    MUTEX_REFERENCES.remove(trimmedKey);
                }
            } finally {
                LOCK.unlock();
            }
        }

        return acquired;
    }

    /**
     * Acquires the lock for the specified key.
     *
     * @param key the key to lock
     */
    public static void lock(String key) {
        String trimmedKey = trimAndValidate(key, "key is null", "key is blank");
        MutexReference mutexReference;
        boolean isReentrant = false;

        LOCK.lock();
        try {
            mutexReference = MUTEX_REFERENCES.get(trimmedKey);
            if (mutexReference == null) {
                mutexReference = new MutexReference();
                MUTEX_REFERENCES.put(trimmedKey, mutexReference);
            } else {
                isReentrant = mutexReference.getLock().isHeldByCurrentThread();
            }

            if (!isReentrant) {
                mutexReference.addThread();
            }
        } finally {
            LOCK.unlock();
        }

        mutexReference.getLock().lock();
    }

    /**
     * Releases the lock for the specified key.
     *
     * @param key the key to unlock
     */
    public static void unlock(String key) {
        String trimmedKey = trimAndValidate(key, "key is null", "key is blank");
        MutexReference mutexReference;
        int holdCountBeforeUnlock;

        LOCK.lock();
        try {
            mutexReference = MUTEX_REFERENCES.get(trimmedKey);
            if (mutexReference == null || mutexReference.getThreadCount() == 0) {
                throw new IllegalMonitorStateException(format("Key [%s] is not locked", trimmedKey));
            }

            if (!mutexReference.getLock().isHeldByCurrentThread()) {
                throw new IllegalMonitorStateException(
                        format("Current thread does not own the Lock for key [%s]", trimmedKey));
            }

            holdCountBeforeUnlock = mutexReference.getLock().getHoldCount();
        } finally {
            LOCK.unlock();
        }

        mutexReference.getLock().unlock();

        if (holdCountBeforeUnlock == 1) {
            LOCK.lock();
            try {
                mutexReference.removeThread();
                if (mutexReference.getThreadCount() == 0) {
                    MUTEX_REFERENCES.remove(trimmedKey);
                }
            } finally {
                LOCK.unlock();
            }
        }
    }

    /**
     * Returns whether the specified key is locked.
     *
     * @param key the key to check
     * @return true if the key is locked, otherwise false
     */
    public static boolean isLocked(String key) {
        String trimmedKey = trimAndValidate(key, "key is null", "key is blank");

        LOCK.lock();
        try {
            MutexReference mutexReference = MUTEX_REFERENCES.get(trimmedKey);
            return mutexReference != null && mutexReference.getLock().isLocked();
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Asserts the number of mutex references.
     *
     * @param size the expected number of mutex references
     */
    static void assertSize(int size) {
        LOCK.lock();
        try {
            if (MUTEX_REFERENCES.size() != size) {
                throw new IllegalStateException("mutexReferences size is incorrect");
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
    private static void notNull(Object object, String message) {
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

    /**
     * Reference to a mutex with tracking of threads holding the lock.
     */
    private static class MutexReference {

        private final ReentrantLock reentrantLock;
        private final Set<Thread> threads;

        /**
         * Constructs a new MutexReference.
         */
        private MutexReference() {
            reentrantLock = new ReentrantLock(true);
            threads = new HashSet<>();
        }

        /**
         * Returns the lock.
         *
         * @return the ReentrantLock
         */
        private ReentrantLock getLock() {
            return reentrantLock;
        }

        /**
         * Adds the current thread to the tracking set.
         */
        private void addThread() {
            threads.add(Thread.currentThread());
        }

        /**
         * Removes the current thread from the tracking set.
         */
        private void removeThread() {
            threads.remove(Thread.currentThread());
        }

        /**
         * Returns the count of threads holding the lock.
         *
         * @return the thread count
         */
        private int getThreadCount() {
            return threads.size();
        }
    }
}
