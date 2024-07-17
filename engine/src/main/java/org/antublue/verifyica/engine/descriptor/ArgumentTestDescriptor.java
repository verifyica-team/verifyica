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
import java.util.function.Consumer;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.Context;
import org.antublue.verifyica.engine.context.ConcreteArgumentContext;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.DisplayNameSupport;
import org.antublue.verifyica.engine.support.ObjectSupport;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
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

        ConcreteArgumentContext concreteArgumentContext = (ConcreteArgumentContext) context;

        Preconditions.notNull(executionRequest, "executionRequest is null");
        Preconditions.notNull(concreteArgumentContext, "concreteArgumentContext is null");
        Preconditions.notNull(concreteArgumentContext.getTestInstance(), "testInstance is null");

        stopWatch.reset();

        concreteArgumentContext.setArgument(testArgument);

        Preconditions.notNull(concreteArgumentContext.getTestInstance(), "testInstance is null");
        Preconditions.notNull(concreteArgumentContext.getArgument(), "testArgument is null");

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME,
                        getParent().get().getDisplayName());

        getMetadata().put(MetadataTestDescriptorConstants.TEST_ARGUMENT, testArgument);

        executionRequest.getEngineExecutionListener().executionStarted(this);

        throwableCollector.execute(() -> invokeBeforeAllMethods(concreteArgumentContext));
        if (throwableCollector.isEmpty()) {
            doExecute(executionRequest, concreteArgumentContext);
        } else {
            doSkip(executionRequest, concreteArgumentContext);
        }
        throwableCollector.execute(() -> invokeAfterAllMethods(concreteArgumentContext));

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

        concreteArgumentContext.setArgument(null);

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

        ConcreteArgumentContext concreteArgumentContext = (ConcreteArgumentContext) context;
        concreteArgumentContext.setArgument(testArgument);

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME, getDisplayName());
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_STATUS,
                        MetadataTestDescriptorConstants.SKIP);

        getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor -> {
                                    if (testDescriptor instanceof ExecutableTestDescriptor) {
                                        ((ExecutableTestDescriptor) testDescriptor)
                                                .skip(executionRequest, concreteArgumentContext);
                                    }
                                });
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
                + " testClass["
                + testClass.getName()
                + "]"
                + " beforeAllMethods["
                + ObjectSupport.toString(beforeAllMethods)
                + "]"
                + " afterAllMethods["
                + ObjectSupport.toString(afterAllMethods)
                + "] "
                + "}";
    }

    private void invokeBeforeAllMethods(ConcreteArgumentContext concreteArgumentContext)
            throws Throwable {
        Object testInstance = concreteArgumentContext.getTestInstance();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "invokeBeforeAllMethods() testClass [%s] testInstance [%s]",
                    testInstance.getClass().getName(), testInstance);
        }

        Preconditions.notNull(concreteArgumentContext, "concreteArgumentContext is null");
        Preconditions.notNull(testInstance, "testInstance is null");

        for (Method method : beforeAllMethods) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "invokeBeforeAllMethods() testClass [%s] testInstance [%s] method [%s]",
                        testInstance.getClass().getName(), testInstance, method);
            }

            method.invoke(testInstance, concreteArgumentContext.toImmutable());
        }
    }

    private void doExecute(
            ExecutionRequest executionRequest, ConcreteArgumentContext concreteArgumentContext) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("execute() testClass [%s]", testClass.getName());
        }

        Preconditions.notNull(executionRequest, "executionRequest is null");

        getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor -> {
                                    if (testDescriptor instanceof TestMethodTestDescriptor) {
                                        ExecutableTestDescriptor executableTestDescriptor =
                                                (ExecutableTestDescriptor) testDescriptor;
                                        executableTestDescriptor.execute(
                                                executionRequest, concreteArgumentContext);
                                    }
                                });
    }

    private void doSkip(
            ExecutionRequest executionRequest, ConcreteArgumentContext concreteArgumentContext) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("skip() testClass [%s]", testClass.getName());
        }

        stopWatch.stop();

        getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor -> {
                                    if (testDescriptor instanceof TestMethodTestDescriptor) {
                                        ExecutableTestDescriptor executableTestDescriptor =
                                                (ExecutableTestDescriptor) testDescriptor;
                                        executableTestDescriptor.skip(
                                                executionRequest, concreteArgumentContext);
                                    }
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

    private void invokeAfterAllMethods(ConcreteArgumentContext concreteArgumentContext)
            throws Throwable {
        Object testInstance = concreteArgumentContext.getTestInstance();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "invokeAfterAllMethods() testClass [%s] testInstance [%s]",
                    testInstance.getClass().getName(), testInstance);
        }

        Preconditions.notNull(concreteArgumentContext, "concreteArgumentContext is null");
        Preconditions.notNull(testInstance, "testInstance is null");

        for (Method method : afterAllMethods) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "invokeAfterAllMethods() testClass [%s] testInstance [%s] method [%s]",
                        testInstance.getClass().getName(), testInstance, method);
            }

            method.invoke(testInstance, concreteArgumentContext.toImmutable());
        }

        if (testArgument instanceof AutoCloseable) {
            ((AutoCloseable) testArgument).close();
        }

        concreteArgumentContext.getStore().clear();
    }
}
