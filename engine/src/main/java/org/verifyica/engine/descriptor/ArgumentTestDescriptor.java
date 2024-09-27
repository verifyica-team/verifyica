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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.verifyica.api.Argument;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Store;
import org.verifyica.api.Verifyica;
import org.verifyica.engine.common.AnsiColor;
import org.verifyica.engine.common.Precondition;
import org.verifyica.engine.common.StackTracePrinter;
import org.verifyica.engine.context.ConcreteArgumentContext;
import org.verifyica.engine.invocation.Invocation;
import org.verifyica.engine.invocation.InvocationContext;
import org.verifyica.engine.invocation.InvocationController;
import org.verifyica.engine.invocation.InvocationResult;
import org.verifyica.engine.invocation.SkipInvocation;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;

/** Class to implement ArgumentTestDescriptor */
public class ArgumentTestDescriptor extends InvocableTestDescriptor {

    private final Class<?> testClass;
    private final int testArgumentIndex;
    private final Argument<?> testArgument;
    private final List<Method> beforeAllMethods;
    private final List<Method> afterAllMethods;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param testClass testClass
     * @param testArgumentIndex testArgumentIndex
     * @param testArgument testArgument
     * @param beforeAllMethods beforeAllMethods
     * @param afterAllMethods afterAllMethods
     */
    public ArgumentTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            int testArgumentIndex,
            Argument<?> testArgument,
            List<Method> beforeAllMethods,
            List<Method> afterAllMethods) {
        super(uniqueId, displayName);

        Precondition.notNull(testClass, "testClass is null");
        Precondition.isTrue(testArgumentIndex >= 0, "testArgumentIndex is less than 0");
        Precondition.notNull(testArgument, "testArgument is null");
        Precondition.notNull(beforeAllMethods, "beforeAllMethods is null");
        Precondition.notNull(afterAllMethods, "afterAllMethods is null");

        this.testClass = testClass;
        this.testArgumentIndex = testArgumentIndex;
        this.testArgument = testArgument;
        this.beforeAllMethods = beforeAllMethods;
        this.afterAllMethods = afterAllMethods;
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
     * Method to get the test Argument index
     *
     * @return the test Argument index
     */
    public int getTestArgumentIndex() {
        return testArgumentIndex;
    }

    /**
     * Method to get the test Argument
     *
     * @return the test Argument
     */
    public Argument<?> getTestArgument() {
        return testArgument;
    }

    /**
     * Method to get a List of beforeAll Methods
     *
     * @return a List of beforeAll Methods
     */
    public List<Method> getBeforeAllMethods() {
        return beforeAllMethods;
    }

    /**
     * Method to get a List of afterAll Methods
     *
     * @return a List of afterAll Methods
     */
    public List<Method> getAfterAllMethods() {
        return afterAllMethods;
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
        return "ArgumentTestDescriptor{"
                + "uniqueId="
                + getUniqueId()
                + ", displayName="
                + getDisplayName()
                + ", testClass="
                + testClass
                + ", testArgumentIndex="
                + testArgumentIndex
                + ", testArgument="
                + testArgument
                + ", beforeAllMethods="
                + beforeAllMethods
                + ", afterAllMethods="
                + afterAllMethods
                + '}';
    }

    /** Class to implement TestInvocation */
    private static class TestInvocation implements Invocation {

        private static final Logger LOGGER = LoggerFactory.getLogger(TestInvocation.class);

        private enum State {
            START,
            BEFORE_ALL,
            TEST_DEPENDENT,
            TEST_INDEPENDENT,
            SKIP,
            AFTER_ALL,
            CLOSE,
            CLEAR,
            END
        }

        private final InvocationContext invocationContext;
        private final ArgumentTestDescriptor argumentTestDescriptor;
        private final List<Method> beforeAllMethods;
        private final List<Method> afterAllMethods;
        private final ArgumentContext argumentContext;
        private final InvocationController invocationController;
        private final EngineExecutionListener engineExecutionListener;

        /**
         * Constructor
         *
         * @param argumentTestDescriptor argumentTestDescriptor
         * @param invocationContext invocationContext
         */
        private TestInvocation(ArgumentTestDescriptor argumentTestDescriptor, InvocationContext invocationContext) {
            this.invocationContext = invocationContext;
            this.argumentTestDescriptor = argumentTestDescriptor;
            this.beforeAllMethods = argumentTestDescriptor.getBeforeAllMethods();
            this.afterAllMethods = argumentTestDescriptor.getAfterAllMethods();
            this.argumentContext =
                    new ConcreteArgumentContext(invocationContext.get(ClassContext.class), argumentTestDescriptor);
            invocationContext.set(ArgumentContext.class, argumentContext);
            this.invocationController = invocationContext.get(InvocationController.class);
            this.engineExecutionListener = invocationContext.get(EngineExecutionListener.class);
        }

        @Override
        public InvocationResult invoke() {
            engineExecutionListener.executionStarted(argumentTestDescriptor);

            List<InvocationResult> invocationResults = new ArrayList<>();
            InvocationResult invocationResult;

            TestInvocation.State state = TestInvocation.State.START;
            while (state != State.END) {
                LOGGER.trace("testDescriptor [%s] state [%s]", argumentTestDescriptor, state);
                switch (state) {
                    case START: {
                        state = State.BEFORE_ALL;
                        break;
                    }
                    case BEFORE_ALL: {
                        invocationResult =
                                invocationController.invokeBeforeAllMethods(beforeAllMethods, argumentContext);
                        invocationResults.add(invocationResult);
                        if (invocationResult.isFailure()) {
                            StackTracePrinter.printStackTrace(
                                    invocationResult.getThrowable(), AnsiColor.TEXT_RED_BOLD, System.err);
                            state = State.SKIP;
                        } else {
                            if (argumentContext
                                    .getClassContext()
                                    .getTestClass()
                                    .isAnnotationPresent(Verifyica.IndependentTests.class)) {
                                state = State.TEST_INDEPENDENT;
                            } else {
                                state = State.TEST_DEPENDENT;
                            }
                        }
                        break;
                    }
                    case TEST_DEPENDENT: {
                        Iterator<InvocableTestDescriptor> invocableTestDescriptorIterator =
                                argumentTestDescriptor.getInvocableChildren().iterator();
                        while (invocableTestDescriptorIterator.hasNext()) {
                            InvocableTestDescriptor invocableTestDescriptor = invocableTestDescriptorIterator.next();
                            invocationResult = invocableTestDescriptor
                                    .getTestInvocation(invocationContext)
                                    .invoke();
                            invocationResults.add(invocationResult);
                            if (invocationResult.isFailure()) {
                                break;
                            }
                        }

                        while (invocableTestDescriptorIterator.hasNext()) {
                            InvocableTestDescriptor invocableTestDescriptor = invocableTestDescriptorIterator.next();
                            invocationResult = invocableTestDescriptor
                                    .getSkipInvocation(invocationContext)
                                    .invoke();
                            invocationResults.add(invocationResult);
                        }

                        state = State.AFTER_ALL;
                        break;
                    }
                    case TEST_INDEPENDENT: {
                        for (InvocableTestDescriptor invocableTestDescriptor :
                                argumentTestDescriptor.getInvocableChildren()) {
                            invocationResult = invocableTestDescriptor
                                    .getTestInvocation(invocationContext)
                                    .invoke();
                            invocationResults.add(invocationResult);
                        }
                        state = State.AFTER_ALL;
                        break;
                    }
                    case SKIP: {
                        for (InvocableTestDescriptor invocableTestDescriptor :
                                argumentTestDescriptor.getInvocableChildren()) {
                            invocableTestDescriptor
                                    .getSkipInvocation(invocationContext)
                                    .invoke();
                            invocationResults.add(invocableTestDescriptor.getInvocationResult());
                        }
                        state = State.AFTER_ALL;
                        break;
                    }
                    case AFTER_ALL: {
                        invocationResult = invocationController.invokeAfterAllMethods(afterAllMethods, argumentContext);
                        invocationResults.add(invocationResult);
                        if (invocationResult.isFailure()) {
                            StackTracePrinter.printStackTrace(
                                    invocationResult.getThrowable(), AnsiColor.TEXT_RED_BOLD, System.err);
                        }
                        state = State.CLOSE;
                        break;
                    }
                    case CLOSE: {
                        Argument<?> testArgument = argumentTestDescriptor.getTestArgument();
                        if (testArgument instanceof AutoCloseable) {
                            try {
                                ((AutoCloseable) testArgument).close();
                                invocationResults.add(InvocationResult.success());
                            } catch (Throwable t) {
                                invocationResults.add(InvocationResult.exception(t));
                                StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
                            }
                        }
                        state = State.CLEAR;
                        break;
                    }
                    case CLEAR: {
                        Store store = argumentContext.getStore();
                        for (Object key : store.keySet()) {
                            Object value = store.remove(key);
                            if (value instanceof AutoCloseable) {
                                try {
                                    ((AutoCloseable) value).close();
                                    invocationResults.add(InvocationResult.success());
                                } catch (Throwable t) {
                                    invocationResults.add(InvocationResult.exception(t));
                                    StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
                                }
                            }
                        }
                        store.clear();
                        state = State.END;
                        break;
                    }
                }
            }

            for (InvocationResult invocationResult2 : invocationResults) {
                if (invocationResult2.isFailure()) {
                    argumentTestDescriptor.setInvocationResult(invocationResult2);
                    engineExecutionListener.executionFinished(
                            argumentTestDescriptor, TestExecutionResult.failed(invocationResult2.getThrowable()));
                    return invocationResult2;
                }
            }

            argumentTestDescriptor.setInvocationResult(InvocationResult.success());
            engineExecutionListener.executionFinished(argumentTestDescriptor, TestExecutionResult.successful());
            return InvocationResult.success();
        }
    }
}
