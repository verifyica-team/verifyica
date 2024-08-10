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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.antublue.verifyica.api.EngineContext;
import org.antublue.verifyica.api.Store;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptorContext;
import org.antublue.verifyica.engine.common.SynchronizedPrintStream;
import org.antublue.verifyica.engine.common.ThrowableCollector;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.configuration.DefaultConfiguration;
import org.antublue.verifyica.engine.context.DefaultEngineContext;
import org.antublue.verifyica.engine.context.DefaultEngineInterceptorContext;
import org.antublue.verifyica.engine.descriptor.ClassTestDescriptor;
import org.antublue.verifyica.engine.descriptor.StatusEngineDescriptor;
import org.antublue.verifyica.engine.descriptor.execution.NamedRunnable;
import org.antublue.verifyica.engine.descriptor.execution.RunnableClassTestDescriptor;
import org.antublue.verifyica.engine.discovery.EngineDiscoveryRequestResolver;
import org.antublue.verifyica.engine.exception.EngineException;
import org.antublue.verifyica.engine.interceptor.internal.engine.EngineInterceptorRegistry;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ExecutorServiceSupport;
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

    static {
        PrintStream systemOut = System.out;
        if (!(systemOut instanceof SynchronizedPrintStream)) {
            System.setOut(new SynchronizedPrintStream(systemOut));
        }

        PrintStream systemErr = System.err;
        if (!(systemErr instanceof SynchronizedPrintStream)) {
            System.setErr(new SynchronizedPrintStream(systemErr));
        }
    }

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

        ExecutorService executorService =
                ExecutorServiceSupport.createExecutorService(getEngineClassParallelism());

        ThrowableCollector throwableCollector = new ThrowableCollector();

        EngineInterceptorContext engineInterceptorContext =
                new DefaultEngineInterceptorContext(engineContext);

        try {
            EngineInterceptorRegistry.getInstance().beforeExecute(engineInterceptorContext);

            executionRequest
                    .getEngineExecutionListener()
                    .executionStarted(executionRequest.getRootTestDescriptor());

            List<ClassTestDescriptor> classTestDescriptors =
                    getClassTestDescriptors(executionRequest);

            LOGGER.trace("classTestDescriptors size [%d]", classTestDescriptors.size());

            List<Future<?>> futures = new ArrayList<>();

            classTestDescriptors.forEach(
                    classTestDescriptor ->
                            futures.add(
                                    executorService.submit(
                                            NamedRunnable.wrap(
                                                    new RunnableClassTestDescriptor(
                                                            executionRequest,
                                                            engineContext,
                                                            classTestDescriptor),
                                                    "verifyica/" + HashSupport.alphanumeric(4)))));

            ExecutorServiceSupport.waitForAll(futures);
        } catch (Throwable t) {
            throwableCollector.add(t);
        } finally {
            executorService.shutdown();

            Store store = engineContext.getStore();
            for (Object key : store.keySet()) {
                Object value = store.get(key);
                if (value instanceof AutoCloseable) {
                    try {
                        ((AutoCloseable) value).close();
                    } catch (Throwable t) {
                        t.printStackTrace(System.err);
                        throwableCollector.add(t);
                    }
                }
            }
            store.clear();

            try {
                EngineInterceptorRegistry.getInstance().afterExecute(engineInterceptorContext);
            } catch (Throwable t) {
                throwableCollector.add(t);
            }
        }

        TestExecutionResult testExecutionResult = throwableCollector.toTestExecutionResult();

        executionRequest
                .getEngineExecutionListener()
                .executionFinished(executionRequest.getRootTestDescriptor(), testExecutionResult);

        LOGGER.trace("execution done");
    }

    private static List<ClassTestDescriptor> getClassTestDescriptors(
            ExecutionRequest executionRequest) {
        return executionRequest.getRootTestDescriptor().getChildren().stream()
                .filter(
                        (Predicate<TestDescriptor>)
                                testDescriptor -> testDescriptor instanceof ClassTestDescriptor)
                .map(
                        (Function<TestDescriptor, ClassTestDescriptor>)
                                testDescriptor -> (ClassTestDescriptor) testDescriptor)
                .collect(Collectors.toList());
    }

    /**
     * Method to get the engine class parallelism value
     *
     * @return the engine parallelism value
     */
    private static int getEngineClassParallelism() {
        int engineParallelism =
                DefaultConfiguration.getInstance()
                        .getOptional(Constants.ENGINE_CLASS_PARALLELISM)
                        .map(
                                value -> {
                                    int intValue;
                                    try {
                                        intValue = Integer.parseInt(value);
                                        if (intValue < 1) {
                                            throw new EngineException(
                                                    format(
                                                            "Invalid %s value [%d]",
                                                            Constants.ENGINE_CLASS_PARALLELISM,
                                                            intValue));
                                        }
                                        return intValue;
                                    } catch (NumberFormatException e) {
                                        throw new EngineException(
                                                format(
                                                        "Invalid %s value [%s]",
                                                        Constants.ENGINE_CLASS_PARALLELISM, value),
                                                e);
                                    }
                                })
                        .orElse(Runtime.getRuntime().availableProcessors());

        LOGGER.trace("getEngineClassParallelism() [%s]", engineParallelism);

        return engineParallelism;
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
