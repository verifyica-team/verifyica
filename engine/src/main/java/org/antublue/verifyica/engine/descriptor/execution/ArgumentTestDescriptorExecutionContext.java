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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.Store;
import org.antublue.verifyica.engine.VerifyicaEngineExecutionContext;
import org.antublue.verifyica.engine.common.Precondition;
import org.antublue.verifyica.engine.common.StateMachine;
import org.antublue.verifyica.engine.context.ConcreteArgumentContext;
import org.antublue.verifyica.engine.descriptor.ArgumentTestDescriptor;
import org.antublue.verifyica.engine.descriptor.TestMethodTestDescriptor;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.junit.platform.engine.TestExecutionResult;

/** Class to implement ArgumentTestDescriptorExecution */
public class ArgumentTestDescriptorExecutionContext implements TestDescriptorExecutionContext {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ArgumentTestDescriptorExecutionContext.class);

    private final VerifyicaEngineExecutionContext verifyicaEngineExecutionContext;
    private final ArgumentTestDescriptor argumentTestDescriptor;
    private final List<Method> beforeAllMethods;
    private final Set<TestMethodTestDescriptor> testMethodTestDescriptors;
    private final List<Method> afterAllMethods;
    private final ArgumentContext argumentContext;

    private enum State {
        START,
        BEFORE_ALL_SUCCESS,
        BEFORE_ALL_FAILURE,
        EXECUTE_SUCCESS,
        EXECUTE_FAILURE,
        SKIP_SUCCESS,
        SKIP_FAILURE,
        AFTER_ALL_SUCCESS,
        AFTER_ALL_FAILURE,
        AUTO_CLOSE_ARGUMENT_SUCCESS,
        AUTO_CLOSE_ARGUMENT_FAILURE,
        AUTO_CLOSE_STORE_SUCCESS,
        AUTO_CLOSE_STORE_FAILURE,
        END
    }

    /**
     * Constructor
     *
     * @param verifyicaEngineExecutionContext verifyicaEngineExecutionContext
     * @param classContext classContext
     * @param argumentTestDescriptor argumentTestDescriptor
     */
    public ArgumentTestDescriptorExecutionContext(
            VerifyicaEngineExecutionContext verifyicaEngineExecutionContext,
            ClassContext classContext,
            ArgumentTestDescriptor argumentTestDescriptor) {
        Precondition.notNull(verifyicaEngineExecutionContext, "executionContext is null");
        Precondition.notNull(classContext, "classContext is null");
        Precondition.notNull(argumentTestDescriptor, "argumentTestDescriptor is null");

        this.verifyicaEngineExecutionContext = verifyicaEngineExecutionContext;
        this.argumentTestDescriptor = argumentTestDescriptor;
        this.beforeAllMethods = argumentTestDescriptor.getBeforeAllMethods();

        this.testMethodTestDescriptors =
                argumentTestDescriptor.getChildren().stream()
                        .map(TestMethodTestDescriptor.class::cast)
                        .collect(
                                Collectors.toCollection(
                                        (Supplier<Set<TestMethodTestDescriptor>>)
                                                LinkedHashSet::new));

        this.afterAllMethods = argumentTestDescriptor.getAfterAllMethods();
        this.argumentContext = new ConcreteArgumentContext(classContext, argumentTestDescriptor);
    }

    @Override
    public void test() {
        LOGGER.trace("test() %s", argumentTestDescriptor);

        verifyicaEngineExecutionContext
                .getEngineExecutionListener()
                .executionStarted(argumentTestDescriptor);

        StateMachine<State> stateMachine =
                new StateMachine<State>()
                        .onState(
                                State.START,
                                () -> {
                                    try {
                                        verifyicaEngineExecutionContext
                                                .getClassInterceptorManager()
                                                .beforeAll(argumentContext, beforeAllMethods);
                                        return StateMachine.Result.of(State.BEFORE_ALL_SUCCESS);
                                    } catch (Throwable t) {
                                        t.printStackTrace(System.err);
                                        return StateMachine.Result.of(State.BEFORE_ALL_FAILURE, t);
                                    }
                                })
                        .onState(
                                State.BEFORE_ALL_SUCCESS,
                                () -> {
                                    try {
                                        testMethodTestDescriptors.forEach(
                                                testMethodTestDescriptor ->
                                                        new TestMethodTestDescriptorExecutionContext(
                                                                        verifyicaEngineExecutionContext,
                                                                        argumentContext,
                                                                        testMethodTestDescriptor)
                                                                .test());
                                        return StateMachine.Result.of(State.EXECUTE_SUCCESS);
                                    } catch (Throwable t) {
                                        t.printStackTrace(System.err);
                                        return StateMachine.Result.of(State.EXECUTE_FAILURE, t);
                                    }
                                })
                        .onState(
                                State.BEFORE_ALL_FAILURE,
                                () -> {
                                    try {
                                        testMethodTestDescriptors.forEach(
                                                testMethodTestDescriptor ->
                                                        new TestMethodTestDescriptorExecutionContext(
                                                                        verifyicaEngineExecutionContext,
                                                                        argumentContext,
                                                                        testMethodTestDescriptor)
                                                                .skip());
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
                                                .afterAll(argumentContext, afterAllMethods);
                                        return StateMachine.Result.of(State.AFTER_ALL_SUCCESS);
                                    } catch (Throwable t) {
                                        t.printStackTrace(System.err);
                                        return StateMachine.Result.of(State.AFTER_ALL_FAILURE, t);
                                    }
                                })
                        .onStates(
                                StateMachine.asList(
                                        State.AFTER_ALL_SUCCESS, State.AFTER_ALL_FAILURE),
                                () -> {
                                    try {
                                        Argument<?> testArgument =
                                                argumentContext.getTestArgument();
                                        if (testArgument instanceof AutoCloseable) {
                                            ((AutoCloseable) testArgument).close();
                                        }
                                        return StateMachine.Result.of(
                                                State.AUTO_CLOSE_ARGUMENT_SUCCESS);
                                    } catch (Throwable t) {
                                        t.printStackTrace(System.err);
                                        return StateMachine.Result.of(
                                                State.AUTO_CLOSE_ARGUMENT_FAILURE, t);
                                    }
                                })
                        .onStates(
                                StateMachine.asList(
                                        State.AUTO_CLOSE_ARGUMENT_SUCCESS,
                                        State.AUTO_CLOSE_ARGUMENT_FAILURE),
                                () -> {
                                    List<Throwable> throwables = new ArrayList<>();
                                    Store store = argumentContext.getStore();
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
                .executionFinished(argumentTestDescriptor, testExecutionResult);
    }

    @Override
    public void skip() {
        LOGGER.trace("skip() %s", argumentTestDescriptor);

        verifyicaEngineExecutionContext
                .getEngineExecutionListener()
                .executionStarted(argumentTestDescriptor);

        testMethodTestDescriptors.forEach(
                testMethodTestDescriptor ->
                        new TestMethodTestDescriptorExecutionContext(
                                        verifyicaEngineExecutionContext,
                                        argumentContext,
                                        testMethodTestDescriptor)
                                .skip());

        verifyicaEngineExecutionContext
                .getEngineExecutionListener()
                .executionFinished(argumentTestDescriptor, TestExecutionResult.aborted(null));
    }
}