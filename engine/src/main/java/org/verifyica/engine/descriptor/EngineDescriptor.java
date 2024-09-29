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

package org.verifyica.engine.descriptor;

import org.junit.platform.engine.UniqueId;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;

/** Class to implement EngineDescriptor */
public class EngineDescriptor extends TestableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(EngineDescriptor.class);

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     */
    public EngineDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);
    }

    @Override
    public TestableTestDescriptor test() {
        /*
        if (getChildren().isEmpty()) {
            return;
        }

        Stopwatch stopwatch = new Stopwatch();

        LOGGER.trace("execute()");

        Configuration configuration = ConcreteConfiguration.getInstance();

        EngineContext engineContext = new ConcreteEngineContext(configuration, VerifyicaTestEngine.staticGetVersion());

        EngineExecutionListener engineExecutionListener = configureEngineExecutionListeners(executionRequest);

        ExecutorService classExecutorService =
                ExecutorSupport.newExecutorService(getEngineClassParallelism(configuration));

        ExecutorService argumentExecutorService = new FairExecutorService(getEngineArgumentParallelism(configuration));

        TestDescriptor engineDescriptor = executionRequest.getRootTestDescriptor();

        for (TestDescriptor testDescriptor : engineDescriptor.getChildren()) {
            Injector.inject(testDescriptor, configuration);
            Injector.inject(testDescriptor, engineContext);
            Injector.inject(testDescriptor, engineExecutionListener);

            for (TestDescriptor childTestDescriptor : testDescriptor.getChildren()) {
                Injector.inject(childTestDescriptor, configuration);
                Injector.inject(childTestDescriptor, engineContext);
                Injector.inject(childTestDescriptor, engineExecutionListener);
                Injector.inject(childTestDescriptor, argumentExecutorService);

                for (TestDescriptor grandChildTestDescriptor : childTestDescriptor.getChildren()) {
                    Injector.inject(grandChildTestDescriptor, configuration);
                    Injector.inject(grandChildTestDescriptor, engineContext);
                    Injector.inject(grandChildTestDescriptor, engineExecutionListener);
                }
            }
        }

        List<Throwable> throwables = new ArrayList<>();

        try {
            if (LOGGER.isTraceEnabled()) {
                traceEngineDescriptor(executionRequest.getRootTestDescriptor());
            }

            try {
                // engineInterceptorManager.preExecute(engineInterceptorContext);

                executionRequest
                        .getEngineExecutionListener()
                        .executionStarted(executionRequest.getRootTestDescriptor());

                List<ExecutableTestDescriptor> executableClassTestDescriptors =
                        executionRequest.getRootTestDescriptor().getChildren().stream()
                                .filter(ExecutableTestDescriptor.EXECUTABLE_TEST_DESCRIPTOR_FILTER)
                                .map(ExecutableTestDescriptor.EXECUTABLE_TEST_DESCRIPTOR_MAPPER)
                                .collect(Collectors.toList());

                List<ExecutableArgumentTestDescriptor> executableArgumentTestDescriptors = new ArrayList<>();
                List<ExecutableTestMethodTestDescriptor> executableTestMethodTestDescriptors = new ArrayList<>();

                for (TestDescriptor testDescriptor :
                        executionRequest.getRootTestDescriptor().getChildren()) {
                    for (TestDescriptor childTestDescriptor : testDescriptor.getChildren()) {
                        executableArgumentTestDescriptors.add((ExecutableArgumentTestDescriptor) childTestDescriptor);
                        for (TestDescriptor grandChildTestDescriptor : childTestDescriptor.getChildren()) {
                            executableTestMethodTestDescriptors.add(
                                    (ExecutableTestMethodTestDescriptor) grandChildTestDescriptor);
                        }
                    }
                }

                LOGGER.trace("classTestDescriptors [%d]", executableClassTestDescriptors.size());
                LOGGER.trace("argumentTestDescriptors [%d]", executableArgumentTestDescriptors.size());
                LOGGER.trace("testMethodTestDescriptors [%d]", executableTestMethodTestDescriptors.size());

                List<Future<?>> futures = new ArrayList<>(executableClassTestDescriptors.size());

                executableClassTestDescriptors.forEach(classTestDescriptor -> {
                    Injector.inject(classTestDescriptor, argumentExecutorService);
                    String hash = HashSupport.alphanumeric(6);
                    String threadName = hash + "/" + hash;
                    ThreadNameRunnable threadNameRunnable =
                            new ThreadNameRunnable(threadName, classTestDescriptor::test);
                    Future<?> future = classExecutorService.submit(threadNameRunnable);
                    futures.add(future);
                });

                ExecutorSupport.waitForAllFutures(futures, classExecutorService);
            } catch (Throwable t) {
                StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
                throwables.add(t);
            } finally {
                ExecutorSupport.shutdownAndAwaitTermination(argumentExecutorService);
                ExecutorSupport.shutdownAndAwaitTermination(classExecutorService);

                Store store = engineContext.getStore();
                for (Object key : store.keySet()) {
                    Object value = store.remove(key);
                    if (value instanceof AutoCloseable) {
                        try {
                            LOGGER.trace("storeAutoClose(" + key + ")");
                            ((AutoCloseable) value).close();
                            LOGGER.trace("storeAutoClose(" + key + ").success");
                        } catch (Throwable t) {
                            LOGGER.trace("storeAutoClose(" + key + ").failure");
                            StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
                            throwables.add(t);
                        }
                    }
                }
                store.clear();

                /*
                try {
                    engineInterceptorManager.postExecute(engineInterceptorContext);
                } catch (Throwable t) {
                    StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
                    throwables.add(t);
                }
                */
        /*
            }
        } finally {
            /*
            engineInterceptorManager.onDestroy(engineInterceptorContext);

            TestExecutionResult testExecutionResult = throwables.isEmpty()
                    ? TestExecutionResult.successful()
                    : TestExecutionResult.failed(throwables.get(0));
            */
        /*
            TestExecutionResult testExecutionResult = TestExecutionResult.successful();

            engineExecutionListener.executionFinished(executionRequest.getRootTestDescriptor(), testExecutionResult);

            LOGGER.trace(
                    "execute() elapsedTime [%d] ms", stopwatch.elapsedTime().toMillis());
        }
        */

        return this;
    }
}
