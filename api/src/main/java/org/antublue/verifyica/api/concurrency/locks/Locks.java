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

package org.antublue.verifyica.api.concurrency.locks;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
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
     */
    public static void execute(Object key, Runnable runnable) {
        notNull(key, "key is null");
        notNull(runnable, "runnable is null");

        boolean lockSuccessful = false;
        LockReference lockReference = getLock(key);

        try {
            lockReference.lock();
            lockSuccessful = true;
            runnable.run();
        } finally {
            if (lockSuccessful) {
                lockReference.unlock();
            }
        }
    }

    /**
     * Execute a Runnable in a lock
     *
     * @param lock lock
     * @param runnable runnable
     */
    public static void execute(Lock lock, Runnable runnable) {
        notNull(lock, "lock is null");
        notNull(runnable, "runnable is null");

        boolean lockSuccessful = false;

        try {
            lock.lock();
            lockSuccessful = true;
            runnable.run();
        } finally {
            if (lockSuccessful) {
                lock.unlock();
            }
        }
    }

    /**
     * Execute a Runnable in a lock
     *
     * @param readWriteLock readWriteLock
     * @param runnable runnable
     */
    public static void execute(ReadWriteLock readWriteLock, Runnable runnable) {
        notNull(readWriteLock, "readWriteLock is null");
        notNull(runnable, "runnable is null");

        boolean lockSuccessful = false;

        try {
            readWriteLock.writeLock().lock();
            lockSuccessful = true;
            runnable.run();
        } finally {
            if (lockSuccessful) {
                readWriteLock.writeLock().unlock();
            }
        }
    }

    /**
     * Execute a Runnable in a lock
     *
     * @param lockProvider lockProvider
     * @param runnable runnable
     */
    public static void execute(LockProvider lockProvider, Runnable runnable) {
        notNull(lockProvider, "lockProvider is null");
        notNull(runnable, "runnable is null");

        boolean lockSuccessful = false;

        try {
            lockProvider.getLock().lock();
            lockSuccessful = true;
            runnable.run();
        } finally {
            if (lockSuccessful) {
                lockProvider.getLock().unlock();
            }
        }
    }

    /**
     * Execute a Callable in a lock
     *
     * @param key key
     * @param callable callable
     * @return the callable result
     * @throws Throwable Throwable
     */
    public static <V> V execute(Object key, Callable<V> callable) throws Throwable {
        notNull(key, "key is null");
        notNull(callable, "callable is null");

        boolean lockSuccessful = false;
        LockReference lockReference = getLock(key);

        try {
            lockReference.lock();
            lockSuccessful = true;
            return callable.call();
        } finally {
            if (lockSuccessful) {
                lockReference.unlock();
            }
        }
    }

    /**
     * Execute a Callable in a lock
     *
     * @param lock lock
     * @param callable callable
     * @return the callable result
     * @throws Throwable Throwable
     */
    public static <V> V execute(Lock lock, Callable<V> callable) throws Throwable {
        notNull(lock, "lock is null");
        notNull(callable, "callable is null");

        boolean lockSuccessful = false;

        try {
            lock.lock();
            lockSuccessful = true;
            return callable.call();
        } finally {
            if (lockSuccessful) {
                lock.unlock();
            }
        }
    }

    /**
     * Execute a Callable in a lock
     *
     * @param readWriteLock readWriteLock
     * @param callable callable
     * @return the callable result
     * @throws Throwable Throwable
     */
    public static <V> V execute(ReadWriteLock readWriteLock, Callable<V> callable)
            throws Throwable {
        notNull(readWriteLock, "readWriteLock is null");
        notNull(callable, "callable is null");

        boolean lockSuccessful = false;

        try {
            readWriteLock.writeLock().lock();
            lockSuccessful = true;
            return callable.call();
        } finally {
            if (lockSuccessful) {
                readWriteLock.writeLock().unlock();
            }
        }
    }

    /**
     * Execute a Callable in a lock
     *
     * @param readWriteLockProvider readWriteLockProvider
     * @param callable callable
     * @return the callable result
     * @throws Throwable Throwable
     */
    public static <V> V execute(ReadWriteLockProvider readWriteLockProvider, Callable<V> callable)
            throws Throwable {
        notNull(readWriteLockProvider, "readWriteLockProvider is null");
        notNull(callable, "callable is null");

        boolean lockSuccessful = false;

        try {
            readWriteLockProvider.getReadWriteLock().writeLock().lock();
            lockSuccessful = true;
            return callable.call();
        } finally {
            if (lockSuccessful) {
                readWriteLockProvider.getReadWriteLock().writeLock().unlock();
            }
        }
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
            boolean lockSuccessful = false;
            LockHolder lockHolder;

            try {
                lock.lock();
                lockSuccessful = true;

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
                if (lockSuccessful) {
                    lock.unlock();
                }
            }

            lockHolder.getReentrantLock().lock();
        }

        void tryLock(Object key, long time, TimeUnit timeUnit) throws InterruptedException {
            boolean lockSuccessful = false;
            LockHolder lockHolder;

            try {
                lock.lock();
                lockSuccessful = true;

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
                if (lockSuccessful) {
                    lock.unlock();
                }
            }

            lockHolder.getReentrantLock().tryLock(time, timeUnit);
        }

        /**
         * Releases a lock
         *
         * @param key key
         */
        void unlock(Object key) {
            boolean lockSuccessful = false;

            try {
                lock.lock();
                lockSuccessful = true;

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
                if (lockSuccessful) {
                    lock.unlock();
                }
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
