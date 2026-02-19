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
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class to implement DirectExecutorService
 */
public class DirectExecutorService extends AbstractExecutorService {

    private static final List<Runnable> EMPTY_LIST = Collections.emptyList();

    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    /**
     * Constructor
     */
    public DirectExecutorService() {
        // INTENTIONALLY EMPTY
    }

    @Override
    public void shutdown() {
        isShutdown.set(true);
    }

    @Override
    public List<Runnable> shutdownNow() {
        isShutdown.set(true);
        return EMPTY_LIST;
    }

    @Override
    public boolean isShutdown() {
        return isShutdown.get();
    }

    @Override
    public boolean isTerminated() {
        return isShutdown.get();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit timeUnit) throws InterruptedException {
        if (!isShutdown.get()) {
            long nanos = timeUnit.toNanos(timeout);
            long deadline = System.nanoTime() + nanos;
            while (!isShutdown.get()) {
                if (nanos <= 0) {
                    return false;
                }
                Thread.sleep(Math.min(nanos / 1_000_000L, 10L));
                nanos = deadline - System.nanoTime();
            }
        }
        return true;
    }

    @Override
    public void execute(Runnable runnable) {
        if (runnable == null) {
            throw new IllegalArgumentException("Runnable is null");
        }
        if (isShutdown.get()) {
            throw new RejectedExecutionException("Executor has been shut down");
        }
        runnable.run();
    }
}
