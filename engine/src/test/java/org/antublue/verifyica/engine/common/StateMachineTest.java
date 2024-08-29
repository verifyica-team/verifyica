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
import org.junit.jupiter.api.Test;

public class StateMachineTest {

    @Test
    public void test() {
        List<String> expected = new ArrayList<>();
        expected.add("start");
        expected.add("one");
        expected.add("two");
        expected.add("three");

        List<String> actual = new ArrayList<>();
        RuntimeException runtimeException = new RuntimeException();

        StateMachine<String> stateMachine =
                new StateMachine<String>()
                        .onState(
                                "start",
                                () -> {
                                    actual.add("start");
                                    System.out.println("start");
                                    return StateMachine.Result.of("one");
                                })
                        .onState(
                                "one",
                                () -> {
                                    actual.add("one");
                                    System.out.println("one");
                                    return StateMachine.Result.of("two");
                                })
                        .onState(
                                "two",
                                () -> {
                                    actual.add("two");
                                    System.out.println("two");
                                    return StateMachine.Result.of("three", runtimeException);
                                })
                        .onState(
                                "three",
                                () -> {
                                    actual.add("three");
                                    System.out.println("end");
                                    return StateMachine.Result.of("end");
                                });

        stateMachine.run("start", "end");

        Optional<StateMachine.Result<String>> stateResult =
                stateMachine.getFirstResultWithThrowable();

        assertThat(stateResult).isNotNull();
        assertThat(stateResult.get().getState()).isEqualTo("three");
        assertThat(stateResult.get().getThrowable()).isSameAs(runtimeException);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testDuplicateAction() {
        StateMachine<String> stateMachine = new StateMachine<>();

        stateMachine.onState("test", () -> StateMachine.Result.of("random"));

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(
                        () -> stateMachine.onState("test", () -> StateMachine.Result.of("random")));

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(
                        () ->
                                stateMachine.onStates(
                                        StateMachine.asList("test", "test2"),
                                        () -> StateMachine.Result.of("random")));
    }

    @Test
    public void testDuplicateStates() {
        StateMachine<String> stateMachine = new StateMachine<>();

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(
                        () ->
                                stateMachine.onStates(
                                        StateMachine.asList("test", "test"),
                                        () -> StateMachine.Result.of("random")));
    }

    @Test
    public void testNoAction() {
        StateMachine<String> stateMachine = new StateMachine<>();

        stateMachine.onState("test", () -> StateMachine.Result.of("random"));

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> stateMachine.run("start", "end"));
    }
}
