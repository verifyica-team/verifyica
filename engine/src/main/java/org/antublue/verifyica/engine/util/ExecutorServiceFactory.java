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

package org.antublue.verifyica.engine.util;

import io.github.thunkware.vt.bridge.ExecutorTool;
import io.github.thunkware.vt.bridge.ThreadCustomizer;
import io.github.thunkware.vt.bridge.ThreadTool;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Class to implement ExecutorServiceFactory */
public class ExecutorServiceFactory {

    /** Constructor */
    private ExecutorServiceFactory() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to create a new ExecutorService. If virtual threads are supported, threadCount is
     * ignored
     *
     * @param threadNamePrefix threadNamePrefix
     * @param threadCount threadCount
     * @return an ExecutorService
     */
    public ExecutorService newExecutorService(String threadNamePrefix, int threadCount) {
        ExecutorService executorService;

        if (ThreadTool.hasVirtualThreads()) {
            executorService =
                    ExecutorTool.newVirtualThreadPerTaskExecutor(
                            ThreadCustomizer.withNamePrefix(threadNamePrefix));
        } else {
            executorService =
                    new ThreadPoolExecutor(
                            threadCount,
                            threadCount,
                            60L,
                            TimeUnit.SECONDS,
                            new ArrayBlockingQueue<>(threadCount * 10),
                            new NamedThreadFactory(threadNamePrefix + "%d"),
                            new BlockingRejectedExecutionHandler());
        }

        return executorService;
    }

    /**
     * Method to get a singleton instance
     *
     * @return the singleton instance
     */
    public static ExecutorServiceFactory getInstance() {
        return SingletonHolder.SINGLETON;
    }

    /**
     * Method to return if using virtual threads
     *
     * @return true if using virtual threads, else false
     */
    public static boolean usingVirtualThreads() {
        return ThreadTool.hasVirtualThreads();
    }

    /** Class to hold the singleton instance */
    private static class SingletonHolder {

        /** The singleton instance */
        private static final ExecutorServiceFactory SINGLETON = new ExecutorServiceFactory();
    }

    /** Class to implement SemaphoreExecutorService */
    private static class SemaphoreExecutorService implements ExecutorService {

        private final ExecutorService executorService;
        private final Semaphore semaphore;

        /**
         * Constructor
         *
         * @param executorService executorService
         * @param permits permits
         */
        public SemaphoreExecutorService(ExecutorService executorService, int permits) {
            this.executorService = executorService;
            this.semaphore = new Semaphore(permits);
        }

        @Override
        public void shutdown() {
            executorService.shutdown();
        }

        @Override
        public List<Runnable> shutdownNow() {
            return executorService.shutdownNow();
        }

        @Override
        public boolean isShutdown() {
            return executorService.isShutdown();
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
            return executorService.submit(wrapTask(task));
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            return executorService.submit(wrapTask(task), result);
        }

        @Override
        public Future<?> submit(Runnable task) {
            return executorService.submit(wrapTask(task));
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
                throws InterruptedException {
            throw new UnsupportedOperationException("invokeAll not supported");
        }

        @Override
        public <T> List<Future<T>> invokeAll(
                Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
                throws InterruptedException {
            throw new UnsupportedOperationException("invokeAll not supported");
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
                throws InterruptedException, ExecutionException {
            throw new UnsupportedOperationException("invokeAny not supported");
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            throw new UnsupportedOperationException("invokeAny not supported");
        }

        @Override
        public void execute(Runnable command) {
            executorService.execute(wrapTask(command));
        }

        /**
         * Method to wrap a task
         *
         * @param task task
         * @return a Callback task
         * @param <T> the type
         */
        private <T> Callable<T> wrapTask(Callable<T> task) {
            return () -> {
                semaphore.acquire();
                try {
                    return task.call();
                } finally {
                    semaphore.release();
                }
            };
        }

        /**
         * Method to wrap a Runnable
         *
         * @param runnable runnable
         * @return a Runnable
         */
        private Runnable wrapTask(Runnable runnable) {
            return () -> {
                try {
                    semaphore.acquire();
                    runnable.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    semaphore.release();
                }
            };
        }
    }
}
