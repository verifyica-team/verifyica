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
import java.util.List;
import java.util.Optional;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.interceptor.ArgumentInterceptorContext;
import org.verifyica.api.interceptor.ClassInterceptor;
import org.verifyica.engine.context.ConcreteArgumentInterceptorContext;
import org.verifyica.engine.injection.Inject;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;

public class ExecutableMethodTestDescriptor extends ExecutableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutableMethodTestDescriptor.class);

    private enum State {
        START,
        BEFORE_EACH,
        TEST,
        AFTER_EACH,
        END
    }

    private final List<Method> beforeEachMethods;
    private final Method testMethod;
    private final List<Method> afterEachMethods;
    private final List<Throwable> throwables;

    @Inject
    private ArgumentContext argumentContext;

    @Inject
    private ArgumentInterceptorContext argumentInterceptorContext;

    private List<ClassInterceptor> classInterceptors;
    private List<ClassInterceptor> classInterceptorsReversed;

    private Class<?> testClass;
    private Object testInstance;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param beforeEachMethods beforeEachMethods
     * @param testMethod testMethod
     * @param afterEachMethods afterEachMethods
     */
    public ExecutableMethodTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            List<Method> beforeEachMethods,
            Method testMethod,
            List<Method> afterEachMethods) {
        super(uniqueId, displayName);

        this.beforeEachMethods = beforeEachMethods;
        this.testMethod = testMethod;
        this.afterEachMethods = afterEachMethods;
        this.throwables = new ArrayList<>();
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
     * Method to get the test Method
     *
     * @return the test Method
     */
    public Method getTestMethod() {
        return testMethod;
    }

    @Override
    public ExecutableMethodTestDescriptor test() {
        try {
            checkInjected(engineExecutionListener, "engineExecutionListener not injected");
            checkInjected(engineInterceptorRegistry, "engineInterceptorRegistry not injected");
            checkInjected(classInterceptorRegistry, "classInterceptorRegistry not injected");
            checkInjected(argumentContext, "argumentContext not injected");
            checkInjected(argumentInterceptorContext, "argumentInterceptorContext not injected");

            classInterceptors = classInterceptorRegistry.getClassInterceptors(argumentContext.getClassContext().getTestClass());

            classInterceptorsReversed = new ArrayList<>(classInterceptors);
            Collections.reverse(classInterceptorsReversed);

            testClass = argumentContext.getClassContext().getTestClass();
            testInstance = argumentContext.getClassContext().getTestInstance();

            engineExecutionListener.executionStarted(this);

            State state = State.START;
            while (state != State.END) {
                LOGGER.trace("testDescriptor [%s] state [%s]", this, state);

                switch (state) {
                    case START: {
                        state = State.BEFORE_EACH;
                        break;
                    }
                    case BEFORE_EACH: {
                        state = doBeforeEach();
                        break;
                    }
                    case TEST: {
                        state = doTest();
                        break;
                    }
                    case AFTER_EACH: {
                        state = doAfterEach();
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

    private State doBeforeEach() {
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
                for (Method method : beforeEachMethods) {
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

        return throwable == null ? State.TEST : State.AFTER_EACH;
    }

    private State doTest() {
        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : classInterceptorRegistry.getClassInterceptors(testClass)) {
                classInterceptor.preTest(argumentInterceptorContext, testMethod);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                testMethod.invoke(testInstance, argumentContext);
            } catch (Throwable t) {
                throwable = t.getCause();
            }
        }

        try {
            for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                classInterceptor.postTest(argumentInterceptorContext, testMethod, throwable);
            }
        } catch (Throwable t) {
            throwable = throwable != null ? t : throwable;
            printStackTrace(throwable);
            throwables.add(throwable);
        }

        return State.AFTER_EACH;
    }

    private State doAfterEach() {
        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : classInterceptorRegistry.getClassInterceptors(testClass)) {
                classInterceptor.preAfterEach(argumentInterceptorContext);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                for (Method method : afterEachMethods) {
                    method.invoke(testInstance, argumentContext);
                }
            } catch (Throwable t) {
                throwable = t.getCause();
            }
        }

        try {
            for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                classInterceptor.postAfterEach(argumentInterceptorContext, throwable);
            }
        } catch (Throwable t) {
            throwable = throwable != null ? t : throwable;
            printStackTrace(throwable);
            throwables.add(throwable);
        }

        return State.END;
    }
}
