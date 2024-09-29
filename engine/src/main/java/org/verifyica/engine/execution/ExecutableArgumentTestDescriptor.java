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
import org.verifyica.api.Verifyica;
import org.verifyica.api.interceptor.ArgumentInterceptorContext;
import org.verifyica.api.interceptor.ClassInterceptor;
import org.verifyica.engine.context.ConcreteArgumentContext;
import org.verifyica.engine.context.ConcreteArgumentInterceptorContext;
import org.verifyica.engine.injection.FieldInjector;
import org.verifyica.engine.injection.Inject;
import org.verifyica.engine.interceptor.ClassInterceptorRegistry;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;

public class ExecutableArgumentTestDescriptor extends ExecutableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutableArgumentTestDescriptor.class);

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
    public ExecutableArgumentTestDescriptor(
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

    public Argument<?> getArgument() {
        return argument;
    }

    @Override
    public ExecutableArgumentTestDescriptor test() {
        try {
            checkInjected(engineExecutionListener, "engineExecutionListener not injected");
            checkInjected(engineInterceptorRegistry, "engineInterceptorRegistry not injected");
            checkInjected(classInterceptorRegistry, "classInterceptorRegistry not injected");
            checkInjected(classContext, "classContext not injected");

            classInterceptors = classInterceptorRegistry.getClassInterceptors(testClass);

            classInterceptorsReversed = new ArrayList<>(classInterceptors);
            Collections.reverse(classInterceptorsReversed);

            argumentContext = new ConcreteArgumentContext(classContext, argumentIndex, argument);
            argumentInterceptorContext = new ConcreteArgumentInterceptorContext(argumentContext);

            FieldInjector.injectFields(this, argumentContext);
            FieldInjector.injectFields(this, argumentInterceptorContext);

            testClass = argumentContext.getClassContext().getTestClass();
            testInstance = argumentContext.getClassContext().getTestInstance();

            engineExecutionListener.executionStarted(this);

            State state = State.START;
            while (state != State.END) {
                LOGGER.trace("testDescriptor [%s] state [%s]", this, state);

                switch (state) {
                    case START: {
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

    private State doBeforeAll() {
        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : classInterceptorRegistry.getClassInterceptors(testClass)) {
                classInterceptor.preBeforeEach(argumentInterceptorContext);
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
                classInterceptor.postBeforeEach(argumentInterceptorContext, throwable);
            }
        } catch (Throwable t) {
            throwable = throwable != null ? t : throwable;
            printStackTrace(throwable);
            throwables.add(throwable);
        }

        State testState;
        if (testClass.isAnnotationPresent(Verifyica.IndependentTests.class)) {
            testState = State.TEST_INDEPENDENT;
        } else {
            testState = State.TEST_DEPENDENT;
        }

        return throwable == null ? testState : State.AFTER_ALL;
    }

    private State doTestDependent() {
        Iterator<ExecutableTestDescriptor> executableTestDescriptorIterator =
                getChildren().stream().map(EXECUTABLE_TEST_DESCRIPTOR_MAPPER).iterator();

        FieldInjector.injectFields(this, argumentContext);

        while (executableTestDescriptorIterator.hasNext()) {
            if (executableTestDescriptorIterator
                    .next()
                    .test()
                    .getExecutionResult()
                    .isFailure()) {
                break;
            }
        }

        while (executableTestDescriptorIterator.hasNext()) {
            ExecutableTestDescriptor executableTestDescriptor = executableTestDescriptorIterator.next();
            executableTestDescriptor.skip();
        }

        return State.AFTER_ALL;
    }

    private State doTestIndependent() {
        Iterator<ExecutableTestDescriptor> executableTestDescriptorIterator =
                getChildren().stream().map(EXECUTABLE_TEST_DESCRIPTOR_MAPPER).iterator();

        while (executableTestDescriptorIterator.hasNext()) {
            ExecutableTestDescriptor executableTestDescriptor = executableTestDescriptorIterator.next();
            FieldInjector.injectFields(executableTestDescriptor, argumentContext);
            executableTestDescriptor.test();
        }

        return State.AFTER_ALL;
    }

    private State doAfterAll() {
        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : classInterceptorRegistry.getClassInterceptors(testClass)) {
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
            throwable = throwable != null ? t : throwable;
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
