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

import org.verifyica.engine.common.Precondition;

/**
 * Class to implement a Result
 *
 * @param <T> the State type
 */
public class Result<T> {

    private final T state;
    private final Throwable throwable;

    /**
     * Constructor
     *
     * @param state state
     * @param throwable throwable
     */
    private Result(T state, Throwable throwable) {
        this.state = state;
        this.throwable = throwable;
    }

    /**
     * Constructor
     *
     * @param state state
     */
    private Result(T state) {
        this(state, null);
    }

    /**
     * Method to get the State
     *
     * @return the State
     */
    public T getState() {
        return state;
    }

    /**
     * Method to get the Throwable
     *
     * @return the Throwable
     */
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String toString() {
        return "Result{" + "state=" + state + ", throwable=" + throwable + '}';
    }

    /**
     * Method to create a Result
     *
     * @param state state
     * @param <T> the State type
     * @return a Result
     */
    public static <T> Result<T> of(T state) {
        Precondition.notNull(state, "state is null");

        return new Result<>(state);
    }

    /**
     * Method to create a Result
     *
     * @param state state
     * @param throwable throwable
     * @param <T> the State type
     * @return a Result
     */
    public static <T> Result<T> of(T state, Throwable throwable) {
        Precondition.notNull(state, "state is null");
        Precondition.notNull(throwable, "throwable is null");

        return new Result<>(state, throwable);
    }
}
