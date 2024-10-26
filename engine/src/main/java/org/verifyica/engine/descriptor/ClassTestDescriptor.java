/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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
import org.verifyica.engine.common.DirectExecutorService;
import org.verifyica.engine.common.SemaphoreRunnable;
import org.verifyica.engine.context.ConcreteClassContext;
import org.verifyica.engine.inject.Inject;
import org.verifyica.engine.inject.Injector;
import org.verifyica.engine.inject.Named;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;
import org.verifyica.engine.support.ExecutorServiceSupport;
import org.verifyica.engine.support.HashSupport;

/** Class to implement ClassTestDescriptor */
@SuppressWarnings("deprecation")
public class ClassTestDescriptor extends TestableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassTestDescriptor.class);

    private static final ExecutorService DIRECT_EXECUTOR_SERVICE = new DirectExecutorService();

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
    @Named(ARGUMENT_EXECUTOR_SERVICE)
    private ExecutorService argumentExecutorService;

    @Inject
    @Named(ENGINE_CONTEXT)
    private EngineContext engineContext;

    private ClassContext classContext;
    private final AtomicReference<Object> testInstanceAtomicReference;
    private boolean markedSkipped;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param tags tags
     * @param testClass testClass
     * @param testArgumentParallelism testArgumentParallelism
     * @param prepareMethods prepareMethods
     * @param concludeMethods concludeMethods
     */
    public ClassTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Set<String> tags,
            Class<?> testClass,
            int testArgumentParallelism,
            List<Method> prepareMethods,
            List<Method> concludeMethods) {
        super(uniqueId, displayName);

        this.tags = tags;
        this.testArgumentParallelism = testArgumentParallelism;
        this.testClass = testClass;
        this.prepareMethods = prepareMethods;
        this.concludeMethods = concludeMethods;
        this.testInstanceAtomicReference = new AtomicReference<>();
        this.throwables = new ArrayList<>();
        this.invocationArguments = new ArrayList<>();
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(ClassSource.from(testClass));
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
    public ClassTestDescriptor test() {
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
                        Predicate<ClassContext> predicate = classInterceptor.predicate();
                        return predicate == null || predicate.test(classContext);
                    })
                    .collect(Collectors.toList());

            classInterceptorsReversed = classInterceptorsReversed.stream()
                    .filter(classInterceptor -> {
                        Predicate<ClassContext> predicate = classInterceptor.predicate();
                        return predicate == null || predicate.test(classContext);
                    })
                    .collect(Collectors.toList());

            engineExecutionListener.executionStarted(this);

            for (TestDescriptor testDescriptor : getChildren()) {
                Injector.inject(ENGINE_EXECUTION_LISTENER, engineExecutionListener, testDescriptor);
                Injector.inject(CLASS_INTERCEPTORS, classInterceptors, testDescriptor);
                Injector.inject(CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, testDescriptor);
                Injector.inject(CLASS_CONTEXT, classContext, testDescriptor);
            }

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
            TestDescriptorStatus testDescriptorStatus;

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
        } catch (Throwable t) {
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

    private State doInstantiate() {
        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : classInterceptors) {
                classInterceptor.preInstantiate(engineContext, testClass);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                Injector.inject(Verifyica.Autowired.class, classContext.getConfiguration(), getTestClass());
                Injector.inject(Verifyica.Autowired.class, engineContext, getTestClass());

                Object object = getTestClass().getConstructor().newInstance();

                Injector.inject(Verifyica.Autowired.class, classContext.getConfiguration(), object);
                Injector.inject(Verifyica.Autowired.class, engineContext, object);

                testInstanceAtomicReference.set(object);

                invocationArguments.add(object);
                invocationArguments.add(classContext);
            } catch (Throwable t) {
                throwable = t.getCause();
            }
        }

        try {
            for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                classInterceptor.postInstantiate(
                        engineContext, testClass, testInstanceAtomicReference.get(), throwable);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        return throwable == null ? State.PREPARE : State.CLEAN_UP;
    }

    private State doPrepare() {
        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : classInterceptors) {
                classInterceptor.prePrepare(classContext);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                for (Method method : prepareMethods) {
                    invoke(method, testInstanceAtomicReference.get(), invocationArguments, true);
                }
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof Execution.ExecutionSkippedException) {
                    markedSkipped = true;
                } else {
                    throwable = cause;
                }
            } catch (Throwable t) {
                throwable = t;
            }
        }

        try {
            for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                classInterceptor.postPrepare(classContext, throwable);
            }
        } catch (Throwable t) {
            throwable = t;
            printStackTrace(t);
            throwables.add(t);
        }

        if (markedSkipped) {
            return State.SKIP;
        } else if (throwable == null) {
            return State.TEST;
        } else {
            return State.CONCLUDE;
        }
    }

    /**
     * Method to test child test descriptors
     */
    private State doTest() {
        ExecutorService executorService;

        if (testArgumentParallelism > 1) {
            executorService = argumentExecutorService;
        } else {
            executorService = DIRECT_EXECUTOR_SERVICE;
        }

        List<TestableTestDescriptor> testableTestDescriptors = getChildren().stream()
                .filter(TESTABLE_TEST_DESCRIPTOR_FILTER)
                .map(TESTABLE_TEST_DESCRIPTOR_MAPPER)
                .collect(Collectors.toList());

        List<Future<?>> futures = new ArrayList<>();

        Semaphore semaphore = new Semaphore(testArgumentParallelism, true);

        for (TestableTestDescriptor testableTestDescriptor : testableTestDescriptors) {
            String threadName = Thread.currentThread().getName();
            threadName = threadName.substring(0, threadName.indexOf("/") + 1) + HashSupport.alphanumeric(6);
            ThreadNameRunnable threadNameRunnable = new ThreadNameRunnable(threadName, testableTestDescriptor::test);
            SemaphoreRunnable semaphoreRunnable = new SemaphoreRunnable(semaphore, threadNameRunnable);
            Future<?> future = executorService.submit(semaphoreRunnable);
            futures.add(future);
        }

        ExecutorServiceSupport.waitForAllFutures(futures, argumentExecutorService);

        return State.CONCLUDE;
    }

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

    private State doConclude() {
        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : classInterceptors) {
                classInterceptor.preConclude(classContext);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                for (Method method : concludeMethods) {
                    invoke(method, testInstanceAtomicReference.get(), invocationArguments, true);
                }
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (!(cause instanceof Execution.ExecutionSkippedException)) {
                    throwable = cause;
                }
            } catch (Throwable t) {
                throwable = t;
            }
        }

        try {
            for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                classInterceptor.postConclude(classContext, throwable);
            }
        } catch (Throwable t) {
            printStackTrace(t);
            throwables.add(t);
        }

        return State.DESTROY;
    }

    private State doDestroy() {
        try {
            for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                classInterceptor.onDestroy(classContext);
            }
        } catch (Throwable t) {
            printStackTrace(t);
            throwables.add(t);
        }

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
        Map<String, Object> map = classContext.getMap();

        Set<Map.Entry<String, Object>> entrySet = map.entrySet();
        for (Map.Entry<String, Object> entry : entrySet) {
            if (entry.getValue() instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) entry.getValue()).close();
                } catch (Throwable t) {
                    printStackTrace(t);
                    throwables.add(t);
                }
            }
        }

        map.clear();

        return State.END;
    }
}
