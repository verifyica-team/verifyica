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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/** Class to implement Locks */
@SuppressWarnings("PMD.UnusedPrivateMethod")
@Deprecated
public class Locks {

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

        LockManager.lock(key);
    }

    /**
     * Try to lock a Lock
     *
     * @param key key
     * @return true if the Lock was locked, else false
     */
    public static boolean tryLock(Object key) {
        notNull(key, "key is null");

        return LockManager.tryLock(key);
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

        return LockManager.tryLock(key, time, timeUnit);
    }

    /**
     * Unlock a Lock
     *
     * @param key key
     */
    public static void unlock(Object key) {
        notNull(key, "key is null");

        LockManager.unlock(key);
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

        return LockManager.isLocked(key);
    }

    /**
     * Execute a Runnable in a Lock
     *
     * @param key key
     * @param runnable runnable
     */
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
    static void assertSize(int size) {
        LockManager.assertSize(size);
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
