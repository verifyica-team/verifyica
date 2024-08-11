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

package org.antublue.verifyica.engine.support;

import io.github.thunkware.vt.bridge.ExecutorTool;
import io.github.thunkware.vt.bridge.SemaphoreExecutor;
import io.github.thunkware.vt.bridge.ThreadTool;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.antublue.verifyica.engine.exception.EngineException;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;

/** Class to implement ExecutorServiceSupport */
public class ExecutorServiceSupport {

    /** Constructor */
    private ExecutorServiceSupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to create a new ExecutorService
     *
     * @param threads threads
     * @return an ExecutorService
     */
    public static ExecutorService newExecutorService(int threads) {
        ArgumentSupport.isTrue(threads > 0, "threads is less than 1");

        ExecutorService executorService;

        if (ThreadTool.hasVirtualThreads()) {
            executorService =
                    new SemaphoreExecutor(
                            ExecutorTool.newVirtualThreadPerTaskExecutor(),
                            new Semaphore(threads, true));
        } else {
            executorService =
                    new ThreadPoolExecutor(
                            threads,
                            threads,
                            60L,
                            TimeUnit.SECONDS,
                            new ArrayBlockingQueue<>(threads * 10),
                            new BlockingRejectedExecutionHandler());
        }

        return executorService;
    }

    /**
     * Method to create an ExecutorService with a fixed number of permits
     *
     * @param executorService executorService
     * @param permits permits
     * @return an ExecutorService with a fixed number of permits
     */
    public static ExecutorService newSemaphoreExecutorService(
            ExecutorService executorService, int permits) {
        ArgumentSupport.notNull(executorService, "executorService is null");
        ArgumentSupport.isTrue(permits > 0, "permits is less than 1");

        return newSemaphoreExecutorService(executorService, new Semaphore(permits, true));
    }

    /**
     * Method to create an ExecutorService with a fixed number of permits
     *
     * @param executorService executorService
     * @param semaphore semaphore
     * @return an ExecutorService with a fixed number of permits
     */
    public static ExecutorService newSemaphoreExecutorService(
            ExecutorService executorService, Semaphore semaphore) {
        ArgumentSupport.notNull(executorService, "executorService is null");
        ArgumentSupport.notNull(semaphore, "semaphore is null");

        return new SemaphoreExecutor(executorService, semaphore);
    }

    /**
     * Method to wait on all Futures
     *
     * @param futures futures
     */
    public static void waitForAll(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Throwable t) {
                throw new EngineException("Exception waiting on future", t);
            }
        }
    }

    /** Class to implement BlockingRejectedExecutionHandler */
    private static class BlockingRejectedExecutionHandler implements RejectedExecutionHandler {

        private static final Logger LOGGER =
                LoggerFactory.getLogger(BlockingRejectedExecutionHandler.class);

        /** Constructor */
        public BlockingRejectedExecutionHandler() {
            // INTENTIONALLY BLANK
        }

        @Override
        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
            ArgumentSupport.notNull(runnable, "runnable is null");
            ArgumentSupport.notNull(executor, "executor is null");

            if (!executor.isShutdown()) {
                try {
                    executor.getQueue().put(runnable);
                } catch (InterruptedException e) {
                    LOGGER.error("Runnable discarded!!!");
                }
            }
        }
    }
}
