/*
 * Copyright (C) Verifyica project authors and contributors. All rights reserved.
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
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import io.github.thunkware.vt.bridge.SemaphoreExecutor;
import io.github.thunkware.vt.bridge.ThreadNameRunnable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
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
import org.verifyica.api.Verifyica;
import org.verifyica.engine.common.AnsiColor;
import org.verifyica.engine.common.EphemeralExecutorService;
import org.verifyica.engine.common.HeadOfQueueRejectedExecutionHandler;
import org.verifyica.engine.common.PlatformThreadFactory;
import org.verifyica.engine.common.StackTracePrinter;
import org.verifyica.engine.common.Stopwatch;
import org.verifyica.engine.common.VirtualThreadFactory;
import org.verifyica.engine.configuration.ConcreteConfiguration;
import org.verifyica.engine.configuration.Constants;
import org.verifyica.engine.context.ConcreteEngineContext;
import org.verifyica.engine.descriptor.TestClassTestDescriptor;
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
import org.verifyica.engine.support.ClassSupport;
import org.verifyica.engine.support.ExecutorServiceSupport;
import org.verifyica.engine.support.HashSupport;
import org.verifyica.engine.support.HierarchyTraversalMode;
import org.verifyica.engine.support.ListSupport;

/**
 * Implements the Verifyica test engine.
 */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class VerifyicaTestEngine implements TestEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyicaTestEngine.class);

    /**
     * The version string used when the actual version cannot be determined.
     */
    private static final String UNKNOWN_VERSION = "unknown";

    /**
     * The display name of the test engine.
     */
    private static final String DISPLAY_NAME = "Verifyica";

    /**
     * The unique identifier of the test engine.
     */
    private static final String ID = DISPLAY_NAME.toLowerCase(Locale.ENGLISH);

    /**
     * The Maven group ID of the engine artifact.
     */
    private static final String GROUP_ID = "org.verifyica";

    /**
     * The Maven artifact ID of the engine.
     */
    private static final String ARTIFACT_ID = "engine";

    /**
     * The version of the test engine.
     */
    private static final String VERSION = version();

    /**
     * The unique ID string for the engine descriptor.
     */
    private static final String UNIQUE_ID = "[engine:" + ID + "]";

    /**
     * The resource path for engine properties.
     */
    private static final String ENGINE_PROPERTIES_RESOURCE = "/engine.properties";

    /**
     * The property key for the engine version in the properties file.
     */
    private static final String ENGINE_PROPERTIES_VERSION_KEY = "version";

    /**
     * Predicate to find an ArgumentExecutorServiceSupplier method.
     */
    private static final Predicate<Method> ARGUMENT_EXECUTOR_SERVICE_SUPPLIER_METHOD =
            new ArgumentExecutorServiceSupplierMethod();

    private final List<Throwable> throwables;

    private Configuration configuration;

    /**
     * Returns the unique ID of this test engine.
     *
     * @return the unique ID of this test engine
     */
    @Override
    public String getId() {
        return ID;
    }

    /**
     * Returns the group ID of this test engine.
     *
     * @return an Optional containing the group ID
     */
    @Override
    public Optional<String> getGroupId() {
        return of(GROUP_ID);
    }

    /**
     * Returns the artifact ID of this test engine.
     *
     * @return an Optional containing the artifact ID
     */
    @Override
    public Optional<String> getArtifactId() {
        return of(ARTIFACT_ID);
    }

    /**
     * Returns the version of this test engine.
     *
     * @return an Optional containing the version
     */
    @Override
    public Optional<String> getVersion() {
        return of(VERSION);
    }

    /**
     * Creates a new VerifyicaTestEngine instance.
     */
    public VerifyicaTestEngine() {
        throwables = new ArrayList<>();
    }

    /**
     * Returns the version.
     *
     * @return the version
     */
    public static String staticGetVersion() {
        return VERSION;
    }

    /**
     * Discovers tests based on the supplied request.
     *
     * @param engineDiscoveryRequest the engine discovery request
     * @param uniqueId the unique ID
     * @return a TestDescriptor representing the discovered tests
     */
    @Override
    public TestDescriptor discover(EngineDiscoveryRequest engineDiscoveryRequest, UniqueId uniqueId) {
        if (!UNIQUE_ID.equals(uniqueId.toString()) || isRunningViaMavenSurefirePlugin()) {
            return new EngineDescriptor(uniqueId, "Verifyica disabled under Maven Surefire");
        }

        LOGGER.trace("discover()");

        Stopwatch stopwatch = new Stopwatch();

        try {
            configuration = ConcreteConfiguration.getInstance();

            EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, DISPLAY_NAME);

            new EngineDiscoveryRequestResolver().resolveSelectors(engineDiscoveryRequest, engineDescriptor);

            LOGGER.trace(
                    "discovered [%d] test classes",
                    engineDescriptor.getChildren().size());
            LOGGER.trace("discover() elapsedTime [%d] ms", stopwatch.elapsed().toMillis());

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

    /**
     * Executes tests based on the supplied request.
     *
     * @param executionRequest the execution request
     */
    @Override
    public void execute(ExecutionRequest executionRequest) {
        if (executionRequest.getRootTestDescriptor().getChildren().isEmpty()) {
            return;
        }

        LOGGER.trace("execute()");

        Stopwatch stopwatch = new Stopwatch();
        EngineExecutionListener engineExecutionListener = null;
        EngineContext engineContext = null;
        EngineInterceptorRegistry engineInterceptorRegistry = null;
        ClassInterceptorRegistry classInterceptorRegistry = null;
        Map<Class<?>, ExecutorService> testClassArgumentExecutorServiceMap = null;

        try {
            if (LOGGER.isTraceEnabled()) {
                traceEngineDescriptor(executionRequest.getRootTestDescriptor());
            }

            ExecutorService testClassExecutorService = createTestClassExecutorService(configuration);
            ExecutorService testArgumentExecutorService = createTestArgumentExecutorService(configuration);

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

                testClassArgumentExecutorServiceMap =
                        configureTestClassArgumentExecutorServices(testableTestDescriptors);

                List<Future<?>> futures = new ArrayList<>(testableTestDescriptors.size());

                for (TestableTestDescriptor testableTestDescriptor : testableTestDescriptors) {
                    Class<?> testClass = ((TestClassTestDescriptor) testableTestDescriptor).getTestClass();

                    ExecutorService testClassArgumentExecutorService =
                            testClassArgumentExecutorServiceMap.getOrDefault(testClass, testArgumentExecutorService);

                    List<ClassInterceptor> classInterceptors =
                            classInterceptorRegistry.getClassInterceptors(engineContext, testClass);

                    List<ClassInterceptor> classInterceptorsReversed = ListSupport.copyAndReverse(classInterceptors);

                    Injector.inject(
                            TestableTestDescriptor.ENGINE_EXECUTION_LISTENER,
                            engineExecutionListener,
                            testableTestDescriptor);

                    Injector.inject(TestableTestDescriptor.ENGINE_CONTEXT, engineContext, testableTestDescriptor);

                    Injector.inject(
                            TestableTestDescriptor.TEST_ARGUMENT_EXECUTOR_SERVICE,
                            testClassArgumentExecutorService,
                            testableTestDescriptor);

                    Injector.inject(
                            TestableTestDescriptor.CLASS_INTERCEPTORS, classInterceptors, testableTestDescriptor);

                    Injector.inject(
                            TestableTestDescriptor.CLASS_INTERCEPTORS_REVERSED,
                            classInterceptorsReversed,
                            testableTestDescriptor);

                    String hash = HashSupport.alphanumeric(6);
                    String threadName = new StringBuilder(13)
                            .append(hash)
                            .append('/')
                            .append(hash)
                            .toString();
                    ThreadNameRunnable threadNameRunnable =
                            new ThreadNameRunnable(threadName, testableTestDescriptor::test);

                    futures.add(testClassExecutorService.submit(threadNameRunnable));
                }

                ExecutorServiceSupport.waitForAllFutures(futures);
            } catch (Throwable t) {
                StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
                throwables.add(t);
            } finally {
                ExecutorServiceSupport.shutdownAndAwaitTermination(testArgumentExecutorService);
                ExecutorServiceSupport.shutdownAndAwaitTermination(testClassExecutorService);

                Map<String, Object> map = engineContext.getMap();

                for (Map.Entry<String, Object> entry : map.entrySet()) {
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

                cleanupTestClassExecutorServices(testClassArgumentExecutorServiceMap);
            }
        } finally {
            if (classInterceptorRegistry != null) {
                classInterceptorRegistry.destroy(engineContext);
            }

            if (engineInterceptorRegistry != null) {
                engineInterceptorRegistry.destroy(engineContext);
            }

            TestExecutionResult testExecutionResult = throwables.isEmpty()
                    ? TestExecutionResult.successful()
                    : TestExecutionResult.failed(throwables.get(0));

            if (engineExecutionListener != null) {
                engineExecutionListener.executionFinished(
                        executionRequest.getRootTestDescriptor(), testExecutionResult);
            }

            LOGGER.trace("execute() elapsedTime [%d] ms", stopwatch.elapsed().toMillis());
        }
    }

    /**
     * Returns the version.
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
            // INTENTIONALLY EMPTY
        }

        return value;
    }

    /**
     * Configures the EngineExecutionListeners.
     *
     * @param executionRequest the execution request
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
     * Returns whether the code is running via the Verifyica Maven plugin.
     *
     * @return true if running via the Verifyica Maven plugin, otherwise false
     */
    private static boolean isRunningViaVerifyicaMavenPlugin() {
        boolean isRunningViaVerifyicaMavenPlugin = "true".equals(System.getProperty(Constants.MAVEN_PLUGIN));

        LOGGER.trace("isRunningViaVerifyicaMavenPlugin [%b]", isRunningViaVerifyicaMavenPlugin);

        return isRunningViaVerifyicaMavenPlugin;
    }

    /**
     * Returns whether the code is running via the Maven Surefire plugin.
     *
     * @return true if running via the Maven Surefire plugin, otherwise false
     */
    private static boolean isRunningViaMavenSurefirePlugin() {
        if (System.getProperty("surefire.test.class.path") != null) {
            return true;
        }

        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
            if (stackTraceElement.getClassName().startsWith("org.apache.maven.surefire")) {
                LOGGER.trace("isRunningViaMavenSurefirePlugin [true]");
                return true;
            }
        }

        LOGGER.trace("isRunningViaMavenSurefirePlugin [false]");
        return false;
    }

    /**
     * Trace logs a test descriptor tree.
     *
     * @param testDescriptor the test descriptor
     */
    private static void traceEngineDescriptor(TestDescriptor testDescriptor) {
        traceTestDescriptor(testDescriptor, 0);
    }

    /**
     * Recursively trace logs a test descriptor tree.
     *
     * @param testDescriptor the test descriptor
     * @param level the indentation level
     */
    private static void traceTestDescriptor(TestDescriptor testDescriptor, int level) {
        String testDescriptorString = testDescriptor.toString();
        int numSpaces = level > 0 ? level * 2 - 1 : 0;
        StringBuilder stringBuilder = new StringBuilder(numSpaces + testDescriptorString.length());
        for (int i = 0; i < numSpaces; i++) {
            stringBuilder.append(' ');
        }
        stringBuilder.append(testDescriptorString);
        LOGGER.trace(stringBuilder.toString());

        for (TestDescriptor childTestDescriptor : testDescriptor.getChildren()) {
            traceTestDescriptor(childTestDescriptor, level + 2);
        }
    }

    /**
     * Creates a test class ExecutorService.
     *
     * @param configuration the configuration
     * @return a test class ExecutorService
     */
    private static ExecutorService createTestClassExecutorService(Configuration configuration) {
        LOGGER.trace("createTestClassExecutorService()");

        String engineThreadType =
                configuration.getProperties().getProperty(Constants.ENGINE_THREAD_TYPE, Constants.VIRTUAL);

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int engineTestClassParallelism = getEngineTestClassParallelism(configuration);

        ThreadFactory threadFactory = createThreadFactory(configuration);

        if (Constants.PLATFORM_EPHEMERAL.equals(engineThreadType.trim())) {
            LOGGER.trace("creating EphemeralExecutorService");
            return new SemaphoreExecutor(
                    new EphemeralExecutorService(threadFactory), new Semaphore(engineTestClassParallelism));
        } else {
            LOGGER.trace("creating ThreadPoolExecutor");

            BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(availableProcessors * 2, true);

            HeadOfQueueRejectedExecutionHandler headOfQueueRejectedExecutionHandler =
                    new HeadOfQueueRejectedExecutionHandler();

            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                    engineTestClassParallelism,
                    engineTestClassParallelism,
                    Long.MAX_VALUE,
                    TimeUnit.MILLISECONDS,
                    blockingQueue,
                    threadFactory,
                    headOfQueueRejectedExecutionHandler);

            threadPoolExecutor.prestartAllCoreThreads();

            return threadPoolExecutor;
        }
    }

    /**
     * Creates a test argument ExecutorService.
     *
     * @param configuration the configuration
     * @return a test argument ExecutorService
     */
    private static ExecutorService createTestArgumentExecutorService(Configuration configuration) {
        LOGGER.trace("createTestArgumentExecutorService()");

        String engineThreadType =
                configuration.getProperties().getProperty(Constants.ENGINE_THREAD_TYPE, Constants.VIRTUAL);

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int engineTestArgumentParallelism = getEngineTestArgumentParallelism(configuration);

        ThreadFactory threadFactory = createThreadFactory(configuration);

        if (Constants.PLATFORM_EPHEMERAL.equals(engineThreadType.trim())) {
            LOGGER.trace("creating EphemeralExecutorService");

            return new SemaphoreExecutor(
                    new EphemeralExecutorService(threadFactory), new Semaphore(engineTestArgumentParallelism));
        } else {
            LOGGER.trace("creating ThreadPoolExecutor");

            BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(availableProcessors * 2, true);

            HeadOfQueueRejectedExecutionHandler headOfQueueRejectedExecutionHandler =
                    new HeadOfQueueRejectedExecutionHandler();

            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                    engineTestArgumentParallelism,
                    engineTestArgumentParallelism,
                    Long.MAX_VALUE,
                    TimeUnit.MILLISECONDS,
                    blockingQueue,
                    threadFactory,
                    headOfQueueRejectedExecutionHandler);

            threadPoolExecutor.prestartAllCoreThreads();

            return threadPoolExecutor;
        }
    }

    /**
     * Creates a ThreadFactory.
     *
     * @param configuration the configuration
     * @return a ThreadFactory
     */
    private static ThreadFactory createThreadFactory(Configuration configuration) {
        LOGGER.trace("createThreadFactory()");

        String engineThreadType =
                configuration.getProperties().getProperty(Constants.ENGINE_THREAD_TYPE, Constants.VIRTUAL);
        LOGGER.trace("engineThreadType [%s]", engineThreadType);

        if (Constants.PLATFORM.equals(engineThreadType.trim())
                || Constants.PLATFORM_EPHEMERAL.equals(engineThreadType.trim())) {
            LOGGER.trace("creating PlatformThreadFactory");
            return new PlatformThreadFactory();
        }

        LOGGER.trace("creating VirtualThreadFactory");

        return new VirtualThreadFactory();
    }

    /**
     * Returns the engine test class parallelism configuration value.
     *
     * @param configuration the configuration
     * @return the engine test class parallelism value
     */
    private static int getEngineTestClassParallelism(Configuration configuration) {
        LOGGER.trace("getEngineTestClassParallelism()");

        int engineTestClassParallelism = ofNullable(
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

        engineTestClassParallelism = Math.max(1, engineTestClassParallelism);

        LOGGER.trace("engineTestClassParallelism [%d]", engineTestClassParallelism);

        return engineTestClassParallelism;
    }

    /**
     * Returns the engine test argument parallelism configuration value.
     *
     * @param configuration the configuration
     * @return the engine test argument parallelism value
     */
    private static int getEngineTestArgumentParallelism(Configuration configuration) {
        LOGGER.trace("getEngineTestArgumentParallelism()");

        int engineTestClassParallelism = getEngineTestClassParallelism(configuration);

        int engineTestArgumentParallelism = ofNullable(
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
                .orElse(engineTestClassParallelism);

        if (engineTestArgumentParallelism < engineTestClassParallelism) {
            LOGGER.warn(
                    "[%s] is less than [%s], setting [%s] to [%d]",
                    Constants.ENGINE_ARGUMENT_PARALLELISM,
                    Constants.ENGINE_CLASS_PARALLELISM,
                    Constants.ENGINE_ARGUMENT_PARALLELISM,
                    engineTestClassParallelism);

            engineTestArgumentParallelism = engineTestClassParallelism;
        }

        engineTestArgumentParallelism = Math.max(1, engineTestArgumentParallelism);

        LOGGER.trace("engineTestArgumentParallelism [%d]", engineTestArgumentParallelism);

        return engineTestArgumentParallelism;
    }

    /**
     * Configures test class argument executor services.
     *
     * @param testableTestDescriptors the testable test descriptors
     * @return a map of test class to ExecutorService
     * @throws IllegalAccessException if the method is not accessible
     * @throws InvocationTargetException if the method invocation fails
     */
    private static Map<Class<?>, ExecutorService> configureTestClassArgumentExecutorServices(
            List<TestableTestDescriptor> testableTestDescriptors)
            throws IllegalAccessException, InvocationTargetException {
        LOGGER.trace("configureTestClassArgumentExecutorServices()");

        Map<Class<?>, ExecutorService> testClassArgumentExecutorServiceMap =
                new HashMap<>((int) (testableTestDescriptors.size() / 0.75f) + 1);

        String annotationDisplayName = "@Verifyica." + ArgumentExecutorServiceSupplierMethod.class.getSimpleName();

        for (TestableTestDescriptor testableTestDescriptor : testableTestDescriptors) {
            Class<?> testClass = ((TestClassTestDescriptor) testableTestDescriptor).getTestClass();

            List<Method> argumentExecutorServiceSupplierMethods = ClassSupport.findMethods(
                    testClass, ARGUMENT_EXECUTOR_SERVICE_SUPPLIER_METHOD, HierarchyTraversalMode.BOTTOM_UP);

            if (!argumentExecutorServiceSupplierMethods.isEmpty()) {
                if (argumentExecutorServiceSupplierMethods.size() > 1) {
                    throw new TestClassDefinitionException(format(
                            "Test class [%s] contains more than one method annotated with [%s]",
                            testClass.getName(), annotationDisplayName));
                }

                Method argumentExecutorServiceSupplierMethod = argumentExecutorServiceSupplierMethods.get(0);

                Object object = argumentExecutorServiceSupplierMethod.invoke(null, (Object[]) null);

                if (object == null) {
                    throw new TestClassDefinitionException(format(
                            "Test class [%s] %s annotated method returned null",
                            testClass.getName(), annotationDisplayName));
                }

                if (!(object instanceof ExecutorService)) {
                    throw new TestClassDefinitionException(format(
                            "Test class [%s] %s annotated method must return an ExecutorService. Return type [%s]",
                            testClass.getName(),
                            annotationDisplayName,
                            object.getClass().getName()));
                }

                LOGGER.trace("testClass [%s] using custom argument ExecutorService", testClass.getName());

                testClassArgumentExecutorServiceMap.put(testClass, (ExecutorService) object);
            }
        }

        LOGGER.trace("testClassArgumentExecutorServiceMap.size() [%d]", testClassArgumentExecutorServiceMap.size());

        return testClassArgumentExecutorServiceMap;
    }

    /**
     * Cleans up test class argument executor services.
     *
     * @param testClassArgumentExecutorServiceMap the test class argument executor service map
     */
    private void cleanupTestClassExecutorServices(Map<Class<?>, ExecutorService> testClassArgumentExecutorServiceMap) {
        LOGGER.trace("cleanupTestClassExecutorServices()");

        if (testClassArgumentExecutorServiceMap == null) {
            return;
        }

        for (Map.Entry<Class<?>, ExecutorService> entry : testClassArgumentExecutorServiceMap.entrySet()) {
            String className = entry.getKey().getName();
            LOGGER.trace("shutting down testClass [%s] executor service", className);

            try {
                ExecutorService executorService = entry.getValue();
                executorService.shutdownNow();
            } catch (Throwable t) {
                LOGGER.error("Exception shutting down test class [%s] executor service", className);
            }
        }

        testClassArgumentExecutorServiceMap.clear();
    }

    /**
     * Predicate to find an ArgumentExecutorServiceSupplier method.
     */
    private static class ArgumentExecutorServiceSupplierMethod implements Predicate<Method> {

        @Override
        public boolean test(Method method) {
            int modifiers = method.getModifiers();
            return Modifier.isPublic(modifiers)
                    && Modifier.isStatic(modifiers)
                    && method.getParameterCount() == 0
                    && !method.getReturnType().equals(Void.TYPE)
                    && method.isAnnotationPresent(Verifyica.ArgumentExecutorServiceSupplier.class);
        }
    }
}
