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

package org.verifyica.engine.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

/** Class to implement NewPlatformThreadExecutorService */
public class NewPlatformThreadExecutorService extends AbstractExecutorService {

    private volatile boolean isShutdown;
    private final List<Thread> runningThreads;

    /** Constructor */
    public NewPlatformThreadExecutorService() {
        runningThreads = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public void execute(Runnable runnable) {
        if (isShutdown) {
            throw new IllegalStateException("Executor service is shut down");
        }

        Thread thread = new Thread(() -> {
            try {
                runningThreads.add(Thread.currentThread());
                runnable.run();
            } finally {
                runningThreads.remove(Thread.currentThread());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void shutdown() {
        isShutdown = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
        isShutdown = true;

        synchronized (runningThreads) {
            for (Thread thread : runningThreads) {
                thread.interrupt();
            }
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
            Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(remainingTime), 100));
        }
        return true;
    }
}
