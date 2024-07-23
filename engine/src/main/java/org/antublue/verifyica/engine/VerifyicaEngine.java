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

package org.antublue.verifyica.engine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.antublue.verifyica.api.engine.ExtensionResult;
import org.antublue.verifyica.engine.context.DefaultEngineContext;
import org.antublue.verifyica.engine.context.DefaultEngineExtensionContext;
import org.antublue.verifyica.engine.discovery.EngineDiscoveryRequestResolver;
import org.antublue.verifyica.engine.execution.ExecutionRequestExecutor;
import org.antublue.verifyica.engine.execution.ExecutionRequestExecutorFactory;
import org.antublue.verifyica.engine.extension.EngineExtensionRegistry;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.util.ThrowableCollector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/** Class to implement VerifyicaEngine */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class VerifyicaEngine implements TestEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyicaEngine.class);

    /** Configuration constant */
    public static final String ID = "verifyica";

    /** Configuration constant */
    private static final String GROUP_ID = "org.antublue.verifyica";

    /** Configuration constant */
    private static final String ARTIFACT_ID = "engine";

    /** Configuration constant */
    public static final String VERSION = version();

    /** UniqueId constant */
    private static final String UNIQUE_ID = "[engine:" + ID + "]";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Optional<String> getGroupId() {
        return Optional.of(GROUP_ID);
    }

    @Override
    public Optional<String> getArtifactId() {
        return Optional.of(ARTIFACT_ID);
    }

    @Override
    public Optional<String> getVersion() {
        return Optional.of(VERSION);
    }

    /** Constructor */
    public VerifyicaEngine() {
        // DO NOTHING
    }

    @Override
    public TestDescriptor discover(
            EngineDiscoveryRequest engineDiscoveryRequest, UniqueId uniqueId) {
        if (!UNIQUE_ID.equals(uniqueId.toString())) {
            return null;
        }

        EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, getId());

        DefaultEngineContext.getInstance();

        LOGGER.trace("discover(" + uniqueId + ")");

        DefaultEngineExtensionContext defaultEngineExtensionContext =
                new DefaultEngineExtensionContext(DefaultEngineContext.getInstance());

        ThrowableCollector throwableCollector = new ThrowableCollector();

        AtomicReference<ExtensionResult> atomicReferenceExtensionResult = new AtomicReference<>();

        throwableCollector.execute(
                () ->
                        atomicReferenceExtensionResult.set(
                                EngineExtensionRegistry.getInstance()
                                        .onInitialize(defaultEngineExtensionContext)));

        if (atomicReferenceExtensionResult.get() == ExtensionResult.PROCEED
                && throwableCollector.isEmpty()) {
            new EngineDiscoveryRequestResolver()
                    .resolveSelectors(engineDiscoveryRequest, engineDescriptor);
        }

        return engineDescriptor;
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        if (executionRequest.getRootTestDescriptor().getChildren().isEmpty()) {
            return;
        }

        LOGGER.trace(
                "execute() rootTestDescriptor children [%d]",
                executionRequest.getRootTestDescriptor().getChildren().size());

        if (LOGGER.isTraceEnabled()) {
            traceEngineDescriptor(executionRequest.getRootTestDescriptor());
        }

        executionRequest
                .getEngineExecutionListener()
                .executionStarted(executionRequest.getRootTestDescriptor());

        DefaultEngineExtensionContext defaultEngineExtensionContext =
                new DefaultEngineExtensionContext(DefaultEngineContext.getInstance());

        ThrowableCollector throwableCollector = new ThrowableCollector();

        AtomicReference<ExtensionResult> atomicReferenceExtensionResult = new AtomicReference<>();

        throwableCollector.execute(
                () ->
                        atomicReferenceExtensionResult.set(
                                EngineExtensionRegistry.getInstance()
                                        .onExecute(defaultEngineExtensionContext)));

        if (atomicReferenceExtensionResult.get() == ExtensionResult.PROCEED
                && throwableCollector.isEmpty()) {
            throwableCollector.execute(
                    () -> {
                        ExecutionRequestExecutor executionRequestExecutor =
                                ExecutionRequestExecutorFactory.createExecutionRequestExecutor();
                        executionRequestExecutor.execute(executionRequest);
                        executionRequestExecutor.await();
                    });
        }

        throwableCollector.execute(
                () ->
                        throwableCollector
                                .getThrowables()
                                .addAll(
                                        EngineExtensionRegistry.getInstance()
                                                .onDestroy(defaultEngineExtensionContext)));

        defaultEngineExtensionContext.getEngineContext().getStore().clear();

        if (throwableCollector.isEmpty()) {
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(
                            executionRequest.getRootTestDescriptor(),
                            TestExecutionResult.successful());
        } else {
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(
                            executionRequest.getRootTestDescriptor(),
                            throwableCollector.toTestExecutionResult());
        }
    }

    /**
     * Method to get the version
     *
     * @return the version
     */
    public static String version() {
        String value = "unknown";

        try (InputStream inputStream =
                VerifyicaEngine.class.getResourceAsStream("/engine.properties")) {
            if (inputStream != null) {
                Properties properties = new Properties();
                properties.load(inputStream);
                value = properties.getProperty("version").trim();
            }
        } catch (IOException e) {
            // DO NOTHING
        }

        return value;
    }

    /**
     * Method trace log a test descriptor tree
     *
     * @param testDescriptor testDescriptor
     */
    private static void traceEngineDescriptor(TestDescriptor testDescriptor) {
        traceTestDescriptor(testDescriptor, 0);
    }

    /**
     * Method to recursively trace log a test descriptor tree
     *
     * @param testDescriptor testDescriptor
     * @param level level
     */
    private static void traceTestDescriptor(TestDescriptor testDescriptor, int level) {
        LOGGER.trace(String.join(" ", Collections.nCopies(level, " ")) + testDescriptor);
        testDescriptor
                .getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor1 -> traceTestDescriptor(testDescriptor1, level + 2));
    }
}
