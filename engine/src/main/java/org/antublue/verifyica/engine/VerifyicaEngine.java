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

package org.antublue.verifyica.engine;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import org.antublue.verifyica.api.EngineContext;
import org.antublue.verifyica.api.extension.engine.EngineExtensionContext;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.context.DefaultClassContext;
import org.antublue.verifyica.engine.context.DefaultEngineContext;
import org.antublue.verifyica.engine.context.DefaultEngineExtensionContext;
import org.antublue.verifyica.engine.descriptor.ExecutableTestDescriptor;
import org.antublue.verifyica.engine.descriptor.StatusEngineDescriptor;
import org.antublue.verifyica.engine.discovery.EngineDiscoveryRequestResolver;
import org.antublue.verifyica.engine.exception.EngineException;
import org.antublue.verifyica.engine.extension.EngineExtensionRegistry;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.util.ExecutorServiceFactory;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/** Class to implement VerifyicaEngine */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class VerifyicaEngine implements TestEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyicaEngine.class);

    /** Configuration constant */
    public static final String ID = "verifyica";

    /** Configuration constant */
    private static final String GROUP_ID = "org.antublue.verifyica";

    /** Configuration constant */
    private static final String ARTIFACT_ID = "engine";

    /** Configuration constant */
    public static final String VERSION = Version.version();

    /** UniqueId constant */
    private static final String UNIQUE_ID = "[engine:" + ID + "]";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Optional<String> getGroupId() {
        return Optional.of(GROUP_ID);
    }

    @Override
    public Optional<String> getArtifactId() {
        return Optional.of(ARTIFACT_ID);
    }

    @Override
    public Optional<String> getVersion() {
        return Optional.of(VERSION);
    }

    /** Constructor */
    public VerifyicaEngine() {
        // INTENTIONALLY BLANK
    }

    @Override
    public TestDescriptor discover(
            EngineDiscoveryRequest engineDiscoveryRequest, UniqueId uniqueId) {
        if (!UNIQUE_ID.equals(uniqueId.toString())) {
            return null;
        }

        LOGGER.trace("discovering test classes and test methods");

        EngineContext engineContext = DefaultEngineContext.getInstance();
        EngineExtensionContext engineExtensionContext =
                new DefaultEngineExtensionContext(engineContext);
        EngineDescriptor engineDescriptor = new StatusEngineDescriptor(uniqueId, getId());

        try {
            EngineExtensionRegistry.getInstance().onInitialize(engineExtensionContext);
            new EngineDiscoveryRequestResolver()
                    .resolveSelectors(engineDiscoveryRequest, engineDescriptor);
        } catch (EngineException e) {
            throw e;
        } catch (Throwable t) {
            throw new EngineException(t);
        }

        LOGGER.trace("discovery done");

        return engineDescriptor;
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        if (executionRequest.getRootTestDescriptor().getChildren().isEmpty()) {
            return;
        }

        LOGGER.trace("executing test classes and test methods");

        if (LOGGER.isTraceEnabled()) {
            traceEngineDescriptor(executionRequest.getRootTestDescriptor());
        }

        EngineContext engineContext = DefaultEngineContext.getInstance();

        EngineExtensionContext engineExtensionContext =
                new DefaultEngineExtensionContext(engineContext);

        ExecutorService executorService = null;

        List<Future<?>> futures = new ArrayList<>();

        Throwable throwable = null;

        try {
            EngineExtensionRegistry.getInstance().beforeExecute(engineExtensionContext);

            executionRequest
                    .getEngineExecutionListener()
                    .executionStarted(executionRequest.getRootTestDescriptor());

            int parallelism = getParallelism(engineContext);

            executorService =
                    ExecutorServiceFactory.getInstance()
                            .newExecutorService("verifyica-", parallelism);

            Semaphore semaphore = null;
            if (ExecutorServiceFactory.usingVirtualThreads()) {
                semaphore = new Semaphore(parallelism);
            }

            final Semaphore finalSemaphore = semaphore;

            for (TestDescriptor testDescriptor :
                    executionRequest.getRootTestDescriptor().getChildren()) {
                if (testDescriptor instanceof ExecutableTestDescriptor) {
                    Future<?> future =
                            executorService.submit(
                                    () -> {
                                        if (!testDescriptor.getChildren().isEmpty()) {
                                            try {
                                                if (finalSemaphore != null) {
                                                    finalSemaphore.acquire();
                                                }
                                                ((ExecutableTestDescriptor) testDescriptor)
                                                        .execute(
                                                                executionRequest,
                                                                new DefaultClassContext(
                                                                        engineContext));
                                            } catch (Throwable t) {
                                                t.printStackTrace(System.err);
                                            } finally {
                                                if (finalSemaphore != null) {
                                                    finalSemaphore.release();
                                                }
                                            }
                                        } else {
                                            try {
                                                if (finalSemaphore != null) {
                                                    finalSemaphore.acquire();
                                                }
                                                ((ExecutableTestDescriptor) testDescriptor)
                                                        .skip(
                                                                executionRequest,
                                                                new DefaultClassContext(
                                                                        engineContext));
                                            } catch (Throwable t) {
                                                t.printStackTrace(System.err);
                                            } finally {
                                                if (finalSemaphore != null) {
                                                    finalSemaphore.release();
                                                }
                                            }
                                        }
                                    });

                    futures.add(future);
                }
            }

            futures.forEach(
                    future -> {
                        try {
                            future.get();
                        } catch (Exception e) {
                            // INTENTIONALLY BLANK
                        }
                    });
        } catch (Throwable t) {
            throwable = t;
        } finally {
            if (executorService != null) {
                executorService.shutdown();
            }

            try {
                EngineExtensionRegistry.getInstance().afterExecute(engineExtensionContext);
            } catch (Throwable t) {
                t.printStackTrace(System.err);
            }
        }

        TestExecutionResult testExecutionResult =
                throwable != null
                        ? TestExecutionResult.successful()
                        : TestExecutionResult.failed(throwable);

        executionRequest
                .getEngineExecutionListener()
                .executionFinished(executionRequest.getRootTestDescriptor(), testExecutionResult);

        LOGGER.trace("execution done");
    }

    /**
     * Method to get the engine parallelism value
     *
     * @param engineContext engineContext
     * @return the engine parallelism value
     */
    private static int getParallelism(EngineContext engineContext) {
        int maxThreadCount = Runtime.getRuntime().availableProcessors();

        return engineContext
                .getConfiguration()
                .getOptional(Constants.ENGINE_PARALLELISM)
                .map(
                        value -> {
                            int intValue;
                            try {
                                intValue = Integer.parseInt(value);
                                if (intValue < 1) {
                                    throw new EngineException(
                                            format(
                                                    "Invalid %s value [%d]",
                                                    Constants.ENGINE_PARALLELISM, intValue));
                                }
                                return intValue;
                            } catch (NumberFormatException e) {
                                throw new EngineException(
                                        format(
                                                "Invalid %s value [%s]",
                                                Constants.ENGINE_PARALLELISM, value),
                                        e);
                            }
                        })
                .orElse(maxThreadCount);
    }

    /**
     * Method trace log a test descriptor tree
     *
     * @param testDescriptor testDescriptor
     */
    private static void traceEngineDescriptor(TestDescriptor testDescriptor) {
        traceTestDescriptor(testDescriptor, 0);
    }

    /**
     * Method to recursively trace log a test descriptor tree
     *
     * @param testDescriptor testDescriptor
     * @param level level
     */
    private static void traceTestDescriptor(TestDescriptor testDescriptor, int level) {
        LOGGER.trace(String.join(" ", Collections.nCopies(level, " ")) + testDescriptor);

        testDescriptor
                .getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor1 -> traceTestDescriptor(testDescriptor1, level + 2));
    }
}
