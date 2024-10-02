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

package org.verifyica.engine.support;

import io.github.thunkware.vt.bridge.ExecutorTool;
import io.github.thunkware.vt.bridge.SemaphoreExecutor;
import io.github.thunkware.vt.bridge.ThreadTool;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.verifyica.engine.common.NewPlatformThreadExecutorService;
import org.verifyica.engine.common.Precondition;
import org.verifyica.engine.configuration.ConcreteConfiguration;
import org.verifyica.engine.configuration.Constants;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;

/** Class to implement ExecutorSupport */
public class ExecutorSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorSupport.class);

    private static final String PLATFORM = "platform";
    private static final String EPHEMERAL = "ephemeral";

    /** Constructor */
    private ExecutorSupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to create a new ExecutorService
     *
     * @param parallelism parallelism
     * @return an ExecutorService
     */
    public static ExecutorService newExecutorService(int parallelism) {
        Precondition.isTrue(parallelism > 0, "parallelism is less than 1");

        LOGGER.trace("newExecutorService() parallelism [%d]", parallelism);

        boolean usePlatformThreads = PLATFORM.equals(
                ConcreteConfiguration.getInstance().getProperties().getProperty(Constants.ENGINE_THREADS));

        ExecutorService executorService;

        if (!usePlatformThreads && ThreadTool.hasVirtualThreads()) {
            LOGGER.trace("using virtual threads");

            executorService = new SemaphoreExecutor(
                    ExecutorTool.newVirtualThreadPerTaskExecutor(), new Semaphore(parallelism, true));
        } else {
            if (EPHEMERAL.equals(ConcreteConfiguration.getInstance()
                    .getProperties()
                    .getProperty(Constants.ENGINE_THREADS_PLATFORM))) {
                LOGGER.trace("using ephemeral platform threads");

                executorService =
                        new SemaphoreExecutor(new NewPlatformThreadExecutorService(), new Semaphore(parallelism, true));
            } else {
                LOGGER.trace("using platform thread pool");

                executorService = new ThreadPoolExecutor(
                        parallelism,
                        parallelism,
                        60L,
                        TimeUnit.SECONDS,
                        new ArrayBlockingQueue<>(parallelism * 10),
                        new BlockingRejectedExecutionHandler());
            }
        }

        return executorService;
    }

    /**
     * Method to wait for all Futures to complete
     *
     * @param futures futures
     * @param executorService executorService
     */
    public static void waitForAllFutures(Collection<Future<?>> futures, ExecutorService executorService) {
        Precondition.notNull(futures, "futures is null");
        Precondition.notNull(executorService, "executorService is null");

        LOGGER.trace("waitForAllFutures() futures [%d]", futures.size());

        CompletionService<Object> completionService = new ExecutorCompletionService<>(executorService);
        Map<Future<?>, Future<?>> futureMap = new HashMap<>();

        for (Future<?> future : futures) {
            futureMap.put(completionService.submit(future::get), future);
        }

        for (int i = 0; i < futures.size(); i++) {
            try {
                Future<Object> completedFuture = completionService.take();
                futureMap.get(completedFuture);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.printf("Error waiting for future [%s]%n", e.getMessage());
            }
        }
    }

    /**
     * Method to shutdown an ExecutorService wait for termination
     *
     * @param executorService executorService
     */
    public static void shutdownAndAwaitTermination(ExecutorService executorService) {
        Precondition.notNull(executorService, "executorService is null");

        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    LOGGER.error("ExecutorService did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /** Class to implement BlockingRejectedExecutionHandler */
    private static class BlockingRejectedExecutionHandler implements RejectedExecutionHandler {

        private static final Logger LOGGER = LoggerFactory.getLogger(BlockingRejectedExecutionHandler.class);

        /** Constructor */
        public BlockingRejectedExecutionHandler() {
            // INTENTIONALLY BLANK
        }

        @Override
        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
            Precondition.notNull(runnable, "runnable is null");
            Precondition.notNull(executor, "executor is null");

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
