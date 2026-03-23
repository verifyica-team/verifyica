/*
 * Copyright 2024-present Verifyica project authors and contributors. All rights reserved.
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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An {@link AbstractExecutorService} implementation that creates a new thread for each task.
 *
 * <p>Each task submitted to this executor service results in the creation of a new thread
 * to execute that task. Threads are created on-demand and are not reused, hence the term
 * "ephemeral." This approach provides complete isolation between tasks but may be less
 * efficient than thread pooling for high-throughput scenarios.
 *
 * <p>This executor service is particularly useful when tasks are long-running or when
 * complete isolation between task executions is required.
 *
 * @see AbstractExecutorService
 * @see ThreadFactory
 */
public class EphemeralExecutorService extends AbstractExecutorService {

    /**
     * The thread factory used to create new threads for executing tasks.
     */
    private final ThreadFactory threadFactory;

    /**
     * A thread-safe set to track currently running threads. This allows for proper shutdown
     */
    private final Set<Thread> runningThreads;

    /**
     * AtomicBoolean to track whether the executor service has been shut down. This ensures that
     */
    private final AtomicBoolean isShutdown;

    /**
     * Constructs a new EphemeralExecutorService with the given thread factory.
     *
     * @param threadFactory the thread factory to use for creating threads
     * @throws IllegalArgumentException if threadFactory is null
     */
    public EphemeralExecutorService(ThreadFactory threadFactory) {
        Precondition.notNull(threadFactory, "threadFactory is null");
        this.threadFactory = threadFactory;
        this.runningThreads = ConcurrentHashMap.newKeySet();
        this.isShutdown = new AtomicBoolean(false);
    }

    @Override
    public void execute(Runnable runnable) {
        Precondition.notNull(runnable, "runnable is null");

        if (isShutdown.get()) {
            throw new RejectedExecutionException("Executor has been shut down");
        }

        AtomicBoolean started = new AtomicBoolean(false);
        Thread thread = threadFactory.newThread(() -> {
            try {
                runnable.run();
            } finally {
                if (started.get()) {
                    runningThreads.remove(Thread.currentThread());
                }
            }
        });

        if (thread == null) {
            throw new IllegalStateException("thread is null");
        }

        thread.setDaemon(true);

        // Add thread to runningThreads BEFORE starting it to avoid race condition
        runningThreads.add(thread);
        started.set(true);

        try {
            thread.start();
        } catch (Throwable t) {
            // Clean up if thread fails to start
            runningThreads.remove(thread);
            started.set(false);
            throw new RejectedExecutionException("Failed to start thread", t);
        }
    }

    @Override
    public void shutdown() {
        isShutdown.set(true);
    }

    @Override
    public List<Runnable> shutdownNow() {
        isShutdown.set(true);

        for (Thread thread : runningThreads) {
            thread.interrupt();
        }

        return Collections.emptyList();
    }

    @Override
    public boolean isShutdown() {
        return isShutdown.get();
    }

    @Override
    public boolean isTerminated() {
        return isShutdown.get() && runningThreads.isEmpty();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);

        while (!isTerminated()) {
            long remainingNanos = deadline - System.nanoTime();
            if (remainingNanos <= 0) {
                return false;
            }

            // Use shorter sleep intervals for better responsiveness
            // Cap at 10ms to balance CPU usage and responsiveness
            long sleepMillis = Math.min(TimeUnit.NANOSECONDS.toMillis(remainingNanos), 10L);
            if (sleepMillis <= 0) {
                // Less than 1ms remaining, spin briefly
                Thread.sleep(1);
            } else {
                Thread.sleep(sleepMillis);
            }
        }

        return true;
    }
}
