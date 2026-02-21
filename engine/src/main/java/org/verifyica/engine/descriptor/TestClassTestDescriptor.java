/*
 * Copyright (C) Verifyica project authors and contributors. All rights reserved.
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

import static java.lang.String.format;
import static java.util.Optional.of;

import io.github.thunkware.vt.bridge.ThreadNameRunnable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.verifyica.api.ClassContext;
import org.verifyica.api.ClassInterceptor;
import org.verifyica.api.EngineContext;
import org.verifyica.api.Execution;
import org.verifyica.api.Verifyica;
import org.verifyica.engine.common.throttle.Throttle;
import org.verifyica.engine.configuration.Constants;
import org.verifyica.engine.context.ConcreteClassContext;
import org.verifyica.engine.inject.Injector;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;
import org.verifyica.engine.support.ExecutorServiceSupport;
import org.verifyica.engine.support.HashSupport;

/**
 * Test descriptor for a test class. Manages the lifecycle of test class execution including
 * instantiation, preparation, test execution, conclusion, and cleanup.
 */
public class TestClassTestDescriptor extends TestableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestClassTestDescriptor.class);

    private enum State {
        START,
        INSTANTIATE,
        PREPARE,
        TEST,
        SKIP,
        CONCLUDE,
        DESTROY,
        CLOSE,
        CLEAN_UP,
        END
    }

    private final Set<String> tags;
    private final int testArgumentParallelism;
    private final Class<?> testClass;
    private final List<Method> prepareMethods;
    private final List<Method> concludeMethods;
    private final List<Object> invocationArguments;
    private final List<Throwable> throwables;

    @Inject
    @Named(ENGINE_EXECUTION_LISTENER)
    private EngineExecutionListener engineExecutionListener;

    @Inject
    @Named(CLASS_INTERCEPTORS)
    private List<ClassInterceptor> classInterceptors;

    @Inject
    @Named(CLASS_INTERCEPTORS_REVERSED)
    private List<ClassInterceptor> classInterceptorsReversed;

    @Inject
    @Named(TEST_ARGUMENT_EXECUTOR_SERVICE)
    private ExecutorService testArgumentExecutorService;

    @Inject
    @Named(ENGINE_CONTEXT)
    private EngineContext engineContext;

    private ClassContext classContext;
    private final AtomicReference<Object> testInstanceAtomicReference;
    private boolean markedSkipped;

    /**
     * Creates a new test class test descriptor.
     *
     * @param uniqueId the unique id
     * @param displayName the display name
     * @param tags the tags
     * @param testClass the test class
     * @param testArgumentParallelism the test argument parallelism
     * @param prepareMethods the prepare methods
     * @param concludeMethods the conclude methods
     */
    public TestClassTestDescriptor(
            final UniqueId uniqueId,
            final String displayName,
            final Set<String> tags,
            final Class<?> testClass,
            final int testArgumentParallelism,
            final List<Method> prepareMethods,
            final List<Method> concludeMethods) {
        super(uniqueId, displayName);

        this.tags = tags;
        this.testArgumentParallelism = Math.max(1, testArgumentParallelism);
        this.testClass = testClass;
        this.prepareMethods = prepareMethods;
        this.concludeMethods = concludeMethods;
        this.testInstanceAtomicReference = new AtomicReference<>();
        this.throwables = new ArrayList<>();
        this.invocationArguments = new ArrayList<>();
    }

    @Override
    public Optional<TestSource> getSource() {
        return of(ClassSource.from(testClass));
    }

    /**
     * Returns the test class.
     *
     * @return the test class
     */
    public Class<?> getTestClass() {
        return testClass;
    }

    @Override
    public TestClassTestDescriptor test() {
        try {
            classContext = new ConcreteClassContext(
                    engineContext,
                    testClass,
                    getDisplayName(),
                    tags,
                    testArgumentParallelism,
                    testInstanceAtomicReference);

            classInterceptors = classInterceptors.stream()
                    .filter(classInterceptor -> {
                        final Predicate<ClassContext> predicate = classInterceptor.predicate();

                        return predicate == null || predicate.test(classContext);
                    })
                    .collect(Collectors.toList());

            classInterceptorsReversed = classInterceptorsReversed.stream()
                    .filter(classInterceptor -> {
                        final Predicate<ClassContext> predicate = classInterceptor.predicate();
                        return predicate == null || predicate.test(classContext);
                    })
                    .collect(Collectors.toList());

            engineExecutionListener.executionStarted(this);

            for (final TestDescriptor testDescriptor : getChildren()) {
                Injector.inject(ENGINE_EXECUTION_LISTENER, engineExecutionListener, testDescriptor);
                Injector.inject(CLASS_INTERCEPTORS, classInterceptors, testDescriptor);
                Injector.inject(CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, testDescriptor);
                Injector.inject(CLASS_CONTEXT, classContext, testDescriptor);
            }

            final Throttle throttle =
                    createThrottle(classContext.getConfiguration(), Constants.ENGINE_CLASS_STATE_MACHINE_THROTTLE);

            State state = State.START;

            while (state != State.END) {
                LOGGER.trace("testDescriptor [%s] state [%s]", this, state);

                throttle.throttle();

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
                        state = doSkipChildren();
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
                        state = doCloseInstance();
                        break;
                    }
                    case CLEAN_UP: {
                        state = doCleanupClassContext();
                        break;
                    }
                    default: {
                        throw new IllegalStateException(format("Invalid State [%s]", state));
                    }
                }
            }

            final TestExecutionResult testExecutionResult;
            final TestDescriptorStatus testDescriptorStatus;

            if (markedSkipped) {
                setTestDescriptorStatus(TestDescriptorStatus.skipped());
                engineExecutionListener.executionSkipped(this, "Skipped");
            } else {
                if (throwables.isEmpty()) {
                    testExecutionResult = TestExecutionResult.successful();
                    testDescriptorStatus = TestDescriptorStatus.passed();
                } else {
                    testExecutionResult = TestExecutionResult.failed(throwables.get(0));
                    testDescriptorStatus = TestDescriptorStatus.failed(throwables.get(0));
                }

                setTestDescriptorStatus(testDescriptorStatus);
                engineExecutionListener.executionFinished(this, testExecutionResult);
            }
        } catch (final Throwable t) {
            printStackTrace(t);
            setTestDescriptorStatus(TestDescriptorStatus.failed(t));
            engineExecutionListener.executionFinished(this, TestExecutionResult.failed(t));
        }

        return this;
    }

    @Override
    public void skip() {
        engineExecutionListener.executionStarted(this);

        getChildren().stream().map(TESTABLE_TEST_DESCRIPTOR_MAPPER).forEach(TestableTestDescriptor::skip);

        engineExecutionListener.executionSkipped(this, "Skipped");

        setTestDescriptorStatus(TestDescriptorStatus.skipped());
    }

    /**
     * Instantiates the test class.
     *
     * @return the next state
     */
    private State doInstantiate() {
        Throwable throwable = null;

        try {
            for (final ClassInterceptor classInterceptor : classInterceptors) {
                classInterceptor.preInstantiate(engineContext, testClass);
            }
        } catch (final Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                Injector.inject(Verifyica.Autowired.class, classContext.getConfiguration(), getTestClass());
                Injector.inject(Verifyica.Autowired.class, engineContext, getTestClass());

                final Object object = getTestClass().getConstructor().newInstance();

                Injector.inject(Verifyica.Autowired.class, classContext.getConfiguration(), object);
                Injector.inject(Verifyica.Autowired.class, engineContext, object);

                testInstanceAtomicReference.set(object);

                invocationArguments.add(object);
                invocationArguments.add(classContext);
            } catch (final InvocationTargetException e) {
                throwable = e.getCause();
            } catch (final Throwable t) {
                throwable = t;
            }
        }

        try {
            for (final ClassInterceptor classInterceptor : classInterceptorsReversed) {
                classInterceptor.postInstantiate(
                        engineContext, testClass, testInstanceAtomicReference.get(), throwable);
            }
        } catch (final Throwable t) {
            // postInstantiate failing should fail the class
            if (throwable == null) {
                throwable = t;
            } else {
                throwable.addSuppressed(t);
            }
        }

        if (throwable != null && !throwables.contains(throwable)) {
            throwables.add(throwable);
        }

        return throwable == null ? State.PREPARE : State.CLEAN_UP;
    }

    /**
     * Prepares the test class.
     *
     * @return the next state
     */
    private State doPrepare() {
        Throwable throwable = null;

        try {
            for (final ClassInterceptor classInterceptor : classInterceptors) {
                classInterceptor.prePrepare(classContext);
            }
        } catch (final Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                for (final Method method : prepareMethods) {
                    invoke(method, testInstanceAtomicReference.get(), invocationArguments, true);
                }
            } catch (final InvocationTargetException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof Execution.ExecutionSkippedException) {
                    markedSkipped = true;
                } else {
                    throwable = cause;
                }
            } catch (final Throwable t) {
                throwable = t;
            }
        }

        try {
            for (final ClassInterceptor classInterceptor : classInterceptorsReversed) {
                classInterceptor.postPrepare(classContext, throwable);
            }
        } catch (final Throwable t) {
            if (throwable == null) {
                throwable = t;
            } else {
                throwable.addSuppressed(t);
            }
            printStackTrace(t);
        }

        if (markedSkipped) {
            return State.SKIP;
        } else if (throwable == null) {
            return State.TEST;
        } else {
            if (!throwables.contains(throwable)) {
                throwables.add(throwable);
            }
            return State.SKIP;
        }
    }

    /**
     * Executes child test descriptors.
     *
     * @return the next state
     */
    private State doTest() {
        final List<TestableTestDescriptor> testableTestDescriptors = getChildren().stream()
                .filter(TESTABLE_TEST_DESCRIPTOR_FILTER)
                .map(TESTABLE_TEST_DESCRIPTOR_MAPPER)
                .collect(Collectors.toList());

        // Single-threaded test argument execution
        if (testArgumentParallelism <= 1) {
            for (final TestableTestDescriptor testableTestDescriptor : testableTestDescriptors) {
                testableTestDescriptor.test();
            }
            collectChildFailures(testableTestDescriptors);
            return State.CONCLUDE;
        }

        // Parallel test argument execution
        final List<Future<?>> futures = new ArrayList<>(testableTestDescriptors.size());
        for (final TestableTestDescriptor testableTestDescriptor : testableTestDescriptors) {
            final String threadName = getNextThreadName(Thread.currentThread().getName());
            final Runnable runnable = new ThreadNameRunnable(threadName, testableTestDescriptor::test);
            futures.add(testArgumentExecutorService.submit(runnable));
        }

        ExecutorServiceSupport.waitForAllFutures(futures);

        collectChildFailures(testableTestDescriptors);

        return State.CONCLUDE;
    }

    /**
     * Collects failures from child test descriptors.
     *
     * @param children the child test descriptors
     */
    private void collectChildFailures(final List<TestableTestDescriptor> children) {
        for (final TestableTestDescriptor child : children) {
            final TestDescriptorStatus status = child.getTestDescriptorStatus();
            if (status != null && status.isFailure()) {
                final Throwable t = status.getThrowable();
                if (t != null && !throwables.contains(t)) {
                    throwables.add(t);
                } else if (t == null && throwables.isEmpty()) {
                    // ensure the class is not reported as passed if a child failed without a throwable
                    throwables.add(new AssertionError("Child test descriptor failed without throwable: " + child));
                }
                break; // fail-fast at class level; remove if you want to aggregate
            }
        }
    }

    /**
     * Skips child test descriptors.
     *
     * @return the next state
     */
    private State doSkipChildren() {
        getChildren().stream().map(TESTABLE_TEST_DESCRIPTOR_MAPPER).forEach(testableTestDescriptor -> {
            Injector.inject(ENGINE_EXECUTION_LISTENER, engineExecutionListener, testableTestDescriptor);
            Injector.inject(CLASS_INTERCEPTORS, classInterceptors, testableTestDescriptor);
            Injector.inject(CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, testableTestDescriptor);
            Injector.inject(CLASS_CONTEXT, classContext, testableTestDescriptor);
            testableTestDescriptor.skip();
        });

        return State.CONCLUDE;
    }

    /**
     * Concludes the test class.
     *
     * @return the next state
     */
    private State doConclude() {
        Throwable throwable = null;

        try {
            for (final ClassInterceptor classInterceptor : classInterceptors) {
                classInterceptor.preConclude(classContext);
            }
        } catch (final Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                for (final Method method : concludeMethods) {
                    invoke(method, testInstanceAtomicReference.get(), invocationArguments, true);
                }
            } catch (final InvocationTargetException e) {
                final Throwable cause = e.getCause();
                if (!(cause instanceof Execution.ExecutionSkippedException)) {
                    throwable = cause;
                }
            } catch (final Throwable t) {
                throwable = t;
            }
        }

        try {
            for (final ClassInterceptor classInterceptor : classInterceptorsReversed) {
                classInterceptor.postConclude(classContext, throwable);
            }
        } catch (final Throwable t) {
            if (throwable == null) {
                throwable = t;
            } else {
                throwable.addSuppressed(t);
            }
            printStackTrace(t);
        }

        if (throwable != null && !throwables.contains(throwable)) {
            throwables.add(throwable);
        }

        return State.DESTROY;
    }

    /**
     * Calls test class interceptors onDestroy.
     *
     * @return the next state
     */
    private State doDestroy() {
        try {
            for (final ClassInterceptor classInterceptor : classInterceptorsReversed) {
                classInterceptor.onDestroy(classContext);
            }
        } catch (final Throwable t) {
            printStackTrace(t);
            throwables.add(t);
        }

        return State.CLOSE;
    }

    /**
     * Closes the test class instance.
     *
     * @return the next state
     */
    private State doCloseInstance() {
        final Object instance = testInstanceAtomicReference.getAndSet(null);

        if (instance instanceof AutoCloseable) {
            try {
                ((AutoCloseable) instance).close();
            } catch (final Throwable t) {
                printStackTrace(t);
                throwables.add(t);
            }
        }

        return State.CLEAN_UP;
    }

    /**
     * Cleans up the class context.
     *
     * @return the next state
     */
    private State doCleanupClassContext() {
        final Map<String, Object> map = classContext.getMap();

        // Avoid CME if close() mutates the map
        final List<Object> values = new ArrayList<>(map.values());
        for (final Object value : values) {
            if (value instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) value).close();
                } catch (final Throwable t) {
                    printStackTrace(t);
                    throwables.add(t);
                }
            }
        }

        map.clear();

        return State.END;
    }

    /**
     * Returns the next thread name based on the current thread name.
     *
     * @param currentThreadName the current thread name
     * @return the next thread name
     */
    private static String getNextThreadName(final String currentThreadName) {
        final int slash = currentThreadName.indexOf('/');
        final String baseThreadName = slash >= 0 ? currentThreadName.substring(0, slash + 1) : currentThreadName + "/";
        return baseThreadName + HashSupport.alphanumeric(6);
    }
}
