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

package org.antublue.verifyica.engine.descriptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.Store;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.engine.common.Precondition;
import org.antublue.verifyica.engine.common.statemachine.Result;
import org.antublue.verifyica.engine.common.statemachine.StateMachine;
import org.antublue.verifyica.engine.context.ConcreteArgumentContext;
import org.antublue.verifyica.engine.interceptor.ClassInterceptorManager;
import org.antublue.verifyica.engine.invocation.InvocableTestDescriptor;
import org.antublue.verifyica.engine.invocation.Invocation;
import org.antublue.verifyica.engine.invocation.InvocationContext;
import org.antublue.verifyica.engine.invocation.InvocationResult;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;

/** Class to implement ArgumentTestDescriptor */
public class ArgumentTestDescriptor extends AbstractTestDescriptor
        implements InvocableTestDescriptor {

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
    public Invocation getInvocation(InvocationContext invocationContext) {
        return new ConcreteInvocation(this, invocationContext);
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

    /** Class to implement ConcreteInvocation */
    private static class ConcreteInvocation implements Invocation {

        private static final Logger LOGGER = LoggerFactory.getLogger(ConcreteInvocation.class);

        private final InvocationContext invocationContext;
        private final ArgumentTestDescriptor argumentTestDescriptor;
        private final List<Method> beforeAllMethods;
        private final Set<TestMethodTestDescriptor> testMethodTestDescriptors;
        private final List<Method> afterAllMethods;
        private final ArgumentContext argumentContext;
        private final ClassInterceptorManager classInterceptorManager;
        private final EngineExecutionListener engineExecutionListener;

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
         * @param argumentTestDescriptor argumentTestDescriptor
         * @param invocationContext invocationContext
         */
        private ConcreteInvocation(
                ArgumentTestDescriptor argumentTestDescriptor,
                InvocationContext invocationContext) {
            this.invocationContext = invocationContext;

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

            this.argumentContext =
                    new ConcreteArgumentContext(
                            invocationContext.get(ClassContext.class), argumentTestDescriptor);

            invocationContext.set(ArgumentContext.class, argumentContext);

            this.classInterceptorManager = invocationContext.get(ClassInterceptorManager.class);

            this.engineExecutionListener = invocationContext.get(EngineExecutionListener.class);
        }

        @Override
        public InvocationResult proceed() {
            LOGGER.trace("proceed() %s", argumentTestDescriptor);

            engineExecutionListener.executionStarted(argumentTestDescriptor);

            StateMachine<State> stateMachine =
                    new StateMachine<State>()
                            .onState(
                                    State.START,
                                    () -> {
                                        try {
                                            classInterceptorManager.beforeAll(
                                                    argumentContext, beforeAllMethods);
                                            return Result.of(State.BEFORE_ALL_SUCCESS);
                                        } catch (Throwable t) {
                                            t.printStackTrace(System.err);
                                            return Result.of(State.BEFORE_ALL_FAILURE, t);
                                        }
                                    });

            if (argumentContext
                    .getClassContext()
                    .getTestClass()
                    .isAnnotationPresent(Verifyica.ScenarioTest.class)) {
                stateMachine.onState(
                        State.BEFORE_ALL_SUCCESS,
                        () -> {
                            List<InvocationResult> invocationResults = new ArrayList<>();

                            Iterator<TestMethodTestDescriptor> testMethodTestDescriptorIterator =
                                    testMethodTestDescriptors.iterator();

                            while (testMethodTestDescriptorIterator.hasNext()) {
                                TestMethodTestDescriptor testMethodTestDescriptor =
                                        testMethodTestDescriptorIterator.next();
                                InvocationResult invocationResult =
                                        testMethodTestDescriptor
                                                .getInvocation(invocationContext)
                                                .proceed();
                                invocationResults.add(invocationResult);
                                if (invocationResult.isSkipped()) {
                                    break;
                                }
                            }

                            while (testMethodTestDescriptorIterator.hasNext()) {
                                TestMethodTestDescriptor testMethodTestDescriptor =
                                        testMethodTestDescriptorIterator.next();
                                InvocationResult invocationResult =
                                        testMethodTestDescriptor.getInvocation(invocationContext).skip();
                                invocationResults.add(invocationResult);
                            }

                            Optional<InvocationResult> optionalInvocationResult =
                                    invocationResults.stream()
                                            .filter(InvocationResult::isFailure)
                                            .findFirst();

                            if (!optionalInvocationResult.isPresent()) {
                                return Result.of(State.EXECUTE_SUCCESS);
                            } else {
                                return Result.of(State.EXECUTE_FAILURE);
                            }
                        });
            } else {
                stateMachine.onState(
                        State.BEFORE_ALL_SUCCESS,
                        () -> {
                            try {
                                testMethodTestDescriptors.forEach(
                                        testMethodTestDescriptor ->
                                                testMethodTestDescriptor
                                                        .getInvocation(invocationContext)
                                                        .proceed());
                                return Result.of(State.EXECUTE_SUCCESS);
                            } catch (Throwable t) {
                                t.printStackTrace(System.err);
                                return Result.of(State.EXECUTE_FAILURE, t);
                            }
                        });
            }

            stateMachine
                    .onState(
                            State.BEFORE_ALL_FAILURE,
                            () -> {
                                try {
                                    testMethodTestDescriptors.forEach(
                                            testMethodTestDescriptor ->
                                                    testMethodTestDescriptor
                                                            .getInvocation(invocationContext)
                                                            .skip());
                                    return Result.of(State.SKIP_SUCCESS);
                                } catch (Throwable t) {
                                    t.printStackTrace(System.err);
                                    return Result.of(State.SKIP_FAILURE, t);
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
                                    classInterceptorManager.afterAll(
                                            argumentContext, afterAllMethods);
                                    return Result.of(State.AFTER_ALL_SUCCESS);
                                } catch (Throwable t) {
                                    t.printStackTrace(System.err);
                                    return Result.of(State.AFTER_ALL_FAILURE, t);
                                }
                            })
                    .onStates(
                            StateMachine.asList(State.AFTER_ALL_SUCCESS, State.AFTER_ALL_FAILURE),
                            () -> {
                                try {
                                    Argument<?> testArgument = argumentContext.getTestArgument();
                                    if (testArgument instanceof AutoCloseable) {
                                        ((AutoCloseable) testArgument).close();
                                    }
                                    return Result.of(State.AUTO_CLOSE_ARGUMENT_SUCCESS);
                                } catch (Throwable t) {
                                    t.printStackTrace(System.err);
                                    return Result.of(State.AUTO_CLOSE_ARGUMENT_FAILURE, t);
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
                                    return Result.of(State.AUTO_CLOSE_STORE_SUCCESS);
                                } else {
                                    return Result.of(
                                            State.AUTO_CLOSE_STORE_FAILURE, throwables.get(0));
                                }
                            })
                    .onStates(
                            StateMachine.asList(
                                    State.AUTO_CLOSE_STORE_SUCCESS, State.AUTO_CLOSE_STORE_FAILURE),
                            () -> Result.of(State.END))
                    .run(State.START, State.END);

            LOGGER.trace("state machine [%s]", stateMachine);

            TestExecutionResult testExecutionResult =
                    stateMachine
                            .getFirstResultWithThrowable()
                            .map(result -> TestExecutionResult.failed(result.getThrowable()))
                            .orElse(TestExecutionResult.successful());

            engineExecutionListener.executionFinished(argumentTestDescriptor, testExecutionResult);

            if (testExecutionResult.getStatus() == TestExecutionResult.Status.FAILED) {
                return InvocationResult.create(InvocationResult.Type.FAILURE);
            } else {
                return InvocationResult.create(InvocationResult.Type.SUCCESS);
            }
        }

        @Override
        public InvocationResult skip() {
            LOGGER.trace("skip() %s", argumentTestDescriptor);

            engineExecutionListener.executionStarted(argumentTestDescriptor);

            testMethodTestDescriptors.forEach(
                    testMethodTestDescriptor ->
                            testMethodTestDescriptor.getInvocation(invocationContext).skip());

            engineExecutionListener.executionFinished(
                    argumentTestDescriptor, TestExecutionResult.aborted(null));

            return InvocationResult.create(InvocationResult.Type.SKIPPED);
        }
    }
}
