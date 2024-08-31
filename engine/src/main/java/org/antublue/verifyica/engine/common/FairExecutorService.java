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

import io.github.thunkware.vt.bridge.ThreadTool;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ThreadSupport;

/** Class to implement FairExecutorService */
public class FairExecutorService extends AbstractExecutorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FairExecutorService.class);

    private final int blockingQueueCount;
    private final List<BlockingQueue<Runnable>> blockingQueues;
    private final List<Thread> threads;
    private final AtomicInteger blockingQueueIndex;
    private final AtomicBoolean isShutdown;

    /**
     * Constructor
     *
     * @param parallelism parallelism
     */
    public FairExecutorService(int parallelism) {
        Precondition.isTrue(parallelism > 0, "parallelism is less than 1");

        LOGGER.trace("parallelism [%d]", parallelism);

        if (ThreadTool.hasVirtualThreads()) {
            LOGGER.trace("using virtual threads");
        } else {
            LOGGER.trace("using platform threads");
        }

        this.blockingQueueCount = parallelism;
        this.blockingQueues = new ArrayList<>(parallelism);
        this.threads = new ArrayList<>(parallelism);
        this.blockingQueueIndex = new AtomicInteger(0);
        this.isShutdown = new AtomicBoolean();

        for (int i = 0; i < parallelism; i++) {
            BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>(10);
            blockingQueues.add(blockingQueue);

            Thread thread = ThreadSupport.newThread(() -> processBlockingQueue(blockingQueue));
            threads.add(thread);
            thread.start();
        }
    }

    @Override
    public void shutdown() {
        isShutdown.set(true);
    }

    @Override
    public List<Runnable> shutdownNow() {
        isShutdown.set(true);
        List<Runnable> remainingRunnables = new ArrayList<>();
        for (BlockingQueue<Runnable> queue : blockingQueues) {
            queue.drainTo(remainingRunnables);
        }
        return remainingRunnables;
    }

    @Override
    public boolean isShutdown() {
        return isShutdown.get();
    }

    @Override
    public boolean isTerminated() {
        return threads.stream().noneMatch(Thread::isAlive);
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long endTime = System.nanoTime() + unit.toNanos(timeout);
        for (Thread thread : threads) {
            long timeLeft = endTime - System.nanoTime();
            if (timeLeft > 0) {
                thread.join(timeLeft / 1_000_000, (int) (timeLeft % 1_000_000));
            } else {
                break;
            }
        }
        return isTerminated();
    }

    @Override
    public void execute(Runnable runnable) {
        Precondition.notNull(runnable, "runnable is null");

        int index = blockingQueueIndex.getAndUpdate(i -> (i + 1) % blockingQueueCount);
        try {
            blockingQueues.get(index).put(runnable);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Method to process a BlockingQueue
     *
     * @param blockingQueue blockingQueue
     */
    private void processBlockingQueue(BlockingQueue<Runnable> blockingQueue) {
        while (!isShutdown.get()) {
            try {
                Runnable runnable = blockingQueue.poll(100, TimeUnit.MILLISECONDS);
                if (runnable != null) {
                    runnable.run();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
