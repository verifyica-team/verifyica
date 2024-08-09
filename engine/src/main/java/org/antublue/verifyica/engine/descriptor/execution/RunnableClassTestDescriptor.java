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

package org.antublue.verifyica.engine.execution;

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.EngineContext;
import org.antublue.verifyica.api.Store;
import org.antublue.verifyica.engine.common.StateTracker;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.configuration.DefaultConfiguration;
import org.antublue.verifyica.engine.context.DefaultClassContext;
import org.antublue.verifyica.engine.context.DefaultClassInstanceContext;
import org.antublue.verifyica.engine.context.ImmutableClassContext;
import org.antublue.verifyica.engine.descriptor.ArgumentTestDescriptor;
import org.antublue.verifyica.engine.descriptor.ClassTestDescriptor;
import org.antublue.verifyica.engine.exception.EngineException;
import org.antublue.verifyica.engine.interceptor.ClassInterceptorRegistry;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ExecutorServiceSupport;
import org.antublue.verifyica.engine.support.HashSupport;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestExecutionResult;

/** Class to implement RunnableClassTestDescriptor */
public class RunnableClassTestDescriptor extends AbstractRunnableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunnableClassTestDescriptor.class);

    private static final ExecutorService EXECUTOR_SERVICE =
            ExecutorServiceSupport.createExecutorService(getEngineArgumentParallelism());

    private final ExecutionRequest executionRequest;
    private final ClassTestDescriptor classTestDescriptor;
    private final Class<?> testClass;
    private final List<Method> prepareMethods;
    private final List<ArgumentTestDescriptor> argumentTestDescriptors;
    private final List<Method> concludeMethods;
    private final ClassContext classContext;

    private DefaultClassInstanceContext classInstanceContext;
    private Object testInstance;

    /**
     * Constructor
     *
     * @param executionRequest executionRequest
     * @param engineContext engineContext
     * @param classTestDescriptor classTestDescriptor
     */
    public RunnableClassTestDescriptor(
            ExecutionRequest executionRequest,
            EngineContext engineContext,
            ClassTestDescriptor classTestDescriptor) {
        this.executionRequest = executionRequest;
        this.classTestDescriptor = classTestDescriptor;
        this.testClass = classTestDescriptor.getTestClass();
        this.prepareMethods = classTestDescriptor.getPrepareMethods();
        this.argumentTestDescriptors = getArgumentTestDescriptors(classTestDescriptor);
        this.concludeMethods = classTestDescriptor.getConcludeMethods();
        this.classContext = new DefaultClassContext(engineContext, classTestDescriptor);
    }

    @Override
    public void execute() {
        LOGGER.trace("execute() %s", classTestDescriptor);

        executionRequest.getEngineExecutionListener().executionStarted(classTestDescriptor);

        StateTracker<String> stateTracker = new StateTracker<>();

        try {
            stateTracker.setState("instantiate");

            Throwable throwable = null;

            try {
                ClassInterceptorRegistry.getInstance()
                        .beforeInstantiate(classContext.getEngineContext(), testClass);

                testInstance =
                        testClass
                                .getDeclaredConstructor((Class<?>[]) null)
                                .newInstance((Object[]) null);

                classInstanceContext = new DefaultClassInstanceContext(classContext, testInstance);
            } catch (Throwable t) {
                throwable = t;
            }

            ClassInterceptorRegistry.getInstance()
                    .afterInstantiate(
                            classContext.getEngineContext(), testClass, testInstance, throwable);

            stateTracker.setState("instantiate.success");
        } catch (Throwable t) {
            stateTracker.setState("instantiate.failure", t);
        }

        if (stateTracker.isState("instantiate.success")) {
            try {
                stateTracker.setState("prepare");

                ClassInterceptorRegistry.getInstance()
                        .prepare(ImmutableClassContext.wrap(classInstanceContext), prepareMethods);

                stateTracker.setState("prepare.success");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.setState("prepare.failure", t);
            }
        }

        if (stateTracker.containsState("prepare.success")) {
            try {
                stateTracker.setState("execute");

                ExecutorService executorService =
                        ExecutorServiceSupport.createSemaphoreExecutorService(
                                EXECUTOR_SERVICE, classTestDescriptor.getTestArgumentParallelism());

                List<Future<?>> futures = new ArrayList<>();

                argumentTestDescriptors.forEach(
                        argumentTestDescriptor ->
                                futures.add(
                                        executorService.submit(
                                                NamedRunnable.wrap(
                                                        new RunnableArgumentTestDescriptor(
                                                                executionRequest,
                                                                classInstanceContext,
                                                                argumentTestDescriptor),
                                                        Thread.currentThread().getName()
                                                                + "/"
                                                                + HashSupport.alphanumeric(4)))));

                ExecutorServiceSupport.waitForAll(futures);

                stateTracker.setState("execute.success");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.setState("execute.failure", t);
            }
        }

        if (stateTracker.containsState("prepare.failure")) {
            try {
                stateTracker.setState("skip");

                argumentTestDescriptors.forEach(
                        argumentTestDescriptor -> {
                            new RunnableArgumentTestDescriptor(
                                            executionRequest,
                                            classInstanceContext,
                                            argumentTestDescriptor)
                                    .skip();
                        });

                executionRequest
                        .getEngineExecutionListener()
                        .executionSkipped(classTestDescriptor, "Skipped");

                stateTracker.setState("skip.success");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.setState("skip.failure", t);
            }
        }

        if (stateTracker.containsState("prepare")) {
            try {
                stateTracker.setState("conclude");

                ClassInterceptorRegistry.getInstance()
                        .conclude(
                                ImmutableClassContext.wrap(classInstanceContext), concludeMethods);

                stateTracker.setState("conclude.success");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.setState("conclude.failure", t);
            }
        }

        if (testInstance instanceof AutoCloseable) {
            try {
                stateTracker.setState("classAutoClose(" + testClass.getName() + ")");

                ((AutoCloseable) testInstance).close();

                stateTracker.setState("classAutoClose(" + testClass.getName() + ").success");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.setState("classAutoClose(" + testClass.getName() + ").failure");
            }
        }

        Store store = classContext.getStore();
        for (Object key : store.keySet()) {
            Object value = store.get(key);
            if (value instanceof AutoCloseable) {
                try {
                    stateTracker.setState("storeAutoClose(" + key + ")");

                    ((AutoCloseable) value).close();

                    stateTracker.setState("storeAutoClose(" + key + ").success");
                } catch (Throwable t) {
                    t.printStackTrace(System.err);
                    stateTracker.setState("storeAutoClose(" + key + ").failure");
                }
            }
        }
        store.clear();

        try {
            stateTracker.setState("destroy");

            try {
                ClassInterceptorRegistry.getInstance().onDestroy(classInstanceContext);
            } finally {
                testInstance = null;
            }

            stateTracker.setState("destroy.success");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            stateTracker.setState("destroy.failure", t);
        }

        if (testInstance instanceof AutoCloseable) {
            try {
                stateTracker.setState("argumentAutoClose(" + testClass.getName() + ")");

                ((AutoCloseable) testInstance).close();

                stateTracker.setState("argumentAutoClose" + testClass.getName() + ").success");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.setState("argumentAutoClose" + testClass.getName() + ").failure");
            }
        }

        LOGGER.trace("state tracker %s [%s]", classTestDescriptor, stateTracker);

        TestExecutionResult testExecutionResult =
                stateTracker
                        .getStateWithThrowable()
                        .map(stateEntry -> TestExecutionResult.failed(stateEntry.getThrowable()))
                        .orElse(TestExecutionResult.successful());

        executionRequest
                .getEngineExecutionListener()
                .executionFinished(classTestDescriptor, testExecutionResult);
    }

    @Override
    public void skip() {
        throw new IllegalStateException("Not implemented");
    }

    /**
     * Method to get the engine argument parallelism value
     *
     * @return the engine parallelism value
     */
    private static int getEngineArgumentParallelism() {
        LOGGER.trace("getEngineArgumentParallelism()");

        int engineClassParallelism = getEngineClassParallelism();

        int engineArgumentParallelism =
                DefaultConfiguration.getInstance()
                        .getOptional(Constants.ENGINE_ARGUMENT_PARALLELISM)
                        .map(
                                value -> {
                                    int intValue;
                                    try {
                                        intValue = Integer.parseInt(value);
                                        if (intValue < 1) {
                                            throw new EngineException(
                                                    format(
                                                            "Invalid %s value [%d]",
                                                            Constants.ENGINE_ARGUMENT_PARALLELISM,
                                                            intValue));
                                        }
                                        return intValue;
                                    } catch (NumberFormatException e) {
                                        throw new EngineException(
                                                format(
                                                        "Invalid %s value [%s]",
                                                        Constants.ENGINE_ARGUMENT_PARALLELISM,
                                                        value),
                                                e);
                                    }
                                })
                        .orElse(Runtime.getRuntime().availableProcessors());

        if (engineArgumentParallelism < engineClassParallelism) {
            LOGGER.warn(
                    "[%s] is less than [%s], setting [%s] to [%d]",
                    Constants.ENGINE_ARGUMENT_PARALLELISM,
                    Constants.ENGINE_CLASS_PARALLELISM,
                    Constants.ENGINE_ARGUMENT_PARALLELISM,
                    engineClassParallelism);

            engineArgumentParallelism = engineClassParallelism;
        }

        LOGGER.trace("engineArgumentParallelism [%s]", engineArgumentParallelism);

        return engineArgumentParallelism;
    }

    /**
     * Method to get the engine class parallelism value
     *
     * @return the engine parallelism value
     */
    private static int getEngineClassParallelism() {
        LOGGER.trace("getEngineClassParallelism()");

        int engineClassParallelism =
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

        LOGGER.trace("engineClassParallelism [%s]", engineClassParallelism);

        return engineClassParallelism;
    }
}
