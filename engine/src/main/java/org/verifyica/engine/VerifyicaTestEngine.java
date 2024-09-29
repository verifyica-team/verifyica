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

package org.verifyica.engine;

import static java.lang.String.format;

import io.github.thunkware.vt.bridge.ThreadNameRunnable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.verifyica.api.Configuration;
import org.verifyica.api.EngineContext;
import org.verifyica.api.Store;
import org.verifyica.api.interceptor.EngineInterceptorContext;
import org.verifyica.engine.common.AnsiColor;
import org.verifyica.engine.common.FairExecutorService;
import org.verifyica.engine.common.StackTracePrinter;
import org.verifyica.engine.common.Stopwatch;
import org.verifyica.engine.common.Streams;
import org.verifyica.engine.configuration.ConcreteConfiguration;
import org.verifyica.engine.configuration.Constants;
import org.verifyica.engine.context.ConcreteEngineContext;
import org.verifyica.engine.context.ConcreteEngineInterceptorContext;
import org.verifyica.engine.exception.EngineConfigurationException;
import org.verifyica.engine.exception.EngineException;
import org.verifyica.engine.execution.EngineDescriptor;
import org.verifyica.engine.execution.TestableTestDescriptor;
import org.verifyica.engine.injection.FieldInjector;
import org.verifyica.engine.interceptor.ClassInterceptorRegistry;
import org.verifyica.engine.interceptor.EngineInterceptorRegistry;
import org.verifyica.engine.listener.ChainedEngineExecutionListener;
import org.verifyica.engine.listener.TracingEngineExecutionListener;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;
import org.verifyica.engine.resolver.EngineDiscoveryRequestResolver;
import org.verifyica.engine.support.ExecutorSupport;
import org.verifyica.engine.support.HashSupport;

