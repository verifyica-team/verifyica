/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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

import static java.lang.String.format;

import java.util.Collection;
import java.util.Optional;

/** Class to implement Trap */
public class Trap {

    private Throwable throwable;

    /**
     * Constructor
     *
     * @param runnable runnable
     */
    public Trap(Runnable runnable) {
        if (runnable == null) {
            throw new IllegalArgumentException("runnable is null");
        }

        try {
            runnable.run();
        } catch (Throwable t) {
            throwable = t;
        }
    }

    /**
     * Get the Throwable is trapped
     *
     * @return an Optional containing the trapped Throwable or an empty Optional if no Throwable was trapped
     */
    public Optional<Throwable> throwable() {
        return Optional.ofNullable(throwable);
    }

    /**
     * Checks if a Throwable was trapped
     *
     * @return true if no Throwable was trapped, else false
     */
    public boolean isEmpty() {
        return throwable == null;
    }

    /**
     * Assert that no Throwable was trapped
     *
     * @throws Throwable The trapped Throwable
     */
    public void assertEmpty() throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Assert that no Throwable was trapped
     *
     * @param trap trap
     * @throws Throwable the first trapped Throwable
     */
    public static void assertEmpty(Trap trap) throws Throwable {
        if (trap == null) {
            throw new IllegalArgumentException("trap is null");
        }

        trap.assertEmpty();
    }

    /**
     * Assert that no Throwables were trapped
     *
     * @param traps traps
     * @throws Throwable the first trapped Throwable
     */
    public static void assertEmpty(Trap... traps) throws Throwable {
        if (traps == null) {
            throw new IllegalArgumentException("traps is null");
        }

        int i = 0;
        for (Trap trap : traps) {
            if (trap == null) {
                throw new IllegalArgumentException(format("traps[%d] is null", i));
            }
            i++;
        }

        for (Trap trap : traps) {
            trap.assertEmpty();
        }
    }

    /**
     * Assert that no Throwables were trapped
     *
     * @param traps traps
     * @throws Throwable the first trapped Throwable
     */
    public static void assertEmpty(Collection<Trap> traps) throws Throwable {
        if (traps == null) {
            throw new IllegalArgumentException("traps is null");
        }

        for (Trap trap : traps) {
            trap.assertEmpty();
        }
    }

    /** Interface to implement Runnable */
    public interface Runnable {

        /**
         * Method to run the Runnable
         *
         * @throws Throwable Throwable
         */
        void run() throws Throwable;
    }
}
