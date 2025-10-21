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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class to implement LockManager
 */
public class LockManager {

    private static final Lock LOCK = new ReentrantLock(true);
    private static final Map<String, LockReference> LOCK_REFERENCES = new HashMap<>();

    /**
     * Constructor
     */
    private LockManager() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Method to try to lock a key
     *
     * @param key key
     * @return true if the lock was free or locked by the current Thread, otherwise false
     */
    public static boolean tryLock(String key) {
        notBlank(key, "key is null", "key is blank");

        String trimmedKey = key.trim();
        LockReference lockReference;

        LOCK.lock();
        try {
            lockReference = LOCK_REFERENCES.get(trimmedKey);
            if (lockReference == null) {
                lockReference = new LockReference();
                LOCK_REFERENCES.put(trimmedKey, lockReference);
            }
            lockReference.addThread();
        } finally {
            LOCK.unlock();
        }

        return lockReference.getLock().tryLock();
    }

    /**
     * Method to try to lock a key
     *
     * @param key key
     * @param timeout timeout
     * @param timeUnit timeUnit
     * @return true if the lock was free or locked by the current Thread, otherwise false
     * @throws InterruptedException InterruptedException
     */
    public static boolean tryLock(String key, long timeout, TimeUnit timeUnit) throws InterruptedException {
        notBlank(key, "key is null", "key is blank");
        notNull(timeUnit, "timeUnit is null");

        String trimmedKey = key.trim();
        LockReference lockReference;

        LOCK.lock();
        try {
            lockReference = LOCK_REFERENCES.get(trimmedKey);
            if (lockReference == null) {
                lockReference = new LockReference();
                LOCK_REFERENCES.put(trimmedKey, lockReference);
            }
            lockReference.addThread();
        } finally {
            LOCK.unlock();
        }

        return lockReference.getLock().tryLock(timeout, timeUnit);
    }

    /**
     * Method to lock a key
     *
     * @param key key
     */
    public static void lock(String key) {
        notBlank(key, "key is null", "key is blank");

        String trimmedKey = key.trim();
        LockReference lockReference;

        LOCK.lock();
        try {
            lockReference = LOCK_REFERENCES.get(trimmedKey);
            if (lockReference == null) {
                lockReference = new LockReference();
                LOCK_REFERENCES.put(trimmedKey, lockReference);
            }
            lockReference.addThread();
        } finally {
            LOCK.unlock();
        }

        lockReference.getLock().lock();
    }

    /**
     * Method to unlock a key
     *
     * @param key key
     */
    public static void unlock(String key) {
        notBlank(key, "key is null", "key is blank");

        String trimmedKey = key.trim();

        LOCK.lock();
        try {
            LockReference lockReference = LOCK_REFERENCES.get(trimmedKey);
            if (lockReference == null || lockReference.getThreadCount() == 0) {
                throw new IllegalMonitorStateException(format("Key [%s] is not locked", trimmedKey));
            }

            if (!lockReference.getLock().isHeldByCurrentThread()) {
                throw new IllegalMonitorStateException(
                        format("Current thread does not own the Lock for key [%s]", trimmedKey));
            }

            lockReference.getLock().unlock();
            lockReference.removeThread();

            if (lockReference.getThreadCount() == 0) {
                LOCK_REFERENCES.remove(trimmedKey);
            }
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Method to return if a key is locked
     *
     * @param key key
     * @return true if the key is locked, otherwise false
     */
    public static boolean isLocked(String key) {
        notBlank(key, "key is null", "key is blank");

        String trimmedKey = key.trim();

        LOCK.lock();
        try {
            return LOCK_REFERENCES.get(trimmedKey) != null;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Method to assert the number of LockReferences
     *
     * @param size size
     */
    static void assertSize(int size) {
        LOCK.lock();
        try {
            if (LOCK_REFERENCES.size() != size) {
                throw new IllegalStateException("lockReferences size is incorrect");
            }
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Method to validate an Object is not null, throwing an IllegalArgumentException if it is null
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
     * Method to validate a String is not null and not blank, throwing an IllegalArgumentException
     * if it is null or blank
     *
     * @param string string
     * @param nullMessage nullMessage
     * @param blankMessage blankMessage
     */
    private static void notBlank(String string, String nullMessage, String blankMessage) {
        if (string == null) {
            throw new IllegalArgumentException(nullMessage);
        }

        if (string.trim().isEmpty()) {
            throw new IllegalArgumentException(blankMessage);
        }
    }

    /**
     * Class to implement LockReference
     */
    private static class LockReference {

        private final ReentrantLock reentrantLock;
        private final Set<Thread> threads;

        /**
         * Constructor
         */
        private LockReference() {
            reentrantLock = new ReentrantLock(true);
            threads = new HashSet<>();
        }

        /**
         * Method to get the Lock
         *
         * @return the Lock
         */
        private ReentrantLock getLock() {
            return reentrantLock;
        }

        /**
         * Method to add the current Thread
         */
        private void addThread() {
            threads.add(Thread.currentThread());
        }

        /**
         * Method to remove the current Thread
         */
        private void removeThread() {
            threads.remove(Thread.currentThread());
        }

        /**
         * Method to get the Thread count
         *
         * @return the Thread count
         */
        private int getThreadCount() {
            return threads.size();
        }
    }
}
