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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;

@DisplayName("ChainedEngineExecutionListener Tests")
public class ChainedEngineExecutionListenerTest {

    @Test
    @DisplayName("Should create with single listener")
    public void shouldCreateWithSingleListener() {
        EngineExecutionListener mockListener = mock(EngineExecutionListener.class);

        ChainedEngineExecutionListener chained = new ChainedEngineExecutionListener(mockListener);

        assertThat(chained).isNotNull();
        assertThat(chained.getEngineExecutionListeners()).hasSize(1);
    }

    @Test
    @DisplayName("Should throw exception for null single listener")
    public void shouldThrowExceptionForNullSingleListener() {
        assertThatThrownBy(() -> new ChainedEngineExecutionListener((EngineExecutionListener) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("engineExecutionListener is null");
    }

    @Test
    @DisplayName("Should create with varargs listeners")
    public void shouldCreateWithVarargsListeners() {
        EngineExecutionListener mockListener1 = mock(EngineExecutionListener.class);
        EngineExecutionListener mockListener2 = mock(EngineExecutionListener.class);

        ChainedEngineExecutionListener chained = new ChainedEngineExecutionListener(mockListener1, mockListener2);

        assertThat(chained).isNotNull();
        assertThat(chained.getEngineExecutionListeners()).hasSize(2);
    }

    @Test
    @DisplayName("Should throw exception for null varargs")
    public void shouldThrowExceptionForNullVarargs() {
        assertThatThrownBy(() -> new ChainedEngineExecutionListener((EngineExecutionListener[]) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("engineExecutionListeners is null");
    }

    @Test
    @DisplayName("Should throw exception for empty varargs")
    public void shouldThrowExceptionForEmptyVarargs() {
        assertThatThrownBy(() -> new ChainedEngineExecutionListener(new EngineExecutionListener[0]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("engineExecutionListeners is empty");
    }

    @Test
    @DisplayName("Should create with collection of listeners")
    public void shouldCreateWithCollectionOfListeners() {
        EngineExecutionListener mockListener1 = mock(EngineExecutionListener.class);
        EngineExecutionListener mockListener2 = mock(EngineExecutionListener.class);
        Collection<EngineExecutionListener> listeners = Arrays.asList(mockListener1, mockListener2);

        ChainedEngineExecutionListener chained = new ChainedEngineExecutionListener(listeners);

        assertThat(chained).isNotNull();
        assertThat(chained.getEngineExecutionListeners()).hasSize(2);
    }

    @Test
    @DisplayName("Should throw exception for null collection")
    public void shouldThrowExceptionForNullCollection() {
        assertThatThrownBy(() -> new ChainedEngineExecutionListener((Collection<EngineExecutionListener>) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("engineExecutionListeners is null");
    }

    @Test
    @DisplayName("Should throw exception for empty collection")
    public void shouldThrowExceptionForEmptyCollection() {
        assertThatThrownBy(() -> new ChainedEngineExecutionListener(Arrays.asList()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("engineExecutionListeners is empty");
    }

    @Test
    @DisplayName("Should add listener")
    public void shouldAddListener() {
        EngineExecutionListener mockListener1 = mock(EngineExecutionListener.class);
        EngineExecutionListener mockListener2 = mock(EngineExecutionListener.class);

        ChainedEngineExecutionListener chained = new ChainedEngineExecutionListener(mockListener1);
        chained.add(mockListener2);

        assertThat(chained.getEngineExecutionListeners()).hasSize(2);
    }

    @Test
    @DisplayName("Should throw exception when adding null listener")
    public void shouldThrowExceptionWhenAddingNullListener() {
        EngineExecutionListener mockListener = mock(EngineExecutionListener.class);
        ChainedEngineExecutionListener chained = new ChainedEngineExecutionListener(mockListener);

        assertThatThrownBy(() -> chained.add(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("engineExecutionListener is null");
    }

    @Test
    @DisplayName("Should return this when adding listener")
    public void shouldReturnThisWhenAddingListener() {
        EngineExecutionListener mockListener1 = mock(EngineExecutionListener.class);
        EngineExecutionListener mockListener2 = mock(EngineExecutionListener.class);

        ChainedEngineExecutionListener chained = new ChainedEngineExecutionListener(mockListener1);
        ChainedEngineExecutionListener result = chained.add(mockListener2);

        assertThat(result).isSameAs(chained);
    }

    @Test
    @DisplayName("Should notify all listeners on dynamicTestRegistered")
    public void shouldNotifyAllListenersOnDynamicTestRegistered() {
        EngineExecutionListener mockListener1 = mock(EngineExecutionListener.class);
        EngineExecutionListener mockListener2 = mock(EngineExecutionListener.class);
        TestDescriptor testDescriptor = mock(TestDescriptor.class);

        ChainedEngineExecutionListener chained = new ChainedEngineExecutionListener(mockListener1, mockListener2);
        chained.dynamicTestRegistered(testDescriptor);

        verify(mockListener1, times(1)).dynamicTestRegistered(testDescriptor);
        verify(mockListener2, times(1)).dynamicTestRegistered(testDescriptor);
    }

    @Test
    @DisplayName("Should notify all listeners on executionSkipped")
    public void shouldNotifyAllListenersOnExecutionSkipped() {
        EngineExecutionListener mockListener1 = mock(EngineExecutionListener.class);
        EngineExecutionListener mockListener2 = mock(EngineExecutionListener.class);
        TestDescriptor testDescriptor = mock(TestDescriptor.class);
        String reason = "Test skipped";

        ChainedEngineExecutionListener chained = new ChainedEngineExecutionListener(mockListener1, mockListener2);
        chained.executionSkipped(testDescriptor, reason);

        verify(mockListener1, times(1)).executionSkipped(testDescriptor, reason);
        verify(mockListener2, times(1)).executionSkipped(testDescriptor, reason);
    }

    @Test
    @DisplayName("Should notify all listeners on executionStarted")
    public void shouldNotifyAllListenersOnExecutionStarted() {
        EngineExecutionListener mockListener1 = mock(EngineExecutionListener.class);
        EngineExecutionListener mockListener2 = mock(EngineExecutionListener.class);
        TestDescriptor testDescriptor = mock(TestDescriptor.class);

        ChainedEngineExecutionListener chained = new ChainedEngineExecutionListener(mockListener1, mockListener2);
        chained.executionStarted(testDescriptor);

        verify(mockListener1, times(1)).executionStarted(testDescriptor);
        verify(mockListener2, times(1)).executionStarted(testDescriptor);
    }

    @Test
    @DisplayName("Should notify all listeners on executionFinished")
    public void shouldNotifyAllListenersOnExecutionFinished() {
        EngineExecutionListener mockListener1 = mock(EngineExecutionListener.class);
        EngineExecutionListener mockListener2 = mock(EngineExecutionListener.class);
        TestDescriptor testDescriptor = mock(TestDescriptor.class);
        TestExecutionResult result = mock(TestExecutionResult.class);

        ChainedEngineExecutionListener chained = new ChainedEngineExecutionListener(mockListener1, mockListener2);
        chained.executionFinished(testDescriptor, result);

        verify(mockListener1, times(1)).executionFinished(testDescriptor, result);
        verify(mockListener2, times(1)).executionFinished(testDescriptor, result);
    }

    @Test
    @DisplayName("Should notify all listeners on reportingEntryPublished")
    public void shouldNotifyAllListenersOnReportingEntryPublished() {
        EngineExecutionListener mockListener1 = mock(EngineExecutionListener.class);
        EngineExecutionListener mockListener2 = mock(EngineExecutionListener.class);
        TestDescriptor testDescriptor = mock(TestDescriptor.class);
        ReportEntry entry = mock(ReportEntry.class);

        ChainedEngineExecutionListener chained = new ChainedEngineExecutionListener(mockListener1, mockListener2);
        chained.reportingEntryPublished(testDescriptor, entry);

        verify(mockListener1, times(1)).reportingEntryPublished(testDescriptor, entry);
        verify(mockListener2, times(1)).reportingEntryPublished(testDescriptor, entry);
    }

    @Test
    @DisplayName("Should handle listener throwing exception")
    public void shouldHandleListenerThrowingException() {
        EngineExecutionListener throwingListener = new EngineExecutionListener() {
            @Override
            public void executionStarted(TestDescriptor testDescriptor) {
                throw new RuntimeException("Test exception");
            }

            @Override
            public void dynamicTestRegistered(TestDescriptor testDescriptor) {
                // INTENTIONALLY EMPTY
            }

            @Override
            public void executionSkipped(TestDescriptor testDescriptor, String reason) {
                // INTENTIONALLY EMPTY
            }

            @Override
            public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
                // INTENTIONALLY EMPTY
            }

            @Override
            public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
                // INTENTIONALLY EMPTY
            }
        };

        EngineExecutionListener mockListener = mock(EngineExecutionListener.class);
        TestDescriptor testDescriptor = mock(TestDescriptor.class);

        ChainedEngineExecutionListener chained = new ChainedEngineExecutionListener(throwingListener, mockListener);

        assertThatCode(() -> chained.executionStarted(testDescriptor))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Test exception");
    }
}
