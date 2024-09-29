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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.verifyica.api.Argument;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Store;
import org.verifyica.api.interceptor.ArgumentInterceptorContext;
import org.verifyica.api.interceptor.ClassInterceptor;
import org.verifyica.engine.context.ConcreteArgumentContext;
import org.verifyica.engine.context.ConcreteArgumentInterceptorContext;
import org.verifyica.engine.injection.FieldInjector;
import org.verifyica.engine.injection.Inject;
import org.verifyica.engine.interceptor.ClassInterceptorRegistry;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;

/** Class to implement ArgumentTestDescriptor */
public class ArgumentTestDescriptor extends TestableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentTestDescriptor.class);

    private enum State {
        START,
        BEFORE_ALL,
        TEST_DEPENDENT,
        TEST_INDEPENDENT,
        SKIP,
        AFTER_ALL,
        CLOSE,
        CLEAN_UP,
        END
    }

    private final int argumentIndex;
    private final Argument<?> argument;
    private final List<Method> beforeAllMethods;
    private final List<Method> afterAllMethods;
    private final List<Throwable> throwables;

    @Inject
    private ClassContext classContext;

    @Inject
    private ClassInterceptorRegistry classInterceptorRegistry;

    private List<ClassInterceptor> classInterceptors;
    private List<ClassInterceptor> classInterceptorsReversed;
    private Class<?> testClass;
    private Object testInstance;

    private ArgumentContext argumentContext;
    private ArgumentInterceptorContext argumentInterceptorContext;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param argumentIndex argumentIndex
     * @param argument argument
     * @param beforeAllMethods beforeAllMethods
     * @param afterAllMethods afterAllMethods
     */
    public ArgumentTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            int argumentIndex,
            Argument<?> argument,
            List<Method> beforeAllMethods,
            List<Method> afterAllMethods) {
        super(uniqueId, displayName);

        this.argumentIndex = argumentIndex;
        this.argument = argument;
        this.beforeAllMethods = beforeAllMethods;
        this.afterAllMethods = afterAllMethods;
        this.throwables = new ArrayList<>();
    }

    /**
     * Method to get argument
     *
     * @return the argument
     */
    public Argument<?> getArgument() {
        return argument;
    }

    @Override
    public ArgumentTestDescriptor test() {
        try {
            engineExecutionListener.executionStarted(this);

            State state = State.START;
            while (state != State.END) {
                LOGGER.trace("testDescriptor [%s] state [%s]", this, state);

                switch (state) {
                    case START: {
                        prepare();
                        state = State.BEFORE_ALL;
                        break;
                    }
                    case BEFORE_ALL: {
                        state = doBeforeAll();
                        break;
                    }
                    case TEST_DEPENDENT: {
                        state = doTestDependent();
                        break;
                    }
                    case TEST_INDEPENDENT: {
                        state = doTestIndependent();
                        break;
                    }
                    case SKIP: {
                        skip();
                        state = State.AFTER_ALL;
                        break;
                    }
                    case AFTER_ALL: {
                        state = doAfterAll();
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

    private void prepare() throws Throwable {
        checkInjected(engineExecutionListener, "engineExecutionListener not injected");
        checkInjected(engineInterceptorRegistry, "engineInterceptorRegistry not injected");
        checkInjected(classInterceptorRegistry, "classInterceptorRegistry not injected");
        checkInjected(classContext, "classContext not injected");

        testClass = classContext.getTestClass();

        classInterceptors = classInterceptorRegistry.getClassInterceptors(testClass);

        classInterceptorsReversed = new ArrayList<>(classInterceptors);
        Collections.reverse(classInterceptorsReversed);

        argumentContext = new ConcreteArgumentContext(classContext, argumentIndex, argument);
        argumentInterceptorContext = new ConcreteArgumentInterceptorContext(argumentContext);

        FieldInjector.injectFields(this, argumentContext);
        FieldInjector.injectFields(this, argumentInterceptorContext);

        testClass = argumentContext.getClassContext().getTestClass();
        testInstance = argumentContext.getClassContext().getTestInstance();
    }

    private State doBeforeAll() {
        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : classInterceptors) {
                classInterceptor.preBeforeAll(argumentInterceptorContext);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                for (Method method : beforeAllMethods) {
                    method.invoke(testInstance, argumentContext);
                }
            } catch (Throwable t) {
                throwable = t.getCause();
            }
        }

        try {
            for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                classInterceptor.postBeforeAll(argumentInterceptorContext, throwable);
            }
        } catch (Throwable t) {
            throwable = t;
            printStackTrace(throwable);
            throwables.add(throwable);
        }

        State testState = State.TEST_DEPENDENT;

        return throwable == null ? testState : State.AFTER_ALL;
    }

    private State doTestDependent() {
        Iterator<TestableTestDescriptor> executableTestDescriptorIterator =
                getChildren().stream().map(TESTABLE_TEST_DESCRIPTOR_MAPPER).iterator();

        FieldInjector.injectFields(this, argumentContext);

        while (executableTestDescriptorIterator.hasNext()) {
            if (executableTestDescriptorIterator
                    .next()
                    .test()
                    .getTestDescriptorStatus()
                    .isFailure()) {
                break;
            }
        }

        while (executableTestDescriptorIterator.hasNext()) {
            TestableTestDescriptor testableTestDescriptor = executableTestDescriptorIterator.next();
            testableTestDescriptor.skip();
        }

        return State.AFTER_ALL;
    }

    private State doTestIndependent() {
        Iterator<TestableTestDescriptor> executableTestDescriptorIterator =
                getChildren().stream().map(TESTABLE_TEST_DESCRIPTOR_MAPPER).iterator();

        while (executableTestDescriptorIterator.hasNext()) {
            TestableTestDescriptor testableTestDescriptor = executableTestDescriptorIterator.next();
            FieldInjector.injectFields(testableTestDescriptor, argumentContext);
            testableTestDescriptor.test();
        }

        return State.AFTER_ALL;
    }

    private State doAfterAll() {
        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : classInterceptors) {
                classInterceptor.preAfterAll(argumentInterceptorContext);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                for (Method method : afterAllMethods) {
                    method.invoke(testInstance, argumentContext);
                }
            } catch (Throwable t) {
                throwable = t.getCause();
            }
        }

        try {
            for (ClassInterceptor classInterceptor : classInterceptorRegistry.getClassInterceptors(testClass)) {
                classInterceptor.postAfterAll(argumentInterceptorContext, throwable);
            }
        } catch (Throwable t) {
            throwable = t;
            printStackTrace(throwable);
            throwables.add(throwable);
        }

        return State.CLOSE;
    }

    private State doClose() {
        if (argument instanceof AutoCloseable) {
            try {
                ((AutoCloseable) argument).close();
            } catch (Throwable t) {
                printStackTrace(t);
                throwables.add(t);
            }
        }

        return State.CLEAN_UP;
    }

    private State doCleanup() {
        Store store = argumentContext.getStore();

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
