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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.Assumptions;
import org.verifyica.api.ClassInterceptor;
import org.verifyica.engine.inject.Inject;
import org.verifyica.engine.inject.Named;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;

/** Class to implement TestMethodDescriptor */
public class TestMethodTestDescriptor extends TestableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestMethodTestDescriptor.class);

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
    @Named(ARGUMENT_CONTEXT)
    private ArgumentContext argumentContext;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param beforeEachMethods beforeEachMethods
     * @param testMethod testMethod
     * @param afterEachMethods afterEachMethods
     */
    public TestMethodTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            List<Method> beforeEachMethods,
            Method testMethod,
            List<Method> afterEachMethods) {
        super(uniqueId, displayName);

        this.beforeEachMethods = beforeEachMethods;
        this.testMethod = testMethod;
        this.afterEachMethods = afterEachMethods;
        this.invocationArguments = new ArrayList<>();
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
    public TestMethodTestDescriptor test() {
        try {
            invocationArguments.add(argumentContext.getTestArgument().getPayload());
            invocationArguments.add(argumentContext.getTestArgument());
            invocationArguments.add(argumentContext);

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
            TestDescriptorStatus testDescriptorStatus;

            if (throwables.isEmpty()) {
                testExecutionResult = TestExecutionResult.successful();
                testDescriptorStatus = TestDescriptorStatus.passed();
            } else {
                Throwable throwable = throwables.get(0);

                if (throwable instanceof Assumptions.Failed) {
                    testExecutionResult = TestExecutionResult.aborted(null);
                    testDescriptorStatus = TestDescriptorStatus.skipped();
                } else {
                    testExecutionResult = TestExecutionResult.failed(throwables.get(0));
                    testDescriptorStatus = TestDescriptorStatus.failed(throwables.get(0));
                }
            }

            setTestDescriptorStatus(testDescriptorStatus);

            if (testDescriptorStatus.isSkipped()) {
                engineExecutionListener.executionSkipped(this, "Skipped");
            } else {
                engineExecutionListener.executionFinished(this, testExecutionResult);
            }
        } catch (Throwable t) {
            printStackTrace(t);
            setTestDescriptorStatus(TestDescriptorStatus.failed(t));
            engineExecutionListener.executionFinished(this, TestExecutionResult.failed(t));
        }

        return this;
    }

    private State doBeforeEach() {
        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : classInterceptors) {
                classInterceptor.preBeforeEach(argumentContext);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                for (Method method : beforeEachMethods) {
                    invoke(method, argumentContext.getClassContext().getTestInstance(), invocationArguments);
                }
            } catch (InvocationTargetException e) {
                throwable = e.getCause();
            } catch (Throwable t) {
                throwable = t;
            }
        }

        if (!classInterceptorsReversed.isEmpty()) {
            try {
                Throwable t = throwable instanceof Assumptions.Failed ? null : throwable;
                for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                    classInterceptor.postBeforeEach(argumentContext, t);
                }
                if (throwable != null) {
                    throwables.add(throwable);
                }
            } catch (Throwable t) {
                throwable = t;
                printStackTrace(throwable);
                throwables.add(throwable);
            }
        } else if (throwable != null) {
            if (!(throwable instanceof Assumptions.Failed)) {
                printStackTrace(throwable);
            }
            throwables.add(throwable);
        }

        return throwable == null ? State.TEST : State.AFTER_EACH;
    }

    private State doTest() {
        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : classInterceptors) {
                classInterceptor.preTest(argumentContext, testMethod);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                invoke(testMethod, argumentContext.getClassContext().getTestInstance(), invocationArguments);
            } catch (InvocationTargetException e) {
                throwable = e.getCause();
            } catch (Throwable t) {
                throwable = t;
            }
        }

        try {
            Throwable t = throwable instanceof Assumptions.Failed ? null : throwable;
            for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                classInterceptor.postTest(argumentContext, testMethod, t);
            }
        } catch (Throwable t) {
            throwable = t;
            printStackTrace(throwable);
            throwables.add(throwable);
        }

        return State.AFTER_EACH;
    }

    private State doAfterEach() {
        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : classInterceptors) {
                classInterceptor.preAfterEach(argumentContext);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                for (Method method : afterEachMethods) {
                    invoke(method, argumentContext.getClassContext().getTestInstance(), invocationArguments);
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
                    classInterceptor.postAfterEach(argumentContext, throwable);
                }
            } catch (Throwable t) {
                printStackTrace(throwable);
                throwables.add(throwable);
            }
        } else if (throwable != null) {
            printStackTrace(throwable);
            throwables.add(throwable);
        }

        return State.END;
    }
}
