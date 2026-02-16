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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Class to implement NewPlatformThreadExecutorService
 */
public class EphemeralExecutorService extends AbstractExecutorService {

    private final ThreadFactory threadFactory;
    private final Set<Thread> runningThreads;
    private volatile boolean isShutdown;

    /**
     * Constructor
     *
     * @param threadFactory threadFactory
     */
    public EphemeralExecutorService(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        this.runningThreads = ConcurrentHashMap.newKeySet();
    }

    @Override
    public void execute(Runnable runnable) {
        if (isShutdown) {
            throw new IllegalStateException("Executor service is shut down");
        }

        Thread thread = threadFactory.newThread(() -> {
            try {
                runnable.run();
            } finally {
                runningThreads.remove(Thread.currentThread());
            }
        });
        thread.setDaemon(true);
        // Add thread to runningThreads BEFORE starting it to avoid race condition
        runningThreads.add(thread);
        thread.start();
    }

    @Override
    public void shutdown() {
        isShutdown = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
        isShutdown = true;

        for (Thread thread : runningThreads) {
            thread.interrupt();
        }

        return new ArrayList<>();
    }

    @Override
    public boolean isShutdown() {
        return isShutdown;
    }

    @Override
    public boolean isTerminated() {
        return isShutdown && runningThreads.isEmpty();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);

        while (!isTerminated()) {
            long remainingTime = deadline - System.nanoTime();
            if (remainingTime <= 0) {
                return false;
            }

            long sleepTimeMillis = Math.min(TimeUnit.NANOSECONDS.toMillis(remainingTime), 1_000);
            Thread.sleep(sleepTimeMillis);
        }

        return true;
    }
}
