/*
 * Copyright (C) Verifyica project authors and contributors. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;

@DisplayName("TracingEngineExecutionListener Tests")
public class TracingEngineExecutionListenerTest {

    @Test
    @DisplayName("Should implement EngineExecutionListener interface")
    public void shouldImplementEngineExecutionListenerInterface() {
        TracingEngineExecutionListener listener = new TracingEngineExecutionListener();

        assertThat(listener).isInstanceOf(EngineExecutionListener.class);
    }

    @Test
    @DisplayName("Should create instance without exception")
    public void shouldCreateInstanceWithoutException() {
        assertThatCode(() -> new TracingEngineExecutionListener()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle dynamicTestRegistered without exception")
    public void shouldHandleDynamicTestRegisteredWithoutException() {
        TracingEngineExecutionListener listener = new TracingEngineExecutionListener();
        TestDescriptor testDescriptor = mock(TestDescriptor.class);

        assertThatCode(() -> listener.dynamicTestRegistered(testDescriptor)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle executionSkipped without exception")
    public void shouldHandleExecutionSkippedWithoutException() {
        TracingEngineExecutionListener listener = new TracingEngineExecutionListener();
        TestDescriptor testDescriptor = mock(TestDescriptor.class);

        assertThatCode(() -> listener.executionSkipped(testDescriptor, "Test skipped"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle executionStarted without exception")
    public void shouldHandleExecutionStartedWithoutException() {
        TracingEngineExecutionListener listener = new TracingEngineExecutionListener();
        TestDescriptor testDescriptor = mock(TestDescriptor.class);

        assertThatCode(() -> listener.executionStarted(testDescriptor)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle executionFinished without exception")
    public void shouldHandleExecutionFinishedWithoutException() {
        TracingEngineExecutionListener listener = new TracingEngineExecutionListener();
        TestDescriptor testDescriptor = mock(TestDescriptor.class);
        TestExecutionResult result = mock(TestExecutionResult.class);

        assertThatCode(() -> listener.executionFinished(testDescriptor, result)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle reportingEntryPublished without exception")
    public void shouldHandleReportingEntryPublishedWithoutException() {
        TracingEngineExecutionListener listener = new TracingEngineExecutionListener();
        TestDescriptor testDescriptor = mock(TestDescriptor.class);
        ReportEntry entry = mock(ReportEntry.class);

        assertThatCode(() -> listener.reportingEntryPublished(testDescriptor, entry))
                .doesNotThrowAnyException();
    }
}