/** Class to implement VerifyicaEngine */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class VerifyicaTestEngine implements TestEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyicaTestEngine.class);

    /** Constant */
    private static final String UNKNOWN_VERSION = "unknown";

    /** Constant */
    private static final String DISPLAY_NAME = "Verifyica";

    /** Constant */
    private static final String ID = DISPLAY_NAME.toLowerCase(Locale.ENGLISH);

    /** Constant */
    private static final String GROUP_ID = "org.verifyica";

    /** Constant */
    private static final String ARTIFACT_ID = "engine";

    /** Constant */
    private static final String VERSION = version();

    /** Constant */
    private static final String UNIQUE_ID = "[engine:" + ID + "]";

    /** Constant */
    private static final String ENGINE_PROPERTIES_RESOURCE = "/engine.properties";

    /** Constant */
    private static final String ENGINE_PROPERTIES_VERSION_KEY = "version";

    private ClassInterceptorRegistry classInterceptorRegistry;

    private EngineInterceptorRegistry engineInterceptorRegistry;

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

    static {
        Streams.fix();
    }

    /** Constructor */
    public VerifyicaTestEngine() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to get the version
     *
     * @return the version
     */
    public static String staticGetVersion() {
        return VERSION;
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest engineDiscoveryRequest, UniqueId uniqueId) {
        if (!UNIQUE_ID.equals(uniqueId.toString()) || isRunningViaMavenSurefirePlugin()) {
            return new org.junit.platform.engine.support.descriptor.EngineDescriptor(
                    uniqueId, "Verifyica disabled under Maven Surefire");
        }

        Stopwatch stopwatch = new Stopwatch();

        LOGGER.trace("discover()");

        try {
            EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, DISPLAY_NAME);

            new EngineDiscoveryRequestResolver().resolveSelectors(engineDiscoveryRequest, engineDescriptor);

            engineInterceptorRegistry = new EngineInterceptorRegistry();
            FieldInjector.injectFields(engineDescriptor, engineInterceptorRegistry);

            classInterceptorRegistry = new ClassInterceptorRegistry();
            FieldInjector.injectFields(engineDescriptor, classInterceptorRegistry);

            LOGGER.trace(
                    "discovered [%d] test classes",
                    engineDescriptor.getChildren().size());
            LOGGER.trace(
                    "discover() elapsedTime [%d] ms", stopwatch.elapsedTime().toMillis());

            return engineDescriptor;
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new EngineException(t);
        }
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        if (executionRequest.getRootTestDescriptor().getChildren().isEmpty()) {
            return;
        }

        Stopwatch stopwatch = new Stopwatch();

        LOGGER.trace("execute()");

        Configuration configuration = ConcreteConfiguration.getInstance();

        EngineContext engineContext = new ConcreteEngineContext(configuration, staticGetVersion());

        EngineInterceptorContext engineInterceptorContext = new ConcreteEngineInterceptorContext(engineContext);

        EngineExecutionListener engineExecutionListener = configureEngineExecutionListeners(executionRequest);

        ExecutorService classExecutorService =
                ExecutorSupport.newExecutorService(getEngineClassParallelism(configuration));

        ExecutorService argumentExecutorService = new FairExecutorService(getEngineArgumentParallelism(configuration));

        TestDescriptor engineDescriptor = executionRequest.getRootTestDescriptor();

        FieldInjector.injectFields(engineDescriptor, configuration);
        FieldInjector.injectFields(engineDescriptor, engineExecutionListener);
        FieldInjector.injectFields(engineDescriptor, argumentExecutorService);
        FieldInjector.injectFields(engineDescriptor, engineContext);
        FieldInjector.injectFields(engineDescriptor, engineInterceptorContext);

        List<Throwable> throwables = new ArrayList<>();

        try {

            if (LOGGER.isTraceEnabled()) {
                traceEngineDescriptor(executionRequest.getRootTestDescriptor());
            }

            try {
                engineInterceptorRegistry.initialize(engineInterceptorContext);
                classInterceptorRegistry.initialize();

                engineExecutionListener.executionStarted(executionRequest.getRootTestDescriptor());

                List<TestableTestDescriptor> executableClassTestDescriptors =
                        executionRequest.getRootTestDescriptor().getChildren().stream()
                                .filter(TestableTestDescriptor.TESTABLE_TEST_DESCRIPTOR_FILTER)
                                .map(TestableTestDescriptor.TESTABLE_TEST_DESCRIPTOR_MAPPER)
                                .collect(Collectors.toList());

                List<Future<?>> futures = new ArrayList<>(executableClassTestDescriptors.size());

                executableClassTestDescriptors.forEach(classTestDescriptor -> {
                    FieldInjector.injectFields(classTestDescriptor, argumentExecutorService);
                    String hash = HashSupport.alphanumeric(6);
                    String threadName = hash + "/" + hash;
                    ThreadNameRunnable threadNameRunnable =
                            new ThreadNameRunnable(threadName, classTestDescriptor::test);
                    Future<?> future = classExecutorService.submit(threadNameRunnable);
                    futures.add(future);
                });

                ExecutorSupport.waitForAllFutures(futures, classExecutorService);
            } catch (Throwable t) {
                StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
                throwables.add(t);
            } finally {
                ExecutorSupport.shutdownAndAwaitTermination(argumentExecutorService);
                ExecutorSupport.shutdownAndAwaitTermination(classExecutorService);

                Store store = engineContext.getStore();
                for (Object key : store.keySet()) {
                    Object value = store.remove(key);
                    if (value instanceof AutoCloseable) {
                        try {
                            LOGGER.trace("storeAutoClose(" + key + ")");
                            ((AutoCloseable) value).close();
                            LOGGER.trace("storeAutoClose(" + key + ").success");
                        } catch (Throwable t) {
                            LOGGER.trace("storeAutoClose(" + key + ").failure");
                            StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
                            throwables.add(t);
                        }
                    }
                }
                store.clear();
            }
        } finally {
            classInterceptorRegistry.destroy();
            engineInterceptorRegistry.destroy(engineInterceptorContext);

            TestExecutionResult testExecutionResult = throwables.isEmpty()
                    ? TestExecutionResult.successful()
                    : TestExecutionResult.failed(throwables.get(0));

            engineExecutionListener.executionFinished(executionRequest.getRootTestDescriptor(), testExecutionResult);

            LOGGER.trace(
                    "execute() elapsedTime [%d] ms", stopwatch.elapsedTime().toMillis());
        }
    }

    /**
     * Method to return the version
     *
     * @return the version
     */
    private static String version() {
        String value = UNKNOWN_VERSION;

        try (InputStream inputStream = VerifyicaTestEngine.class.getResourceAsStream(ENGINE_PROPERTIES_RESOURCE)) {
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
     * Method to configure EngineExecutionListeners
     *
     * @return an EngineExecutionListener
     */
    private static EngineExecutionListener configureEngineExecutionListeners(ExecutionRequest executionRequest) {
        LOGGER.trace("configureEngineExecutionListeners()");

        if (isRunningViaVerifyicaMavenPlugin()) {
            return executionRequest.getEngineExecutionListener();
        }

        return new ChainedEngineExecutionListener(
                new TracingEngineExecutionListener(), executionRequest.getEngineExecutionListener());
    }

    /**
     * Method to return whether the code is running via the Verifyica Maven plugin
     *
     * @return true if running via the Verifyica Maven plugin, else false
     */
    private static boolean isRunningViaVerifyicaMavenPlugin() {
        boolean isRunningViaVerifyicaMavenPlugin = "true".equals(System.getProperty(Constants.MAVEN_PLUGIN));

        LOGGER.trace("isRunningViaVerifyicaMavenPlugin [%b]", isRunningViaVerifyicaMavenPlugin);

        return isRunningViaVerifyicaMavenPlugin;
    }

    /**
     * Method to return whether the code is running via the Maven Surefire plugin
     *
     * @return true if running via the Maven Surefire plugin, else false
     */
    private static boolean isRunningViaMavenSurefirePlugin() {
        if (System.getProperty("surefire.test.class.path") != null) {
            return true;
        }

        boolean isRunningViaMavenSurefirePlugin = Arrays.stream(
                        Thread.currentThread().getStackTrace())
                .anyMatch(
                        stackTraceElement -> stackTraceElement.getClassName().startsWith("org.apache.maven.surefire"));

        LOGGER.trace("isRunningViaMavenSurefirePlugin [%b]", isRunningViaMavenSurefirePlugin);

        return isRunningViaMavenSurefirePlugin;
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

        testDescriptor.getChildren().forEach((Consumer<TestDescriptor>)
                childTestDescriptor -> traceTestDescriptor(childTestDescriptor, level + 2));
    }

    /**
     * Method to get the engine class parallelism configuration value
     *
     * @return the engine parallelism value
     */
    private static int getEngineClassParallelism(Configuration configuration) {
        LOGGER.trace("getEngineClassParallelism()");

        int engineClassParallelism = configuration
                .getOptional(Constants.ENGINE_CLASS_PARALLELISM)
                .map(value -> {
                    int intValue;
                    try {
                        intValue = Integer.parseInt(value);
                        if (intValue < 1) {
                            throw new EngineConfigurationException(
                                    format("Invalid %s value [%d]", Constants.ENGINE_CLASS_PARALLELISM, intValue));
                        }
                        return intValue;
                    } catch (NumberFormatException e) {
                        throw new EngineConfigurationException(
                                format("Invalid %s value [%s]", Constants.ENGINE_CLASS_PARALLELISM, value), e);
                    }
                })
                .orElse(Runtime.getRuntime().availableProcessors());

        LOGGER.trace("engineClassParallelism [%d]", engineClassParallelism);

        return engineClassParallelism;
    }

    /**
     * Method to get the engine argument parallelism configuration value
     *
     * @return the engine parallelism value
     */
    private static int getEngineArgumentParallelism(Configuration configuration) {
        LOGGER.trace("getEngineArgumentParallelism()");

        int engineClassParallelism = getEngineClassParallelism(configuration);

        int engineArgumentParallelism = configuration
                .getOptional(Constants.ENGINE_ARGUMENT_PARALLELISM)
                .map(value -> {
                    int intValue;
                    try {
                        intValue = Integer.parseInt(value);
                        if (intValue < 1) {
                            throw new EngineConfigurationException(
                                    format("Invalid %s value [%d]", Constants.ENGINE_ARGUMENT_PARALLELISM, intValue));
                        }
                        return intValue;
                    } catch (NumberFormatException e) {
                        throw new EngineConfigurationException(
                                format("Invalid %s value [%s]", Constants.ENGINE_ARGUMENT_PARALLELISM, value), e);
                    }
                })
                .orElse(engineClassParallelism);

        if (engineArgumentParallelism < engineClassParallelism) {
            LOGGER.warn(
                    "[%s] is less than [%s], setting [%s] to [%d]",
                    Constants.ENGINE_ARGUMENT_PARALLELISM,
                    Constants.ENGINE_CLASS_PARALLELISM,
                    Constants.ENGINE_ARGUMENT_PARALLELISM,
                    engineClassParallelism);

            engineArgumentParallelism = engineClassParallelism;
        }

        LOGGER.trace("engineArgumentParallelism [%d]", engineArgumentParallelism);

        return engineArgumentParallelism;
    }
}
