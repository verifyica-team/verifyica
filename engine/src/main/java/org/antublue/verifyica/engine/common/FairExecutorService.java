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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** Class to implement FaireExecutorService */
public class FairExecutorService extends AbstractExecutorService {

    private final ExecutorService executorService;
    private final Map<Thread, Queue<Runnable>> taskQueues;
    private final Lock lock;
    private final AtomicBoolean isShutdown;
    private final AtomicBoolean isTerminating;

    /**
     * Constructor
     *
     * @param executorService executorService
     */
    public FairExecutorService(ExecutorService executorService) {
        Precondition.notNull(executorService, "executorService is null");

        this.executorService = executorService;
        this.taskQueues = new ConcurrentHashMap<>();
        this.lock = new ReentrantLock(true);
        this.isShutdown = new AtomicBoolean(false);
        this.isTerminating = new AtomicBoolean(false);
    }

    @Override
    public void execute(Runnable task) {
        Precondition.notNull(task, "task is null");

        if (isShutdown.get()) {
            throw new RejectedExecutionException("ExecutorService is shut down");
        }

        Thread currentThread = Thread.currentThread();
        taskQueues.computeIfAbsent(currentThread, k -> new LinkedList<>()).offer(task);
        processTaskQueue();
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        Precondition.notNull(task, "task is null");

        RunnableFuture<T> futureTask = new FutureTask<>(task);
        execute(futureTask);
        return futureTask;
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        Precondition.notNull(task, "task is null");
        Precondition.notNull(result, "result is null");

        RunnableFuture<T> futureTask = new FutureTask<>(task, result);
        execute(futureTask);
        return futureTask;
    }

    @Override
    public Future<?> submit(Runnable task) {
        Precondition.notNull(task, "task is null");

        RunnableFuture<?> futureTask = new FutureTask<>(task, null);
        execute(futureTask);
        return futureTask;
    }

    @Override
    public void shutdown() {
        isShutdown.set(true);
        executorService.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        isShutdown.set(true);
        isTerminating.set(true);
        List<Runnable> pendingTasks = new ArrayList<>();
        taskQueues.values().forEach(pendingTasks::addAll);
        taskQueues.clear();
        pendingTasks.addAll(executorService.shutdownNow());
        return pendingTasks;
    }

    @Override
    public boolean isShutdown() {
        return isShutdown.get();
    }

    @Override
    public boolean isTerminated() {
        return executorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        Precondition.notNull(unit, "unit is null");

        return executorService.awaitTermination(timeout, unit);
    }

    /**
     * Method to process the task queue fairly
     */
    private void processTaskQueue() {
        lock.lock();
        try {
            for (Map.Entry<Thread, Queue<Runnable>> entry : taskQueues.entrySet()) {
                Queue<Runnable> queue = entry.getValue();
                Runnable task = queue.poll();
                if (task != null) {
                    executorService.submit(
                            () -> {
                                try {
                                    task.run();
                                } finally {
                                    processTaskQueue();
                                }
                            });
                }
                if (queue.isEmpty()) {
                    taskQueues.remove(entry.getKey());
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
