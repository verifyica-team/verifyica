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
    public void execute() {
        LOGGER.trace("execute() %s", argumentTestDescriptor);

        executionRequest.getEngineExecutionListener().executionStarted(argumentTestDescriptor);

        StateTracker<String> stateTracker = new StateTracker<>();

        try {
            stateTracker.setState("beforeAll");

            ClassInterceptorRegistry.getInstance().beforeAll(argumentContext, beforeAllMethods);

            stateTracker.setState("beforeAll.success");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            stateTracker.setState("beforeAll.failure", t);
        }

        if (stateTracker.isState("beforeAll.success")) {
            try {
                stateTracker.setState("execute");

                testMethodTestDescriptors.forEach(
                        methodTestDescriptor ->
                                new RunnableTestMethodTestDescriptor(
                                                executionRequest,
                                                argumentContext,
                                                methodTestDescriptor)
                                        .run());

                stateTracker.setState("execute.success");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.setState("execute.failure", t);
            }
        }

        if (stateTracker.containsState("beforeAll.failure")) {
            try {
                stateTracker.setState("skip");

                testMethodTestDescriptors.forEach(
                        methodTestDescriptor ->
                                new RunnableTestMethodTestDescriptor(
                                                executionRequest,
                                                argumentContext,
                                                methodTestDescriptor)
                                        .skip());

                stateTracker.setState("skip.success");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.setState("skip.failure", t);
            }
        }

        try {
            stateTracker.setState("afterAll");

            ClassInterceptorRegistry.getInstance().afterAll(argumentContext, afterAllMethods);

            stateTracker.setState("afterAll.success");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            stateTracker.setState("afterAll.failure", t);
        }

        Argument<?> testArgument = argumentContext.getTestArgument();
        if (testArgument instanceof AutoCloseable) {
            try {
                stateTracker.setState("argumentAutoClose(" + testArgument.getName() + ")");

                ((AutoCloseable) testArgument).close();

                stateTracker.setState("argumentAutoClose(" + testArgument.getName() + ").success");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.setState("argumentAutoClose(" + testArgument.getName() + ").failure");
            }
        }

        Store store = argumentContext.getStore();
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

        LOGGER.trace("state tracker %s [%s]", argumentTestDescriptor, stateTracker);

        TestExecutionResult testExecutionResult =
                stateTracker
                        .getStateWithThrowable()
                        .map(stateEntry -> TestExecutionResult.failed(stateEntry.getThrowable()))
                        .orElse(TestExecutionResult.successful());

        executionRequest
                .getEngineExecutionListener()
                .executionFinished(argumentTestDescriptor, testExecutionResult);
    }

    @Override
    public void skip() {
        LOGGER.trace("skip() %s", argumentTestDescriptor);

        executionRequest.getEngineExecutionListener().executionStarted(argumentTestDescriptor);

        testMethodTestDescriptors.forEach(
                methodTestDescriptor ->
                        new RunnableTestMethodTestDescriptor(
                                        executionRequest, argumentContext, methodTestDescriptor)
                                .skip());

        executionRequest
                .getEngineExecutionListener()
                .executionFinished(argumentTestDescriptor, TestExecutionResult.aborted(null));
    }
}
