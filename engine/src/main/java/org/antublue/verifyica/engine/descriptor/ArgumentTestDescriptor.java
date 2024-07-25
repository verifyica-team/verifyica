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
import org.antublue.verifyica.engine.context.DefaultArgumentContext;
import org.antublue.verifyica.engine.extension.ClassExtensionRegistry;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.DisplayNameSupport;
import org.antublue.verifyica.engine.support.ObjectSupport;
import org.antublue.verifyica.engine.util.StateTracker;
import org.junit.platform.commons.util.Preconditions;
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
        LOGGER.trace("execute ArgumentTestDescriptor [%s]", this);

        DefaultArgumentContext defaultArgumentContext = (DefaultArgumentContext) context;

        Preconditions.notNull(executionRequest, "executionRequest is null");
        Preconditions.notNull(defaultArgumentContext, "defaultArgumentContext is null");
        Preconditions.notNull(defaultArgumentContext.getTestInstance(), "testInstance is null");

        stopWatch.reset();

        defaultArgumentContext.setTestArgument(testArgument);

        Preconditions.notNull(defaultArgumentContext.getTestInstance(), "testInstance is null");
        Preconditions.notNull(defaultArgumentContext.getTestArgument(), "testArgument is null");

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME,
                        getParent().get().getDisplayName());

        getMetadata().put(MetadataTestDescriptorConstants.TEST_ARGUMENT, testArgument);

        executionRequest.getEngineExecutionListener().executionStarted(this);

        StateTracker<String> stateTracker = new StateTracker<>();

        try {
            stateTracker.put("beforeAll");
            beforeAll(defaultArgumentContext);
            stateTracker.put("beforeAll->SUCCESS");
        } catch (Throwable t) {
            stateTracker.put("beforeAll->FAILURE", t);
            t.printStackTrace(System.err);
        }

        if (stateTracker.contains("beforeAll->SUCCESS")) {
            try {
                stateTracker.put("doExecute");
                doExecute(executionRequest, defaultArgumentContext);
                stateTracker.put("doExecute->SUCCESS");
            } catch (Throwable t) {
                stateTracker.put("doExecute->FAILURE", t);
                // Don't log the throwable since it's from downstream test descriptors
            }
        }

        if (stateTracker.contains("beforeAll->FAILURE")) {
            try {
                stateTracker.put("doSkip");
                doSkip(executionRequest, defaultArgumentContext);
                stateTracker.put("doSkip->SUCCESS");
            } catch (Throwable t) {
                stateTracker.put("doSkip->FAILURE", t);
                // Don't log the throwable since it's from downstream test descriptors
            }
        }

        try {
            stateTracker.put("afterAll");
            afterAll(defaultArgumentContext);
            stateTracker.put("afterAll->SUCCESS");
        } catch (Throwable t) {
            stateTracker.put("afterAll->FAILURE", t);
            t.printStackTrace(System.err);
        }

        stopWatch.stop();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(this);
            stateTracker
                    .entrySet()
                    .forEach(
                            new Consumer<StateTracker.Entry<String>>() {
                                @Override
                                public void accept(StateTracker.Entry<String> stateTrackerEntry) {
                                    LOGGER.trace("%s %s", this, stateTrackerEntry);
                                }
                            });
        }

        StateTracker.Entry<String> entry = stateTracker.getFirstStateEntryWithThrowable();

        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_DURATION,
                        stopWatch.elapsedTime());

        TestExecutionResult testExecutionResult = TestExecutionResult.successful();

        if (entry != null) {
            testExecutionResult = TestExecutionResult.failed(entry.getThrowable());
        }

        executionRequest.getEngineExecutionListener().executionFinished(this, testExecutionResult);
    }

    @Override
    public void skip(ExecutionRequest executionRequest, Context context) {
        LOGGER.trace("skip [%s]", this);

        stopWatch.reset();

        DefaultArgumentContext defaultArgumentContext = (DefaultArgumentContext) context;
        defaultArgumentContext.setTestArgument(testArgument);

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME, getDisplayName());

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
    private void beforeAll(DefaultArgumentContext defaultArgumentContext) throws Throwable {
        LOGGER.trace(
                "beforeAll() testClass [%s] argument [%s]",
                testClass.getName(), defaultArgumentContext.getTestArgument().getName());

        ClassExtensionRegistry.getInstance().beforeAll(defaultArgumentContext, beforeAllMethods);
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
    private void doSkip(
            ExecutionRequest executionRequest, DefaultArgumentContext defaultArgumentContext) {
        LOGGER.trace(
                "doSkip() testClass [%s] argument [%s]",
                testClass.getName(), defaultArgumentContext.getTestArgument().getName());

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
    private void afterAll(DefaultArgumentContext defaultArgumentContext) throws Throwable {
        LOGGER.trace(
                "afterAll() testClass [%s] argument [%s]",
                testClass.getName(), defaultArgumentContext.getTestArgument().getName());

        ClassExtensionRegistry.getInstance().afterAll(defaultArgumentContext, afterAllMethods);

        if (testArgument instanceof AutoCloseable) {
            ((AutoCloseable) testArgument).close();
        }
    }
}
