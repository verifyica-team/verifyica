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

import io.github.thunkware.vt.bridge.ThreadNameRunnable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.verifyica.api.ClassContext;
import org.verifyica.api.EngineContext;
import org.verifyica.api.Store;
import org.verifyica.engine.common.AnsiColor;
import org.verifyica.engine.common.Precondition;
import org.verifyica.engine.common.SemaphoreRunnable;
import org.verifyica.engine.common.StackTracePrinter;
import org.verifyica.engine.context.ConcreteClassContext;
import org.verifyica.engine.invocation.Invocation;
import org.verifyica.engine.invocation.InvocationContext;
import org.verifyica.engine.invocation.InvocationController;
import org.verifyica.engine.invocation.InvocationResult;
import org.verifyica.engine.invocation.SkipInvocation;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;
import org.verifyica.engine.support.ExecutorSupport;
import org.verifyica.engine.support.HashSupport;

/** Class to implement ClassTestDescriptor */
public class ClassTestDescriptor extends InvocableTestDescriptor {

    private final int testArgumentParallelism;
    private final Class<?> testClass;
    private final List<Method> prepareMethods;
    private final List<Method> concludeMethods;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param testClass testClass
     * @param testArgumentParallelism testArgumentParallelism
     * @param prepareMethods prepareMethods
     * @param concludeMethods concludeMethods
     */
    public ClassTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            int testArgumentParallelism,
            List<Method> prepareMethods,
            List<Method> concludeMethods) {
        super(uniqueId, displayName);

        Precondition.notNull(testClass, "testClass is null");
        Precondition.notBlank(displayName, "displayName is null", "displayName is blank");
        Precondition.isTrue(testArgumentParallelism >= 1, "testArgumentParallelism is less than 1");
        Precondition.notNull(prepareMethods, "prepareMethods is null");
        Precondition.notNull(concludeMethods, "concludeMethods is null");

        this.testArgumentParallelism = testArgumentParallelism;
        this.testClass = testClass;
        this.prepareMethods = prepareMethods;
        this.concludeMethods = concludeMethods;
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
     * Method to get test argument parallelism
     *
     * @return test argument parallelism
     */
    public int getTestArgumentParallelism() {
        return testArgumentParallelism;
    }

    /**
     * Method to get the test class
     *
     * @return the test class
     */
    public Class<?> getTestClass() {
        return testClass;
    }

    /**
     * Method to get a List of prepare Methods
     *
     * @return a List of prepare methods
     */
    public List<Method> getPrepareMethods() {
        return prepareMethods;
    }

    /**
     * Method to get a List of conclude Methods
     *
     * @return a List of conclude methods
     */
    public List<Method> getConcludeMethods() {
        return concludeMethods;
    }

    @Override
    public String toString() {
        return "ClassTestDescriptor{"
                + "uniqueId="
                + getUniqueId()
                + ", displayName="
                + getDisplayName()
                + ", testClass="
                + testClass
                + ", parallelism="
                + testArgumentParallelism
                + ", prepareMethods="
                + prepareMethods
                + ", concludeMethods="
                + concludeMethods
                + '}';
    }

    @Override
    public Invocation getTestInvocation(InvocationContext invocationContext) {
        return new TestInvocation(this, invocationContext);
    }

    @Override
    public Invocation getSkipInvocation(InvocationContext invocationContext) {
        return new SkipInvocation(this, invocationContext);
    }

    /** Class to implement TestInvocation */
    private static class TestInvocation implements Invocation {

        private static final Logger LOGGER = LoggerFactory.getLogger(TestInvocation.class);

        private enum State {
            START,
            INSTANTIATE,
            PREPARE,
            TEST,
            SKIP,
            CONCLUDE,
            CLOSE,
            CLEAR,
            DESTROY,
            END
        }

        private final InvocationContext invocationContext;
        private final ClassTestDescriptor classTestDescriptor;
        private final Class<?> testClass;
        private final List<Method> prepareMethods;
        private final Set<ArgumentTestDescriptor> argumentTestDescriptors;
        private final List<Method> concludeMethods;
        private final ClassContext classContext;
        private final InvocationController invocationController;
        private final ExecutorService argumentExecutorService;
        private final EngineExecutionListener engineExecutionListener;
        private final AtomicReference<Object> testInstanceReference;

        /**
         * Constructor
         *
         * @param classTestDescriptor classTestDescriptor
         * @param invocationContext   invocationContext
         */
        private TestInvocation(ClassTestDescriptor classTestDescriptor, InvocationContext invocationContext) {
            this.invocationContext = invocationContext;
            this.classTestDescriptor = classTestDescriptor;
            this.testClass = classTestDescriptor.getTestClass();
            this.prepareMethods = classTestDescriptor.getPrepareMethods();
            this.argumentTestDescriptors = classTestDescriptor.getChildren().stream()
                    .map(ArgumentTestDescriptor.class::cast)
                    .collect(Collectors.toCollection((Supplier<Set<ArgumentTestDescriptor>>) LinkedHashSet::new));
            this.concludeMethods = classTestDescriptor.getConcludeMethods();
            this.testInstanceReference = new AtomicReference<>();
            this.classContext = new ConcreteClassContext(
                    invocationContext.get(EngineContext.class), classTestDescriptor, testInstanceReference);
            invocationContext.set(ClassContext.class, classContext);
            this.invocationController = invocationContext.get(InvocationController.class);
            this.argumentExecutorService = invocationContext.get(InvocationContext.ARGUMENT_EXECUTOR_SERVICE);
            this.engineExecutionListener = invocationContext.get(EngineExecutionListener.class);
        }

        @Override
        public InvocationResult invoke() {
            engineExecutionListener.executionStarted(classTestDescriptor);

            List<InvocationResult> invocationResults = Collections.synchronizedList(new ArrayList<>());
            InvocationResult invocationResult;

            State state = State.START;
            while (state != TestInvocation.State.END) {
                LOGGER.trace("testDescriptor [%s] state [%s]", classTestDescriptor, state);
                switch (state) {
                    case START: {
                        state = State.INSTANTIATE;
                        break;
                    }
                    case INSTANTIATE: {
                        invocationResult = invocationController.invokeInstantiate(testClass, testInstanceReference);
                        invocationResults.add(invocationResult);
                        if (invocationResult.isFailure()) {
                            StackTracePrinter.printStackTrace(
                                    invocationResult.getThrowable(), AnsiColor.TEXT_RED_BOLD, System.err);
                            state = State.SKIP;
                        } else {
                            state = State.PREPARE;
                        }
                        break;
                    }
                    case PREPARE: {
                        invocationResult = invocationController.invokePrepareMethods(prepareMethods, classContext);
                        invocationResults.add(invocationResult);
                        if (invocationResult.isFailure()) {
                            StackTracePrinter.printStackTrace(
                                    invocationResult.getThrowable(), AnsiColor.TEXT_RED_BOLD, System.err);
                            state = State.SKIP;
                        } else {
                            state = State.TEST;
                        }
                        break;
                    }
                    case TEST: {
                        invocationResult = test();
                        invocationResults.add(invocationResult);
                        state = State.CONCLUDE;
                        break;
                    }
                    case SKIP: {
                        for (InvocableTestDescriptor invocableTestDescriptor :
                                classTestDescriptor.getInvocableChildren()) {
                            invocableTestDescriptor
                                    .getSkipInvocation(invocationContext)
                                    .invoke();
                            invocationResults.add(invocableTestDescriptor.getInvocationResult());
                        }
                        state = State.CONCLUDE;
                        break;
                    }
                    case CONCLUDE: {
                        invocationResult = invocationController.invokeConcludeMethods(concludeMethods, classContext);
                        invocationResults.add(invocationResult);
                        if (invocationResult.isFailure()) {
                            StackTracePrinter.printStackTrace(
                                    invocationResult.getThrowable(), AnsiColor.TEXT_RED_BOLD, System.err);
                        }
                        state = State.DESTROY;
                        break;
                    }
                    case DESTROY: {
                        invocationResult = invocationController.invokeOnDestroy(classContext);
                        invocationResults.add(invocationResult);
                        if (invocationResult.isFailure()) {
                            StackTracePrinter.printStackTrace(
                                    invocationResult.getThrowable(), AnsiColor.TEXT_RED_BOLD, System.err);
                        }
                        state = State.CLOSE;
                        break;
                    }
                    case CLOSE: {
                        Object testInstance = testInstanceReference.get();
                        try {
                            if (testInstance instanceof AutoCloseable) {
                                ((AutoCloseable) testInstance).close();
                            }
                            invocationResults.add(InvocationResult.success());
                        } catch (Throwable t) {
                            invocationResults.add(InvocationResult.exception(t));
                            StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
                        } finally {
                            testInstanceReference.set(null);
                        }
                        state = State.CLEAR;
                        break;
                    }
                    case CLEAR: {
                        Store store = classContext.getStore();
                        for (Object key : store.keySet()) {
                            Object value = store.remove(key);
                            if (value instanceof AutoCloseable) {
                                try {
                                    ((AutoCloseable) value).close();
                                    invocationResults.add(InvocationResult.success());
                                } catch (Throwable t) {
                                    StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
                                    invocationResults.add(InvocationResult.exception(t));
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
                    classTestDescriptor.setInvocationResult(invocationResult2);
                    engineExecutionListener.executionFinished(
                            classTestDescriptor, TestExecutionResult.failed(invocationResult2.getThrowable()));
                    return invocationResult2;
                }
            }

            classTestDescriptor.setInvocationResult(InvocationResult.success());
            engineExecutionListener.executionFinished(classTestDescriptor, TestExecutionResult.successful());
            return InvocationResult.success();
        }

        private InvocationResult test() {
            int testArgumentParallelism = classTestDescriptor.getTestArgumentParallelism();
            if (testArgumentParallelism > 1) {
                List<Future<?>> futures = new ArrayList<>();

                Semaphore semaphore = new Semaphore(testArgumentParallelism, true);

                argumentTestDescriptors.forEach(argumentTestDescriptor -> {
                    String threadName = Thread.currentThread().getName();
                    threadName = threadName.substring(0, threadName.indexOf("/") + 1) + HashSupport.alphanumeric(6);

                    futures.add(argumentExecutorService.submit(new SemaphoreRunnable(
                            semaphore, new ThreadNameRunnable(threadName, () -> argumentTestDescriptor
                                    .getTestInvocation(invocationContext.copy())
                                    .invoke()))));
                });

                ExecutorSupport.waitForAllFutures(futures, argumentExecutorService);
            } else {
                argumentTestDescriptors.forEach(argumentTestDescriptor -> argumentTestDescriptor
                        .getTestInvocation(invocationContext)
                        .invoke());
            }

            for (ArgumentTestDescriptor argumentTestDescriptor : argumentTestDescriptors) {
                InvocationResult invocationResult = argumentTestDescriptor.getInvocationResult();
                if (invocationResult.isFailure()) {
                    return invocationResult;
                }
            }

            return InvocationResult.success();
        }
    }
}
