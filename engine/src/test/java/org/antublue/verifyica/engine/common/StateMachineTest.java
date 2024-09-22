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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.antublue.verifyica.engine.common.statemachine.Result;
import org.antublue.verifyica.engine.common.statemachine.StateMachine;
import org.junit.jupiter.api.Test;

public class StateMachineTest {

    private enum State {
        START,
        ONE,
        TWO,
        THREE,
        END,
        RANDOM,
        TEST1,
        TEST2
    }

    @Test
    public void test() {
        List<State> expected = new ArrayList<>();
        expected.add(State.START);
        expected.add(State.ONE);
        expected.add(State.TWO);
        expected.add(State.THREE);

        List<State> actual = new ArrayList<>();
        RuntimeException runtimeException = new RuntimeException();

        StateMachine<State> stateMachine =
                new StateMachine<State>()
                        .onState(
                                State.START,
                                () -> {
                                    actual.add(State.START);
                                    return Result.of(State.ONE);
                                })
                        .onState(
                                State.ONE,
                                () -> {
                                    actual.add(State.ONE);
                                    return Result.of(State.TWO);
                                })
                        .onState(
                                State.TWO,
                                () -> {
                                    actual.add(State.TWO);
                                    return Result.of(State.THREE, runtimeException);
                                })
                        .onState(
                                State.THREE,
                                () -> {
                                    actual.add(State.THREE);
                                    return Result.of(State.END);
                                });

        stateMachine.run(State.START, State.END);

        Optional<Result<State>> stateResult = stateMachine.getFirstResultWithThrowable();

        assertThat(stateResult).isNotNull();
        assertThat(stateResult).isPresent();
        assertThat(stateResult.get().getState()).isEqualTo(State.THREE);
        assertThat(stateResult.get().getThrowable()).isSameAs(runtimeException);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testDuplicateAction() {
        StateMachine<State> stateMachine = new StateMachine<>();

        stateMachine.onState(State.TEST1, () -> Result.of(State.RANDOM));

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> stateMachine.onState(State.TEST1, () -> Result.of(State.RANDOM)));

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(
                        () ->
                                stateMachine.onStates(
                                        StateMachine.asList(State.TEST1, State.TEST2),
                                        () -> Result.of(State.RANDOM)));
    }

    @Test
    public void testDuplicateStates() {
        StateMachine<State> stateMachine = new StateMachine<>();

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(
                        () ->
                                stateMachine.onStates(
                                        StateMachine.asList(State.TEST1, State.TEST1),
                                        () -> Result.of(State.RANDOM)));
    }

    @Test
    public void testNoAction() {
        StateMachine<State> stateMachine = new StateMachine<>();

        stateMachine.onState(State.TEST1, () -> Result.of(State.RANDOM));

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> stateMachine.run(State.START, State.END));
    }
}
