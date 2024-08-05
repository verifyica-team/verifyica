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

package org.antublue.verifyica.api.concurrency;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

/** Class to implement ConcurrencySupport */
public class ConcurrencySupport {

    private static final LockManager LOCK_MANAGER = new LockManager();

    /** Constructor */
    private ConcurrencySupport() {
        // INTENTIONALLY BLANK
    }

    public static int getLockCount() {
        return LOCK_MANAGER.getLockCount();
    }

    /**
     * Method to get a LockReference
     *
     * @param key key
     * @return a LockReference
     */
    public static LockReference getLock(Object key) {
        notNull(key, "key is null");

        return new DefaultLockReference(LOCK_MANAGER, key);
    }

    /**
     * Execute a Runnable in a lock
     *
     * @param key key
     * @param runnable runnable
     */
    public static void executeInLock(Object key, Runnable runnable) {
        notNull(key, "key is null");
        notNull(runnable, "runnable is null");

        LockReference lockReference = getLock(key);

        lockReference.lock();
        try {
            runnable.run();
        } finally {
            lockReference.unlock();
        }
    }

    /**
     * Execute a Callable in a lock
     *
     * @param key key
     * @param callable callable
     * @return the callable result
     * @throws Throwable Throwable
     * @param <V> the type
     */
    public static <V> V executeInLock(Object key, Callable<V> callable) throws Throwable {
        notNull(key, "key is null");
        notNull(callable, "callable is null");

        LockReference lockReference = getLock(key);

        lockReference.lock();
        try {
            return callable.call();
        } finally {
            lockReference.unlock();
        }
    }

