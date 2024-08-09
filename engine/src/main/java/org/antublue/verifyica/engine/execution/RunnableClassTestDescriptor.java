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
    protected void execute() {
        LOGGER.trace("execute() %s", classTestDescriptor);

        executionRequest.getEngineExecutionListener().executionStarted(classTestDescriptor);

        StateTracker<String> stateTracker = new StateTracker<>();

        try {
            stateTracker.put("instantiateTestInstance");

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

            stateTracker.put("instantiateTestInstance->SUCCESS");
        } catch (Throwable t) {
            stateTracker.put("instantiateTestInstance->FAILURE", t);
        }

        if (stateTracker.contains("instantiateTestInstance->SUCCESS")) {
            try {
                stateTracker.put("prepare");

                ClassInterceptorRegistry.getInstance()
                        .prepare(ImmutableClassContext.wrap(classInstanceContext), prepareMethods);

                stateTracker.put("prepare->SUCCESS");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.put("prepare->FAILURE", t);
            }
        }

        if (stateTracker.contains("prepare->SUCCESS")) {
            try {
                stateTracker.put("doExecute");

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

                stateTracker.put("doExecute->SUCCESS");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.put("doExecute->FAILURE", t);
            }
        }

        if (stateTracker.contains("prepare->FAILURE")) {
            try {
                stateTracker.put("doSkip");

                argumentTestDescriptors.forEach(
                        argumentTestDescriptor -> {
                            // argumentTestDescriptor.skip(executionRequest, );
                        });

                executionRequest
                        .getEngineExecutionListener()
                        .executionSkipped(classTestDescriptor, "Skipped");

                stateTracker.put("doSkip->SUCCESS");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.put("doSkip->FAILURE", t);
            }
        }

        if (stateTracker.contains("prepare")) {
            try {
                stateTracker.put("conclude");

                ClassInterceptorRegistry.getInstance()
                        .conclude(
                                ImmutableClassContext.wrap(classInstanceContext), concludeMethods);

                stateTracker.put("conclude->SUCCESS");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.put("conclude->FAILURE", t);
            }
        }

        if (testInstance instanceof AutoCloseable) {
            try {
                stateTracker.put("classAutoClose(" + testClass.getName() + ")");

                ((AutoCloseable) testInstance).close();

                stateTracker.put("classAutoClose(" + testClass.getName() + ")->SUCCESS");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.put("classAutoClose(" + testClass.getName() + ")->FAILURE");
            }
        }

        Store store = classContext.getStore();
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

            try {
                ClassInterceptorRegistry.getInstance().onDestroy(classInstanceContext);
            } finally {
                testInstance = null;
            }

            stateTracker.put("destroyTestInstance->SUCCESS");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            stateTracker.put("destroyTestInstance->FAILURE", t);
        }

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

        LOGGER.trace("state tracker %s [%s]", classTestDescriptor, stateTracker);

        TestExecutionResult testExecutionResult =
                stateTracker
                        .getFirstStateEntryWithThrowable()
                        .map(entry -> TestExecutionResult.failed(entry.getThrowable()))
                        .orElse(TestExecutionResult.successful());

        executionRequest
                .getEngineExecutionListener()
                .executionFinished(classTestDescriptor, testExecutionResult);
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
