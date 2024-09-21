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

package org.antublue.verifyica.engine.descriptor;

import java.util.Objects;

/** Class to implement InvocationConstant */
public class InvocationConstant {

    /** Invocation constant for the test class ExecutorService */
    public static final InvocationConstant CLASS_EXECUTOR_SERVICE =
            new InvocationConstant("CLASS_EXECUTOR_SERVICE");

    /** Invocation constant from the test argument ExecutorService */
    public static final InvocationConstant ARGUMENT_EXECUTOR_SERVICE =
            new InvocationConstant("ARGUMENT_EXECUTOR_SERVICE");

    private final String value;

    /**
     * Constructor
     *
     * @param value value
     */
    private InvocationConstant(String value) {
        this.value = getClass().getName() + "." + value;
    }

    /**
     * Method to get the value
     *
     * @return the value
     */
    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvocationConstant that = (InvocationConstant) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
