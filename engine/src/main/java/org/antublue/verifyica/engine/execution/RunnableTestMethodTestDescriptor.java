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

import java.lang.reflect.Method;
import java.util.List;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.engine.common.StateTracker;
import org.antublue.verifyica.engine.descriptor.TestMethodTestDescriptor;
import org.antublue.verifyica.engine.interceptor.ClassInterceptorRegistry;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestExecutionResult;

public class RunnableTestMethodTestDescriptor extends AbstractRunnableTestDescriptor {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RunnableTestMethodTestDescriptor.class);

    private final ExecutionRequest executionRequest;
    private final ArgumentContext argumentContext;
    private final TestMethodTestDescriptor testMethodTestDescriptor;
    private final List<Method> beforeEachMethods;
    private final Method testMethod;
    private final List<Method> afterEachMethods;

    public RunnableTestMethodTestDescriptor(
            ExecutionRequest executionRequest,
            ArgumentContext argumentContext,
            TestMethodTestDescriptor testMethodTestDescriptor) {
        this.executionRequest = executionRequest;
        this.argumentContext = argumentContext;
        this.testMethodTestDescriptor = testMethodTestDescriptor;
        this.beforeEachMethods = testMethodTestDescriptor.getBeforeEachMethods();
        this.testMethod = testMethodTestDescriptor.getTestMethod();
        this.afterEachMethods = testMethodTestDescriptor.getAfterEachMethods();
    }

    @Override
    protected void execute() {
        LOGGER.trace("execute() %s", testMethodTestDescriptor);

        executionRequest.getEngineExecutionListener().executionStarted(testMethodTestDescriptor);

        StateTracker<String> stateTracker = new StateTracker<>();

        try {
            stateTracker.put("beforeEach");

            ClassInterceptorRegistry.getInstance().beforeEach(argumentContext, beforeEachMethods);

            stateTracker.put("beforeEach->SUCCESS");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            stateTracker.put("beforeEach->FAILURE", t);
        }

        if (stateTracker.contains("beforeEach->SUCCESS")) {
            try {
                stateTracker.put("test");

                ClassInterceptorRegistry.getInstance().test(argumentContext, testMethod);

                stateTracker.put("test->SUCCESS");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.put("test->FAILURE", t);
            }
        }

        try {
            stateTracker.put("afterEach");

            ClassInterceptorRegistry.getInstance().afterEach(argumentContext, afterEachMethods);

            stateTracker.put("afterEach->SUCCESS");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            stateTracker.put("afterEach->FAILURE", t);
        }

        LOGGER.trace("state tracker [%s]", stateTracker);

        TestExecutionResult testExecutionResult =
                stateTracker
                        .getFirstStateEntryWithThrowable()
                        .map(entry -> TestExecutionResult.failed(entry.getThrowable()))
                        .orElse(TestExecutionResult.successful());

        executionRequest
                .getEngineExecutionListener()
                .executionFinished(testMethodTestDescriptor, testExecutionResult);
    }
}
