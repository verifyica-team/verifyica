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

import io.github.thunkware.vt.bridge.ThreadTool;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import org.antublue.verifyica.api.Context;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.context.DefaultArgumentContext;
import org.antublue.verifyica.engine.context.DefaultClassContext;
import org.antublue.verifyica.engine.context.DefaultEngineContext;
import org.antublue.verifyica.engine.extension.ClassExtensionRegistry;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ObjectSupport;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

/** Class to implement ClassTestDescriptor */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class ClassTestDescriptor extends ExecutableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassTestDescriptor.class);

    private static final boolean useVirtualThreads =
            ThreadTool.hasVirtualThreads()
                    && "virtual"
                            .equalsIgnoreCase(
                                    DefaultEngineContext.getInstance()
                                            .getConfiguration()
                                            .get(Constants.ENGINE_EXECUTOR_TYPE));

    private final Class<?> testClass;
    private final int parallelism;
    private final List<Method> prepareMethods;
    private final List<Method> concludeMethods;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param testClass testClass
     * @param prepareMethods prepareMethods
     * @param concludeMethods concludeMethods
     * @param parallelism parallelism
     */
    public ClassTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            List<Method> prepareMethods,
            List<Method> concludeMethods,
            int parallelism) {
        super(uniqueId, displayName);

        Preconditions.notNull(testClass, "testClass is null");

        this.testClass = testClass;
        this.prepareMethods = prepareMethods;
        this.concludeMethods = concludeMethods;
        this.parallelism = parallelism;
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
        LOGGER.trace("execute [%s]", toString());

        stopWatch.reset();

        DefaultClassContext defaultClassContext = (DefaultClassContext) context;

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME, getDisplayName());

        executionRequest.getEngineExecutionListener().executionStarted(this);

        List<Throwable> throwables = new ArrayList<>();

        try {
            instantiateTestInstance(defaultClassContext);
            try {
                prepare(defaultClassContext);
                try {
                    doExecute(executionRequest, defaultClassContext);
                } catch (Throwable t) {
                    throwables.add(t);
                }
            } catch (Throwable t) {
                throwables.add(t);
                try {
                    doSkip(executionRequest, defaultClassContext);
                } catch (Throwable tt) {
                    throwables.add(tt);
                }
            } finally {
                try {
                    conclude(defaultClassContext);
                } catch (Throwable t) {
                    throwables.add(t);
                }
            }
        } catch (Throwable t) {
            throwables.add(t);
        } finally {
            try {
                destroyTestInstance(defaultClassContext);
            } catch (Throwable t) {
                throwables.add(t);
            }
        }

        stopWatch.stop();

        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_DURATION,
                        stopWatch.elapsedTime());
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_STATUS,
                        throwables.isEmpty()
                                ? MetadataTestDescriptorConstants.PASS
                                : MetadataTestDescriptorConstants.FAIL);

        TestExecutionResult testExecutionResult = TestExecutionResult.successful();

        if (!throwables.isEmpty()) {
            testExecutionResult = TestExecutionResult.failed(throwables.get(0));
        }

        executionRequest.getEngineExecutionListener().executionFinished(this, testExecutionResult);
    }

    @Override
    public void skip(ExecutionRequest executionRequest, Context context) {
        LOGGER.trace("skip [%s]", toString());

        stopWatch.reset();

        DefaultClassContext defaultClassContext = (DefaultClassContext) context;

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME, getDisplayName());
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_DURATION,
                        stopWatch.elapsedTime());
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_STATUS,
                        MetadataTestDescriptorConstants.SKIP);

        getChildren().stream()
                .map(ToExecutableTestDescriptor.INSTANCE)
                .forEach(
                        executableTestDescriptor ->
                                executableTestDescriptor.skip(
                                        executionRequest, defaultClassContext));

        stopWatch.stop();

        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_DURATION,
                        stopWatch.elapsedTime());

        executionRequest.getEngineExecutionListener().executionSkipped(this, "Skipped");
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
                + " prepareMethods ["
                + ObjectSupport.toString(prepareMethods)
                + "]"
                + " concludeMethods ["
                + ObjectSupport.toString(concludeMethods)
                + "] "
                + "}";
    }

    /**
     * Method to instantiate the test instance
     *
     * @param defaultClassContext defaultClassContext
     * @throws Throwable Throwable
     */
    private void instantiateTestInstance(DefaultClassContext defaultClassContext) throws Throwable {
        LOGGER.trace("instantiateTestInstance() testClass [%s]", testClass.getName());

        ClassExtensionRegistry.getInstance()
                .beforeInstantiate(defaultClassContext.getEngineContext(), testClass);

        Object testInstance =
                testClass.getDeclaredConstructor((Class<?>[]) null).newInstance((Object[]) null);

        defaultClassContext.setTestClass(testClass);
        defaultClassContext.setTestInstance(testInstance);
    }

    /**
     * Method to invoke all prepare methods
     *
     * @param defaultClassContext defaultClassContext
     * @throws Throwable Throwable
     */
    private void prepare(DefaultClassContext defaultClassContext) throws Throwable {
        LOGGER.trace("prepare() testClass [%s]", testClass.getName());

        ClassExtensionRegistry.getInstance()
                .prepare(defaultClassContext.asImmutable(), prepareMethods);
    }

    /**
     * Method to execute all child test descriptors
     *
     * @param executionRequest executionRequest
     * @param defaultClassContext defaultClassContext
     */
    private void doExecute(
            ExecutionRequest executionRequest, DefaultClassContext defaultClassContext) {
        LOGGER.trace("doExecute() testClass [%s]", testClass.getName());

        Preconditions.notNull(defaultClassContext, "defaultClassContext is null");
        Preconditions.notNull(defaultClassContext.getTestInstance(), "testInstance is null");

        CountDownLatch countDownLatch = new CountDownLatch(getChildren().size());
        Semaphore semaphore = new Semaphore(parallelism);

        getChildren().stream()
                .map(ToExecutableTestDescriptor.INSTANCE)
                .forEach(
                        executableTestDescriptor -> {
                            DefaultArgumentContext defaultArgumentContext =
                                    new DefaultArgumentContext(defaultClassContext);

                            defaultArgumentContext.setTestInstance(
                                    defaultClassContext.getTestInstance());

                            if (parallelism > 1) {
                                try {
                                    semaphore.acquire();
                                } catch (Throwable t) {
                                    // INTENTIONALLY BLANK
                                }

                                Runnable runnable =
                                        () -> {
                                            try {
                                                executableTestDescriptor.execute(
                                                        executionRequest, defaultArgumentContext);
                                            } finally {
                                                semaphore.release();
                                                countDownLatch.countDown();
                                            }
                                        };

                                Thread thread;

                                if (useVirtualThreads) {
                                    thread = ThreadTool.unstartedVirtualThread(runnable);
                                } else {
                                    thread = new Thread(runnable);
                                }

                                thread.setDaemon(true);
                                thread.setName(Thread.currentThread().getName());
                                thread.start();
                            } else {
                                try {
                                    executableTestDescriptor.execute(
                                            executionRequest, defaultArgumentContext);
                                } finally {
                                    countDownLatch.countDown();
                                }
                            }
                        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            // INTENTIONALLY BLANK
        }
    }

    /**
     * Method to skip child test descriptors
     *
     * @param executionRequest executionRequest
     * @param defaultClassContext defaultClassContext
     */
    private void doSkip(
            ExecutionRequest executionRequest, DefaultClassContext defaultClassContext) {
        LOGGER.trace("doSkip() testClass [%s]", testClass.getName());

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME, getDisplayName());
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_DURATION,
                        Duration.ofMillis(0));

        executionRequest.getEngineExecutionListener().executionSkipped(this, "Skipped");

        getChildren().stream()
                .map(ToExecutableTestDescriptor.INSTANCE)
                .forEach(
                        executableTestDescriptor -> {
                            DefaultArgumentContext defaultArgumentContext =
                                    new DefaultArgumentContext(defaultClassContext);

                            defaultArgumentContext.setTestInstance(
                                    defaultClassContext.getTestInstance());

                            executableTestDescriptor.skip(executionRequest, defaultArgumentContext);
                        });
    }

    /**
     * Method to invoke all conclude methods
     *
     * @param defaultClassContext defaultClassContext
     * @throws Throwable Throwable
     */
    private void conclude(DefaultClassContext defaultClassContext) throws Throwable {
        LOGGER.trace("conclude() testClass [%s]", testClass.getName());

        ClassExtensionRegistry.getInstance()
                .conclude(defaultClassContext.asImmutable(), concludeMethods);
    }

    /**
     * Method to destroy the test instance
     *
     * @param defaultClassContext defaultClassContext
     * @throws Throwable Throwable
     */
    private void destroyTestInstance(DefaultClassContext defaultClassContext) throws Throwable {
        Object testInstance = defaultClassContext.getTestInstance();

        LOGGER.trace("destroyTestInstance() testClass [%s]", testClass.getName(), testInstance);

        Throwable throwable = null;

        try {
            ClassExtensionRegistry.getInstance().beforeDestroy(defaultClassContext);
        } catch (Throwable t) {
            throwable = t;
        }

        defaultClassContext.setTestInstance(null);

        if (testInstance instanceof AutoCloseable) {
            try {
                ((AutoCloseable) testInstance).close();
            } catch (Throwable t) {
                if (throwable == null) {
                    throwable = t;
                }
            }
        }

        if (throwable != null) {
            throw throwable;
        }
    }
}
