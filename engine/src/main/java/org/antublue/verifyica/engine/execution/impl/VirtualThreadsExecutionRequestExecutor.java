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

import io.github.thunkware.vt.bridge.ThreadTool;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.antublue.verifyica.api.EngineContext;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.context.ConcreteClassContext;
import org.antublue.verifyica.engine.context.ConcreteEngineContext;
import org.antublue.verifyica.engine.descriptor.ExecutableTestDescriptor;
import org.antublue.verifyica.engine.exception.EngineException;
import org.antublue.verifyica.engine.execution.ExecutionRequestExecutor;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

/** Class to implement VirtualThreadsExecutionRequestExecutor */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class VirtualThreadsExecutionRequestExecutor implements ExecutionRequestExecutor {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(VirtualThreadsExecutionRequestExecutor.class);

    private static final int MAX_THREAD_COUNT = Integer.MAX_VALUE;

    private final CountDownLatch countDownLatch;

    /** Constructor */
    public VirtualThreadsExecutionRequestExecutor() {
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

            AtomicReference<CountDownLatch> countDownLatch = new AtomicReference<>();
            EngineContext engineContext = ConcreteEngineContext.getInstance();

            try {
                ConfigurationParameters configurationParameters =
                        executionRequest.getConfigurationParameters();

                int threadCount =
                        configurationParameters
                                .get(Constants.PARALLELISM)
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

                LOGGER.trace("%s = [%d]", Constants.PARALLELISM, threadCount);

                engineExecutionListener.executionStarted(executionRequest.getRootTestDescriptor());

                Set<? extends TestDescriptor> testDescriptors = rootTestDescriptor.getChildren();

                LOGGER.trace("test descriptor count [%d]", testDescriptors.size());

                countDownLatch.set(new CountDownLatch(testDescriptors.size()));
                Semaphore semaphore = new Semaphore(threadCount);
                AtomicInteger threadId = new AtomicInteger(1);

                for (TestDescriptor testDescriptor : testDescriptors) {
                    if (testDescriptor instanceof ExecutableTestDescriptor) {
                        ExecutableTestDescriptor executableTestDescriptor =
                                (ExecutableTestDescriptor) testDescriptor;

                        try {
                            semaphore.acquire();

                            Thread thread =
                                    ThreadTool.unstartedVirtualThread(
                                            () -> {
                                                try {
                                                    executableTestDescriptor.execute(
                                                            executionRequest,
                                                            new ConcreteClassContext(
                                                                    engineContext));
                                                } catch (Throwable t) {
                                                    t.printStackTrace(System.err);
                                                } finally {
                                                    countDownLatch.get().countDown();
                                                    threadId.decrementAndGet();
                                                    semaphore.release();
                                                }
                                            });
                            thread.setName(format("verifyica-%02d", threadId.getAndIncrement()));
                            thread.start();
                        } catch (InterruptedException e) {
                            // DO NOTHING
                        }
                    }
                }
            } finally {
                try {
                    countDownLatch.get().await();
                } catch (InterruptedException e) {
                    // DO NOTHING
                }

                engineContext.getStore().clear();
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
            // DO NOTHING
        }
    }
}
