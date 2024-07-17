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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import org.antublue.verifyica.api.Context;
import org.antublue.verifyica.engine.configuration.ConcreteConfiguration;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.context.ConcreteArgumentContext;
import org.antublue.verifyica.engine.context.ConcreteClassContext;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ObjectSupport;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
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
                                    ConcreteConfiguration.getInstance()
                                            .get(Constants.THREAD_TYPE)
                                            .orElse("platform"));

    private final Class<?> testClass;
    private final int permits;
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
     * @param permits permits
     */
    public ClassTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            List<Method> prepareMethods,
            List<Method> concludeMethods,
            int permits) {
        super(uniqueId, displayName);

        Preconditions.notNull(testClass, "testClass is null");

        this.testClass = testClass;
        this.prepareMethods = prepareMethods;
        this.concludeMethods = concludeMethods;
        this.permits = permits;
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
            LOGGER.trace("execute ClassTestDescriptor [%s]", toString());
        }

        stopWatch.reset();

        ConcreteClassContext concreteClassContext = (ConcreteClassContext) context;

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME, getDisplayName());

        executionRequest.getEngineExecutionListener().executionStarted(this);

        throwableCollector.execute(() -> createTestInstance(concreteClassContext));
        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(() -> invokePrepareMethods(concreteClassContext));
            if (throwableCollector.isEmpty()) {
                doExecute(executionRequest, concreteClassContext);
            } else {
                doSkip(executionRequest, concreteClassContext);
            }
            throwableCollector.execute(() -> invokeConcludeMethods(concreteClassContext));
        }
        throwableCollector.execute(() -> destroyTestInstance(concreteClassContext));

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
            LOGGER.trace("skip ClassTestDescriptor [%s]", toString());
        }

        stopWatch.reset();

        ConcreteClassContext concreteClassContext = (ConcreteClassContext) context;

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

        getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor -> {
                                    if (testDescriptor instanceof ExecutableTestDescriptor) {
                                        ((ExecutableTestDescriptor) testDescriptor)
                                                .skip(executionRequest, concreteClassContext);
                                    }
                                });

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
                + " testClass["
                + testClass.getName()
                + "]"
                + " prepareMethods["
                + ObjectSupport.toString(prepareMethods)
                + "]"
                + " concludeMethods["
                + ObjectSupport.toString(concludeMethods)
                + "] "
                + "}";
    }

    private void createTestInstance(ConcreteClassContext concreteClassContext) throws Throwable {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("createTestInstance() testClass [%s]", testClass.getName());
        }

        Object testInstance =
                testClass.getDeclaredConstructor((Class<?>[]) null).newInstance((Object[]) null);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "createTestInstance() testClass [%s] testInstance [%s]",
                    testClass.getName(), testInstance);
        }

        concreteClassContext.setTestInstance(testInstance);
    }

    private void invokePrepareMethods(ConcreteClassContext concreteClassContext) throws Throwable {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("invokePrepareMethods() testClass [%s]", testClass.getName());
        }

        for (Method method : prepareMethods) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "invokePrepareMethods() testClass [%s] method [%s]",
                        testClass.getName(), method);
            }

            method.invoke(null, concreteClassContext.toImmutable());
        }
    }

    private void doExecute(
            ExecutionRequest executionRequest, ConcreteClassContext concreteClassContext) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("doExecute() testClass [%s]", testClass.getName());
        }

        Preconditions.notNull(concreteClassContext, "concreteClassContext is null");
        Preconditions.notNull(
                concreteClassContext.getTestInstance(), "concreteClassContext is null");

        CountDownLatch countDownLatch = new CountDownLatch(getChildren().size());
        Semaphore semaphore = new Semaphore(permits);

        getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor -> {
                                    if (testDescriptor instanceof ArgumentTestDescriptor) {
                                        ExecutableTestDescriptor executableTestDescriptor =
                                                (ExecutableTestDescriptor) testDescriptor;

                                        ConcreteArgumentContext concreteArgumentContext =
                                                new ConcreteArgumentContext(concreteClassContext);

                                        concreteArgumentContext.setTestInstance(
                                                concreteClassContext.getTestInstance());

                                        if (permits > 1) {
                                            try {
                                                semaphore.acquire();
                                            } catch (Throwable t) {
                                                // DO NOTHING
                                            }

                                            Runnable runnable =
                                                    () -> {
                                                        try {
                                                            executableTestDescriptor.execute(
                                                                    executionRequest,
                                                                    concreteArgumentContext);
                                                        } finally {
                                                            semaphore.release();
                                                            countDownLatch.countDown();
                                                        }
                                                    };

                                            Thread thread;

                                            if (useVirtualThreads) {
                                                thread =
                                                        ThreadTool.unstartedVirtualThread(runnable);
                                            } else {
                                                thread = new Thread(runnable);
                                            }

                                            thread.setDaemon(true);
                                            thread.setName(Thread.currentThread().getName());
                                            thread.start();
                                        } else {
                                            try {
                                                executableTestDescriptor.execute(
                                                        executionRequest, concreteArgumentContext);
                                            } finally {
                                                countDownLatch.countDown();
                                            }
                                        }
                                    }
                                });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            // DO NOTHING
        }
    }

    private void doSkip(
            ExecutionRequest executionRequest, ConcreteClassContext concreteClassContext) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("doSkip() testClass [%s]", testClass.getName());
        }

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME, getDisplayName());
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_DURATION,
                        Duration.ofMillis(0));

        executionRequest.getEngineExecutionListener().executionSkipped(this, "Skipped");

        getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor -> {
                                    if (testDescriptor instanceof ArgumentTestDescriptor) {
                                        ArgumentTestDescriptor argumentTestDescriptor =
                                                (ArgumentTestDescriptor) testDescriptor;

                                        ConcreteArgumentContext concreteArgumentContext =
                                                new ConcreteArgumentContext(concreteClassContext);

                                        concreteArgumentContext.setTestInstance(
                                                concreteClassContext.getTestInstance());

                                        argumentTestDescriptor.skip(
                                                executionRequest, concreteArgumentContext);
                                    }
                                });
    }

    private void invokeConcludeMethods(ConcreteClassContext concreteClassContext) throws Throwable {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("invokeConcludeMethods() testClass [%s]", testClass.getName());
        }

        for (Method method : concludeMethods) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "invokeConcludeMethods() testClass [%s] method [%s]",
                        testClass.getName(), method);
            }

            method.invoke(null, concreteClassContext.toImmutable());
        }

        concreteClassContext.getStore().clear();
    }

    private void destroyTestInstance(ConcreteClassContext concreteClassContext) {
        Object testInstance = concreteClassContext.getTestInstance();
        LOGGER.trace("destroyTestInstance() testClass [%s]", testClass.getName(), testInstance);
        concreteClassContext.setTestInstance(null);
    }
}
