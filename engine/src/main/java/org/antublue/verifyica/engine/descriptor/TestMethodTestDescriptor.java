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
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.antublue.verifyica.api.Context;
import org.antublue.verifyica.engine.context.DefaultArgumentContext;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.DisplayNameSupport;
import org.antublue.verifyica.engine.support.ObjectSupport;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

/** Class to implement MethodTestDescriptor */
public class TestMethodTestDescriptor extends ExecutableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestMethodTestDescriptor.class);

    private final Class<?> testClass;
    private final List<Method> beforeEachMethods;
    private final Method testMethod;
    private final List<Method> afterEachMethods;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param testClass testClass
     * @param beforeEachMethods beforeEachMethods
     * @param testMethod testMethod
     * @param afterEachMethods afterEachMethods
     */
    public TestMethodTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            List<Method> beforeEachMethods,
            Method testMethod,
            List<Method> afterEachMethods) {
        super(uniqueId, displayName);
        this.testClass = testClass;
        this.beforeEachMethods = beforeEachMethods;
        this.testMethod = testMethod;
        this.afterEachMethods = afterEachMethods;
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(MethodSource.from(testMethod));
    }

    @Override
    public void execute(ExecutionRequest executionRequest, Context context) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("execute ClassTestDescriptor [%s]", toString());
        }

        DefaultArgumentContext defaultArgumentContext = (DefaultArgumentContext) context;

        Preconditions.notNull(executionRequest, "executionRequest is null");
        Preconditions.notNull(defaultArgumentContext.getTestInstance(), "testInstance is null");
        Preconditions.notNull(defaultArgumentContext.getArgument(), "testArgument is null");

        stopWatch.reset();

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME,
                        getParent().get().getParent().get().getDisplayName());

        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_ARGUMENT,
                        defaultArgumentContext.getArgument());
        getMetadata().put(MetadataTestDescriptorConstants.TEST_METHOD, testMethod);
        getMetadata()
                .put(MetadataTestDescriptorConstants.TEST_METHOD_DISPLAY_NAME, getDisplayName());

        executionRequest.getEngineExecutionListener().executionStarted(this);

        throwableCollector.execute(() -> invokeBeforeEachMethods(defaultArgumentContext));
        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(() -> invokeTestMethod(defaultArgumentContext));
        }
        throwableCollector.execute(() -> invokeAfterEachMethods(defaultArgumentContext));

        stopWatch.stop();

        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_DURATION,
                        stopWatch.elapsedTime());

        List<Throwable> throwables = collectThrowables();
        throwableCollector.getThrowables().addAll(throwables);

        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_STATUS,
                        throwableCollector.isEmpty()
                                ? MetadataTestDescriptorConstants.PASS
                                : MetadataTestDescriptorConstants.FAIL);

        executionRequest
                .getEngineExecutionListener()
                .executionFinished(this, throwableCollector.toTestExecutionResult());
    }

    @Override
    public void skip(ExecutionRequest executionRequest, Context context) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("execute ClassTestDescriptor [%s]", toString());
        }

        stopWatch.reset();

        DefaultArgumentContext defaultArgumentContext = (DefaultArgumentContext) context;

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME,
                        DisplayNameSupport.getDisplayName(testClass));
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_ARGUMENT,
                        defaultArgumentContext.getArgument());
        getMetadata().put(MetadataTestDescriptorConstants.TEST_METHOD, testMethod);
        getMetadata()
                .put(MetadataTestDescriptorConstants.TEST_METHOD_DISPLAY_NAME, getDisplayName());
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_DURATION,
                        Duration.ofMillis(0));
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_STATUS,
                        MetadataTestDescriptorConstants.SKIP);

        executionRequest
                .getEngineExecutionListener()
                .executionSkipped(
                        this,
                        format(
                                "Argument [%s] test method [%s] skipped",
                                defaultArgumentContext.getArgument().getName(),
                                testMethod.getName()));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + " "
                + getUniqueId()
                + " { "
                + "testClass["
                + testClass.getName()
                + "]"
                + " beforeEachMethods["
                + ObjectSupport.toString(beforeEachMethods)
                + "]"
                + " testMethod["
                + testMethod.getName()
                + "] afterEachMethods["
                + ObjectSupport.toString(afterEachMethods)
                + "] "
                + "}";
    }

    /**
     * Method to invoke all before all methods
     *
     * @param defaultArgumentContext defaultArgumentContext
     * @throws Throwable Throwable
     */
    private void invokeBeforeEachMethods(DefaultArgumentContext defaultArgumentContext)
            throws Throwable {
        Object testInstance = defaultArgumentContext.getTestInstance();
        Preconditions.notNull(testInstance, "testInstance is null");

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "invokeBeforeEachMethods() testClass [%s] testInstance [%s]",
                    testInstance.getClass().getName(), testInstance);
        }

        for (Method method : beforeEachMethods) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "invokeBeforeEachMethods() testClass [%s] testInstance [%s] method [%s]",
                        testInstance.getClass().getName(), testInstance, method);
            }

            method.invoke(testInstance, defaultArgumentContext.asImmutable());
        }
    }

    /**
     * Method to invoke the test method
     *
     * @param defaultArgumentContext defaultArgumentContext
     * @throws Throwable Throwable
     */
    private void invokeTestMethod(DefaultArgumentContext defaultArgumentContext) throws Throwable {
        Object testInstance = defaultArgumentContext.getTestInstance();
        Preconditions.notNull(testInstance, "testInstance is null");

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "test() testClass [%s] testInstance [%s] method [%s]",
                    testInstance.getClass().getName(), testInstance, testMethod);
        }

        testMethod.invoke(testInstance, defaultArgumentContext.asImmutable());
    }

    /**
     * Method to invoke all after each methods
     *
     * @param defaultArgumentContext defaultArgumentContext
     * @throws Throwable Throwable
     */
    private void invokeAfterEachMethods(DefaultArgumentContext defaultArgumentContext)
            throws Throwable {
        Object testInstance = defaultArgumentContext.getTestInstance();
        Preconditions.notNull(testInstance, "testInstance is null");

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "invokeAfterEachMethods() testClass [%s] testInstance [%s]",
                    testInstance.getClass().getName(), testInstance);
        }

        for (Method method : afterEachMethods) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "invokeAfterEachMethods() testClass [%s] testInstance [%s] method [%s]",
                        testInstance.getClass().getName(), testInstance, method);
            }

            method.invoke(testInstance, defaultArgumentContext.asImmutable());
        }
    }
}
