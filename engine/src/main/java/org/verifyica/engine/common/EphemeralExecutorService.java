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
 * Class to implement EphemeralExecutorService
 */
public class EphemeralExecutorService extends AbstractExecutorService {

    private final ThreadFactory threadFactory;
    private final Set<Thread> runningThreads;
    private final AtomicBoolean isShutdown;

    /**
     * Constructor
     *
     * @param threadFactory threadFactory
     */
    public EphemeralExecutorService(ThreadFactory threadFactory) {
        if (threadFactory == null) {
            throw new IllegalArgumentException("threadFactory is null");
        }
        this.threadFactory = threadFactory;
        this.runningThreads = ConcurrentHashMap.newKeySet();
        this.isShutdown = new AtomicBoolean(false);
    }

    @Override
    public void execute(Runnable runnable) {
        if (runnable == null) {
            throw new IllegalArgumentException("runnable is null");
        }

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
