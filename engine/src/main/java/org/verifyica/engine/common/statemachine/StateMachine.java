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

package org.verifyica.engine.common.statemachine;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.verifyica.engine.common.Precondition;

/**
 * Class to implement StateMachine
 *
 * @param <T> the State type
 */
public class StateMachine<T extends Enum<T>> {

    private final Map<T, Action<T>> actions;
    private final List<Result<T>> results;

    /** Constructor */
    public StateMachine() {
        this.actions = new HashMap<>();
        this.results = new ArrayList<>();
    }

    /**
     * Method to register an Action
     *
     * @param state state
     * @param action action
     * @return the StateMachine
     */
    public StateMachine<T> onState(T state, Action<T> action) {
        Precondition.notNull(state, "state is null");
        Precondition.notNull(action, "action is null");

        return onStates(asList(state), action);
    }

    /**
     * Method to register an Action
     *
     * @param states states
     * @param action action
     * @return the StateMachine
     */
    public StateMachine<T> onStates(List<T> states, Action<T> action) {
        Precondition.notNull(states, "states is null");
        Precondition.isFalse(states.isEmpty(), "states is empty");
        Precondition.notNull(action, "action is null");

        for (T state : states) {
            if (actions.putIfAbsent(state, action) != null) {
                throw new IllegalStateException(
                        format("Action already registered for State [%s]", state));
            }
        }

        return this;
    }

    /**
     * Method to run the StateMachine
     *
     * @param initialState initialState
     * @param finalState finalState
     * @return the StateMachine
     */
    public StateMachine<T> run(T initialState, T finalState) {
        Precondition.notNull(initialState, "initialState is null");
        Precondition.notNull(finalState, "finalState is null");

        T state = initialState;
        results.add(Result.of(state));
        Action<T> action;
        Result<T> result;

        do {
            action = actions.get(state);
            if (action == null) {
                throw new IllegalStateException(
                        format("No Action registered for State [%s]", state));
            }

            result = action.execute();
            if (result == null) {
                throw new IllegalStateException(
                        format("Action for State [%s] returned null", state));
            }

            if (result.getState() == null) {
                throw new IllegalStateException(
                        format("Action for State [%s] returned a Result with a null State", state));
            }

            results.add(result);
            state = result.getState();
        } while (state != finalState && state.compareTo(finalState) != 0);

        return this;
    }

    /**
     * Method to get the first Result with a Throwable
     *
     * @return an Optional containing the first Result with a Throwable, or Optional.empty()
     */
    public Optional<Result<T>> getFirstResultWithThrowable() {
        return results.stream().filter(result -> result.getThrowable() != null).findFirst();
    }

    @Override
    public String toString() {
        return "StateMachine{" + "results=" + results + '}';
    }

    /**
     * Method to create a List of States from an array of States
     *
     * @param states states
     * @return a List of States
     * @param <T> the State type
     */
    @SafeVarargs
    public static <T> List<T> asList(T... states) {
        Precondition.notNull(states, "states is null");
        Precondition.isTrue(states.length > 0, "states is empty");

        for (int i = 0; i < states.length; i++) {
            Precondition.notNull(states[i], format("State[%d] is null", i));
        }

        return Arrays.asList(states);
    }
}
