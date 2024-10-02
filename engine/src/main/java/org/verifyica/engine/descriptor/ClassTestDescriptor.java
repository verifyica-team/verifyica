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

import static java.lang.String.format;

import io.github.thunkware.vt.bridge.ThreadNameRunnable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.verifyica.api.ClassContext;
import org.verifyica.api.ClassInterceptor;
import org.verifyica.api.EngineContext;
import org.verifyica.api.Store;
import org.verifyica.engine.common.DirectExecutorService;
import org.verifyica.engine.common.SemaphoreRunnable;
import org.verifyica.engine.context.ConcreteClassContext;
import org.verifyica.engine.inject.Inject;
import org.verifyica.engine.inject.Injector;
import org.verifyica.engine.inject.Named;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;
import org.verifyica.engine.support.ExecutorSupport;
import org.verifyica.engine.support.HashSupport;

/** Class to implement ClassTestDescriptor */
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

    private final int testArgumentParallelism;
    private final Class<?> testClass;
    private final List<Method> prepareMethods;
    private final List<Method> concludeMethods;
    private final List<Object> invocationArguments;
    private final List<Throwable> throwables;

    @Inject
    @Named("engineExecutionListener")
    private EngineExecutionListener engineExecutionListener;

    @Inject
    @Named("classInterceptors")
    private List<ClassInterceptor> classInterceptors;

    @Inject
    @Named("classInterceptorsReversed")
    private List<ClassInterceptor> classInterceptorsReversed;

    @Inject
    @Named("argumentExecutorService")
    private ExecutorService argumentExecutorService;

    @Inject
    @Named("engineContext")
    private EngineContext engineContext;

    private ClassContext classContext;
    private final AtomicReference<Object> testInstanceAtomicReference;

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
                    engineContext, testClass, getDisplayName(), testArgumentParallelism, testInstanceAtomicReference);

            classInterceptors = classInterceptors.stream()
                    .filter(classInterceptor -> {
                        Predicate<ClassContext> predicate = classInterceptor.predicate();
                        return predicate == null || predicate.test(classContext);
                    })
                    .collect(Collectors.toList());

            classInterceptorsReversed = new ArrayList<>(classInterceptors);
            Collections.reverse(classInterceptorsReversed);

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
            TestDescriptorStatus testDescriptorStatus;

            if (throwables.isEmpty()) {
                testExecutionResult = TestExecutionResult.successful();
                testDescriptorStatus = TestDescriptorStatus.passed();
            } else {
                testExecutionResult = TestExecutionResult.failed(throwables.get(0));
                testDescriptorStatus = TestDescriptorStatus.failed(throwables.get(0));
            }

            setTestDescriptorStatus(testDescriptorStatus);
            engineExecutionListener.executionFinished(this, testExecutionResult);
        } catch (Throwable t) {
            printStackTrace(t);
            setTestDescriptorStatus(TestDescriptorStatus.failed(t));
            engineExecutionListener.executionFinished(this, TestExecutionResult.failed(t));
        }

        return this;
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
                Object object = getTestClass().getConstructor().newInstance();

                Injector.inject("onfiguration", classContext.getConfiguration(), object);

                testInstanceAtomicReference.set(object);
                invocationArguments.add(object);
                invocationArguments.add(classContext);
            } catch (Throwable t) {
                throwable = t.getCause();
            }
        }

        if (!classInterceptorsReversed.isEmpty()) {
            try {
                for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                    classInterceptor.postInstantiate(
                            engineContext, testClass, testInstanceAtomicReference.get(), throwable);
                }
            } catch (Throwable t) {
                throwable = t;
            }
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
                    invoke(method, testInstanceAtomicReference.get(), invocationArguments);
                }
            } catch (InvocationTargetException e) {
                throwable = e.getCause();
            } catch (Throwable t) {
                throwable = t;
            }
        }

        if (!classInterceptorsReversed.isEmpty()) {
            try {
                for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                    classInterceptor.postPrepare(classContext, throwable);
                }
            } catch (Throwable t) {
                throwable = t;
                printStackTrace(throwable);
                throwables.add(throwable);
            }
        } else if (throwable != null) {
            printStackTrace(throwable);
            throwables.add(throwable);
        }

        return throwable == null ? State.TEST : State.CONCLUDE;
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
            Injector.inject("engineExecutionListener", engineExecutionListener, testableTestDescriptor);
            Injector.inject("classInterceptors", classInterceptors, testableTestDescriptor);
            Injector.inject("classInterceptorsReversed", classInterceptorsReversed, testableTestDescriptor);
            Injector.inject("classContext", classContext, testableTestDescriptor);

            String threadName = Thread.currentThread().getName();
            threadName = threadName.substring(0, threadName.indexOf("/") + 1) + HashSupport.alphanumeric(6);
            ThreadNameRunnable threadNameRunnable = new ThreadNameRunnable(threadName, testableTestDescriptor::test);
            SemaphoreRunnable semaphoreRunnable = new SemaphoreRunnable(semaphore, threadNameRunnable);
            Future<?> future = executorService.submit(semaphoreRunnable);
            futures.add(future);
        }

        ExecutorSupport.waitForAllFutures(futures, argumentExecutorService);

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
                    invoke(method, testInstanceAtomicReference.get(), invocationArguments);
                }
            } catch (InvocationTargetException e) {
                throwable = e.getCause();
            } catch (Throwable t) {
                throwable = t;
            }
        }

        if (!classInterceptorsReversed.isEmpty()) {
            try {
                for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                    classInterceptor.postConclude(classContext, throwable);
                }
            } catch (Throwable t) {
                throwable = t;
                printStackTrace(throwable);
                throwables.add(throwable);
            }
        } else if (throwable != null) {
            printStackTrace(throwable);
            throwables.add(throwable);
        }

        return State.DESTROY;
    }

    private State doDestroy() {
        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                classInterceptor.onDestroy(classContext);
            }
        } catch (Throwable t) {
            throwable = t;
            printStackTrace(throwable);
            throwables.add(throwable);
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
