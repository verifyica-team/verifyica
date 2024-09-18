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
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** Class to implement Locks */
@SuppressWarnings("PMD.UnusedPrivateMethod")
public class Locks {

    private static final LockManager LOCK_MANAGER = new LockManager();

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

        LOCK_MANAGER.lock(key);
    }

    /**
     * Try to lock a Lock
     *
     * @param key key
     * @return true if the Lock was locked, else false
     */
    public static boolean tryLock(Object key) {
        notNull(key, "key is null");

        return LOCK_MANAGER.tryLock(key);
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

        return LOCK_MANAGER.tryLock(key, time, timeUnit);
    }

    /**
     * Unlock a Lock
     *
     * @param key key
     */
    public static void unlock(Object key) {
        notNull(key, "key is null");

        LOCK_MANAGER.unlock(key);
    }

    /**
     * Check if a key is locked. Use cautiously since locking/unlocking may be called by another
     * thread before/after the lock check
     *
     * @param key key
     * @return true if the key is locked, else false
     */
    public static boolean isLocked(Object key) {
        notNull(key, "key is null");

        return LOCK_MANAGER.isLocked(key);
    }

    /**
     * Execute a Runnable in a Lock
     *
     * @param key key
     * @param runnable runnable
     */
    @Deprecated
    public static void run(Object key, Runnable runnable) {
        execute(key, runnable);
    }

    /**
     * Execute a Runnable in a Lock
     *
     * @param key key
     * @param runnable runnable
     */
    public static void execute(Object key, Runnable runnable) {
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
    @Deprecated
    public void run(Configuration configuration, Runnable runnable) {
        execute(configuration, runnable);
    }

    /**
     * Execute a Runnable in a Configuration Lock
     *
     * @param configuration configuration
     * @param runnable runnable
     */
    public static void execute(Configuration configuration, Runnable runnable) {
        notNull(configuration, "configuration is null");
        notNull(runnable, "runnable is null");

        execute(configuration.getLock(), runnable);
    }

    /**
     * Execute a Runnable in a Context Lock
     *
     * @param context context
     * @param runnable runnable
     */
    @Deprecated
    public static void run(Context context, Runnable runnable) {
        execute(context, runnable);
    }

    /**
     * Execute a Runnable in a Context Lock
     *
     * @param context context
     * @param runnable runnable
     */
    public static void execute(Context context, Runnable runnable) {
        notNull(context, "context is null");
        notNull(runnable, "runnable is null");

        execute(context.getLock(), runnable);
    }

    /**
     * Execute a Runnable in a Store Lock
     *
     * @param store store
     * @param runnable runnable
     */
    @Deprecated
    public static void run(Store store, Runnable runnable) {
        execute(store, runnable);
    }

    /**
     * Execute a Runnable in a Store Lock
     *
     * @param store store
     * @param runnable runnable
     */
    public static void execute(Store store, Runnable runnable) {
        notNull(store, "store is null");
        notNull(runnable, "runnable is null");

        execute(store.getLock(), runnable);
    }

    /**
     * Execute a Runnable in a Store Lock
     *
     * @param lock lock
     * @param runnable runnable
     */
    @Deprecated
    public static void run(Lock lock, Runnable runnable) {
        execute(lock, runnable);
    }

    /**
     * Execute a Runnable in a Store Lock
     *
     * @param lock lock
     * @param runnable runnable
     */
    public static void execute(Lock lock, Runnable runnable) {
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
    @Deprecated
    public static <V> V call(Object key, Callable<V> callable) throws Throwable {
        return execute(key, callable);
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
    public static <V> V execute(Object key, Callable<V> callable) throws Throwable {
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
    @Deprecated
    public static <V> V call(Configuration configuration, Callable<V> callable) throws Throwable {
        return execute(configuration, callable);
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
    public static <V> V execute(Configuration configuration, Callable<V> callable)
            throws Throwable {
        notNull(configuration, "configuration is null");
        notNull(callable, "callable is null");

        return execute(configuration.getLock(), callable);
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
    @Deprecated
    public static <V> V call(Context context, Callable<V> callable) throws Throwable {
        return execute(context, callable);
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
    public static <V> V execute(Context context, Callable<V> callable) throws Throwable {
        notNull(context, "context is null");
        notNull(callable, "callable is null");

        return execute(context.getLock(), callable);
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
    @Deprecated
    public static <V> V call(Store store, Callable<V> callable) throws Throwable {
        return execute(store, callable);
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
    public static <V> V execute(Store store, Callable<V> callable) throws Throwable {
        notNull(store, "store is null");
        notNull(callable, "callable is null");

        return execute(store.getLock(), callable);
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
    @Deprecated
    public static <V> V call(Lock lock, Callable<V> callable) throws Throwable {
        return execute(lock, callable);
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
    public static <V> V execute(Lock lock, Callable<V> callable) throws Throwable {
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
        LOCK_MANAGER.assertEmpty();
    }

    /** For testing */
    static void assertNotEmpty() {
        LOCK_MANAGER.assertNotEmpty();
    }

    /** For testing */
    static void assertSize(int size) {
        LOCK_MANAGER.assertSize(size);
    }

    /** Class to implement LockManager */
    private static class LockManager {

        private final Lock lockManagerLock;
        private final Map<Object, LockReference> lockReferences;

        /** Constructor */
        public LockManager() {
            lockManagerLock = new ReentrantLock(true);
            lockReferences = new HashMap<>();
        }

        /**
         * Method to try to lock a key
         *
         * @param key key
         * @return true if the lock was free or locked the current Thread, else false
         */
        private boolean tryLock(Object key) {
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
        private boolean tryLock(Object key, long timeout, TimeUnit timeUnit)
                throws InterruptedException {
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
        private void lock(Object key) {
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
        private void unlock(Object key) {
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
        private boolean isLocked(Object key) {
            lockManagerLock.lock();
            try {
                return lockReferences.get(key) != null;
            } finally {
                lockManagerLock.unlock();
            }
        }

        /** Method to assert there are no LockReferences */
        private void assertEmpty() {
            lockManagerLock.lock();
            try {
                if (!lockReferences.isEmpty()) {
                    throw new IllegalStateException("lockReferences is not empty");
                }
            } finally {
                lockManagerLock.unlock();
            }
        }

        /** Method to assert there are LockReferences */
        private void assertNotEmpty() {
            lockManagerLock.lock();
            try {
                if (lockReferences.isEmpty()) {
                    throw new IllegalStateException("lockReferences is empty");
                }
            } finally {
                lockManagerLock.unlock();
            }
        }

        /**
         * Method to assert the number of LockReferences
         *
         * @param size size
         */
        private void assertSize(int size) {
            lockManagerLock.lock();
            try {
                if (lockReferences.size() != size) {
                    throw new IllegalStateException("lockReferences size is incorrect");
                }
            } finally {
                lockManagerLock.unlock();
            }
        }

        /** Class to implement LockReference */
        private static class LockReference {

            private final ReentrantLock reentrantLock;
            private final Set<Thread> threads;

            /** Constructor */
            public LockReference() {
                reentrantLock = new ReentrantLock(true);
                threads = new HashSet<>();
            }

            /**
             * Method to get the Lock
             *
             * @return the Lock
             */
            public ReentrantLock getLock() {
                return reentrantLock;
            }

            /** Method to add the current Thread */
            public void addThread() {
                threads.add(Thread.currentThread());
            }

            /** Method to remove the current Thread */
            public void removeThread() {
                threads.remove(Thread.currentThread());
            }

            /**
             * Method to get the Thread count
             *
             * @return the Thread count
             */
            public int getThreadCount() {
                return threads.size();
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
