/*
 * Copyright (C) Verifyica project authors and contributors
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
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
 * Class to implement VerifyicaEngine
 */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class VerifyicaTestEngine implements TestEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyicaTestEngine.class);

    /**
     * Constant
     */
    private static final String UNKNOWN_VERSION = "unknown";

    /**
     * Constant
     */
    private static final String DISPLAY_NAME = "Verifyica";

    /**
     * Constant
     */
    private static final String ID = DISPLAY_NAME.toLowerCase(Locale.ENGLISH);

    /**
     * Constant
     */
    private static final String GROUP_ID = "org.verifyica";

    /**
     * Constant
     */
    private static final String ARTIFACT_ID = "engine";

    /**
     * Constant
     */
    private static final String VERSION = version();

    /**
     * Constant
     */
    private static final String UNIQUE_ID = "[engine:" + ID + "]";

    /**
     * Constant
     */
    private static final String ENGINE_PROPERTIES_RESOURCE = "/engine.properties";

    /**
     * Constant
     */
    private static final String ENGINE_PROPERTIES_VERSION_KEY = "version";

    /**
     * Predicate to find an ArgumentExecutorServiceSupplier method
     */
    private static final Predicate<Method> ARGUMENT_EXECUTOR_SERVICE_SUPPLIER_METHOD =
            new ArgumentExecutorServiceSupplierMethod();

    private final List<Throwable> throwables;

    private Configuration configuration;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Optional<String> getGroupId() {
        return of(GROUP_ID);
    }

    @Override
    public Optional<String> getArtifactId() {
        return of(ARTIFACT_ID);
    }

    @Override
    public Optional<String> getVersion() {
        return of(VERSION);
    }

    /**
     * Constructor
     */
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
        Map<Class<?>, ExecutorService> testClassArgumentExecutorServiceMap = new HashMap<>();

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

                configureTestClassArgumentExecutorServices(
                        testableTestDescriptors, testClassArgumentExecutorServiceMap);

                List<Future<?>> futures = new ArrayList<>();

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
                    String threadName = hash + "/" + hash;
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
            // INTENTIONALLY EMPTY
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
     * @return true if running via the Verifyica Maven plugin, otherwise false
     */
    private static boolean isRunningViaVerifyicaMavenPlugin() {
        boolean isRunningViaVerifyicaMavenPlugin = "true".equals(System.getProperty(Constants.MAVEN_PLUGIN));

        LOGGER.trace("isRunningViaVerifyicaMavenPlugin [%b]", isRunningViaVerifyicaMavenPlugin);

        return isRunningViaVerifyicaMavenPlugin;
    }

    /**
     * Method to return whether the code is running via the Maven Surefire plugin
     *
     * @return true if running via the Maven Surefire plugin, otherwise false
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
     * Method to create a test class ExecutorService
     *
     * @param configuration configuration
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
     * Method to create a test argument ExecutorService
     *
     * @param configuration configuration
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
     * Method to create a ThreadFactory
     *
     * @param configuration configuration
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
     * Method to get the engine test class parallelism configuration value
     *
     * @return the engine parallelism value
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
     * Method to get the engine test argument parallelism configuration value
     *
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
     * Method to configure test class argument executor services
     *
     * @param testableTestDescriptors testableTestDescriptors
     * @param testClassArgumentExecutorServiceMap testClassArgumentExecutorServiceMap
     * @throws IllegalAccessException IllegalAccessException
     * @throws InvocationTargetException InvocationTargetException
     */
    private static void configureTestClassArgumentExecutorServices(
            List<TestableTestDescriptor> testableTestDescriptors,
            Map<Class<?>, ExecutorService> testClassArgumentExecutorServiceMap)
            throws IllegalAccessException, InvocationTargetException {
        LOGGER.trace("configureTestClassArgumentExecutorServices()");

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

                LOGGER.trace("test class [%s] using custom argument ExecutorService", testClass.getName());

                testClassArgumentExecutorServiceMap.put(testClass, (ExecutorService) object);
            }
        }

        LOGGER.trace("testClassArgumentExecutorServiceMap.size() [%d]", testClassArgumentExecutorServiceMap.size());
    }

    /**
     * Method to clean up test class argument executor services
     *
     * @param testClassArgumentExecutorServiceMap testClassArgumentExecutorServiceMap
     */
    private void cleanupTestClassExecutorServices(Map<Class<?>, ExecutorService> testClassArgumentExecutorServiceMap) {
        LOGGER.trace("cleanupTestClassExecutorServices()");

        for (Map.Entry<Class<?>, ExecutorService> entry : testClassArgumentExecutorServiceMap.entrySet()) {
            LOGGER.trace(
                    "shutting down test class [%s] executor service",
                    entry.getKey().getName());

            try {
                ExecutorService executorService = entry.getValue();
                executorService.shutdownNow();
            } catch (Throwable t) {
                LOGGER.error(
                        "Exception shutting down test class [%s] executor service",
                        entry.getKey().getName());
            }
        }

        testClassArgumentExecutorServiceMap.clear();
    }

    /**
     * Predicate to find an ArgumentExecutorServiceSupplier method
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
