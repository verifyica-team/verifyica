/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
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
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.verifyica.api.ClassInterceptor;
import org.verifyica.api.Configuration;
import org.verifyica.api.EngineContext;
import org.verifyica.engine.common.AnsiColor;
import org.verifyica.engine.common.FairExecutorService;
import org.verifyica.engine.common.StackTracePrinter;
import org.verifyica.engine.common.Stopwatch;
import org.verifyica.engine.configuration.ConcreteConfiguration;
import org.verifyica.engine.configuration.Constants;
import org.verifyica.engine.context.ConcreteEngineContext;
import org.verifyica.engine.descriptor.ClassTestDescriptor;
import org.verifyica.engine.descriptor.TestableTestDescriptor;
import org.verifyica.engine.exception.EngineConfigurationException;
import org.verifyica.engine.exception.EngineException;
import org.verifyica.engine.exception.TestClassDefinitionException;
import org.verifyica.engine.inject.Injector;
import org.verifyica.engine.interceptor.ClassInterceptorRegistry;
import org.verifyica.engine.interceptor.EngineInterceptorRegistry;
import org.verifyica.engine.listener.ChainedEngineExecutionListener;
import org.verifyica.engine.listener.TracingEngineExecutionListener;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;
import org.verifyica.engine.resolver.EngineDiscoveryRequestResolver;
import org.verifyica.engine.support.ExecutorServiceSupport;
import org.verifyica.engine.support.HashSupport;
import org.verifyica.engine.support.ListSupport;

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

    private final List<Throwable> throwables;

    private Configuration configuration;
    private EngineExecutionListener engineExecutionListener;
    private EngineContext engineContext;
    private EngineInterceptorRegistry engineInterceptorRegistry;
    private ClassInterceptorRegistry classInterceptorRegistry;

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
    public VerifyicaTestEngine() {
        throwables = new ArrayList<>();
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
            return new EngineDescriptor(uniqueId, "Verifyica disabled under Maven Surefire");
        }

        Stopwatch stopwatch = new Stopwatch();

        LOGGER.trace("discover()");

        try {
            configuration = ConcreteConfiguration.getInstance();

            EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, DISPLAY_NAME);

            new EngineDiscoveryRequestResolver().resolveSelectors(engineDiscoveryRequest, engineDescriptor);

            LOGGER.trace(
                    "discovered [%d] test classes",
                    engineDescriptor.getChildren().size());
            LOGGER.trace(
                    "discover() elapsedTime [%d] ms", stopwatch.elapsedTime().toMillis());

            return engineDescriptor;
        } catch (TestClassDefinitionException e) {
            if (!isRunningViaVerifyicaMavenPlugin()) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
            throw e;
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

        try {
            if (LOGGER.isTraceEnabled()) {
                traceEngineDescriptor(executionRequest.getRootTestDescriptor());
            }

            ExecutorService classExecutorService =
                    ExecutorServiceSupport.newExecutorService(getEngineClassParallelism(configuration));

            ExecutorService argumentExecutorService =
                    new FairExecutorService(getEngineArgumentParallelism(configuration));

            engineExecutionListener = configureEngineExecutionListeners(executionRequest);
            engineInterceptorRegistry = new EngineInterceptorRegistry(configuration);
            classInterceptorRegistry = new ClassInterceptorRegistry(configuration);
            engineContext = new ConcreteEngineContext(configuration, staticGetVersion());

            try {
                engineInterceptorRegistry.initialize(engineContext);
                classInterceptorRegistry.initialize(engineContext);

                engineExecutionListener.executionStarted(executionRequest.getRootTestDescriptor());

                List<TestableTestDescriptor> testableTestDescriptors =
                        executionRequest.getRootTestDescriptor().getChildren().stream()
                                .filter(TestableTestDescriptor.TESTABLE_TEST_DESCRIPTOR_FILTER)
                                .map(TestableTestDescriptor.TESTABLE_TEST_DESCRIPTOR_MAPPER)
                                .collect(Collectors.toList());

                List<Future<?>> futures = new ArrayList<>();

                for (TestableTestDescriptor testableTestDescriptor : testableTestDescriptors) {
                    Class<?> testClass = ((ClassTestDescriptor) testableTestDescriptor).getTestClass();
                    List<ClassInterceptor> classInterceptors =
                            classInterceptorRegistry.getClassInterceptors(engineContext, testClass);
                    List<ClassInterceptor> classInterceptorsReversed = ListSupport.copyAndReverse(classInterceptors);

                    Injector.inject(
                            TestableTestDescriptor.ENGINE_EXECUTION_LISTENER,
                            engineExecutionListener,
                            testableTestDescriptor);
                    Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, engineContext, testableTestDescriptor);
                    Injector.inject(
                            TestableTestDescriptor.ARGUMENT_EXECUTOR_SERVICE,
                            argumentExecutorService,
                            testableTestDescriptor);
                    Injector.inject(
                            TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, testableTestDescriptor);
                    Injector.inject(
                            TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED,
                            classInterceptorsReversed,
                            testableTestDescriptor);

                    String hash = HashSupport.alphanumeric(6);
                    String threadName = hash + "/" + hash;
                    ThreadNameRunnable threadNameRunnable =
                            new ThreadNameRunnable(threadName, testableTestDescriptor::test);
                    Future<?> future = classExecutorService.submit(threadNameRunnable);
                    futures.add(future);

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // INTENTIONALLY BLANK
                    }
                }

                ExecutorServiceSupport.waitForAllFutures(futures, classExecutorService);
            } catch (Throwable t) {
                StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
                throwables.add(t);
            } finally {
                ExecutorServiceSupport.shutdownAndAwaitTermination(argumentExecutorService);
                ExecutorServiceSupport.shutdownAndAwaitTermination(classExecutorService);

                Map<String, Object> map = engineContext.getMap();

                Set<Map.Entry<String, Object>> entrySet = map.entrySet();
                for (Map.Entry<String, Object> entry : entrySet) {
                    if (entry.getValue() instanceof AutoCloseable) {
                        try {
                            ((AutoCloseable) entry.getValue()).close();
                        } catch (Throwable t) {
                            StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
                            throwables.add(t);
                        }
                    }
                }

                map.clear();
            }
        } finally {
            classInterceptorRegistry.destroy(engineContext);
            engineInterceptorRegistry.destroy(engineContext);

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

        int engineClassParallelism = Optional.ofNullable(
                        configuration.getProperties().getProperty(Constants.ENGINE_CLASS_PARALLELISM))
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

        int engineArgumentParallelism = Optional.ofNullable(
                        configuration.getProperties().getProperty(Constants.ENGINE_ARGUMENT_PARALLELISM))
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
