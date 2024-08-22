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

public class StateSetTest {

    @Test
    public void test() {
        StateSet<String> stateSet = new StateSet<>();

        assertThat(stateSet.getObservedStateEntries()).isNotNull();
        assertThat(stateSet.getObservedStateEntries()).isEmpty();

        stateSet.setCurrentState("FOO");

        assertThat(stateSet.getObservedStateEntries()).isNotNull();
        assertThat(stateSet.getObservedStateEntries()).hasSize(1);
        assertThat(stateSet.getFirstStateEntryWithThrowable()).isEmpty();

        stateSet.setCurrentState("BAR", new RuntimeException());

        assertThat(stateSet.isCurrentState("BAR")).isTrue();
        assertThat(stateSet.getCurrentState()).isEqualTo("BAR");
        assertThat(stateSet.getObservedStateEntries()).isNotNull();
        assertThat(stateSet.getObservedStateEntries()).hasSize(2);
        assertThat(stateSet.getFirstStateEntryWithThrowable()).isNotEmpty();

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> stateSet.setCurrentState("FOO"));

        stateSet.clear();

        assertThat(stateSet.isEmpty()).isTrue();
        assertThat(stateSet.getObservedStateEntries()).isNotNull();
        assertThat(stateSet.getObservedStateEntries()).isEmpty();
    }
}
