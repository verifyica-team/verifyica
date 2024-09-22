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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.antublue.verifyica.api.Configuration;
import org.antublue.verifyica.api.EngineContext;
import org.antublue.verifyica.api.Store;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptorContext;
import org.antublue.verifyica.engine.common.FairExecutorService;
import org.antublue.verifyica.engine.common.Stopwatch;
import org.antublue.verifyica.engine.common.Streams;
import org.antublue.verifyica.engine.configuration.ConcreteConfiguration;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.context.ConcreteEngineContext;
import org.antublue.verifyica.engine.context.ConcreteEngineInterceptorContext;
import org.antublue.verifyica.engine.descriptor.ArgumentTestDescriptor;
import org.antublue.verifyica.engine.descriptor.ClassTestDescriptor;
import org.antublue.verifyica.engine.descriptor.InvocationContext;
import org.antublue.verifyica.engine.descriptor.StatusEngineDescriptor;
import org.antublue.verifyica.engine.descriptor.TestMethodTestDescriptor;
import org.antublue.verifyica.engine.exception.EngineConfigurationException;
import org.antublue.verifyica.engine.exception.EngineException;
import org.antublue.verifyica.engine.interceptor.ClassInterceptorManager;
import org.antublue.verifyica.engine.interceptor.EngineInterceptorManager;
import org.antublue.verifyica.engine.listener.ChainedEngineExecutionListener;
import org.antublue.verifyica.engine.listener.TracingEngineExecutionListener;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.resolver.EngineDiscoveryRequestResolver;
import org.antublue.verifyica.engine.support.ExecutorSupport;
import org.antublue.verifyica.engine.support.HashSupport;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

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
    private static final String GROUP_ID = "org.antublue.verifyica";

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

    /** Predicate to filter TestDescriptors */
    private static final Predicate<TestDescriptor> CLASS_TEST_DESCRIPTOR =
            testDescriptor -> testDescriptor instanceof ClassTestDescriptor;

    /** Function to map a TestDescriptor to a ClassTestDescriptor */
    private static final Function<TestDescriptor, ClassTestDescriptor> MAP_CLASS_TEST_DESCRIPTOR =
            testDescriptor -> (ClassTestDescriptor) testDescriptor;

    private InvocationContext invocationContext;

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

    private void initialize() {
        Configuration configuration = ConcreteConfiguration.getInstance();

        EngineInterceptorManager engineInterceptorManager = new EngineInterceptorManager();

        EngineContext engineContext = new ConcreteEngineContext(configuration, staticGetVersion());

        EngineInterceptorContext engineInterceptorContext =
                new ConcreteEngineInterceptorContext(engineContext);

        ClassInterceptorManager classInterceptorManager =
                new ClassInterceptorManager(engineInterceptorContext);

        ExecutorService classExecutorService =
                ExecutorSupport.newExecutorService(getEngineClassParallelism(configuration));

        ExecutorService argumentExecutorService =
                new FairExecutorService(getEngineArgumentParallelism(configuration));

        invocationContext =
                new InvocationContext()
                        .set(Configuration.class, configuration)
                        .set(EngineContext.class, engineContext)
                        .set(EngineInterceptorContext.class, engineInterceptorContext)
                        .set(EngineInterceptorManager.class, engineInterceptorManager)
                        .set(ClassInterceptorManager.class, classInterceptorManager)
                        .set(InvocationContext.CLASS_EXECUTOR_SERVICE, classExecutorService)
                        .set(InvocationContext.ARGUMENT_EXECUTOR_SERVICE, argumentExecutorService);
    }

    @Override
    public TestDescriptor discover(
            EngineDiscoveryRequest engineDiscoveryRequest, UniqueId uniqueId) {
        if (!UNIQUE_ID.equals(uniqueId.toString()) || isRunningViaMavenSurefirePlugin()) {
            return new EngineDescriptor(uniqueId, "Verifyica disabled under Maven Surefire");
        }

        Stopwatch stopWatch = new Stopwatch();

        LOGGER.trace("discover()");

        EngineDescriptor engineDescriptor = new StatusEngineDescriptor(uniqueId, DISPLAY_NAME);

        try {
            initialize();

            new EngineDiscoveryRequestResolver(invocationContext)
                    .resolveSelectors(engineDiscoveryRequest, engineDescriptor);
        } catch (EngineException e) {
            throw e;
        } catch (Throwable t) {
            throw new EngineException(t);
        }

        LOGGER.trace("discovered [%d] test classes", engineDescriptor.getChildren().size());
        LOGGER.trace("discover() elapsedTime [%d] ms", stopWatch.elapsedTime().toMillis());

        return engineDescriptor;
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        if (executionRequest.getRootTestDescriptor().getChildren().isEmpty()) {
            return;
        }

        if (invocationContext == null) {
            throw new IllegalStateException("Not initialized");
        }

        Stopwatch stopWatch = new Stopwatch();

        LOGGER.trace("execute()");

        invocationContext.set(
                EngineExecutionListener.class, configureEngineExecutionListeners(executionRequest));

        ExecutorService classExecutorService =
                invocationContext.get(InvocationContext.CLASS_EXECUTOR_SERVICE);

        ExecutorService argumentExecutorService =
                invocationContext.get(InvocationContext.ARGUMENT_EXECUTOR_SERVICE);

        EngineContext engineContext = invocationContext.get(EngineContext.class);

        EngineInterceptorContext engineInterceptorContext =
                invocationContext.get(EngineInterceptorContext.class);

        EngineInterceptorManager engineInterceptorManager =
                invocationContext.get(EngineInterceptorManager.class);

        EngineExecutionListener engineExecutionListener =
                invocationContext.get(EngineExecutionListener.class);

        List<Throwable> throwables = new ArrayList<>();

        try {
            if (LOGGER.isTraceEnabled()) {
                traceEngineDescriptor(executionRequest.getRootTestDescriptor());
            }

            try {
                engineInterceptorManager.preExecute(engineInterceptorContext);

                executionRequest
                        .getEngineExecutionListener()
                        .executionStarted(executionRequest.getRootTestDescriptor());

                List<ClassTestDescriptor> classTestDescriptors =
                        executionRequest.getRootTestDescriptor().getChildren().stream()
                                .filter(CLASS_TEST_DESCRIPTOR)
                                .map(MAP_CLASS_TEST_DESCRIPTOR)
                                .collect(Collectors.toList());

                List<ArgumentTestDescriptor> argumentTestDescriptors = new ArrayList<>();
                List<TestMethodTestDescriptor> testMethodTestDescriptors = new ArrayList<>();

                for (TestDescriptor testDescriptor :
                        executionRequest.getRootTestDescriptor().getChildren()) {
                    for (TestDescriptor testDescriptor1 : testDescriptor.getChildren()) {
                        argumentTestDescriptors.add((ArgumentTestDescriptor) testDescriptor1);
                        for (TestDescriptor testDescriptor2 : testDescriptor1.getChildren()) {
                            testMethodTestDescriptors.add(
                                    (TestMethodTestDescriptor) testDescriptor2);
                        }
                    }
                }

                LOGGER.trace("classTestDescriptors [%d]", classTestDescriptors.size());
                LOGGER.trace("argumentTestDescriptors [%d]", argumentTestDescriptors.size());
                LOGGER.trace("testMethodTestDescriptors [%d]", testMethodTestDescriptors.size());

                List<Future<?>> futures = new ArrayList<>(classTestDescriptors.size());

                classTestDescriptors.forEach(
                        classTestDescriptor -> {
                            String threadName = HashSupport.alphanumeric(6);
                            threadName += "/" + threadName;

                            futures.add(
                                    classExecutorService.submit(
                                            new ThreadNameRunnable(
                                                    threadName,
                                                    () ->
                                                            classTestDescriptor.testInvocation(
                                                                    invocationContext.copy()))));
                        });

                ExecutorSupport.waitForAllFutures(futures, classExecutorService);
            } catch (Throwable t) {
                t.printStackTrace(System.err);
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
                            t.printStackTrace(System.err);
                            throwables.add(t);
                        }
                    }
                }
                store.clear();

                try {
                    engineInterceptorManager.postExecute(engineInterceptorContext);
                } catch (Throwable t) {
                    t.printStackTrace(System.err);
                    throwables.add(t);
                }
            }
        } finally {
            engineInterceptorManager.onDestroy(engineInterceptorContext);

            TestExecutionResult testExecutionResult =
                    throwables.isEmpty()
                            ? TestExecutionResult.successful()
                            : TestExecutionResult.failed(throwables.get(0));

            engineExecutionListener.executionFinished(
                    executionRequest.getRootTestDescriptor(), testExecutionResult);

            LOGGER.trace("execute() elapsedTime [%d] ms", stopWatch.elapsedTime().toMillis());
        }
    }

    /**
     * Method to return the version
     *
     * @return the version
     */
    private static String version() {
        String value = UNKNOWN_VERSION;

        try (InputStream inputStream =
                VerifyicaTestEngine.class.getResourceAsStream(ENGINE_PROPERTIES_RESOURCE)) {
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
    private static EngineExecutionListener configureEngineExecutionListeners(
            ExecutionRequest executionRequest) {
        LOGGER.trace("configureEngineExecutionListeners()");

        if (isRunningViaVerifyicaMavenPlugin()) {
            return executionRequest.getEngineExecutionListener();
        }

        return new ChainedEngineExecutionListener(
                new TracingEngineExecutionListener(),
                executionRequest.getEngineExecutionListener());
    }

    /**
     * Method to return whether the code is running via the Verifyica Maven plugin
     *
     * @return true if running via the Verifyica Maven plugin, else false
     */
    private static boolean isRunningViaVerifyicaMavenPlugin() {
        boolean isRunningViaVerifyicaMavenPlugin =
                "true".equals(System.getProperty(Constants.MAVEN_PLUGIN));

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

        boolean isRunningViaMavenSurefirePlugin =
                Arrays.stream(Thread.currentThread().getStackTrace())
                        .anyMatch(
                                stackTraceElement ->
                                        stackTraceElement
                                                .getClassName()
                                                .startsWith("org.apache.maven.surefire"));

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

        testDescriptor
                .getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                childTestDescriptor ->
                                        traceTestDescriptor(childTestDescriptor, level + 2));
    }

    /**
     * Method to get the engine class parallelism configuration value
     *
     * @return the engine parallelism value
     */
    private static int getEngineClassParallelism(Configuration configuration) {
        LOGGER.trace("getEngineClassParallelism()");

        int engineClassParallelism =
                configuration
                        .getOptional(Constants.ENGINE_CLASS_PARALLELISM)
                        .map(
                                value -> {
                                    int intValue;
                                    try {
                                        intValue = Integer.parseInt(value);
                                        if (intValue < 1) {
                                            throw new EngineConfigurationException(
                                                    format(
                                                            "Invalid %s value [%d]",
                                                            Constants.ENGINE_CLASS_PARALLELISM,
                                                            intValue));
                                        }
                                        return intValue;
                                    } catch (NumberFormatException e) {
                                        throw new EngineConfigurationException(
                                                format(
                                                        "Invalid %s value [%s]",
                                                        Constants.ENGINE_CLASS_PARALLELISM, value),
                                                e);
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

        int engineArgumentParallelism =
                configuration
                        .getOptional(Constants.ENGINE_ARGUMENT_PARALLELISM)
                        .map(
                                value -> {
                                    int intValue;
                                    try {
                                        intValue = Integer.parseInt(value);
                                        if (intValue < 1) {
                                            throw new EngineConfigurationException(
                                                    format(
                                                            "Invalid %s value [%d]",
                                                            Constants.ENGINE_ARGUMENT_PARALLELISM,
                                                            intValue));
                                        }
                                        return intValue;
                                    } catch (NumberFormatException e) {
                                        throw new EngineConfigurationException(
                                                format(
                                                        "Invalid %s value [%s]",
                                                        Constants.ENGINE_ARGUMENT_PARALLELISM,
                                                        value),
                                                e);
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
