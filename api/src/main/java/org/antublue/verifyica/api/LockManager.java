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

package org.antublue.verifyica.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** Class to implement LockManager */
public class LockManager {

    private static final Lock lockManagerLock = new ReentrantLock(true);
    private static final Map<Object, LockReference> lockReferences = new HashMap<>();

    /** Constructor */
    private LockManager() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to try to lock a key
     *
     * @param key key
     * @return true if the lock was free or locked the current Thread, else false
     */
    public static boolean tryLock(Object key) {
        notNull(key, "key is null");

        LockReference lockReference;

        lockManagerLock.lock();
        try {
            lockReference = lockReferences.get(key);
            if (lockReference == null) {
                lockReference = new LockReference();
                lockReferences.put(key, lockReference);
            }
            lockReference.addThread();
        } finally {
            lockManagerLock.unlock();
        }

        return lockReference.getLock().tryLock();
    }

    /**
     * Method to try to lock a key
     *
     * @param key key
     * @param timeout timeout
     * @param timeUnit timeUnit
     * @return true if the lock was free or locked the current Thread, else false
     * @throws InterruptedException InterruptedException
     */
    public static boolean tryLock(Object key, long timeout, TimeUnit timeUnit)
            throws InterruptedException {
        notNull(key, "key is null");
        notNull(timeUnit, "timeUnit is null");

        LockReference lockReference;

        lockManagerLock.lock();
        try {
            lockReference = lockReferences.get(key);
            if (lockReference == null) {
                lockReference = new LockReference();
                lockReferences.put(key, lockReference);
            }
            lockReference.addThread();
        } finally {
            lockManagerLock.unlock();
        }

        return lockReference.getLock().tryLock(timeout, timeUnit);
    }

    /**
     * Method to lock a key
     *
     * @param key key
     */
    public static void lock(Object key) {
        notNull(key, "key is null");

        LockReference lockReference;

        lockManagerLock.lock();
        try {
            lockReference = lockReferences.get(key);
            if (lockReference == null) {
                lockReference = new LockReference();
                lockReferences.put(key, lockReference);
            }
            lockReference.addThread();
        } finally {
            lockManagerLock.unlock();
        }

        lockReference.getLock().lock();
    }

    /**
     * Method to unlock a key
     *
     * @param key key
     */
    public static void unlock(Object key) {
        notNull(key, "key is null");

        lockManagerLock.lock();
        try {
            LockReference lockReference = lockReferences.get(key);
            if (lockReference == null || lockReference.getThreadCount() == 0) {
                throw new IllegalMonitorStateException("Key is not locked");
            }

            if (!lockReference.getLock().isHeldByCurrentThread()) {
                throw new IllegalMonitorStateException("Current thread does not own the Lock");
            }

            lockReference.getLock().unlock();
            lockReference.removeThread();

            if (lockReference.getThreadCount() == 0) {
                lockReferences.remove(key);
            }
        } finally {
            lockManagerLock.unlock();
        }
    }

    /**
     * Method to return if a key is locked
     *
     * @param key key
     * @return true if the key is locked, else false
     */
    public static boolean isLocked(Object key) {
        notNull(key, "key is null");

        lockManagerLock.lock();
        try {
            return lockReferences.get(key) != null;
        } finally {
            lockManagerLock.unlock();
        }
    }

    /**
     * Method to assert the number of LockReferences
     *
     * @param size size
     */
    static void assertSize(int size) {
        lockManagerLock.lock();
        try {
            if (lockReferences.size() != size) {
                throw new IllegalStateException("lockReferences size is incorrect");
            }
        } finally {
            lockManagerLock.unlock();
        }
    }

    /**
     * Checks if an Object is null
     *
     * @param object object
     * @param message message
     */
    protected static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /** Class to implement LockReference */
    private static class LockReference {

        private final ReentrantLock reentrantLock;
        private final Set<Thread> threads;

        /** Constructor */
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

        /** Method to add the current Thread */
        private void addThread() {
            threads.add(Thread.currentThread());
        }

        /** Method to remove the current Thread */
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
