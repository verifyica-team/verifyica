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
import org.antublue.verifyica.engine.configuration.ConcreteConfiguration;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.execution.impl.PlatformThreadsExecutionRequestExecutor;
import org.antublue.verifyica.engine.execution.impl.VirtualThreadsExecutionRequestExecutor;

/** Class to implement ExecutionContextExecutorFactory */
public class ExecutionRequestExecutorFactory {

    private static final boolean useVirtualThreads =
            ThreadTool.hasVirtualThreads()
                    && "virtual"
                            .equalsIgnoreCase(
                                    ConcreteConfiguration.getInstance()
                                            .get(Constants.THREAD_TYPE)
                                            .orElse("platform"));

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
        if (useVirtualThreads) {
            return new VirtualThreadsExecutionRequestExecutor();
        } else {
            return new PlatformThreadsExecutionRequestExecutor();
        }
    }
}
