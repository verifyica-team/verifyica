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

package org.verifyica.engine.execution;

import static java.lang.String.format;

import io.github.thunkware.vt.bridge.ThreadNameRunnable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Store;
import org.verifyica.engine.common.SemaphoreRunnable;
import org.verifyica.engine.context.ConcreteClassContext;
import org.verifyica.engine.injection.FieldInjector;
import org.verifyica.engine.injection.Inject;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;
import org.verifyica.engine.support.ExecutorSupport;
import org.verifyica.engine.support.HashSupport;

/** Class to implement ExecutableClassTestDescriptor */
public class ExecutableClassTestDescriptor extends ExecutableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutableClassTestDescriptor.class);

    private enum State {
        START,
        INSTANTIATE,
        PREPARE,
        TEST,
        SKIP,
        CONCLUDE,
        CLOSE,
        CLEAN_UP,
        DESTROY,
        END
    }

    private final int testArgumentParallelism;
    private final Class<?> testClass;
    private final List<Method> prepareMethods;
    private final List<Method> concludeMethods;

    @Inject
    private ExecutorService argumentExecutorService;

    @Inject
    private ClassContext classContext;

    private AtomicReference<Object> testInstanceAtomicReference;
    private List<Throwable> throwables;

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
    public ExecutableClassTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            int testArgumentParallelism,
            List<Method> prepareMethods,
            List<Method> concludeMethods) {
        super(uniqueId, displayName);

        this.testArgumentParallelism = testArgumentParallelism;
        this.testClass = testClass;
        this.prepareMethods = prepareMethods;
        this.concludeMethods = concludeMethods;
        this.testInstanceAtomicReference = new AtomicReference<>();
        this.throwables = new ArrayList<>();
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(ClassSource.from(testClass));
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

    @Override
    public String toString() {
        return "ExecutableClassTestDescriptor{"
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
    public ExecutableClassTestDescriptor test() {
        try {
            checkInjected(engineExecutionListener, "engineExecutionListener not injected");
            checkInjected(argumentExecutorService, "argumentExecutorService not injected");

            classContext = new ConcreteClassContext(
                    engineContext, testClass, getDisplayName(), testArgumentParallelism, testInstanceAtomicReference);

            FieldInjector.injectFields(getChildren(), classContext);

            engineExecutionListener.executionStarted(this);

            State state = State.START;
            while (state != State.END) {
                LOGGER.trace("testDescriptor [%s] state [%s]", this, state);

                switch (state) {
                    case START: {
                        state = State.INSTANTIATE;
                        break;
                    }
                    case INSTANTIATE: {
                        state = doInstantiate();
                        break;
                    }
                    case PREPARE: {
                        state = doPrepare();
                        break;
                    }
                    case TEST: {
                        state = doTest();
                        break;
                    }
                    case SKIP: {
                        skip();
                        state = State.CONCLUDE;
                        break;
                    }
                    case CONCLUDE: {
                        state = doConclude();
                        break;
                    }
                    case DESTROY: {
                        state = doDestroy();
                        break;
                    }
                    case CLOSE: {
                        state = doClose();
                        break;
                    }
                    case CLEAN_UP: {
                        state = doCleanup();
                        break;
                    }
                    default: {
                        throw new IllegalStateException(format("Invalid State [%s]", state));
                    }
                }
            }

            TestExecutionResult testExecutionResult;
            ExecutionResult executionResult;

            if (throwables.isEmpty()) {
                testExecutionResult = TestExecutionResult.successful();
                executionResult = ExecutionResult.passed();
            } else {
                testExecutionResult = TestExecutionResult.failed(throwables.get(0));
                executionResult = ExecutionResult.failed(throwables.get(0));
            }

            setExecutionResult(executionResult);
            engineExecutionListener.executionFinished(this, testExecutionResult);
        } catch (Throwable t) {
            printStackTrace(t);
        }

        return this;
    }

    private State doInstantiate() {
        try {
            testInstanceAtomicReference.set(getTestClass().getConstructor().newInstance());

            return State.PREPARE;
        } catch (Throwable t) {
            printStackTrace(t);
            throwables.add(t);
        }

        return State.CLEAN_UP;
    }

    private State doPrepare() {
        try {
            for (Method method : prepareMethods) {
                method.invoke(testInstanceAtomicReference.get(), classContext);
            }

            return State.TEST;
        } catch (Throwable t) {
            printStackTrace(t.getCause());
            throwables.add(t.getCause());
        }

        return State.CONCLUDE;
    }

    /**
     * Method to test child test descriptors
     */
    private State doTest() {
        if (testArgumentParallelism > 1) {
            List<Future<?>> futures = new ArrayList<>();

            Semaphore semaphore = new Semaphore(testArgumentParallelism, true);

            List<ExecutableTestDescriptor> executableTestDescriptors = getChildren().stream()
                    .filter(ExecutableTestDescriptor.EXECUTABLE_TEST_DESCRIPTOR_FILTER)
                    .map(ExecutableTestDescriptor.EXECUTABLE_TEST_DESCRIPTOR_MAPPER)
                    .collect(Collectors.toList());

            for (ExecutableTestDescriptor executableTestDescriptor : executableTestDescriptors) {
                FieldInjector.injectFields(executableTestDescriptor, classContext);

                String threadName = Thread.currentThread().getName();
                threadName = threadName.substring(0, threadName.indexOf("/") + 1) + HashSupport.alphanumeric(6);
                ThreadNameRunnable threadNameRunnable =
                        new ThreadNameRunnable(threadName, executableTestDescriptor::test);
                SemaphoreRunnable semaphoreRunnable = new SemaphoreRunnable(semaphore, threadNameRunnable);
                Future<?> future = argumentExecutorService.submit(semaphoreRunnable);
                futures.add(future);
            }

            ExecutorSupport.waitForAllFutures(futures, argumentExecutorService);
        } else {
            getChildren().stream().map(EXECUTABLE_TEST_DESCRIPTOR_MAPPER).forEach(executableTestDescriptor -> {
                FieldInjector.injectFields(executableTestDescriptor, classContext);
                executableTestDescriptor.test();
            });
        }

        return State.CONCLUDE;
    }

    private State doConclude() {
        for (Method method : concludeMethods) {
            try {
                method.invoke(testInstanceAtomicReference.get(), classContext);
            } catch (Throwable t) {
                printStackTrace(t.getCause());
                throwables.add(t.getCause());
            }
        }

        return State.DESTROY;
    }

    private State doDestroy() {
        return State.CLOSE;
    }

    private State doClose() {
        if (testInstanceAtomicReference.get() instanceof AutoCloseable) {
            try {
                ((AutoCloseable) testInstanceAtomicReference.get()).close();
            } catch (Throwable t) {
                printStackTrace(t);
                throwables.add(t);
            }
        }

        testInstanceAtomicReference.set(null);

        return State.CLEAN_UP;
    }

    private State doCleanup() {
        Store store = classContext.getStore();

        Set<Object> keySet = store.keySet();
        for (Object key : keySet) {
            Object value = store.remove(key);
            if (value instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) value).close();
                } catch (Throwable t) {
                    printStackTrace(t);
                    throwables.add(t);
                }
            }
        }

        return State.END;
    }
}
