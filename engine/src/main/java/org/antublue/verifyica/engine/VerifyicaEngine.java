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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import org.antublue.verifyica.api.EngineContext;
import org.antublue.verifyica.api.Store;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptorContext;
import org.antublue.verifyica.engine.common.ExecutorServiceFactory;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.context.DefaultClassContext;
import org.antublue.verifyica.engine.context.DefaultEngineContext;
import org.antublue.verifyica.engine.context.DefaultEngineInterceptorContext;
import org.antublue.verifyica.engine.descriptor.ExecutableTestDescriptor;
import org.antublue.verifyica.engine.descriptor.StatusEngineDescriptor;
import org.antublue.verifyica.engine.discovery.EngineDiscoveryRequestResolver;
import org.antublue.verifyica.engine.exception.EngineException;
import org.antublue.verifyica.engine.interceptor.internal.engine.EngineInterceptorRegistry;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.HashSupport;
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

    /** Constant */
    private static final String ENGINE_PROPERTIES_RESOURCE = "/engine.properties";

    /** Constant */
    private static final String ENGINE_PROPERTIES_VERSION_KEY = "version";

    /** Constant */
    private static final String UNKNOWN_VERSION = "unknown";

    /** Constant */
    private static final String ID = "verifyica";

    /** Constant */
    private static final String GROUP_ID = "org.antublue.verifyica";

    /** Constant */
    private static final String ARTIFACT_ID = "engine";

    /** Constant */
    private static final String VERSION = version();

    /** Constant */
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

    /**
     * Method to return the version
     *
     * @return the version
     */
    public static String staticGetVersion() {
        return VERSION;
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

        LOGGER.trace("discovering test classes and test methods ...");

        EngineContext engineContext = DefaultEngineContext.getInstance();
        EngineInterceptorContext engineInterceptorContext =
                new DefaultEngineInterceptorContext(engineContext);
        EngineDescriptor engineDescriptor = new StatusEngineDescriptor(uniqueId, getId());

        try {
            EngineInterceptorRegistry.getInstance().onInitialize(engineInterceptorContext);

            new EngineDiscoveryRequestResolver()
                    .resolveSelectors(engineDiscoveryRequest, engineDescriptor);
        } catch (EngineException e) {
            throw e;
        } catch (Throwable t) {
            throw new EngineException(t);
        }

        LOGGER.trace("discovered [%d] test classes", engineDescriptor.getChildren().size());

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

        EngineInterceptorContext engineInterceptorContext =
                new DefaultEngineInterceptorContext(engineContext);

        ExecutorService executorService = null;

        List<Future<Throwable>> futures = new ArrayList<>();

        List<Throwable> throwables = new ArrayList<>();

        try {
            EngineInterceptorRegistry.getInstance().beforeExecute(engineInterceptorContext);

            executionRequest
                    .getEngineExecutionListener()
                    .executionStarted(executionRequest.getRootTestDescriptor());

            int parallelism = getParallelism(engineContext);

            executorService = ExecutorServiceFactory.getInstance().newExecutorService(parallelism);

            final Semaphore semaphore =
                    ExecutorServiceFactory.usingVirtualThreads()
                            ? new Semaphore(parallelism)
                            : null;

            for (TestDescriptor testDescriptor :
                    executionRequest.getRootTestDescriptor().getChildren()) {
                if (testDescriptor instanceof ExecutableTestDescriptor) {
                    futures.add(
                            executorService.submit(
                                    () -> {
                                        Throwable throwable = null;

                                        Thread.currentThread()
                                                .setName(
                                                        "verifyica-"
                                                                + HashSupport.alphaNumericHash(4));

                                        try {
                                            if (semaphore != null) {
                                                semaphore.acquire();
                                            }

                                            try {
                                                ((ExecutableTestDescriptor) testDescriptor)
                                                        .execute(
                                                                executionRequest,
                                                                new DefaultClassContext(
                                                                        engineContext));
                                            } catch (Throwable t) {
                                                throwable = t;
                                                t.printStackTrace(System.err);
                                            } finally {
                                                if (semaphore != null) {
                                                    semaphore.release();
                                                }
                                            }
                                        } catch (Throwable outerT) {
                                            throwable = outerT;
                                            outerT.printStackTrace(System.err);
                                        }

                                        return throwable;
                                    }));
                }
            }

            futures.forEach(
                    future -> {
                        try {
                            Throwable throwable = future.get();
                            if (throwable != null) {
                                throwables.add(throwable);
                            }
                        } catch (Throwable t) {
                            t.printStackTrace(System.err);
                        }
                    });
        } catch (Throwable t) {
            throwables.add(t);
        } finally {
            if (executorService != null) {
                executorService.shutdown();
            }

            Store store = engineContext.getStore();
            for (Object key : store.keySet()) {
                Object value = store.get(key);
                if (value instanceof AutoCloseable) {
                    try {
                        ((AutoCloseable) value).close();
                    } catch (Throwable t) {
                        t.printStackTrace(System.err);
                        throwables.add(t);
                    }
                }
            }
            store.clear();

            try {
                EngineInterceptorRegistry.getInstance().afterExecute(engineInterceptorContext);
            } catch (Throwable t) {
                throwables.add(t);
            }
        }

        throwables.removeIf(Objects::isNull);

        TestExecutionResult testExecutionResult =
                !throwables.isEmpty()
                        ? TestExecutionResult.failed(throwables.get(0))
                        : TestExecutionResult.successful();

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
     * Method to return the version
     *
     * @return the version
     */
    private static String version() {
        String value = UNKNOWN_VERSION;

        try (InputStream inputStream =
                VerifyicaEngine.class.getResourceAsStream(ENGINE_PROPERTIES_RESOURCE)) {
            if (inputStream != null) {
                Properties properties = new Properties();
                properties.load(inputStream);
                value = properties.getProperty(ENGINE_PROPERTIES_VERSION_KEY).trim();
            }
        } catch (IOException e) {
            // INTENTIONALLY BLANK
        }

        return value;
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
