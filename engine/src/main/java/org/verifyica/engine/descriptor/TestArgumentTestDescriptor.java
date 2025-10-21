/*
 * Copyright (C) Verifyica project authors and contributors
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.verifyica.api.Argument;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.ClassInterceptor;
import org.verifyica.api.Execution;
import org.verifyica.engine.common.throttle.Throttle;
import org.verifyica.engine.configuration.Constants;
import org.verifyica.engine.context.ConcreteArgumentContext;
import org.verifyica.engine.inject.Injector;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;

/**
 * Class to implement TestArgumentTestDescriptor
 */
public class TestArgumentTestDescriptor extends TestableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestArgumentTestDescriptor.class);

    /**
     * Enum representing state.
     */
    private enum State {

        /**
         * Start state.
         */
        START,

        /**
         * Before all state.
         */
        BEFORE_ALL,

        /**
         * Test state.
         */
        TEST,

        /**
         * Skip state.
         */
        SKIP,

        /**
         * After all state.
         */
        AFTER_ALL,

        /**
         * Close state.
         */
        CLOSE,

        /**
         * Clean up state.
         */
        CLEAN_UP,

        /**
         * End state.
         */
        END
    }

    private final int argumentIndex;
    private final Argument<?> testArgument;
    private final List<Method> beforeAllMethods;
    private final List<Method> afterAllMethods;
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
    @Named(CLASS_CONTEXT)
    private ClassContext classContext;

    private ArgumentContext argumentContext;
    private boolean markSkipped;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param argumentIndex argumentIndex
     * @param testArgument argument
     * @param beforeAllMethods beforeAllMethods
     * @param afterAllMethods afterAllMethods
     */
    public TestArgumentTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            int argumentIndex,
            Argument<?> testArgument,
            List<Method> beforeAllMethods,
            List<Method> afterAllMethods) {
        super(uniqueId, displayName);

        this.argumentIndex = argumentIndex;
        this.testArgument = testArgument;
        this.beforeAllMethods = beforeAllMethods;
        this.afterAllMethods = afterAllMethods;
        this.invocationArguments = new ArrayList<>();
        this.throwables = new ArrayList<>();
    }

    /**
     * Method to get argument
     *
     * @return the argument
     */
    public Argument<?> getTestArgument() {
        return testArgument;
    }

    @Override
    public TestArgumentTestDescriptor test() {
        try {
            argumentContext = new ConcreteArgumentContext(classContext, argumentIndex, testArgument);

            invocationArguments.add(argumentContext.getTestArgument().getPayload());
            invocationArguments.add(argumentContext.getTestArgument());
            invocationArguments.add(argumentContext);

            for (TestDescriptor testDescriptor : getChildren()) {
                Injector.inject(ENGINE_EXECUTION_LISTENER, engineExecutionListener, testDescriptor);
                Injector.inject(CLASS_INTERCEPTORS, classInterceptors, testDescriptor);
                Injector.inject(CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, testDescriptor);
                Injector.inject(ARGUMENT_CONTEXT, argumentContext, testDescriptor);
            }

            engineExecutionListener.executionStarted(this);

            Throttle throttle =
                    createThrottle(classContext.getConfiguration(), Constants.ENGINE_ARGUMENT_STATE_MACHINE_THROTTLE);

            State state = State.START;

            while (state != State.END) {
                LOGGER.trace("testDescriptor [%s] state [%s]", this, state);

                throttle.throttle();

                switch (state) {
                    case START: {
                        state = State.BEFORE_ALL;
                        break;
                    }
                    case BEFORE_ALL: {
                        state = doBeforeAll();
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
                    case AFTER_ALL: {
                        state = doAfterAll();
                        break;
                    }
                    case CLOSE: {
                        state = doCloseArgument();
                        break;
                    }
                    case CLEAN_UP: {
                        state = doCleanupArgumentContext();
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

        getChildren().stream().map(TESTABLE_TEST_DESCRIPTOR_MAPPER).forEach(testableTestDescriptor -> {
            Injector.inject(ENGINE_EXECUTION_LISTENER, engineExecutionListener, testableTestDescriptor);
            Injector.inject(CLASS_INTERCEPTORS, classInterceptors, testableTestDescriptor);
            Injector.inject(CLASS_INTERCEPTORS_REVERSED, classInterceptorsReversed, testableTestDescriptor);
            Injector.inject(CLASS_CONTEXT, classContext, testableTestDescriptor);
            testableTestDescriptor.skip();
        });

        engineExecutionListener.executionSkipped(this, "Skipped");

        setTestDescriptorStatus(TestDescriptorStatus.skipped());
    }

    /**
     * Method to do before all
     *
     * @return the next state
     */
    private State doBeforeAll() {
        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : classInterceptors) {
                classInterceptor.preBeforeAll(argumentContext);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                for (Method method : beforeAllMethods) {
                    invoke(method, classContext.getTestInstance(), invocationArguments);
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
                classInterceptor.postBeforeAll(argumentContext, throwable);
            }
        } catch (Throwable t) {
            throwable = t;
            printStackTrace(t);
            throwables.add(t);
        }

        if (markSkipped) {
            return State.SKIP;
        } else if (throwable == null) {
            return State.TEST;
        } else {
            return State.SKIP;
        }
    }

    /**
     * Method to test child test descriptors
     *
     * @return the next state
     */
    private State doTest() {
        Iterator<TestableTestDescriptor> testableTestDescriptorIterator =
                getChildren().stream().map(TESTABLE_TEST_DESCRIPTOR_MAPPER).iterator();

        while (testableTestDescriptorIterator.hasNext()) {
            TestableTestDescriptor testableTestDescriptor = testableTestDescriptorIterator.next();
            if (testableTestDescriptor.test().getTestDescriptorStatus().isFailure()) {
                break;
            }
        }

        while (testableTestDescriptorIterator.hasNext()) {
            TestableTestDescriptor testableTestDescriptor = testableTestDescriptorIterator.next();
            testableTestDescriptor.skip();
        }

        return State.AFTER_ALL;
    }

    /**
     * Method to do skip child test descriptors
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

        return State.AFTER_ALL;
    }

    /**
     * Method to do after all
     *
     * @return the next state
     */
    private State doAfterAll() {
        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : classInterceptors) {
                classInterceptor.preAfterAll(argumentContext);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                for (Method method : afterAllMethods) {
                    invoke(method, classContext.getTestInstance(), invocationArguments);
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
                classInterceptor.postAfterAll(argumentContext, throwable);
            }
        } catch (Throwable t) {
            printStackTrace(t);
            throwables.add(t);
        }

        return State.CLOSE;
    }

    /**
     * Method to close the argument
     *
     * @return the next state
     */
    private State doCloseArgument() {
        if (testArgument instanceof AutoCloseable) {
            try {
                ((AutoCloseable) testArgument).close();
            } catch (Throwable t) {
                printStackTrace(t);
                throwables.add(t);
            }
        }

        return State.CLEAN_UP;
    }

    /**
     * Method to clean up the argument context
     *
     * @return the next state
     */
    private State doCleanupArgumentContext() {
        Map<String, Object> map = argumentContext.getMap();

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
