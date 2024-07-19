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
import org.antublue.verifyica.api.Argument;
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
     * @param beforeAllMethods beforeAllMethods
     * @param afterAllMethods afterAllMethods
     * @param testArgument testArgument
     */
    public ArgumentTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            List<Method> beforeAllMethods,
            List<Method> afterAllMethods,
            Argument<?> testArgument) {
        super(uniqueId, displayName);

        Preconditions.notNull(testClass, "testClass is null");
        Preconditions.notNull(testArgument, "testArgument is null");

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
    public void execute(ExecutionRequest executionRequest, Context context) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("execute ArgumentTestDescriptor [%s]", toString());
        }

        DefaultArgumentContext defaultArgumentContext = (DefaultArgumentContext) context;

        Preconditions.notNull(executionRequest, "executionRequest is null");
        Preconditions.notNull(defaultArgumentContext, "defaultArgumentContext is null");
        Preconditions.notNull(defaultArgumentContext.getTestInstance(), "testInstance is null");

        stopWatch.reset();

        defaultArgumentContext.setArgument(testArgument);

        Preconditions.notNull(defaultArgumentContext.getTestInstance(), "testInstance is null");
        Preconditions.notNull(defaultArgumentContext.getArgument(), "testArgument is null");

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME,
                        getParent().get().getDisplayName());

        getMetadata().put(MetadataTestDescriptorConstants.TEST_ARGUMENT, testArgument);

        executionRequest.getEngineExecutionListener().executionStarted(this);

        throwableCollector.execute(() -> invokeBeforeAllMethods(defaultArgumentContext));
        if (throwableCollector.isEmpty()) {
            executeChildren(executionRequest, defaultArgumentContext);
        } else {
            skipChildren(executionRequest, defaultArgumentContext);
        }
        throwableCollector.execute(() -> invokeAfterAllMethods(defaultArgumentContext));

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

        defaultArgumentContext.setArgument(null);

        executionRequest
                .getEngineExecutionListener()
                .executionFinished(this, throwableCollector.toTestExecutionResult());
    }

    @Override
    public void skip(ExecutionRequest executionRequest, Context context) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("skip ArgumentTestDescriptor [%s]", toString());
        }

        stopWatch.reset();

        DefaultArgumentContext defaultArgumentContext = (DefaultArgumentContext) context;
        defaultArgumentContext.setArgument(testArgument);

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME, getDisplayName());
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_STATUS,
                        MetadataTestDescriptorConstants.SKIP);

        getChildren().stream()
                .map(ToExecutableTestDescriptor.INSTANCE)
                .forEach(
                        executableTestDescriptor ->
                                executableTestDescriptor.skip(
                                        executionRequest, defaultArgumentContext));

        stopWatch.stop();

        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_DURATION,
                        stopWatch.elapsedTime());

        executionRequest
                .getEngineExecutionListener()
                .executionSkipped(this, format("Argument [%s] skipped", testArgument.getName()));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + " "
                + getUniqueId()
                + " {"
                + " testClass ["
                + testClass.getName()
                + "]"
                + " beforeAllMethods ["
                + ObjectSupport.toString(beforeAllMethods)
                + "]"
                + " afterAllMethods ["
                + ObjectSupport.toString(afterAllMethods)
                + "] "
                + "}";
    }

    /**
     * Method to invoke all before all methods
     *
     * @param defaultArgumentContext defaultArgumentContext
     * @throws Throwable Throwable
     */
    private void invokeBeforeAllMethods(DefaultArgumentContext defaultArgumentContext)
            throws Throwable {
        Object testInstance = defaultArgumentContext.getTestInstance();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "invokeBeforeAllMethods() testClass [%s] testInstance [%s]",
                    testInstance.getClass().getName(), testInstance);
        }

        Preconditions.notNull(defaultArgumentContext, "defaultArgumentContext is null");
        Preconditions.notNull(testInstance, "testInstance is null");

        for (Method method : beforeAllMethods) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "invokeBeforeAllMethods() testClass [%s] testInstance [%s] method [%s]",
                        testInstance.getClass().getName(), testInstance, method);
            }

            method.invoke(testInstance, defaultArgumentContext.asImmutable());
        }
    }

    /**
     * Method to execute all child test descriptors
     *
     * @param executionRequest executionRequest
     * @param defaultArgumentContext defaultArgumentContext
     */
    private void executeChildren(
            ExecutionRequest executionRequest, DefaultArgumentContext defaultArgumentContext) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("executeChildren() testClass [%s]", testClass.getName());
        }

        Preconditions.notNull(executionRequest, "executionRequest is null");

        getChildren().stream()
                .map(ToExecutableTestDescriptor.INSTANCE)
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
    private void skipChildren(
            ExecutionRequest executionRequest, DefaultArgumentContext defaultArgumentContext) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("skipChildren() testClass [%s]", testClass.getName());
        }

        stopWatch.stop();

        getChildren().stream()
                .map(ToExecutableTestDescriptor.INSTANCE)
                .forEach(
                        executableTestDescriptor -> {
                            executableTestDescriptor.skip(executionRequest, defaultArgumentContext);
                        });

        stopWatch.reset();

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME,
                        DisplayNameSupport.getDisplayName(testClass));
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_DURATION,
                        Duration.ofMillis(0));
        getMetadata().put(MetadataTestDescriptorConstants.TEST_ARGUMENT, testArgument);
    }

    /**
     * Method to invoke all after all methods
     *
     * @param defaultArgumentContext defaultArgumentContext
     * @throws Throwable Throwable
     */
    private void invokeAfterAllMethods(DefaultArgumentContext defaultArgumentContext)
            throws Throwable {
        Object testInstance = defaultArgumentContext.getTestInstance();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "invokeAfterAllMethods() testClass [%s] testInstance [%s]",
                    testInstance.getClass().getName(), testInstance);
        }

        Preconditions.notNull(defaultArgumentContext, "defaultArgumentContext is null");
        Preconditions.notNull(testInstance, "testInstance is null");

        for (Method method : afterAllMethods) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "invokeAfterAllMethods() testClass [%s] testInstance [%s] method [%s]",
                        testInstance.getClass().getName(), testInstance, method);
            }

            method.invoke(testInstance, defaultArgumentContext.asImmutable());
        }

        defaultArgumentContext.getStore().clear();

        if (testArgument instanceof AutoCloseable) {
            ((AutoCloseable) testArgument).close();
        }
    }
}
