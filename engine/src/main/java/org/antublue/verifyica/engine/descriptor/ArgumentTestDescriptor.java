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

package org.antublue.verifyica.engine.descriptor;

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.Context;
import org.antublue.verifyica.api.Store;
import org.antublue.verifyica.engine.common.StateTracker;
import org.antublue.verifyica.engine.context.DefaultArgumentContext;
import org.antublue.verifyica.engine.interceptor.ClassInterceptorRegistry;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ArgumentSupport;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

/** Class to implement ArgumentTestDescriptor */
public class ArgumentTestDescriptor extends ExecutableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentTestDescriptor.class);

    private final Class<?> testClass;
    private final List<Method> beforeAllMethods;
    private final List<Method> afterAllMethods;
    private final Argument<?> testArgument;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param testClass testClass
     * @param testArgument testArgument
     * @param beforeAllMethods beforeAllMethods
     * @param afterAllMethods afterAllMethods
     */
    public ArgumentTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            Argument<?> testArgument,
            List<Method> beforeAllMethods,
            List<Method> afterAllMethods) {
        super(uniqueId, displayName);

        ArgumentSupport.notNull(testClass, "testClass is null");
        ArgumentSupport.notNull(testArgument, "testArgument is null");

        this.testClass = testClass;
        this.beforeAllMethods = beforeAllMethods;
        this.afterAllMethods = afterAllMethods;
        this.testArgument = testArgument;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(ClassSource.from(testClass));
    }

    @Override
    public Type getType() {
        return Type.CONTAINER_AND_TEST;
    }

    @Override
    public Class<?> getTestClass() {
        return testClass;
    }

    /**
     * Method to get the test argument
     *
     * @return the test argument
     */
    public Argument<?> getTestArgument() {
        return testArgument;
    }

    @Override
    public void execute(ExecutionRequest executionRequest, Context context) {
        LOGGER.trace("execute [%s]", this);

        DefaultArgumentContext defaultArgumentContext = (DefaultArgumentContext) context;

        ArgumentSupport.notNull(executionRequest, "executionRequest is null");
        ArgumentSupport.notNull(defaultArgumentContext, "defaultArgumentContext is null");
        ArgumentSupport.notNull(defaultArgumentContext.getTestInstance(), "testInstance is null");

        getStopWatch().reset();

        defaultArgumentContext.setTestArgument(testArgument);

        ArgumentSupport.notNull(defaultArgumentContext.getTestInstance(), "testInstance is null");
        ArgumentSupport.notNull(defaultArgumentContext.getTestArgument(), "testArgument is null");

        executionRequest.getEngineExecutionListener().executionStarted(this);

        StateTracker<String> stateTracker = new StateTracker<>();

        try {
            stateTracker.put("beforeAll");
            beforeAll(defaultArgumentContext);
            stateTracker.put("beforeAll->SUCCESS");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            stateTracker.put("beforeAll->FAILURE", t);
        }

        if (stateTracker.contains("beforeAll->SUCCESS")) {
            try {
                stateTracker.put("doExecute");
                doExecute(executionRequest, defaultArgumentContext);
                stateTracker.put("doExecute->SUCCESS");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.put("doExecute->FAILURE", t);
            }
        }

        if (stateTracker.contains("beforeAll->FAILURE")) {
            try {
                stateTracker.put("doSkip");
                doSkip(executionRequest, defaultArgumentContext);
                stateTracker.put("doSkip->SUCCESS");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.put("doSkip->FAILURE", t);
            }
        }

        try {
            stateTracker.put("afterAll");
            afterAll(defaultArgumentContext);
            stateTracker.put("afterAll->SUCCESS");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            stateTracker.put("afterAll->FAILURE", t);
        }

        if (testArgument instanceof AutoCloseable) {
            try {
                stateTracker.put("argumentAutoClose(" + testArgument.getName() + ")");
                ((AutoCloseable) testArgument).close();
                stateTracker.put("argumentAutoClose" + testArgument.getName() + ")->SUCCESS");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                stateTracker.put("argumentAutoClose" + testArgument.getName() + ")->FAILURE");
            }
        }

        Store store = defaultArgumentContext.getStore();
        for (Object key : store.keySet()) {
            Object value = store.get(key);
            if (value instanceof AutoCloseable) {
                try {
                    stateTracker.put("storeAutoClose(" + key + ")");
                    ((AutoCloseable) value).close();
                    stateTracker.put("storeAutoClose(" + key + ")->SUCCESS");
                } catch (Throwable t) {
                    t.printStackTrace(System.err);
                    stateTracker.put("storeAutoClose(" + key + ")->FAILURE");
                }
            }
        }
        store.clear();

        getStopWatch().stop();

        LOGGER.trace("state tracker [%s]", stateTracker);

        TestExecutionResult testExecutionResult =
                stateTracker
                        .getFirstStateEntryWithThrowable()
                        .map(entry -> TestExecutionResult.failed(entry.getThrowable()))
                        .orElse(TestExecutionResult.successful());

        executionRequest.getEngineExecutionListener().executionFinished(this, testExecutionResult);
    }

    @Override
    public void skip(ExecutionRequest executionRequest, Context context) {
        LOGGER.trace("skip [%s]", this);

        getStopWatch().reset();

        DefaultArgumentContext defaultArgumentContext = (DefaultArgumentContext) context;
        defaultArgumentContext.setTestArgument(testArgument);

        getChildren().stream()
                .map(TO_EXECUTABLE_TEST_DESCRIPTOR)
                .forEach(
                        executableTestDescriptor ->
                                executableTestDescriptor.skip(
                                        executionRequest, defaultArgumentContext));

        getStopWatch().stop();

        executionRequest
                .getEngineExecutionListener()
                .executionSkipped(this, format("Argument [%s] skipped", testArgument.getName()));
    }

    @Override
    public String toString() {
        return "ArgumentTestDescriptor{"
                + "uniqueId="
                + getUniqueId()
                + ", testClass="
                + testClass
                + ", testArgument="
                + testArgument
                + ", beforeAllMethods="
                + beforeAllMethods
                + ", afterAllMethods="
                + afterAllMethods
                + '}';
    }

    /**
     * Method to invoke all before all methods
     *
     * @param defaultArgumentContext defaultArgumentContext
     * @throws Throwable Throwable
     */
    private void beforeAll(DefaultArgumentContext defaultArgumentContext) throws Throwable {
        LOGGER.trace(
                "beforeAll() testClass [%s] argument [%s]",
                testClass.getName(), defaultArgumentContext.getTestArgument().getName());

        ClassInterceptorRegistry.getInstance().beforeAll(defaultArgumentContext, beforeAllMethods);
    }

    /**
     * Method to execute all child test descriptors
     *
     * @param executionRequest executionRequest
     * @param defaultArgumentContext defaultArgumentContext
     */
    private void doExecute(
            ExecutionRequest executionRequest, DefaultArgumentContext defaultArgumentContext) {
        LOGGER.trace(
                "doExecute() testClass [%s] argument [%s]",
                testClass.getName(), defaultArgumentContext.getTestArgument().getName());

        ArgumentSupport.notNull(executionRequest, "executionRequest is null");

        getChildren().stream()
                .map(TO_EXECUTABLE_TEST_DESCRIPTOR)
                .forEach(
                        executableTestDescriptor ->
                                executableTestDescriptor.execute(
                                        executionRequest, defaultArgumentContext));
    }

    /**
     * Method to skip all child test descriptors
     *
     * @param executionRequest executionRequest
     * @param defaultArgumentContext defaultArgumentContext
     */
    private void doSkip(
            ExecutionRequest executionRequest, DefaultArgumentContext defaultArgumentContext) {
        LOGGER.trace(
                "doSkip() testClass [%s] argument [%s]",
                testClass.getName(), defaultArgumentContext.getTestArgument().getName());

        getChildren().stream()
                .map(TO_EXECUTABLE_TEST_DESCRIPTOR)
                .forEach(
                        executableTestDescriptor -> {
                            executableTestDescriptor.skip(executionRequest, defaultArgumentContext);
                        });
    }

    /**
     * Method to invoke all after all methods
     *
     * @param defaultArgumentContext defaultArgumentContext
     * @throws Throwable Throwable
     */
    private void afterAll(DefaultArgumentContext defaultArgumentContext) throws Throwable {
        LOGGER.trace(
                "afterAll() testClass [%s] argument [%s]",
                testClass.getName(), defaultArgumentContext.getTestArgument().getName());

        ClassInterceptorRegistry.getInstance().afterAll(defaultArgumentContext, afterAllMethods);
    }
}
