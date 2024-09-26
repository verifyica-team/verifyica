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

package org.verifyica.engine.descriptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.verifyica.api.ArgumentContext;
import org.verifyica.engine.common.AnsiColor;
import org.verifyica.engine.common.Precondition;
import org.verifyica.engine.common.StackTracePrinter;
import org.verifyica.engine.invocation.Invocation;
import org.verifyica.engine.invocation.InvocationContext;
import org.verifyica.engine.invocation.InvocationController;
import org.verifyica.engine.invocation.InvocationResult;
import org.verifyica.engine.invocation.SkipInvocation;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;

/** Class to implement TestMethodTestDescriptor */
public class TestMethodTestDescriptor extends InvocableTestDescriptor {

    private final List<Method> beforeEachMethods;
    private final Method testMethod;
    private final List<Method> afterEachMethods;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param beforeEachMethods beforeEachMethods
     * @param testMethod testMethod
     * @param afterEachMethods afterEachMethods
     */
    public TestMethodTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            List<Method> beforeEachMethods,
            Method testMethod,
            List<Method> afterEachMethods) {
        super(uniqueId, displayName);

        Precondition.notNull(beforeEachMethods, "beforeEachMethods is null");
        Precondition.notNull(testMethod, "testMethod is null");
        Precondition.notNull(afterEachMethods, "afterEachMethods is null");

        this.beforeEachMethods = beforeEachMethods;
        this.testMethod = testMethod;
        this.afterEachMethods = afterEachMethods;
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(MethodSource.from(testMethod));
    }

    /**
     * Method to get List of beforeEach Methods
     *
     * @return a List of beforeEach Methods
     */
    public List<Method> getBeforeEachMethods() {
        return beforeEachMethods;
    }

    /**
     * Method to get the test Method
     *
     * @return the test Method
     */
    public Method getTestMethod() {
        return testMethod;
    }

    /**
     * Method to get a List of afterEach Methods
     *
     * @return a List of afterEach Methods
     */
    public List<Method> getAfterEachMethods() {
        return afterEachMethods;
    }

    @Override
    public Invocation getTestInvocation(InvocationContext invocationContext) {
        return new TestInvocation(this, invocationContext);
    }

    @Override
    public Invocation getSkipInvocation(InvocationContext invocationContext) {
        return new SkipInvocation(this, invocationContext);
    }

    @Override
    public String toString() {
        return "TestMethodTestDescriptor{"
                + "uniqueId="
                + getUniqueId()
                + ", displayName="
                + getDisplayName()
                + ", beforeEachMethods="
                + beforeEachMethods
                + ", testMethod="
                + testMethod
                + ", afterEachMethods="
                + afterEachMethods
                + '}';
    }

    /** Class to implement TestInvocation */
    private static class TestInvocation implements Invocation {

        private static final Logger LOGGER = LoggerFactory.getLogger(TestInvocation.class);

        private enum State {
            START,
            BEFORE_EACH,
            TEST,
            AFTER_EACH,
            END
        }

        private final TestMethodTestDescriptor testMethodTestDescriptor;
        private final EngineExecutionListener engineExecutionListener;
        private final ArgumentContext argumentContext;
        private final InvocationController invocationController;

        /**
         * Constructor
         *
         * @param testMethodTestDescriptor testMethodTestDescriptor
         * @param invocationContext invocationContext
         */
        private TestInvocation(TestMethodTestDescriptor testMethodTestDescriptor, InvocationContext invocationContext) {
            this.testMethodTestDescriptor = testMethodTestDescriptor;
            this.engineExecutionListener = invocationContext.get(EngineExecutionListener.class);
            this.invocationController = invocationContext.get(InvocationController.class);
            this.argumentContext = invocationContext.get(ArgumentContext.class);
        }

        @Override
        public InvocationResult invoke() {
            engineExecutionListener.executionStarted(testMethodTestDescriptor);

            List<InvocationResult> invocationResults = new ArrayList<>();
            InvocationResult invocationResult;

            State state = State.START;
            while (state != State.END) {
                LOGGER.trace("testDescriptor [%s] state [%s]", testMethodTestDescriptor, state);
                switch (state) {
                    case START: {
                        state = State.BEFORE_EACH;
                        break;
                    }
                    case BEFORE_EACH: {
                        invocationResult = invocationController.invokeBeforeEachMethods(
                                testMethodTestDescriptor.getBeforeEachMethods(), argumentContext);
                        invocationResults.add(invocationResult);
                        if (invocationResult.isFailure()) {
                            StackTracePrinter.printStackTrace(
                                    invocationResult.getThrowable(), AnsiColor.TEXT_RED_BOLD, System.err);
                            state = State.AFTER_EACH;
                        } else {
                            state = State.TEST;
                        }
                        break;
                    }
                    case TEST: {
                        invocationResult = invocationController.invokeTestMethod(
                                testMethodTestDescriptor.getTestMethod(), argumentContext);
                        invocationResults.add(invocationResult);
                        if (invocationResult.isFailure()) {
                            StackTracePrinter.printStackTrace(
                                    invocationResult.getThrowable(), AnsiColor.TEXT_RED_BOLD, System.err);
                        }
                        state = State.AFTER_EACH;
                        break;
                    }
                    case AFTER_EACH: {
                        invocationResult = invocationController.invokeAfterEachMethods(
                                testMethodTestDescriptor.getAfterEachMethods(), argumentContext);
                        invocationResults.add(invocationResult);
                        if (invocationResult.isFailure()) {
                            StackTracePrinter.printStackTrace(
                                    invocationResult.getThrowable(), AnsiColor.TEXT_RED_BOLD, System.err);
                        }
                        state = State.END;
                        break;
                    }
                }
            }

            for (InvocationResult invocationResult2 : invocationResults) {
                if (invocationResult2.isFailure()) {
                    testMethodTestDescriptor.setInvocationResult(invocationResult2);
                    engineExecutionListener.executionFinished(
                            testMethodTestDescriptor, TestExecutionResult.failed(invocationResult2.getThrowable()));
                    return invocationResult2;
                }
            }

            testMethodTestDescriptor.setInvocationResult(InvocationResult.success());
            engineExecutionListener.executionFinished(testMethodTestDescriptor, TestExecutionResult.successful());
            return InvocationResult.success();
        }
    }
}
