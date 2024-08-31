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
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import org.antublue.verifyica.api.Configuration;
import org.antublue.verifyica.api.Context;
import org.antublue.verifyica.api.Store;

/** Class to implement ConcurrencySupport */
public class ConcurrencySupport {

    /** Enum to implement LockType */
    public enum LockType {
        /** READ Lock */
        READ,
        /** WRITE Lock */
        WRITE
    }

    private static final LockManager LOCK_MANAGER = new LockManager();

    /** Constructor */
    private ConcurrencySupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Acquire a LockReference to a Lock.
     *
     * @param key key
     * @return a LockReference
     */
    public static LockReference getLockReference(Object key) {
        notNull(key, "key is null");

        return new DefaultLockReference(LOCK_MANAGER, key);
    }

    /**
     * Execute a Runnable in a lock
     *
     * @param key key
     * @param runnable runnable
     */
    public static void run(Object key, Runnable runnable) {
        notNull(key, "key is null");
        notNull(runnable, "runnable is null");

        LockReference lockReference = getLockReference(key);
        lockReference.lock();
        try {
            runnable.run();
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
    public static void run(Lock lock, Runnable runnable) {
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
     * Execute a Runnable in a Configuration lock
     *
     * @param configuration configuration
     * @param runnable runnable
     */
    public static void run(Configuration configuration, Runnable runnable) {
        notNull(configuration, "configuration is null");
        notNull(runnable, "runnable is null");

        run(configuration.getLock(), runnable);
    }

    /**
     * Execute a Runnable in a Context lock
     *
     * @param context context
     * @param runnable runnable
     */
    public static void run(Context context, Runnable runnable) {
        notNull(context, "context is null");
        notNull(runnable, "runnable is null");

        run(context.getLock(), runnable);
    }

    /**
     * Execute a Runnable in a Store lock
     *
     * @param store store
     * @param runnable runnable
     */
    public static void run(Store store, Runnable runnable) {
        notNull(store, "store is null");
        notNull(runnable, "runnable is null");

        run(store.getLock(), runnable);
    }

    /**
     * Execute a Runnable in a lock
     *
     * @param readWriteLock readWriteLock
     * @param lockType lockType
     * @param runnable runnable
     */
    @Deprecated
    public static void run(ReadWriteLock readWriteLock, LockType lockType, Runnable runnable) {
        notNull(readWriteLock, "readWriteLock is null");
        notNull(lockType, "lockType is null");
        notNull(runnable, "runnable is null");

        Lock lock;

        switch (lockType) {
            case READ:
                {
                    lock = readWriteLock.readLock();
                    notNull(lock, "readWriteLock.readLock() is null");
                    break;
                }
            case WRITE:
                {
                    lock = readWriteLock.writeLock();
                    notNull(lock, "readWriteLock.writeLock() is null");
                    break;
                }
            default:
                {
                    throw new IllegalArgumentException(format("Invalid lockType [%s]", lockType));
                }
        }

        run(lock, runnable);
    }

    /**
     * Execute a Runnable in a Semaphore
     *
     * @param semaphore semaphore
     * @param runnable runnable
     * @throws InterruptedException InterruptedException
     */
    public static void run(Semaphore semaphore, Runnable runnable) throws InterruptedException {
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
     * Execute a Callable in a lock
     *
     * @param key key
     * @param callable callable
     * @return the callable result
     * @throws Throwable Throwable
     * @param <V> the type
     */
    public static <V> V call(Object key, Callable<V> callable) throws Throwable {
        notNull(key, "key is null");
        notNull(callable, "callable is null");

        LockReference lockReference = getLockReference(key);
        lockReference.lock();
        try {
            return callable.call();
        } finally {
            lockReference.unlock();
        }
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
    public static <V> V call(Lock lock, Callable<V> callable) throws Throwable {
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
    @Deprecated
    public static <V> V call(ReadWriteLock readWriteLock, Callable<V> callable) throws Throwable {
        notNull(readWriteLock, "readWriteLock is null");
        notNull(callable, "callable is null");

        Lock lock = readWriteLock.writeLock();
        notNull(lock, "readWriteLock.writeLock() is null");

        return call(lock, callable);
    }

    /**
     * Execute a Callable in a lock
     *
     * @param readWriteLock readWriteLock
     * @param lockType lockType
     * @param callable callable
     * @return the callable result
     * @throws Throwable Throwable
     * @param <V> the type
     */
    @Deprecated
    public static <V> V call(ReadWriteLock readWriteLock, LockType lockType, Callable<V> callable)
            throws Throwable {
        notNull(readWriteLock, "readWriteLock is null");
        notNull(lockType, "lockType is null");
        notNull(callable, "callable is null");

        Lock lock;

        switch (lockType) {
            case READ:
                {
                    lock = readWriteLock.readLock();
                    notNull(lock, "readWriteLock.readLock() is null");
                    break;
                }
            case WRITE:
                {
                    lock = readWriteLock.writeLock();
                    notNull(lock, "readWriteLock.writeLock() is null");
                    break;
                }
            default:
                {
                    throw new IllegalArgumentException(format("Invalid lockType [%s]", lockType));
                }
        }

        return call(lock, callable);
    }

    /**
     * Execute a Callable in a Context lock
     *
     * @param configuration configuration
     * @param callable callable
     * @return the callable result
     * @throws Throwable Throwable
     * @param <V> the type
     */
    public static <V> V call(Configuration configuration, Callable<V> callable) throws Throwable {
        notNull(configuration, "configuration is null");
        notNull(callable, "callable is null");

        return call(configuration.getLock(), callable);
    }

    /**
     * Execute a Callable in a Context lock
     *
     * @param context context
     * @param callable callable
     * @return the callable result
     * @throws Throwable Throwable
     * @param <V> the type
     */
    public static <V> V call(Context context, Callable<V> callable) throws Throwable {
        notNull(context, "context is null");
        notNull(callable, "callable is null");

        return call(context.getLock(), callable);
    }

    /**
     * Execute a Callable in a Store lock
     *
     * @param store store
     * @param callable callable
     * @return the callable result
     * @throws Throwable Throwable
     * @param <V> the type
     */
    public static <V> V call(Store store, Callable<V> callable) throws Throwable {
        notNull(store, "store is null");
        notNull(callable, "callable is null");

        return call(store.getLock(), callable);
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
    public static <V> V call(Semaphore semaphore, Callable<V> callable) throws Exception {
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

        /**
         * Get the LockReference key
         *
         * @return the LockReference key
         */
        Object key();

        /** Lock the Lock */
        void lock();

        /**
         * Tries to lock the lock
         *
         * @return true if the lock was acquired, else false
         */
        boolean tryLock();

        /**
         * Tries to lock the Lock
         *
         * @param timeout timeout
         * @param timeUnit timeUnit
         * @return true if the lock was acquired, else false
         * @throws InterruptedException InterruptedException
         */
        boolean tryLock(long timeout, TimeUnit timeUnit) throws InterruptedException;

        /**
         * Unlocks the Lock
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
        public Object key() {
            return this.key;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DefaultLockReference that = (DefaultLockReference) o;
            return Objects.equals(lockManager, that.lockManager) && Objects.equals(key, that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lockManager, key);
        }
    }

    /** Class to implement LockManager */
    private static class LockManager {

        private final ReentrantLock reentrantLock;
        private final Map<Object, ReentrantLock> lockMap;
        private final Map<Object, Counter> lockCounterMap;

        /** Constructor */
        private LockManager() {
            reentrantLock = new ReentrantLock(true);
            lockMap = new HashMap<>();
            lockCounterMap = new HashMap<>();
        }

        /**
         * Lock a Lock
         *
         * @param key key
         */
        public void lock(Object key) {
            ReentrantLock lock;
            Counter counter;

            reentrantLock.lock();
            try {
                lock = lockMap.computeIfAbsent(key, k -> new ReentrantLock(true));
                counter = lockCounterMap.computeIfAbsent(key, k -> new Counter());
                counter.increment();
            } finally {
                reentrantLock.unlock();
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

            reentrantLock.lock();
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
                reentrantLock.unlock();
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

            reentrantLock.lock();
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
                reentrantLock.unlock();
            }
        }

        /**
         * Unlock the Lock
         *
         * @param key key
         */
        public void unlock(Object key) {
            reentrantLock.lock();
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
                reentrantLock.unlock();
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
