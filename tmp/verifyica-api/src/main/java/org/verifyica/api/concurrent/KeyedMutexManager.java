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
 * Class to implement KeyedMutexManager
 * 
 * <p>Manages mutexes (locks) by string keys, allowing thread-safe synchronization
 * across different parts of an application using the same key.</p>
 */
public class KeyedMutexManager {

    private static final Lock LOCK = new ReentrantLock(true);
    private static final Map<String, MutexReference> MUTEX_REFERENCES = new HashMap<>();

    /**
     * Constructor
     */
    private KeyedMutexManager() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Try to lock a key
     *
     * @param key key
     * @return true if the lock was free or locked by the current Thread, otherwise false
     */
    public static boolean tryLock(String key) {
        notBlank(key, "key is null", "key is blank");

        String trimmedKey = key.trim();
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
     * Try to lock a key
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
     * Lock a key
     *
     * @param key key
     */
    public static void lock(String key) {
        notBlank(key, "key is null", "key is blank");

        String trimmedKey = key.trim();
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
     * Unlock a key
     *
     * @param key key
     */
    public static void unlock(String key) {
        notBlank(key, "key is null", "key is blank");

        String trimmedKey = key.trim();
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
     * Return if a key is locked
     *
     * @param key key
     * @return true if the key is locked, otherwise false
     */
    public static boolean isLocked(String key) {
        notBlank(key, "key is null", "key is blank");

        String trimmedKey = key.trim();

        LOCK.lock();
        try {
            MutexReference mutexReference = MUTEX_REFERENCES.get(trimmedKey);
            return mutexReference != null && mutexReference.getLock().isLocked();
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Assert the number of MutexReferences
     *
     * @param size size
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
     * Validate an Object is not null, throwing an IllegalArgumentException if it is null
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
     * Validate a String is not null and not blank, throwing an IllegalArgumentException
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
     * Class to implement MutexReference
     */
    private static class MutexReference {

        private final ReentrantLock reentrantLock;
        private final Set<Thread> threads;

        /**
         * Constructor
         */
        private MutexReference() {
            reentrantLock = new ReentrantLock(true);
            threads = new HashSet<>();
        }

        /**
         * Get the Lock
         *
         * @return the Lock
         */
        private ReentrantLock getLock() {
            return reentrantLock;
        }

        /**
         * Add the current Thread
         */
        private void addThread() {
            threads.add(Thread.currentThread());
        }

        /**
         * Remove the current Thread
         */
        private void removeThread() {
            threads.remove(Thread.currentThread());
        }

        /**
         * Get the Thread count
         *
         * @return the Thread count
         */
        private int getThreadCount() {
            return threads.size();
        }
    }
}
