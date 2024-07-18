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

package org.antublue.verifyica.engine.execution;

import io.github.thunkware.vt.bridge.ThreadTool;
import java.util.Optional;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.configuration.DefaultConfiguration;
import org.antublue.verifyica.engine.execution.impl.PlatformThreadsExecutionRequestExecutor;
import org.antublue.verifyica.engine.execution.impl.VirtualThreadsExecutionRequestExecutor;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;

/** Class to implement ExecutionRequestExecutorFactory */
public class ExecutionRequestExecutorFactory {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ExecutionRequestExecutorFactory.class);

    private static final boolean useVirtualThreads =
            ThreadTool.hasVirtualThreads()
                    && Optional.ofNullable(
                                    DefaultConfiguration.getInstance()
                                            .getProperty(Constants.ENGINE_EXECUTOR_TYPE))
                            .map(value -> "virtual".equalsIgnoreCase(value))
                            .orElse(true);

    /** Constructor */
    private ExecutionRequestExecutorFactory() {
        // DO NOTHING
    }

    /**
     * Method to create an ExecutionRequestExecutor
     *
     * @return an ExecutionContextExecutor
     */
    public static ExecutionRequestExecutor createExecutionRequestExecutor() {
        ExecutionRequestExecutor executionRequestExecutor;

        if (useVirtualThreads) {
            executionRequestExecutor = new VirtualThreadsExecutionRequestExecutor();
        } else {
            executionRequestExecutor = new PlatformThreadsExecutionRequestExecutor();
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "executionRequestExecutor [%s]", executionRequestExecutor.getClass().getName());
        }

        return executionRequestExecutor;
    }
}
