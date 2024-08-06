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

package org.antublue.verifyica.engine.descriptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import org.antublue.verifyica.api.Context;
import org.antublue.verifyica.api.Store;
import org.antublue.verifyica.engine.context.DefaultArgumentContext;
import org.antublue.verifyica.engine.context.DefaultClassContext;
import org.antublue.verifyica.engine.context.ImmutableClassContext;
import org.antublue.verifyica.engine.interceptor.ClassInterceptorRegistry;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ArgumentSupport;
import org.antublue.verifyica.engine.support.HashSupport;
import org.antublue.verifyica.engine.util.ExecutorServiceFactory;
import org.antublue.verifyica.engine.util.StateTracker;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

/** Class to implement ClassTestDescriptor */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class ClassTestDescriptor extends ExecutableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassTestDescriptor.class);

    private final Class<?> testClass;
    private final int parallelism;
    private final List<Method> prepareMethods;
    private final List<Method> concludeMethods;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param testClass testClass
     * @param prepareMethods prepareMethods
     * @param concludeMethods concludeMethods
     * @param parallelism parallelism
     */
    public ClassTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            List<Method> prepareMethods,
            List<Method> concludeMethods,
            int parallelism) {
        super(uniqueId, displayName);

        ArgumentSupport.notNull(testClass, "testClass is null");

        this.testClass = testClass;
        this.prepareMethods = prepareMethods;
        this.concludeMethods = concludeMethods;
        this.parallelism = parallelism;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(ClassSource.from(testClass));
    }

    @Override
    public Type getType() {
        return Type.CONTAINER_AND_TEST;
    }

    @Override
    public Class<?> getTestClass() {
        return testClass;
    }

    @Override
    public void execute(ExecutionRequest executionRequest, Context context) {
        LOGGER.trace("execute [%s]", this);

        getStopWatch().reset();

        DefaultClassContext defaultClassContext = (DefaultClassContext) context;

        executionRequest.getEngineExecutionListener().executionStarted(this);

        StateTracker<String> stateTracker = new StateTracker<>();

        try {
            stateTracker.put("instantiateTestInstance");
            instantiateTestInstance(defaultClassContext);
            stateTracker.put("instantiateTestInstance->SUCCESS");
        } catch (Throwable t) {
            stateTracker.put("instantiateTestInstance->FAILURE", t);
        }

        if (stateTracker.contains("instantiateTestInstance->SUCCESS")) {
            try {
                stateTracker.put("prepare");
                prepare(defaultClassContext);
                stateTracker.put("prepare->SUCCESS");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.put("prepare->FAILURE", t);
            }
        }

        if (stateTracker.contains("prepare->SUCCESS")) {
            try {
                stateTracker.put("doExecute");
                doExecute(executionRequest, defaultClassContext);
                stateTracker.put("doExecute->SUCCESS");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.put("doExecute->FAILURE", t);
            }
        }

        if (stateTracker.contains("prepare->FAILURE")) {
            try {
                stateTracker.put("doSkip");
                doSkip(executionRequest, defaultClassContext);
                stateTracker.put("doSkip->SUCCESS");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.put("doSkip->FAILURE", t);
            }
        }

        if (stateTracker.contains("prepare")) {
            try {
                stateTracker.put("conclude");
                conclude(defaultClassContext);
                stateTracker.put("conclude->SUCCESS");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.put("conclude->FAILURE", t);
            }
        }

        Object testInstance = defaultClassContext.getTestInstance();
        if (testInstance instanceof AutoCloseable) {
            try {
                stateTracker.put("argumentAutoClose(" + testClass.getName() + ")");
                ((AutoCloseable) testInstance).close();
                stateTracker.put("argumentAutoClose" + testClass.getName() + ")->SUCCESS");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.put("argumentAutoClose" + testClass.getName() + ")->FAILURE");
            }
        }

        Store store = defaultClassContext.getStore();
        for (Object key : store.keySet()) {
            Object value = store.get(key);
            if (value instanceof AutoCloseable) {
                try {
                    stateTracker.put("storeAutoClose(" + key + ")");
                    ((AutoCloseable) value).close();
                    stateTracker.put("storeAutoClose(" + key + ")->SUCCESS");
                } catch (Throwable t) {
                    t.printStackTrace(System.err);
                    stateTracker.put("storeAutoClose(" + key + ")->FAILURE");
                }
            }
        }
        store.clear();

        try {
            stateTracker.put("destroyTestInstance");
            destroyTestInstance(defaultClassContext);
            stateTracker.put("destroyTestInstance->SUCCESS");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            stateTracker.put("destroyTestInstance->FAILURE", t);
        }

        defaultClassContext.setTestInstance(null);

        if (testInstance instanceof AutoCloseable) {
            try {
                stateTracker.put("argumentAutoClose(" + testClass.getName() + ")");
                ((AutoCloseable) testInstance).close();
                stateTracker.put("argumentAutoClose" + testClass.getName() + ")->SUCCESS");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.put("argumentAutoClose" + testClass.getName() + ")->FAILURE");
            }
        }

        getStopWatch().stop();

        LOGGER.trace("state tracker [%s]", stateTracker);

        TestExecutionResult testExecutionResult =
                stateTracker
                        .getFirstStateEntryWithThrowable()
                        .map(entry -> TestExecutionResult.failed(entry.getThrowable()))
                        .orElse(TestExecutionResult.successful());

        executionRequest.getEngineExecutionListener().executionFinished(this, testExecutionResult);
    }

    @Override
    public void skip(ExecutionRequest executionRequest, Context context) {
        LOGGER.trace("skip [%s]", this);

        getStopWatch().reset();

        DefaultClassContext defaultClassContext = (DefaultClassContext) context;

        getChildren().stream()
                .map(TO_EXECUTABLE_TEST_DESCRIPTOR)
                .forEach(
                        executableTestDescriptor ->
                                executableTestDescriptor.skip(
                                        executionRequest, defaultClassContext));

        getStopWatch().stop();

        executionRequest.getEngineExecutionListener().executionSkipped(this, "Skipped");
    }

    @Override
    public String toString() {
        return "ClassTestDescriptor{"
                + "uniqueId="
                + getUniqueId()
                + ", testClass="
                + testClass
                + ", parallelism="
                + parallelism
                + ", prepareMethods="
                + prepareMethods
                + ", concludeMethods="
                + concludeMethods
                + '}';
    }

    /**
     * Method to instantiate the test instance
     *
     * @param defaultClassContext defaultClassContext
     * @throws Throwable Throwable
     */
    private void instantiateTestInstance(DefaultClassContext defaultClassContext) throws Throwable {
        LOGGER.trace("instantiateTestInstance() testClass [%s]", testClass.getName());

        Throwable throwable = null;
        Object testInstance = null;

        try {
            ClassInterceptorRegistry.getInstance()
                    .beforeInstantiate(defaultClassContext.getEngineContext(), testClass);

            testInstance =
                    testClass
                            .getDeclaredConstructor((Class<?>[]) null)
                            .newInstance((Object[]) null);

            defaultClassContext.setTestClass(testClass);
            defaultClassContext.setTestInstance(testInstance);
            defaultClassContext.setTestArgumentParallelism(parallelism);
        } catch (Throwable t) {
            throwable = t;
        }

        ClassInterceptorRegistry.getInstance()
                .afterInstantiate(
                        defaultClassContext.getEngineContext(), testClass, testInstance, throwable);
    }

    /**
     * Method to invoke all prepare methods
     *
     * @param defaultClassContext defaultClassContext
     * @throws Throwable Throwable
     */
    private void prepare(DefaultClassContext defaultClassContext) throws Throwable {
        LOGGER.trace("prepare() testClass [%s]", testClass.getName());

        ClassInterceptorRegistry.getInstance()
                .prepare(ImmutableClassContext.wrap(defaultClassContext), prepareMethods);
    }

    /**
     * Method to execute all child test descriptors
     *
     * @param executionRequest executionRequest
     * @param defaultClassContext defaultClassContext
     */
    private void doExecute(
            ExecutionRequest executionRequest, DefaultClassContext defaultClassContext)
            throws Throwable {
        LOGGER.trace("doExecute() testClass [%s]", testClass.getName());

        ArgumentSupport.notNull(defaultClassContext, "defaultClassContext is null");
        ArgumentSupport.notNull(defaultClassContext.getTestInstance(), "testInstance is null");

        ExecutorService executorService = null;

        List<Throwable> throwables = new ArrayList<>();

        try {
            executorService = ExecutorServiceFactory.getInstance().newExecutorService(parallelism);

            if (parallelism > 1) {
                final Semaphore semaphore =
                        ExecutorServiceFactory.usingVirtualThreads()
                                ? new Semaphore(parallelism)
                                : null;

                final String baseThreadName = Thread.currentThread().getName();

                List<Future<Throwable>> futures = new ArrayList<>();

                for (TestDescriptor testDescriptor : getChildren()) {
                    if (testDescriptor instanceof ExecutableTestDescriptor) {
                        ExecutableTestDescriptor executableTestDescriptor =
                                (ExecutableTestDescriptor) testDescriptor;

                        DefaultArgumentContext defaultArgumentContext =
                                new DefaultArgumentContext(defaultClassContext);

                        defaultArgumentContext.setTestInstance(
                                defaultClassContext.getTestInstance());

                        futures.add(
                                executorService.submit(
                                        () -> {
                                            Throwable throwable = null;

                                            Thread.currentThread()
                                                    .setName(
                                                            baseThreadName
                                                                    + "/"
                                                                    + HashSupport.alphaNumericHash(
                                                                            4));

                                            try {
                                                if (semaphore != null) {
                                                    semaphore.acquire();
                                                }

                                                try {
                                                    executableTestDescriptor.execute(
                                                            executionRequest,
                                                            defaultArgumentContext);
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
            } else {
                // Run directly, but mimic thread naming
                String threadName = Thread.currentThread().getName();

                try {
                    for (TestDescriptor testDescriptor : getChildren()) {
                        if (testDescriptor instanceof ExecutableTestDescriptor) {
                            Thread.currentThread()
                                    .setName(threadName + "/" + HashSupport.alphaNumericHash(4));

                            ExecutableTestDescriptor executableTestDescriptor =
                                    (ExecutableTestDescriptor) testDescriptor;

                            DefaultArgumentContext defaultArgumentContext =
                                    new DefaultArgumentContext(defaultClassContext);

                            defaultArgumentContext.setTestInstance(
                                    defaultClassContext.getTestInstance());

                            executableTestDescriptor.execute(
                                    executionRequest, defaultArgumentContext);
                        }
                    }
                } catch (Throwable t) {
                    throwables.add(t);
                    t.printStackTrace(System.err);
                } finally {
                    Thread.currentThread().setName(threadName);
                }
            }
        } finally {
            if (executorService != null) {
                executorService.shutdown();
            }
        }

        throwables.removeIf(Objects::isNull);
        if (!throwables.isEmpty()) {
            throw throwables.get(0);
        }
    }

    /**
     * Method to skip child test descriptors
     *
     * @param executionRequest executionRequest
     * @param defaultClassContext defaultClassContext
     */
    private void doSkip(
            ExecutionRequest executionRequest, DefaultClassContext defaultClassContext) {
        LOGGER.trace("doSkip() testClass [%s]", testClass.getName());

        executionRequest.getEngineExecutionListener().executionSkipped(this, "Skipped");

        getChildren().stream()
                .map(TO_EXECUTABLE_TEST_DESCRIPTOR)
                .forEach(
                        executableTestDescriptor -> {
                            DefaultArgumentContext defaultArgumentContext =
                                    new DefaultArgumentContext(defaultClassContext);

                            defaultArgumentContext.setTestInstance(
                                    defaultClassContext.getTestInstance());

                            executableTestDescriptor.skip(executionRequest, defaultArgumentContext);
                        });
    }

    /**
     * Method to invoke all conclude methods
     *
     * @param defaultClassContext defaultClassContext
     * @throws Throwable Throwable
     */
    private void conclude(DefaultClassContext defaultClassContext) throws Throwable {
        LOGGER.trace("conclude() testClass [%s]", testClass.getName());

        ClassInterceptorRegistry.getInstance()
                .conclude(ImmutableClassContext.wrap(defaultClassContext), concludeMethods);
    }

    /**
     * Method to destroy the test instance
     *
     * @param defaultClassContext defaultClassContext
     * @throws Throwable Throwable
     */
    private void destroyTestInstance(DefaultClassContext defaultClassContext) throws Throwable {
        LOGGER.trace("destroyTestInstance() testClass [%s]", testClass.getName());

        ClassInterceptorRegistry.getInstance().onDestroy(defaultClassContext);
    }
}
