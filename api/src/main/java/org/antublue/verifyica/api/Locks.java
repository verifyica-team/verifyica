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

import static java.lang.String.format;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** Class to implement Locks */
public class Locks {

    private static final LockManager LOCK_MANAGER = new LockManager();

    /** Constructor */
    private Locks() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to get a LockReference
     *
     * @param key key
     * @return a LockReference
     */
    public static LockReference getLock(Object key) {
        notNull(key, "key is null");

        return new DefaultLockReference(LOCK_MANAGER, key.toString());
    }

    /**
     * Execute a Runnable in a lock
     *
     * @param key key
     * @param runnable runnable
     * @return the elapsed time in millisecond
     */
    public static long execute(Object key, Runnable runnable) {
        notNull(key, "key is null");
        toStringNotEmpty(key, "key toString() is empty");
        notNull(runnable, "runnable is null");

        long t0 = System.currentTimeMillis();

        try {
            getLock(key.toString()).lock();
            runnable.run();
        } finally {
            getLock(key.toString()).unlock();
        }

        return System.currentTimeMillis() - t0;
    }

    /**
     * Execute an Executable in a lock
     *
     * @param key key
     * @param throwableRunnable executable
     * @return the Duration
     * @throws Throwable Throwable
     */
    public static Duration execute(Object key, ThrowableRunnable throwableRunnable)
            throws Throwable {
        notNull(key, "key is null");
        toStringNotEmpty(key, "key toString() is empty");
        notNull(throwableRunnable, "executable is null");

        long t0 = System.nanoTime();
        LockReference lockReference = getLock(key.toString());

        try {
            lockReference.lock();
            throwableRunnable.run();
        } finally {
            lockReference.unlock();
        }

        return Duration.of(System.nanoTime() - t0, ChronoUnit.NANOS);
    }

    /**
     * Checks if an Object is null
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
     * Check if an Object toString is not empty
     *
     * @param object object
     * @param message message
     */
    private static void toStringNotEmpty(Object object, String message) {
        if (object.toString().trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /** Interface to implement LockReference */
    public interface LockReference {

        /** Method to lock the Lock */
        void lock();

        /**
         * Method to try to lock the Lock
         *
         * @throws InterruptedException InterruptedException
         */
        void tryLock() throws InterruptedException;

        /**
         * Method to try to lock the Lock
         *
         * @param time time
         * @param timeUnit timeUnit
         * @throws InterruptedException InterruptedException
         */
        void tryLock(long time, TimeUnit timeUnit) throws InterruptedException;

        /** Method to unlock the Lock */
        void unlock();
    }

    /** Interface to implement ThrowableRunnable */
    public interface ThrowableRunnable {

        /**
         * Method to implement run
         *
         * @throws Throwable Throwable
         */
        void run() throws Throwable;
    }

    /** Class to implement DefaultLockReference */
    private static class DefaultLockReference implements LockReference {

        private final LockManager lockManager;
        private final Object key;

        /**
         * Constructor
         *
         * @param key key
         */
        private DefaultLockReference(LockManager lockManager, Object key) {
            this.lockManager = lockManager;
            this.key = key;
        }

        @Override
        public void lock() {
            lockManager.lock(key);
        }

        @Override
        public void tryLock() throws InterruptedException {
            tryLock(Long.MAX_VALUE, TimeUnit.DAYS);
        }

        @Override
        public void tryLock(long time, TimeUnit timeUnit) throws InterruptedException {
            notNull(timeUnit, "timeUnit it null");
            lockManager.tryLock(key, time, timeUnit);
        }

        @Override
        public void unlock() {
            lockManager.unlock(key);
        }

        @Override
        public String toString() {
            return key.toString();
        }
    }

    /** Class to implement LockManager */
    private static class LockManager {

        private final Lock lock = new ReentrantLock(true);
        private final Map<Object, LockHolder> map = new HashMap<>();

        /**
         * Acquires a lock
         *
         * @param key key
         */
        void lock(Object key) {
            LockHolder lockHolder;

            try {
                lock.lock();

                lockHolder =
                        map.compute(
                                key,
                                (k, lh) -> {
                                    if (lh == null) {
                                        lh = new LockHolder();
                                    }
                                    return lh;
                                });

                lockHolder.increaseLockCount();
            } finally {
                lock.unlock();
            }

            lockHolder.getReentrantLock().lock();
        }

        void tryLock(Object key, long time, TimeUnit timeUnit) throws InterruptedException {
            LockHolder lockHolder;
            try {
                lock.lock();

                lockHolder =
                        map.compute(
                                key,
                                (k, lh) -> {
                                    if (lh == null) {
                                        lh = new LockHolder();
                                    }
                                    return lh;
                                });

                lockHolder.increaseLockCount();
            } finally {
                lock.unlock();
            }

            lockHolder.getReentrantLock().tryLock(time, timeUnit);
        }

        /**
         * Releases a lock
         *
         * @param key key
         */
        void unlock(Object key) {
            try {
                lock.lock();

                LockHolder lockHolder = map.get(key);
                if (lockHolder == null) {
                    throw new IllegalMonitorStateException(
                            format("LockReference [%s] not locked", key));
                }

                if (lockHolder.getLockCount() == 0) {
                    throw new IllegalMonitorStateException(
                            format("LockReference [%s] already unlocked", key));
                }

                lockHolder.getReentrantLock().unlock();
                lockHolder.decreaseLockCount();

                if (lockHolder.getLockCount() == 0) {
                    map.remove(key);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /** Class to implement LockHolder */
    private static class LockHolder {

        private final ReentrantLock reentrantLock;
        private int lockCount;

        /** Constructor */
        LockHolder() {
            reentrantLock = new ReentrantLock(true);
        }

        /**
         * Get the lock
         *
         * @return the Lock
         */
        Lock getReentrantLock() {
            return reentrantLock;
        }

        /** Increase the lock count */
        void increaseLockCount() {
            lockCount++;
        }

        /** Decrease the lock count */
        void decreaseLockCount() {
            lockCount--;
        }

        /**
         * Get the lock count
         *
         * @return the lock count
         */
        int getLockCount() {
            return lockCount;
        }
    }
}
