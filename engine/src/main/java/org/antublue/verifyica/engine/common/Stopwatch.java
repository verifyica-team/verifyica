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

package org.antublue.verifyica.engine.common;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/** Class to implement StopWatch */
@SuppressWarnings("UnusedReturnValue")
public class Stopwatch {

    private final ReadWriteLock readWriteLock;
    private long startNanoTime;
    private Long stopNanoTime;

    /**
     * Constructor
     *
     * <p>The StopWatch starts automatically
     */
    public Stopwatch() {
        readWriteLock = new ReentrantReadWriteLock(true);
        reset();
    }

    /**
     * Method to reset the StopWatch
     *
     * @return the StopWatch
     */
    public Stopwatch reset() {
        getReadWriteLock().writeLock().lock();
        try {
            startNanoTime = System.nanoTime();
            stopNanoTime = null;
            return this;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Method to stop the StopWatch
     *
     * @return the StopWatch
     */
    public Stopwatch stop() {
        getReadWriteLock().writeLock().lock();
        try {
            stopNanoTime = System.nanoTime();
            return this;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Method to get the StopWatch elapsed time in nanoseconds
     *
     * @return the StopWatch elapsed time in nanoseconds
     */
    public Duration elapsedTime() {
        getReadWriteLock().readLock().lock();
        try {
            if (stopNanoTime == null) {
                return Duration.ofNanos(System.nanoTime() - startNanoTime);
            } else {
                return Duration.ofNanos(stopNanoTime - startNanoTime);
            }
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public String toString() {
        return String.valueOf(elapsedTime().toNanos());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stopwatch stopWatch = (Stopwatch) o;

        getReadWriteLock().readLock().lock();
        try {
            return startNanoTime == stopWatch.startNanoTime
                    && Objects.equals(stopNanoTime, stopWatch.stopNanoTime);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public int hashCode() {
        getReadWriteLock().readLock().lock();
        try {
            return Objects.hash(startNanoTime, stopNanoTime);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Method to get the ReadWriteLock
     *
     * @return the ReadWriteLock
     */
    private ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }
}
