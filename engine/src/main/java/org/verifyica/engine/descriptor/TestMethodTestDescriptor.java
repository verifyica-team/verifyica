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
import static java.util.Optional.of;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassInterceptor;
import org.verifyica.api.Execution;
import org.verifyica.engine.common.Throttle;
import org.verifyica.engine.configuration.Constants;
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

    private boolean markSkipped;

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
        return of(MethodSource.from(testMethod));
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

            Throttle throttle =
                    createThrottle(argumentContext.getConfiguration(), Constants.ENGINE_TEST_STATE_MACHINE_THROTTLE);

            State state = State.START;
            while (state != State.END) {
                LOGGER.trace("testDescriptor [%s] state [%s]", this, state);
                throttle.throttle();

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

            if (markSkipped) {
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
        engineExecutionListener.executionSkipped(this, "Skipped");

        setTestDescriptorStatus(TestDescriptorStatus.skipped());
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

        if (!markSkipped && throwable == null) {
            try {
                for (Method method : beforeEachMethods) {
                    invoke(method, argumentContext.getClassContext().getTestInstance(), invocationArguments);
                }
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof Execution.ExecutionSkippedException) {
                    markSkipped = true;
                } else {
                    throwable = cause;
                }
            } catch (Throwable t) {
                throwable = t;
            }
        }

        try {
            for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                classInterceptor.postBeforeEach(argumentContext, throwable);
            }
        } catch (Throwable t) {
            throwable = t;
            printStackTrace(t);
            throwables.add(t);
        }

        if (markSkipped) {
            return State.AFTER_EACH;
        } else if (throwable == null) {
            return State.TEST;
        } else {
            return State.AFTER_EACH;
        }
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
                Throwable cause = e.getCause();
                if (cause instanceof Execution.ExecutionSkippedException) {
                    markSkipped = true;
                } else {
                    throwable = cause;
                }
            } catch (Throwable t) {
                throwable = t;
            }
        }

        try {
            for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                classInterceptor.postTest(argumentContext, testMethod, throwable);
            }
        } catch (Throwable t) {
            printStackTrace(t);
            throwables.add(t);
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
                classInterceptor.postAfterEach(argumentContext, throwable);
            }
        } catch (Throwable t) {
            printStackTrace(t);
            throwables.add(t);
        }

        return State.END;
    }
}
