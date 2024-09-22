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

import io.github.thunkware.vt.bridge.ThreadNameRunnable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.EngineContext;
import org.antublue.verifyica.api.Store;
import org.antublue.verifyica.engine.common.Precondition;
import org.antublue.verifyica.engine.common.SemaphoreRunnable;
import org.antublue.verifyica.engine.common.StateMachine;
import org.antublue.verifyica.engine.context.ConcreteClassContext;
import org.antublue.verifyica.engine.interceptor.ClassInterceptorManager;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ExecutorSupport;
import org.antublue.verifyica.engine.support.HashSupport;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;

/** Class to implement ClassTestDescriptor */
public class ClassTestDescriptor extends AbstractTestDescriptor implements InvocableTestDescriptor {

    private final int testArgumentParallelism;
    private final Class<?> testClass;
    private final List<Method> prepareMethods;
    private final List<Method> concludeMethods;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param testClass testClass
     * @param testArgumentParallelism testArgumentParallelism
     * @param prepareMethods prepareMethods
     * @param concludeMethods concludeMethods
     */
    public ClassTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            int testArgumentParallelism,
            List<Method> prepareMethods,
            List<Method> concludeMethods) {
        super(uniqueId, displayName);

        Precondition.notNull(testClass, "testClass is null");
        Precondition.notBlank(displayName, "displayName is null", "displayName is blank");
        Precondition.isTrue(testArgumentParallelism >= 1, "testArgumentParallelism is less than 1");
        Precondition.notNull(prepareMethods, "prepareMethods is null");
        Precondition.notNull(concludeMethods, "concludeMethods is null");

        this.testArgumentParallelism = testArgumentParallelism;
        this.testClass = testClass;
        this.prepareMethods = prepareMethods;
        this.concludeMethods = concludeMethods;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(ClassSource.from(testClass));
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    /**
     * Method to get test argument parallelism
     *
     * @return test argument parallelism
     */
    public int getTestArgumentParallelism() {
        return testArgumentParallelism;
    }

    /**
     * Method to get the test class
     *
     * @return the test class
     */
    public Class<?> getTestClass() {
        return testClass;
    }

    /**
     * Method to get a List of prepare Methods
     *
     * @return a List of prepare methods
     */
    public List<Method> getPrepareMethods() {
        return prepareMethods;
    }

    /**
     * Method to get a List of conclude Methods
     *
     * @return a List of conclude methods
     */
    public List<Method> getConcludeMethods() {
        return concludeMethods;
    }

    @Override
    public String toString() {
        return "ClassTestDescriptor{"
                + "uniqueId="
                + getUniqueId()
                + ", displayName="
                + getDisplayName()
                + ", testClass="
                + testClass
                + ", parallelism="
                + testArgumentParallelism
                + ", prepareMethods="
                + prepareMethods
                + ", concludeMethods="
                + concludeMethods
                + '}';
    }

    @Override
    public TestExecutionResult testInvocation(InvocationContext invocationContext) {
        Precondition.notNull(invocationContext, "engineExecutionContext is null");

        return new Executor(invocationContext, this).test();
    }

    @Override
    public void skipInvocation(InvocationContext invocationContext) {
        Precondition.notNull(invocationContext, "engineExecutionContext is null");

        new Executor(invocationContext, this).skip();
    }

    /** Class to implement Executor */
    public static class Executor {

        private static final Logger LOGGER = LoggerFactory.getLogger(Executor.class);

        private final InvocationContext invocationContext;
        private final ClassTestDescriptor classTestDescriptor;
        private final Class<?> testClass;
        private final List<Method> prepareMethods;
        private final Set<ArgumentTestDescriptor> argumentTestDescriptors;
        private final List<Method> concludeMethods;
        private final ClassContext classContext;
        private final ClassInterceptorManager classInterceptorManager;
        private final ExecutorService argumentExecutorService;
        private final EngineExecutionListener engineExecutionListener;
        private final AtomicReference<Object> testInstanceReference;

        private enum State {
            START,
            INSTANTIATE_SUCCESS,
            INSTANTIATE_FAILURE,
            PREPARE_SUCCESS,
            PREPARE_FAILURE,
            EXECUTE_SUCCESS,
            EXECUTE_FAILURE,
            SKIP_SUCCESS,
            SKIP_FAILURE,
            CONCLUDE_SUCCESS,
            CONCLUDE_FAILURE,
            ON_DESTROY_SUCCESS,
            ON_DESTROY_FAILURE,
            AUTO_CLOSE_INSTANCE_SUCCESS,
            AUTO_CLOSE_INSTANCE_FAILURE,
            AUTO_CLOSE_STORE_SUCCESS,
            AUTO_CLOSE_STORE_FAILURE,
            END
        }

        /**
         * Constructor
         *
         * @param invocationContext invocationContext
         * @param classTestDescriptor classTestDescriptor
         */
        private Executor(
                InvocationContext invocationContext, ClassTestDescriptor classTestDescriptor) {
            this.invocationContext = invocationContext;

            this.classTestDescriptor = classTestDescriptor;

            this.testClass = classTestDescriptor.getTestClass();

            this.prepareMethods = classTestDescriptor.getPrepareMethods();

            this.argumentTestDescriptors =
                    classTestDescriptor.getChildren().stream()
                            .map(ArgumentTestDescriptor.class::cast)
                            .collect(
                                    Collectors.toCollection(
                                            (Supplier<Set<ArgumentTestDescriptor>>)
                                                    LinkedHashSet::new));

            this.concludeMethods = classTestDescriptor.getConcludeMethods();

            this.testInstanceReference = new AtomicReference<>();

            this.classContext =
                    new ConcreteClassContext(
                            invocationContext.get(EngineContext.class),
                            classTestDescriptor,
                            testInstanceReference);

            invocationContext.set(ClassContext.class, classContext);

            this.classInterceptorManager = invocationContext.get(ClassInterceptorManager.class);

            this.argumentExecutorService =
                    invocationContext.get(InvocationContext.ARGUMENT_EXECUTOR_SERVICE);

            this.engineExecutionListener = invocationContext.get(EngineExecutionListener.class);
        }

        /**
         * Method to test
         *
         * @return a TestExecutionResult
         */
        private TestExecutionResult test() {
            LOGGER.trace("test() %s", classTestDescriptor);

            engineExecutionListener.executionStarted(classTestDescriptor);

            StateMachine<State> stateMachine =
                    new StateMachine<State>()
                            .onState(
                                    State.START,
                                    () -> {
                                        try {
                                            classInterceptorManager.instantiate(
                                                    testClass, testInstanceReference);

                                            return StateMachine.Result.of(
                                                    State.INSTANTIATE_SUCCESS);
                                        } catch (Throwable t) {
                                            return StateMachine.Result.of(
                                                    State.INSTANTIATE_FAILURE, t);
                                        }
                                    })
                            .onState(
                                    State.INSTANTIATE_SUCCESS,
                                    () -> {
                                        try {
                                            classInterceptorManager.prepare(
                                                    classContext, prepareMethods);
                                            return StateMachine.Result.of(State.PREPARE_SUCCESS);
                                        } catch (Throwable t) {
                                            t.printStackTrace(System.err);
                                            return StateMachine.Result.of(State.PREPARE_FAILURE, t);
                                        }
                                    })
                            .onState(
                                    State.PREPARE_SUCCESS,
                                    () -> {
                                        try {
                                            int testArgumentParallelism =
                                                    classTestDescriptor
                                                            .getTestArgumentParallelism();

                                            LOGGER.trace(
                                                    "argumentTestDescriptors size [%d]",
                                                    argumentTestDescriptors.size());

                                            LOGGER.trace(
                                                    "testArgumentParallelism [%d]",
                                                    testArgumentParallelism);

                                            if (testArgumentParallelism > 1) {
                                                List<Future<?>> futures = new ArrayList<>();

                                                Semaphore semaphore =
                                                        new Semaphore(
                                                                testArgumentParallelism, true);

                                                argumentTestDescriptors.forEach(
                                                        argumentTestDescriptor -> {
                                                            String threadName =
                                                                    Thread.currentThread()
                                                                            .getName();

                                                            threadName =
                                                                    threadName.substring(
                                                                                    0,
                                                                                    threadName
                                                                                                    .indexOf(
                                                                                                            "/")
                                                                                            + 1)
                                                                            + HashSupport
                                                                                    .alphanumeric(
                                                                                            6);

                                                            futures.add(
                                                                    argumentExecutorService.submit(
                                                                            new SemaphoreRunnable(
                                                                                    semaphore,
                                                                                    new ThreadNameRunnable(
                                                                                            threadName,
                                                                                            () ->
                                                                                                    argumentTestDescriptor
                                                                                                            .testInvocation(
                                                                                                                    invocationContext
                                                                                                                            .copy())))));
                                                        });

                                                ExecutorSupport.waitForAllFutures(
                                                        futures, argumentExecutorService);
                                            } else {
                                                argumentTestDescriptors.forEach(
                                                        argumentTestDescriptor ->
                                                                argumentTestDescriptor
                                                                        .testInvocation(
                                                                                invocationContext));
                                            }

                                            return StateMachine.Result.of(State.EXECUTE_SUCCESS);
                                        } catch (Throwable t) {
                                            t.printStackTrace(System.err);
                                            return StateMachine.Result.of(State.EXECUTE_FAILURE, t);
                                        }
                                    })
                            .onState(
                                    State.PREPARE_FAILURE,
                                    () -> {
                                        try {
                                            argumentTestDescriptors.forEach(
                                                    argumentTestDescriptor ->
                                                            argumentTestDescriptor.skipInvocation(
                                                                    invocationContext));

                                            engineExecutionListener.executionSkipped(
                                                    classTestDescriptor, "Skipped");

                                            return StateMachine.Result.of(State.SKIP_SUCCESS);
                                        } catch (Throwable t) {
                                            t.printStackTrace(System.err);
                                            return StateMachine.Result.of(State.SKIP_FAILURE, t);
                                        }
                                    })
                            .onStates(
                                    StateMachine.asList(
                                            State.EXECUTE_SUCCESS,
                                            State.EXECUTE_FAILURE,
                                            State.SKIP_SUCCESS,
                                            State.SKIP_FAILURE),
                                    () -> {
                                        try {
                                            classInterceptorManager.conclude(
                                                    classContext, concludeMethods);

                                            return StateMachine.Result.of(State.CONCLUDE_SUCCESS);
                                        } catch (Throwable t) {
                                            t.printStackTrace(System.err);
                                            return StateMachine.Result.of(
                                                    State.CONCLUDE_FAILURE, t);
                                        }
                                    })
                            .onStates(
                                    StateMachine.asList(
                                            State.CONCLUDE_SUCCESS, State.CONCLUDE_FAILURE),
                                    () -> {
                                        try {
                                            classInterceptorManager.onDestroy(classContext);
                                            return StateMachine.Result.of(State.ON_DESTROY_SUCCESS);
                                        } catch (Throwable t) {
                                            t.printStackTrace(System.err);
                                            return StateMachine.Result.of(
                                                    State.ON_DESTROY_FAILURE, t);
                                        }
                                    })
                            .onStates(
                                    StateMachine.asList(
                                            State.ON_DESTROY_SUCCESS, State.ON_DESTROY_FAILURE),
                                    () -> {
                                        try {
                                            Object testInstance = testInstanceReference.get();
                                            if (testInstance instanceof AutoCloseable) {
                                                ((AutoCloseable) testInstance).close();
                                            }
                                            return StateMachine.Result.of(
                                                    State.AUTO_CLOSE_INSTANCE_SUCCESS);
                                        } catch (Throwable t) {
                                            t.printStackTrace(System.err);
                                            return StateMachine.Result.of(
                                                    State.AUTO_CLOSE_INSTANCE_FAILURE, t);
                                        } finally {
                                            testInstanceReference.set(null);
                                        }
                                    })
                            .onStates(
                                    StateMachine.asList(
                                            State.AUTO_CLOSE_INSTANCE_SUCCESS,
                                            State.AUTO_CLOSE_INSTANCE_FAILURE),
                                    () -> {
                                        List<Throwable> throwables = new ArrayList<>();
                                        Store store = classContext.getStore();
                                        for (Object key : store.keySet()) {
                                            Object value = store.remove(key);
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
                                        if (throwables.isEmpty()) {
                                            return StateMachine.Result.of(
                                                    State.AUTO_CLOSE_STORE_SUCCESS);
                                        } else {
                                            return StateMachine.Result.of(
                                                    State.AUTO_CLOSE_STORE_FAILURE,
                                                    throwables.get(0));
                                        }
                                    })
                            .onStates(
                                    StateMachine.asList(
                                            State.INSTANTIATE_FAILURE,
                                            State.AUTO_CLOSE_STORE_SUCCESS,
                                            State.AUTO_CLOSE_STORE_FAILURE),
                                    () -> StateMachine.Result.of(State.END))
                            .run(State.START, State.END);

            LOGGER.trace("state machine [%s]", stateMachine);

            TestExecutionResult testExecutionResult =
                    stateMachine
                            .getFirstResultWithThrowable()
                            .map(result -> TestExecutionResult.failed(result.getThrowable()))
                            .orElse(TestExecutionResult.successful());

            engineExecutionListener.executionFinished(classTestDescriptor, testExecutionResult);

            return testExecutionResult;
        }

        /** Method to skip */
        private void skip() {
            throw new IllegalStateException("Not implemented");
        }
    }
}
