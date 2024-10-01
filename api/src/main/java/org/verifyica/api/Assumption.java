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

package org.verifyica.api;

import java.util.function.BooleanSupplier;

/** Class to implement Assumption */
public class Assumption extends RuntimeException {

    /**
     * Constructor
     */
    private Assumption() {
        super();
    }

    /**
     * Constructor
     *
     * @param message message
     */
    private Assumption(String message) {
        super(message);
    }

    /**
     * Validate an assumption is true
     *
     * @param booleanSupplier booleanSupplier
     * @return null;
     * @param <V> ignored
     * @throws Assumption Assumption
     */
    public static <V> V assumeTrue(BooleanSupplier booleanSupplier) throws Assumption {
        if (booleanSupplier == null || !booleanSupplier.getAsBoolean()) {
            throw new Assumption();
        }

        return null;
    }

    /**
     * Validate an assumption is true
     *
     * @param condition condition
     * @return null;
     * @throws Assumption Assumption
     */
    public static <V> V assumeTrue(boolean condition) throws Assumption {
        if (!condition) {
            throw new Assumption();
        }
        return null;
    }

    /**
     * Validate an assumption is false
     *
     * @param booleanSupplier booleanSupplier
     * @return null;
     * @param <V> ignored
     * @throws Assumption Assumption
     */
    public static <V> V assumeFalse(BooleanSupplier booleanSupplier) throws Assumption {
        if (booleanSupplier != null && booleanSupplier.getAsBoolean()) {
            throw new Assumption();
        }

        return null;
    }

    /**
     * Validate an assumption is false
     *
     * @param condition condition
     * @return null;
     * @throws Assumption Assumption
     */
    public static <V> V assumeFalse(boolean condition) throws Assumption {
        if (condition) {
            throw new Assumption();
        }
        return null;
    }
}
