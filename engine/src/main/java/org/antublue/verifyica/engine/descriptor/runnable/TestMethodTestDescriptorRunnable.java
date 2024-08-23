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

package org.antublue.verifyica.engine.descriptor.runnable;

import java.lang.reflect.Method;
import java.util.List;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.engine.common.Precondition;
import org.antublue.verifyica.engine.common.StateSet;
import org.antublue.verifyica.engine.descriptor.TestMethodTestDescriptor;
import org.antublue.verifyica.engine.interceptor.ClassInterceptorRegistry;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestExecutionResult;

/** Class to implement TestMethodTestDescriptorRunnable */
public class TestMethodTestDescriptorRunnable extends AbstractTestDescriptorRunnable {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TestMethodTestDescriptorRunnable.class);

    private final ExecutionRequest executionRequest;
    private final ArgumentContext argumentContext;
    private final TestMethodTestDescriptor testMethodTestDescriptor;
    private final List<Method> beforeEachMethods;
    private final Method testMethod;
    private final List<Method> afterEachMethods;

    /**
     * Constructor
     *
     * @param executionRequest executionRequest
     * @param argumentContext argumentContext
     * @param testMethodTestDescriptor testMethodTestDescriptor
     */
    public TestMethodTestDescriptorRunnable(
            ExecutionRequest executionRequest,
            ArgumentContext argumentContext,
            TestMethodTestDescriptor testMethodTestDescriptor) {
        Precondition.notNull(executionRequest, "executionRequest is null");
        Precondition.notNull(argumentContext, "argumentContext is null");
        Precondition.notNull(testMethodTestDescriptor, "testMethodTestDescriptor is null");

        this.executionRequest = executionRequest;
        this.argumentContext = argumentContext;
        this.testMethodTestDescriptor = testMethodTestDescriptor;
        this.beforeEachMethods = testMethodTestDescriptor.getBeforeEachMethods();
        this.testMethod = testMethodTestDescriptor.getTestMethod();
        this.afterEachMethods = testMethodTestDescriptor.getAfterEachMethods();
    }

    @Override
    public void execute() {
        LOGGER.trace("execute() %s", testMethodTestDescriptor);

        executionRequest.getEngineExecutionListener().executionStarted(testMethodTestDescriptor);

        StateSet<String> stateSet = new StateSet<>();

        try {
            stateSet.setCurrentState("beforeEach");

            ClassInterceptorRegistry.getInstance().beforeEach(argumentContext, beforeEachMethods);

            stateSet.setCurrentState("beforeEach.success");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            stateSet.setCurrentState("beforeEach.failure", t);
        }

        if (stateSet.hasObservedState("beforeEach.success")) {
            try {
                stateSet.setCurrentState("test");

                ClassInterceptorRegistry.getInstance().test(argumentContext, testMethod);

                stateSet.setCurrentState("test.success");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateSet.setCurrentState("test.failure", t);
            }
        }

        try {
            stateSet.setCurrentState("afterEach");

            ClassInterceptorRegistry.getInstance().afterEach(argumentContext, afterEachMethods);

            stateSet.setCurrentState("afterEach.success");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            stateSet.setCurrentState("afterEach.failure", t);
        }

        LOGGER.trace("state tracker [%s]", stateSet);

        TestExecutionResult testExecutionResult =
                stateSet.getFirstStateEntryWithThrowable()
                        .map(stateEntry -> TestExecutionResult.failed(stateEntry.getThrowable()))
                        .orElse(TestExecutionResult.successful());

        executionRequest
                .getEngineExecutionListener()
                .executionFinished(testMethodTestDescriptor, testExecutionResult);
    }

    @Override
    public void skip() {
        LOGGER.trace("skip() %s", testMethodTestDescriptor);

        executionRequest.getEngineExecutionListener().executionStarted(testMethodTestDescriptor);

        executionRequest
                .getEngineExecutionListener()
                .executionFinished(testMethodTestDescriptor, TestExecutionResult.aborted(null));
    }
}
