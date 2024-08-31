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

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** Class to implement Locks */
public class Locks {

    private static final InternalLockManager INTERNAL_LOCK_MANAGER = new InternalLockManager();

    /** Constructor */
    private Locks() {
        // INTENTIONALLY BLANK
    }

    /**
     * Lock a Lock
     *
     * @param key key
     */
    public static void lock(Object key) {
        notNull(key, "key is null");
        INTERNAL_LOCK_MANAGER.lock(key);
    }

    /**
     * Try to lock a Lock
     *
     * @param key key
     * @return true if the Lock was locked, else false
     */
    public static boolean tryLock(Object key) {
        notNull(key, "key is null");
        return INTERNAL_LOCK_MANAGER.tryLock(key);
    }

    /**
     * Try to lock a Lock
     *
     * @param key key
     * @param time time
     * @param timeUnit timeUnit
     * @return true if the Lock was locked, else false
     * @throws InterruptedException InterruptedException
     */
    public static boolean tryLock(Object key, long time, TimeUnit timeUnit)
            throws InterruptedException {
        notNull(key, "key is null");
        notNull(timeUnit, "timeUnit is null");
        return INTERNAL_LOCK_MANAGER.tryLock(key, time, timeUnit);
    }

    /**
     * Unlock a Lock
     *
     * @param key key
     */
    public static void unlock(Object key) {
        INTERNAL_LOCK_MANAGER.unlock(key);
    }

    /**
     * Execute a Runnable in a Lock
     *
     * @param key key
     * @param runnable runnable
     */
    public static void run(Object key, Runnable runnable) {
        notNull(key, "key is null");
        notNull(runnable, "runnable is null");

        lock(key);
        try {
            runnable.run();
        } finally {
            unlock(key);
        }
    }

    /**
     * Execute a Runnable in a Configuration Lock
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
     * Execute a Runnable in a Context Lock
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
     * Execute a Runnable in a Store Lock
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
     * Execute a Runnable in a Store Lock
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
     * Execute a Callable in a Lock
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

        lock(key);
        try {
            return callable.call();
        } finally {
            unlock(key);
        }
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
     * Execute a Callable in a Store lock
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

    /** For testing */
    static void assertEmpty() {
        INTERNAL_LOCK_MANAGER.assertEmpty();
    }

    /** Class to implement InternalLockManager */
    private static class InternalLockManager {

        private static final ConcurrentHashMap<Object, LockReference> lockReferences =
                new ConcurrentHashMap<>();

        /** Constructor */
        private InternalLockManager() {
            // INTENTIONALLY BLANK
        }

        /**
         * Lock a Lock
         *
         * @param key key
         */
        public void lock(Object key) {
            notNull(key, "key is null");

            LockReference lockReference =
                    lockReferences.compute(
                            key,
                            (k, v) -> v == null ? new LockReference() : v.incrementThreadCount());

            lockReference.getLock().lock();
        }

        /**
         * Try to lock a Lock
         *
         * @param key key
         * @return true if the Lock was locked, else false
         */
        public boolean tryLock(Object key) {
            notNull(key, "key is null");

            LockReference lockReference =
                    lockReferences.compute(
                            key,
                            (k, v) -> v == null ? new LockReference() : v.incrementThreadCount());

            return lockReference.getLock().tryLock();
        }

        /**
         * Try to lock a Lock
         *
         * @param key key
         * @return true if the Lock was locked, else false
         */
        public boolean tryLock(Object key, long time, TimeUnit timeUnit)
                throws InterruptedException {
            LockReference lockReference =
                    lockReferences.compute(
                            key,
                            (k, v) -> v == null ? new LockReference() : v.incrementThreadCount());

            return lockReference.getLock().tryLock(time, timeUnit);
        }

        /**
         * Unlock a lock
         *
         * @param key key
         */
        public void unlock(Object key) {
            notNull(key, "key is null");

            LockReference lockReference = lockReferences.get(key);
            lockReference.reentrantLock.unlock();
            if (lockReference.decrementThreadCount() == 0) {
                lockReferences.remove(key, lockReference);
            }
        }

        /** For testing */
        private void assertEmpty() {
            if (!lockReferences.isEmpty()) {
                throw new IllegalStateException("size should be 0");
            }
        }

        /** Class to implement LockReference */
        private static class LockReference {

            private final ReentrantLock reentrantLock;
            private final AtomicInteger numberOfThreadsInQueue;

            /** Constructor */
            private LockReference() {
                reentrantLock = new ReentrantLock(true);
                numberOfThreadsInQueue = new AtomicInteger(1);
            }

            /**
             * Method to get the LockContainer's Lock
             *
             * @return the LockContainer's lock
             */
            public Lock getLock() {
                return reentrantLock;
            }

            /**
             * Increment the thread count
             *
             * @return this
             */
            private LockReference incrementThreadCount() {
                numberOfThreadsInQueue.incrementAndGet();
                return this;
            }

            /**
             * Decrement the thread count
             *
             * @return this
             */
            private int decrementThreadCount() {
                return numberOfThreadsInQueue.decrementAndGet();
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
}
