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

package org.antublue.verifyica.engine.descriptor.execution;

import io.github.thunkware.vt.bridge.ThreadNameRunnable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.Store;
import org.antublue.verifyica.engine.VerifyicaEngineExecutionContext;
import org.antublue.verifyica.engine.common.Precondition;
import org.antublue.verifyica.engine.common.SemaphoreRunnable;
import org.antublue.verifyica.engine.common.StateMachine;
import org.antublue.verifyica.engine.context.ConcreteClassContext;
import org.antublue.verifyica.engine.descriptor.ArgumentTestDescriptor;
import org.antublue.verifyica.engine.descriptor.ClassTestDescriptor;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ExecutorSupport;
import org.antublue.verifyica.engine.support.HashSupport;
import org.junit.platform.engine.TestExecutionResult;

/** Class to implement ClassTestDescriptorExecution */
public class ClassTestDescriptorExecutionContext implements TestDescriptorExecutionContext {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ClassTestDescriptorExecutionContext.class);

    private final VerifyicaEngineExecutionContext verifyicaEngineExecutionContext;
    private final ClassTestDescriptor classTestDescriptor;
    private final Class<?> testClass;
    private final List<Method> prepareMethods;
    private final Set<ArgumentTestDescriptor> argumentTestDescriptors;
    private final List<Method> concludeMethods;
    private final ClassContext classContext;
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
     * @param verifyicaEngineExecutionContext verifyicaEngineExecutionContext
     * @param classTestDescriptor classTestDescriptor
     */
    public ClassTestDescriptorExecutionContext(
            VerifyicaEngineExecutionContext verifyicaEngineExecutionContext,
            ClassTestDescriptor classTestDescriptor) {
        Precondition.notNull(verifyicaEngineExecutionContext, "executionContext is null");
        Precondition.notNull(classTestDescriptor, "classTestDescriptor is null");

        this.verifyicaEngineExecutionContext = verifyicaEngineExecutionContext;
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
                        verifyicaEngineExecutionContext.getEngineContext(),
                        classTestDescriptor,
                        testInstanceReference);
    }

    @Override
    public TestExecutionResult test() {
        LOGGER.trace("test() %s", classTestDescriptor);

        verifyicaEngineExecutionContext
                .getEngineExecutionListener()
                .executionStarted(classTestDescriptor);

        StateMachine<State> stateMachine =
                new StateMachine<State>()
                        .onState(
                                State.START,
                                () -> {
                                    try {
                                        Object testInstance =
                                                verifyicaEngineExecutionContext
                                                        .getClassInterceptorManager()
                                                        .instantiate(testClass);

                                        testInstanceReference.set(testInstance);

                                        return StateMachine.Result.of(State.INSTANTIATE_SUCCESS);
                                    } catch (Throwable t) {
                                        return StateMachine.Result.of(State.INSTANTIATE_FAILURE, t);
                                    }
                                })
                        .onState(
                                State.INSTANTIATE_SUCCESS,
                                () -> {
                                    try {
                                        verifyicaEngineExecutionContext
                                                .getClassInterceptorManager()
                                                .prepare(classContext, prepareMethods);
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
                                                classTestDescriptor.getTestArgumentParallelism();

                                        LOGGER.trace(
                                                "argumentTestDescriptors size [%d]",
                                                argumentTestDescriptors.size());
                                        LOGGER.trace(
                                                "testArgumentParallelism [%d]",
                                                testArgumentParallelism);

                                        if (testArgumentParallelism > 1) {
                                            List<Future<?>> futures = new ArrayList<>();

                                            Semaphore semaphore =
                                                    new Semaphore(testArgumentParallelism, true);

                                            argumentTestDescriptors.forEach(
                                                    argumentTestDescriptor -> {
                                                        String threadName =
                                                                Thread.currentThread().getName();

                                                        threadName =
                                                                threadName.substring(
                                                                                0,
                                                                                threadName.indexOf(
                                                                                                "/")
                                                                                        + 1)
                                                                        + HashSupport.alphanumeric(
                                                                                6);

                                                        futures.add(
                                                                verifyicaEngineExecutionContext
                                                                        .getArgumentExecutorService()
                                                                        .submit(
                                                                                new SemaphoreRunnable(
                                                                                        semaphore,
                                                                                        new ThreadNameRunnable(
                                                                                                threadName,
                                                                                                () ->
                                                                                                        new ArgumentTestDescriptorExecutionContext(
                                                                                                                        verifyicaEngineExecutionContext,
                                                                                                                        classContext,
                                                                                                                        argumentTestDescriptor)
                                                                                                                .test()))));
                                                    });

                                            ExecutorSupport.waitForAllFutures(
                                                    futures,
                                                    verifyicaEngineExecutionContext
                                                            .getArgumentExecutorService());
                                        } else {
                                            argumentTestDescriptors.forEach(
                                                    argumentTestDescriptor ->
                                                            new ArgumentTestDescriptorExecutionContext(
                                                                            verifyicaEngineExecutionContext,
                                                                            classContext,
                                                                            argumentTestDescriptor)
                                                                    .test());
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
                                                        new ArgumentTestDescriptorExecutionContext(
                                                                        verifyicaEngineExecutionContext,
                                                                        classContext,
                                                                        argumentTestDescriptor)
                                                                .skip());

                                        verifyicaEngineExecutionContext
                                                .getEngineExecutionListener()
                                                .executionSkipped(classTestDescriptor, "Skipped");

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
                                        verifyicaEngineExecutionContext
                                                .getClassInterceptorManager()
                                                .conclude(classContext, concludeMethods);

                                        return StateMachine.Result.of(State.CONCLUDE_SUCCESS);
                                    } catch (Throwable t) {
                                        t.printStackTrace(System.err);
                                        return StateMachine.Result.of(State.CONCLUDE_FAILURE, t);
                                    }
                                })
                        .onStates(
                                StateMachine.asList(State.CONCLUDE_SUCCESS, State.CONCLUDE_FAILURE),
                                () -> {
                                    try {
                                        verifyicaEngineExecutionContext
                                                .getClassInterceptorManager()
                                                .onDestroy(classContext);
                                        return StateMachine.Result.of(State.ON_DESTROY_SUCCESS);
                                    } catch (Throwable t) {
                                        t.printStackTrace(System.err);
                                        return StateMachine.Result.of(State.ON_DESTROY_FAILURE, t);
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
                                                State.AUTO_CLOSE_STORE_FAILURE, throwables.get(0));
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

        verifyicaEngineExecutionContext
                .getEngineExecutionListener()
                .executionFinished(classTestDescriptor, testExecutionResult);

        return testExecutionResult;
    }

    @Override
    public void skip() {
        throw new IllegalStateException("Not implemented");
    }
}
