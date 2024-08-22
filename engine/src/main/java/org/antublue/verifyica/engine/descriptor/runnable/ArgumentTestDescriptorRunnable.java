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
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.Store;
import org.antublue.verifyica.engine.common.Precondition;
import org.antublue.verifyica.engine.common.StateSet;
import org.antublue.verifyica.engine.context.DefaultArgumentContext;
import org.antublue.verifyica.engine.descriptor.ArgumentTestDescriptor;
import org.antublue.verifyica.engine.descriptor.TestMethodTestDescriptor;
import org.antublue.verifyica.engine.interceptor.ClassInterceptorRegistry;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestExecutionResult;

/** Class to implement ArgumentTestDescriptorRunnable */
public class ArgumentTestDescriptorRunnable extends AbstractTestDescriptorRunnable {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ArgumentTestDescriptorRunnable.class);

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
    public ArgumentTestDescriptorRunnable(
            ExecutionRequest executionRequest,
            ClassContext classContext,
            ArgumentTestDescriptor argumentTestDescriptor) {
        Precondition.notNull(executionRequest, "executionRequest is null");
        Precondition.notNull(classContext, "classContext is null");
        Precondition.notNull(argumentTestDescriptor, "argumentTestDescriptor is null");

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

        StateSet<String> stateSet = new StateSet<>();

        try {
            stateSet.setCurrentState("beforeAll");

            ClassInterceptorRegistry.getInstance().beforeAll(argumentContext, beforeAllMethods);

            stateSet.setCurrentState("beforeAll.success");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            stateSet.setCurrentState("beforeAll.failure", t);
        }

        if (stateSet.isCurrentState("beforeAll.success")) {
            try {
                stateSet.setCurrentState("execute");

                testMethodTestDescriptors.forEach(
                        methodTestDescriptor ->
                                new TestMethodTestDescriptorRunnable(
                                                executionRequest,
                                                argumentContext,
                                                methodTestDescriptor)
                                        .run());

                stateSet.setCurrentState("execute.success");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateSet.setCurrentState("execute.failure", t);
            }
        }

        if (stateSet.hasObservedState("beforeAll.failure")) {
            try {
                stateSet.setCurrentState("skip");

                testMethodTestDescriptors.forEach(
                        methodTestDescriptor ->
                                new TestMethodTestDescriptorRunnable(
                                                executionRequest,
                                                argumentContext,
                                                methodTestDescriptor)
                                        .skip());

                stateSet.setCurrentState("skip.success");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateSet.setCurrentState("skip.failure", t);
            }
        }

        try {
            stateSet.setCurrentState("afterAll");

            ClassInterceptorRegistry.getInstance().afterAll(argumentContext, afterAllMethods);

            stateSet.setCurrentState("afterAll.success");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            stateSet.setCurrentState("afterAll.failure", t);
        }

        Argument<?> testArgument = argumentContext.getTestArgument();
        if (testArgument instanceof AutoCloseable) {
            try {
                stateSet.setCurrentState("argumentAutoClose(" + testArgument.getName() + ")");

                ((AutoCloseable) testArgument).close();

                stateSet.setCurrentState(
                        "argumentAutoClose(" + testArgument.getName() + ").success");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateSet.setCurrentState(
                        "argumentAutoClose(" + testArgument.getName() + ").failure");
            }
        }

        Store store = argumentContext.getStore();
        for (Object key : store.keySet()) {
            Object value = store.remove(key);
            if (value instanceof AutoCloseable) {
                try {
                    stateSet.setCurrentState("storeAutoClose(" + key + ")");

                    ((AutoCloseable) value).close();

                    stateSet.setCurrentState("storeAutoClose(" + key + ").success");
                } catch (Throwable t) {
                    t.printStackTrace(System.err);
                    stateSet.setCurrentState("storeAutoClose(" + key + ").failure");
                }
            }
        }
        store.clear();

        LOGGER.trace("state tracker %s [%s]", argumentTestDescriptor, stateSet);

        TestExecutionResult testExecutionResult =
                stateSet.getFirstStateEntryWithThrowable()
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
                        new TestMethodTestDescriptorRunnable(
                                        executionRequest, argumentContext, methodTestDescriptor)
                                .skip());

        executionRequest
                .getEngineExecutionListener()
                .executionFinished(argumentTestDescriptor, TestExecutionResult.aborted(null));
    }
}