    /**
     * Execute a Runnable in a lock
     *
     * @param lock lock
     * @param runnable runnable
     */
    public static void executeInLock(Lock lock, Runnable runnable) {
        notNull(lock, "lock is null");
        notNull(runnable, "runnable is null");

        lock.lock();
        try {
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Execute a Runnable in a lock
     *
     * @param lockProvider lockProvider
     * @param runnable runnable
     */
    public static void executeInLock(LockProvider lockProvider, Runnable runnable) {
        notNull(lockProvider, "lockProvider is null");
        notNull(runnable, "runnable is null");

        Lock lock = lockProvider.getLock();
        notNull(lock, "lockProvider.getLock() is null");

        executeInLock(lock, runnable);
    }

    /**
     * Execute a Runnable in a lock
     *
     * @param readWriteLock readWriteLock
     * @param runnable runnable
     */
    public static void executeInLock(ReadWriteLock readWriteLock, Runnable runnable) {
        notNull(readWriteLock, "readWriteLock is null");
        notNull(runnable, "runnable is null");

        Lock lock = readWriteLock.writeLock();
        notNull(lock, "readWriteLock.writeLock() is null");

        executeInLock(lock, runnable);
    }

    /**
     * Execute a Callable in a lock
     *
     * @param lock lock
     * @param callable callable
     * @return the callable result
     * @throws Throwable Throwable
     * @param <V> the type
     */
    public static <V> V executeInLock(Lock lock, Callable<V> callable) throws Throwable {
        notNull(lock, "lock is null");
        notNull(callable, "callable is null");

        lock.lock();
        try {
            return callable.call();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Execute a Callable in a lock
     *
     * @param readWriteLock readWriteLock
     * @param callable callable
     * @return the callable result
     * @throws Throwable Throwable
     * @param <V> the type
     */
    public static <V> V executeInLock(ReadWriteLock readWriteLock, Callable<V> callable)
            throws Throwable {
        notNull(readWriteLock, "readWriteLock is null");
        notNull(callable, "callable is null");

        Lock lock = readWriteLock.writeLock();
        notNull(lock, "readWriteLock.writeLock() is null");

        return executeInLock(lock, callable);
    }

    /**
     * Execute a Callable in a lock
     *
     * @param lockProvider lockProvider
     * @param callable callable
     * @return the callable result
     * @throws Throwable Throwable
     * @param <V> the type
     */
    public static <V> V executeInLock(LockProvider lockProvider, Callable<V> callable)
            throws Throwable {
        notNull(lockProvider, "lockProvider is null");
        notNull(callable, "callable is null");

        Lock lock = lockProvider.getLock();
        notNull(lock, "lockProvider.getLock() is null");

        return executeInLock(lock, callable);
    }

    /**
     * Execute a Runnable in a Semaphore
     *
     * @param semaphore semaphore
     * @param runnable runnable
     * @throws InterruptedException InterruptedException
     */
    public static void executeInSemaphore(Semaphore semaphore, Runnable runnable)
            throws InterruptedException {
        notNull(semaphore, "semaphore is null");
        notNull(runnable, "runnable is null");

        semaphore.acquire();
        try {
            runnable.run();
        } finally {
            semaphore.release();
        }
    }

    /**
     * Execute a Callable in a Semaphore
     *
     * @param semaphore semaphore
     * @param callable callable
     * @return the callable result
     * @param <V> the callable result type
     * @throws Exception Exception
     */
    public static <V> V executeInSemaphore(Semaphore semaphore, Callable<V> callable)
            throws Exception {
        notNull(semaphore, "semaphore is null");
        notNull(callable, "callable is null");

        semaphore.acquire();
        try {
            return callable.call();
        } finally {
            semaphore.release();
        }
    }

    /**
     * Execute a Runnable in a Semaphore
     *
     * @param semaphoreProvider semaphoreProvider
     * @param runnable runnable
     * @throws InterruptedException InterruptedException
     */
    public static void executeInSemaphore(SemaphoreProvider semaphoreProvider, Runnable runnable)
            throws InterruptedException {
        notNull(semaphoreProvider, "semaphoreProvider is null");
        notNull(runnable, "runnable is null");

        Semaphore semaphore = semaphoreProvider.getSemaphore();
        notNull(semaphore, "semaphoreProvider.getSemaphore() is null");

        executeInSemaphore(semaphore, runnable);
    }

    /**
     * Execute a Callable in a Semaphore
     *
     * @param semaphoreProvider semaphoreProvider
     * @param callable callable
     * @return the callable result
     * @param <V> the callable result type
     * @throws Exception Exception
     */
    public static <V> V executeInSemaphore(
            SemaphoreProvider semaphoreProvider, Callable<V> callable) throws Exception {
        notNull(semaphoreProvider, "semaphoreProvider is null");
        notNull(callable, "callable is null");

        Semaphore semaphore = semaphoreProvider.getSemaphore();
        notNull(semaphore, "semaphoreProvider.getSemaphore() is null");

        return executeInSemaphore(semaphore, callable);
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
         * Method to try to acquire the Lock
         *
         * @return true if the lock was acquired, else false
         * @throws InterruptedException InterruptedException
         */
        boolean tryLock() throws InterruptedException;

        /**
         * Method to try to acquire the Lock
         *
         * @param timeout timeout
         * @param timeUnit timeUnit
         * @return true if the lock was acquired, else false
         * @throws InterruptedException InterruptedException
         */
        boolean tryLock(long timeout, TimeUnit timeUnit) throws InterruptedException;

        /**
         * Method to unlock the Lock
         *
         * @throws IllegalMonitorStateException if the current thread does not hold this lock
         */
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
        public boolean tryLock() {
            return lockManager.tryLock(key);
        }

        @Override
        public boolean tryLock(long timeout, TimeUnit timeUnit) throws InterruptedException {
            notNull(timeUnit, "timeUnit it null");
            return lockManager.tryLock(key, timeout, timeUnit);
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

        private final ReentrantLock lockManagerLock;
        private final Map<Object, ReentrantLock> lockMap;
        private final Map<Object, Counter> lockCounterMap;

        /** Constructor */
        public LockManager() {
            lockManagerLock = new ReentrantLock(true);
            lockMap = new HashMap<>();
            lockCounterMap = new HashMap<>();
        }

        public int getLockCount() {
            lockManagerLock.lock();

            try {
                return Math.max(lockMap.size(), lockCounterMap.size());
            } finally {
                lockManagerLock.unlock();
            }
        }

        /**
         * Lock a lock
         *
         * @param key key
         */
        public void lock(Object key) {
            ReentrantLock lock;
            Counter counter;

            lockManagerLock.lock();

            try {
                lock = lockMap.computeIfAbsent(key, k -> new ReentrantLock(true));
                counter = lockCounterMap.computeIfAbsent(key, k -> new Counter());
                counter.increment();
            } finally {
                lockManagerLock.unlock();
            }

            lock.lock();
        }

        /**
         * Try to lock a Lock
         *
         * @param key key
         * @return true if the Lock was locked, else false
         */
        public boolean tryLock(Object key) {
            ReentrantLock lock;
            Counter counter;

            lockManagerLock.lock();

            try {
                lock = lockMap.computeIfAbsent(key, k -> new ReentrantLock(true));

                counter = lockCounterMap.computeIfAbsent(key, k -> new Counter());
                counter.increment();

                if (lock.tryLock()) {
                    return true;
                } else {
                    if (counter.decrement() == 0) {
                        lockMap.remove(key);
                        lockCounterMap.remove(key);
                    }
                    return false;
                }
            } finally {
                lockManagerLock.unlock();
            }
        }

        /**
         * Try to lock a Lock
         *
         * @param key key
         * @param timeout timeout
         * @param timeUnit timeUnit
         * @return true if the Lock was locked, else false
         * @throws InterruptedException InterruptedException
         */
        public boolean tryLock(Object key, long timeout, TimeUnit timeUnit)
                throws InterruptedException {
            ReentrantLock lock;
            Counter counter;

            lockManagerLock.lock();

            try {
                lock = lockMap.computeIfAbsent(key, k -> new ReentrantLock(true));

                counter = lockCounterMap.computeIfAbsent(key, k -> new Counter());
                counter.increment();

                if (lock.tryLock(timeout, timeUnit)) {
                    return true;
                } else {
                    if (counter.decrement() == 0) {
                        lockMap.remove(key);
                        lockCounterMap.remove(key);
                    }
                    return false;
                }
            } finally {
                lockManagerLock.unlock();
            }
        }

        /**
         * Unlock the Lock
         *
         * @param key key
         */
        public void unlock(Object key) {
            lockManagerLock.lock();

            try {
                ReentrantLock lock = lockMap.get(key);
                if (lock != null) {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                        if (lockCounterMap.get(key).decrement() == 0) {
                            lockMap.remove(key);
                            lockCounterMap.remove(key);
                        }
                    } else {
                        throw new IllegalMonitorStateException(
                                format(
                                        "Current thread does not hold the lock for the given key"
                                                + " [%s]",
                                        key));
                    }
                } else {
                    throw new IllegalMonitorStateException(
                            format("No lock found for the given key [%s]", key));
                }
            } finally {
                lockManagerLock.unlock();
            }
        }
    }

    /** Class to implement Counter */
    private static class Counter {

        private int count;

        /** Constructor */
        public Counter() {
            // INTENTIONALLY BLANK
        }

        /** Increment the counter */
        public void increment() {
            count++;
        }

        /**
         * Decrement the counter
         *
         * @return the count
         */
        public int decrement() {
            count--;
            if (count < 0) {
                count = 0;
            }
            return count;
        }
    }
}
