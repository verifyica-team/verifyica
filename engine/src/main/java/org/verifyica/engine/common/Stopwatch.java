/*
 * Copyright (C) Verifyica project authors and contributors
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

package org.verifyica.engine.common;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class to implement a Stopwatch
 *
 * <p>This class provides a simple way to measure elapsed time, supporting
 * multiple start/stop cycles with accumulated duration. All operations are
 * thread-safe.
 */
public final class Stopwatch {

    /**
     * Unique identifier for this stopwatch instance.
     */
    private final FastId fastId = FastId.randomFastId();

    /**
     * Read-write lock to ensure thread safety.
     */
    private final ReadWriteLock readWriteLock;

    /**
     * Accumulated time across completed runs (nanoseconds).
     */
    private long accumulatedNanos;

    /**
     * Start time of current running segment (nanoseconds), valid only when running
     */
    private long startNanoTime;

    /**
     * Whether the stopwatch is currently running.
     */
    private boolean running;

    /**
     * Constructor
     *
     * <p>The Stopwatch starts automatically.
     */
    public Stopwatch() {
        this.readWriteLock = new ReentrantReadWriteLock(true);
        reset(); // auto-starts
    }

    /**
     * Reset the stopwatch and start it running.
     *
     * @return this stopwatch
     */
    public Stopwatch reset() {
        readWriteLock.writeLock().lock();
        try {
            accumulatedNanos = 0L;
            startNanoTime = System.nanoTime();
            running = true;
            return this;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Start (or resume) the stopwatch. No-op if already running.
     *
     * @return this stopwatch
     */
    public Stopwatch start() {
        readWriteLock.writeLock().lock();
        try {
            if (!running) {
                startNanoTime = System.nanoTime();
                running = true;
            }
            return this;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Stop (pause) the stopwatch and accumulate elapsed time.
     * No-op if already stopped.
     *
     * @return this stopwatch
     */
    public Stopwatch stop() {
        readWriteLock.writeLock().lock();
        try {
            if (running) {
                long now = System.nanoTime();
                accumulatedNanos += (now - startNanoTime);
                running = false;
            }
            return this;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Returns the total elapsed time across all runs.
     * If running, includes time since the last start.
     *
     * @return total elapsed duration
     */
    public Duration elapsed() {
        readWriteLock.readLock().lock();
        try {
            long total = accumulatedNanos;
            if (running) {
                total += (System.nanoTime() - startNanoTime);
            }
            return Duration.ofNanos(total);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Alias for {@link #elapsed()}.
     *
     * @return total elapsed duration
     */
    public Duration lap() {
        return elapsed();
    }

    /**
     * Check if the stopwatch is currently running.
     *
     * @return whether the stopwatch is currently running
     */
    public boolean isRunning() {
        readWriteLock.readLock().lock();
        try {
            return running;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public String toString() {
        return String.valueOf(elapsed().toNanos());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Stopwatch)) return false;
        Stopwatch stopwatch = (Stopwatch) o;
        return Objects.equals(fastId, stopwatch.fastId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fastId);
    }
}
