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

package org.antublue.verifyica.engine.execution.impl;

import static java.lang.String.format;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.antublue.verifyica.api.EngineContext;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.context.DefaultClassContext;
import org.antublue.verifyica.engine.context.DefaultEngineContext;
import org.antublue.verifyica.engine.descriptor.ToExecutableTestDescriptor;
import org.antublue.verifyica.engine.exception.EngineException;
import org.antublue.verifyica.engine.execution.ExecutionRequestExecutor;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.util.BlockingRejectedExecutionHandler;
import org.antublue.verifyica.engine.util.NamedThreadFactory;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

/** Class to implement PlatformThreadsExecutionRequestExecutor */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class PlatformThreadsExecutionRequestExecutor implements ExecutionRequestExecutor {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(PlatformThreadsExecutionRequestExecutor.class);

    private static final int MAX_THREAD_COUNT =
            Math.max(1, Runtime.getRuntime().availableProcessors() - 2);

    private final CountDownLatch countDownLatch;

    /** Constructor */
    public PlatformThreadsExecutionRequestExecutor() {
        countDownLatch = new CountDownLatch(1);
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        try {
            LOGGER.trace(
                    "execute() children [%d]",
                    executionRequest.getRootTestDescriptor().getChildren().size());

            EngineExecutionListener engineExecutionListener =
                    executionRequest.getEngineExecutionListener();

            TestDescriptor rootTestDescriptor = executionRequest.getRootTestDescriptor();

            ExecutorService executorService = null;
            AtomicReference<CountDownLatch> countDownLatch = new AtomicReference<>();
            EngineContext engineContext = DefaultEngineContext.getInstance();

            try {
                ConfigurationParameters configurationParameters =
                        executionRequest.getConfigurationParameters();

                int threadCount =
                        configurationParameters
                                .get(Constants.ENGINE_PARALLELISM)
                                .map(
                                        value -> {
                                            int intValue;
                                            try {
                                                intValue = Integer.parseInt(value);
                                                if (intValue < 1) {
                                                    throw new EngineException(
                                                            format(
                                                                    "Invalid thread count [%d]",
                                                                    intValue));
                                                }
                                                return intValue;
                                            } catch (NumberFormatException e) {
                                                throw new EngineException(
                                                        format("Invalid thread count [%s]", value),
                                                        e);
                                            }
                                        })
                                .orElse(MAX_THREAD_COUNT);

                LOGGER.trace("%s = [%d]", Constants.ENGINE_PARALLELISM, threadCount);

                executorService =
                        new ThreadPoolExecutor(
                                threadCount,
                                threadCount,
                                60L,
                                TimeUnit.SECONDS,
                                new ArrayBlockingQueue<>(threadCount * 10),
                                new NamedThreadFactory("verifyica-%02d"),
                                new BlockingRejectedExecutionHandler());

                executionRequest.getRootTestDescriptor();

                Set<? extends TestDescriptor> testDescriptors = rootTestDescriptor.getChildren();

                LOGGER.trace("test descriptor count [%d]", testDescriptors.size());

                countDownLatch.set(new CountDownLatch(testDescriptors.size()));

                final ExecutorService finalExecutorService = executorService;

                testDescriptors.stream()
                        .map(ToExecutableTestDescriptor.INSTANCE)
                        .forEach(
                                executableTestDescriptor ->
                                        finalExecutorService.submit(
                                                () -> {
                                                    try {
                                                        executableTestDescriptor.execute(
                                                                executionRequest,
                                                                new DefaultClassContext(
                                                                        engineContext));
                                                    } catch (Throwable t) {
                                                        t.printStackTrace(System.err);
                                                    } finally {
                                                        countDownLatch.get().countDown();
                                                    }
                                                }));
            } finally {
                try {
                    countDownLatch.get().await();
                } catch (InterruptedException e) {
                    // INTENTIONALLY BLANK
                }

                engineContext.getStore().clear();

                if (executorService != null) {
                    executorService.shutdown();
                }
            }

            engineExecutionListener.executionFinished(
                    rootTestDescriptor, TestExecutionResult.successful());
        } finally {
            countDownLatch.countDown();
        }
    }

    @Override
    public void await() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            // INTENTIONALLY BLANK
        }
    }
}
