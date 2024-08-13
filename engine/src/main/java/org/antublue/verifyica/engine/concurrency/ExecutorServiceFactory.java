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

package org.antublue.verifyica.engine.concurrency;

import io.github.thunkware.vt.bridge.ExecutorTool;
import io.github.thunkware.vt.bridge.ThreadTool;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.antublue.verifyica.engine.exception.EngineException;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ArgumentSupport;

/** Class to implement ExecutorServiceFactory */
public class ExecutorServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorServiceFactory.class);

    /** Constructor */
    private ExecutorServiceFactory() {
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

        LOGGER.trace("newExecutorService() threads [%d]", threads);

        ExecutorService executorService;

        if (ThreadTool.hasVirtualThreads()) {
            LOGGER.trace("using virtual threads");

            executorService = ExecutorTool.newVirtualThreadPerTaskExecutor();
        } else {
            LOGGER.trace("using platform threads");

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
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
