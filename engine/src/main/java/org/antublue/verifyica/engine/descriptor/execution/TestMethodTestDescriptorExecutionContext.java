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
import java.util.List;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.engine.VerifyicaEngineExecutionContext;
import org.antublue.verifyica.engine.common.Precondition;
import org.antublue.verifyica.engine.common.StateMachine;
import org.antublue.verifyica.engine.descriptor.TestMethodTestDescriptor;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.junit.platform.engine.TestExecutionResult;

/** Class to implement TestMethodTestDescriptorExecution */
public class TestMethodTestDescriptorExecutionContext implements TestDescriptorExecutionContext {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TestMethodTestDescriptorExecutionContext.class);

    private final VerifyicaEngineExecutionContext verifyicaEngineExecutionContext;
    private final ArgumentContext argumentContext;
    private final TestMethodTestDescriptor testMethodTestDescriptor;
    private final List<Method> beforeEachMethods;
    private final Method testMethod;
    private final List<Method> afterEachMethods;

    private enum State {
        START,
        BEFORE_EACH_SUCCESS,
        BEFORE_EACH_FAILURE,
        TEST_SUCCESS,
        TEST_FAILURE,
        AFTER_EACH_SUCCESS,
        AFTER_EACH_FAILURE,
        END
    }

    /**
     * Constructor
     *
     * @param verifyicaEngineExecutionContext verifyicaEngineExecutionContext
     * @param argumentContext argumentContext
     * @param testMethodTestDescriptor testMethodTestDescriptor
     */
    public TestMethodTestDescriptorExecutionContext(
            VerifyicaEngineExecutionContext verifyicaEngineExecutionContext,
            ArgumentContext argumentContext,
            TestMethodTestDescriptor testMethodTestDescriptor) {
        Precondition.notNull(verifyicaEngineExecutionContext, "executionContext is null");
        Precondition.notNull(argumentContext, "argumentContext is null");
        Precondition.notNull(testMethodTestDescriptor, "testMethodTestDescriptor is null");

        this.verifyicaEngineExecutionContext = verifyicaEngineExecutionContext;
        this.argumentContext = argumentContext;
        this.testMethodTestDescriptor = testMethodTestDescriptor;
        this.beforeEachMethods = testMethodTestDescriptor.getBeforeEachMethods();
        this.testMethod = testMethodTestDescriptor.getTestMethod();
        this.afterEachMethods = testMethodTestDescriptor.getAfterEachMethods();
    }

    @Override
    public void test() {
        LOGGER.trace("test() %s", testMethodTestDescriptor);

        verifyicaEngineExecutionContext
                .getEngineExecutionListener()
                .executionStarted(testMethodTestDescriptor);

        StateMachine<State> stateMachine =
                new StateMachine<State>()
                        .onState(
                                State.START,
                                () -> {
                                    try {
                                        verifyicaEngineExecutionContext
                                                .getClassInterceptorManager()
                                                .beforeEach(argumentContext, beforeEachMethods);
                                        return StateMachine.Result.of(State.BEFORE_EACH_SUCCESS);
                                    } catch (Throwable t) {
                                        t.printStackTrace(System.err);
                                        return StateMachine.Result.of(State.BEFORE_EACH_FAILURE, t);
                                    }
                                })
                        .onState(
                                State.BEFORE_EACH_SUCCESS,
                                () -> {
                                    try {
                                        verifyicaEngineExecutionContext
                                                .getClassInterceptorManager()
                                                .test(argumentContext, testMethod);
                                        return StateMachine.Result.of(State.TEST_SUCCESS);
                                    } catch (Throwable t) {
                                        t.printStackTrace(System.err);
                                        return StateMachine.Result.of(State.TEST_FAILURE, t);
                                    }
                                })
                        .onStates(
                                StateMachine.asList(
                                        State.BEFORE_EACH_FAILURE,
                                        State.TEST_SUCCESS,
                                        State.TEST_FAILURE),
                                () -> {
                                    try {
                                        verifyicaEngineExecutionContext
                                                .getClassInterceptorManager()
                                                .afterEach(argumentContext, afterEachMethods);
                                        return StateMachine.Result.of(State.AFTER_EACH_SUCCESS);
                                    } catch (Throwable t) {
                                        t.printStackTrace(System.err);
                                        return StateMachine.Result.of(State.AFTER_EACH_FAILURE, t);
                                    }
                                })
                        .onStates(
                                StateMachine.asList(
                                        State.AFTER_EACH_SUCCESS, State.AFTER_EACH_FAILURE),
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
                .executionFinished(testMethodTestDescriptor, testExecutionResult);
    }

    @Override
    public void skip() {
        LOGGER.trace("skip() %s", testMethodTestDescriptor);

        verifyicaEngineExecutionContext
                .getEngineExecutionListener()
                .executionStarted(testMethodTestDescriptor);

        verifyicaEngineExecutionContext
                .getEngineExecutionListener()
                .executionFinished(testMethodTestDescriptor, TestExecutionResult.aborted(null));
    }
}
