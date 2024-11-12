/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** Class to implement FairExecutorService */
public class FairExecutorService implements ExecutorService {

    private final ExecutorService executorService;
    private final Map<Thread, Queue<Runnable>> taskQueueByThread;
    private final Lock lock = new ReentrantLock();
    private final Queue<Thread> threadQueue;
    private final Condition notEmpty;
    private volatile boolean isShutdown = false;

    /**
     * Constructor
     *
     * @param executorService executorService
     */
    public FairExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        this.taskQueueByThread = new ConcurrentHashMap<>();
        this.threadQueue = new LinkedList<>();
        this.notEmpty = lock.newCondition();
    }

    @Override
    public void execute(Runnable task) {
        if (isShutdown) {
            throw new RejectedExecutionException("ExecutorService has been shut down.");
        }

        Thread currentThread = Thread.currentThread();

        lock.lock();
        try {
            taskQueueByThread
                    .computeIfAbsent(currentThread, k -> {
                        threadQueue.add(currentThread);
                        return new LinkedList<>();
                    })
                    .add(task);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }

        executorService.execute(this::runTasks);
    }

    /**
     * Method to run tasks
     */
    private void runTasks() {
        while (true) {
            Runnable task = null;

            lock.lock();
            try {
                while (threadQueue.isEmpty() && !isShutdown) {
                    notEmpty.await();
                }

                if (isShutdown && threadQueue.isEmpty()) {
                    break;
                }

                Thread nextThread = threadQueue.peek();
                Queue<Runnable> nextQueue = taskQueueByThread.get(nextThread);

                if (nextQueue != null && !nextQueue.isEmpty()) {
                    task = nextQueue.poll();
                    if (nextQueue.isEmpty()) {
                        threadQueue.poll();
                        taskQueueByThread.remove(nextThread);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } finally {
                lock.unlock();
            }

            if (task != null) {
                task.run();
            } else {
                break;
            }
        }
    }

    @Override
    public void shutdown() {
        isShutdown = true;
        lock.lock();
        try {
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
        executorService.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        isShutdown = true;
        lock.lock();
        try {
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
        return executorService.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return isShutdown;
    }

    @Override
    public boolean isTerminated() {
        return executorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        execute(() -> {
            try {
                future.complete(task.call());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        CompletableFuture<T> future = new CompletableFuture<>();
        execute(() -> {
            try {
                task.run();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public Future<?> submit(Runnable task) {
        return submit(task, null);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
        List<Future<T>> futures = new ArrayList<>();
        for (Callable<T> task : tasks) {
            futures.add(submit(task));
        }
        return futures;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) {
        List<Future<T>> futures = new ArrayList<>();
        long end = System.nanoTime() + unit.toNanos(timeout);

        for (Callable<T> task : tasks) {
            long timeLeft = end - System.nanoTime();
            if (timeLeft <= 0) break;

            futures.add(submit(task));
        }
        return futures;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        try {
            return invokeAny(tasks, Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (TimeoutException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        List<Future<T>> futures = invokeAll(tasks, timeout, unit);
        for (Future<T> future : futures) {
            if (future.isDone()) {
                try {
                    return future.get();
                } catch (ExecutionException e) {
                    throw e;
                } finally {
                    for (Future<T> f : futures) {
                        f.cancel(true);
                    }
                }
            }
        }
        throw new TimeoutException("Timeout waiting for tasks to complete.");
    }
}
