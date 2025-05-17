/*
 * Copyright (C) Verifyica project authors and contributors
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

package org.verifyica.engine.listener;

import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;

/** Class to implement TracingEngineExecutionListener */
public class TracingEngineExecutionListener implements EngineExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TracingEngineExecutionListener.class);

    /** Constructor */
    public TracingEngineExecutionListener() {
        // INTENTIONALLY BLANK
    }

    @Override
    public void dynamicTestRegistered(TestDescriptor testDescriptor) {
        LOGGER.trace("dynamicTestRegistered() testDescriptor [%s]", testDescriptor);
    }

    @Override
    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        LOGGER.trace("executionSkipped() testDescriptor [%s] reason [%s]", testDescriptor, reason);
    }

    @Override
    public void executionStarted(TestDescriptor testDescriptor) {
        LOGGER.trace("executionStarted() testDescriptor [%s]", testDescriptor);
    }

    @Override
    public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        LOGGER.trace(
                "executionFinished() testDescriptor [%s] testExecutionResult [%s]",
                testDescriptor, testExecutionResult);
    }

    @Override
    public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry reportEntry) {
        LOGGER.trace("reportingEntryPublished() testDescriptor [%s] reportEntry [%s]", testDescriptor, reportEntry);
    }
}
