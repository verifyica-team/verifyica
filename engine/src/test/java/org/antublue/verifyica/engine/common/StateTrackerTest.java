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

package org.antublue.verifyica.engine.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

public class StateTrackerTest {

    @Test
    public void test() {
        StateTracker<String> stateTracker = new StateTracker<>();

        assertThat(stateTracker.getObservedStates()).isNotNull();
        assertThat(stateTracker.getObservedStates()).isEmpty();

        stateTracker.setState("FOO");

        assertThat(stateTracker.getObservedStates()).isNotNull();
        assertThat(stateTracker.getObservedStates()).hasSize(1);
        assertThat(stateTracker.getStateWithThrowable()).isEmpty();

        stateTracker.setState("BAR", new RuntimeException());

        assertThat(stateTracker.isState("BAR")).isTrue();
        assertThat(stateTracker.getState()).isEqualTo("BAR");
        assertThat(stateTracker.getObservedStates()).isNotNull();
        assertThat(stateTracker.getObservedStates()).hasSize(2);
        assertThat(stateTracker.getStateWithThrowable()).isNotEmpty();

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> stateTracker.setState("FOO"));

        stateTracker.clear();

        assertThat(stateTracker.getObservedStates()).isNotNull();
        assertThat(stateTracker.getObservedStates()).isEmpty();
    }
}
