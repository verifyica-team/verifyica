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
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.Store;
import org.antublue.verifyica.engine.common.StateTracker;
import org.antublue.verifyica.engine.context.DefaultArgumentContext;
import org.antublue.verifyica.engine.descriptor.ArgumentTestDescriptor;
import org.antublue.verifyica.engine.descriptor.TestMethodTestDescriptor;
import org.antublue.verifyica.engine.interceptor.ClassInterceptorRegistry;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestExecutionResult;

/** Class to implement ArgumentTestDescriptorRunnable */
public class RunnableArgumentTestDescriptor extends AbstractRunnableTestDescriptor {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RunnableArgumentTestDescriptor.class);

    private final ExecutionRequest executionRequest;
    private final ArgumentTestDescriptor argumentTestDescriptor;
    private final List<Method> beforeAllMethods;
    private final List<TestMethodTestDescriptor> testMethodTestDescriptors;
    private final List<Method> afterAllMethods;
    private final ArgumentContext argumentContext;

    /**
     * Constructor
     *
     * @param executionRequest executionRequest
     * @param classContext classContext
     * @param argumentTestDescriptor argumentTestDescriptor
     */
    public RunnableArgumentTestDescriptor(
            ExecutionRequest executionRequest,
            ClassContext classContext,
            ArgumentTestDescriptor argumentTestDescriptor) {
        this.executionRequest = executionRequest;
        this.argumentTestDescriptor = argumentTestDescriptor;
        this.beforeAllMethods = argumentTestDescriptor.getBeforeAllMethods();
        this.testMethodTestDescriptors = getTestMethodTestDescriptors(argumentTestDescriptor);
        this.afterAllMethods = argumentTestDescriptor.getAfterAllMethods();
        this.argumentContext = new DefaultArgumentContext(classContext, argumentTestDescriptor);
    }

    @Override
    protected void execute() {
        LOGGER.trace("execute() %s", argumentTestDescriptor);

        executionRequest.getEngineExecutionListener().executionStarted(argumentTestDescriptor);

        StateTracker<String> stateTracker = new StateTracker<>();

        try {
            stateTracker.put("beforeAll");

            ClassInterceptorRegistry.getInstance().beforeAll(argumentContext, beforeAllMethods);

            stateTracker.put("beforeAll->SUCCESS");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            stateTracker.put("beforeAll->FAILURE", t);
        }

        if (stateTracker.contains("beforeAll->SUCCESS")) {
            try {
                stateTracker.put("doExecute");

                testMethodTestDescriptors.forEach(
                        methodTestDescriptor ->
                                new RunnableTestMethodTestDescriptor(
                                                executionRequest,
                                                argumentContext,
                                                methodTestDescriptor)
                                        .run());

                stateTracker.put("doExecute->SUCCESS");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.put("doExecute->FAILURE", t);
            }
        }

        if (stateTracker.contains("beforeAll->FAILURE")) {
            try {
                stateTracker.put("doSkip");

                /*
                getChildren().stream()
                        .map(TO_EXECUTABLE_TEST_DESCRIPTOR)
                        .forEach(
                                executableTestDescriptor -> {
                                    executableTestDescriptor.skip(executionRequest, defaultArgumentContext);
                                });
                 */

                stateTracker.put("doSkip->SUCCESS");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.put("doSkip->FAILURE", t);
            }
        }

        try {
            stateTracker.put("afterAll");

            ClassInterceptorRegistry.getInstance().afterAll(argumentContext, afterAllMethods);

            stateTracker.put("afterAll->SUCCESS");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            stateTracker.put("afterAll->FAILURE", t);
        }

        Argument<?> testArgument = argumentContext.getTestArgument();
        if (testArgument instanceof AutoCloseable) {
            try {
                stateTracker.put("argumentAutoClose(" + testArgument.getName() + ")");

                ((AutoCloseable) testArgument).close();

                stateTracker.put("argumentAutoClose(" + testArgument.getName() + ")->SUCCESS");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.put("argumentAutoClose(" + testArgument.getName() + ")->FAILURE");
            }
        }

        Store store = argumentContext.getStore();
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

        LOGGER.trace("state tracker %s [%s]", argumentTestDescriptor, stateTracker);

        TestExecutionResult testExecutionResult =
                stateTracker
                        .getFirstStateEntryWithThrowable()
                        .map(entry -> TestExecutionResult.failed(entry.getThrowable()))
                        .orElse(TestExecutionResult.successful());

        executionRequest
                .getEngineExecutionListener()
                .executionFinished(argumentTestDescriptor, testExecutionResult);
    }
}
