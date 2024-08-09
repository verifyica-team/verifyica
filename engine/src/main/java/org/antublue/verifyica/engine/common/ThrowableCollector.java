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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.platform.engine.TestExecutionResult;

/** Class to implement ThrowableCollector */
public class ThrowableCollector {

    private final List<Throwable> throwables;

    /** Constructor */
    public ThrowableCollector() {
        throwables = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Method to execute an Executable, collecting a Throwable if generated
     *
     * @param executable executable
     */
    public void execute(Executable executable) {
        try {
            executable.execute();
        } catch (Throwable t) {
            throwables.add(t);
        }
    }

    /**
     * Method to add a Throwable is not null
     *
     * @param throwable throwable
     */
    public void add(Throwable throwable) {
        if (throwable != null) {
            throwables.add(throwable);
        }
    }

    /**
     * Method to add a Throwable is not null
     *
     * @param throwables throwables
     */
    public void addAll(List<Throwable> throwables) {
        if (throwables != null) {
            throwables.forEach(throwable -> add(throwable));
        }
    }

    public void add(ThrowableCollector throwableCollector) {
        throwables.addAll(throwableCollector.throwables);
    }

    /**
     * Method to return if empty
     *
     * @return true if empty, else false
     */
    public boolean isEmpty() {
        return throwables.isEmpty();
    }

    /**
     * Method to get the first Throwable
     *
     * @return the first Throwable is not empty, else null
     */
    public Throwable getThrowable() {
        return !throwables.isEmpty() ? throwables.get(0) : null;
    }

    /**
     * Method to assert that no Throwables have been collect, throwing the first Throwable is not
     * empty
     *
     * @throws Throwable Throwable
     */
    public void assertEmpty() throws Throwable {
        Throwable throwable = getThrowable();
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Method to get a TestResult based on whether a Throwable was collected
     *
     * @return successful TestResult is not Throwables were collected, else a failed TestResult
     */
    public TestExecutionResult toTestExecutionResult() {
        Throwable throwable = getThrowable();
        if (throwable != null) {
            return TestExecutionResult.failed(throwable);
        } else {
            return TestExecutionResult.successful();
        }
    }

    /** Interface to implement Executable */
    public interface Executable {

        /**
         * Method to execute the Executable
         *
         * @throws Throwable Throwable
         */
        void execute() throws Throwable;
    }
}
