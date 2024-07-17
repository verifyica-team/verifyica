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

package org.antublue.verifyica.engine.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.commons.util.UnrecoverableExceptions;
import org.junit.platform.engine.TestExecutionResult;

/** Class to implement ThrowableCollector */
public class ThrowableCollector {

    private final List<Throwable> throwables;
    private final boolean printStackTrace;

    /** Constructor */
    public ThrowableCollector() {
        this.throwables = Collections.synchronizedList(new ArrayList<>());
        this.printStackTrace = true;
    }

    /**
     * Method to execute an Executable
     *
     * @param executable executable
     */
    public void execute(Executable executable) {
        Throwable throwable = null;

        try {
            executable.execute();
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable != null) {
            if (throwable instanceof InvocationTargetException) {
                throwable = throwable.getCause();
            }

            if (printStackTrace) {
                synchronized (System.out) {
                    synchronized (System.err) {
                        throwable.printStackTrace();
                    }
                }
            }

            UnrecoverableExceptions.rethrowIfUnrecoverable(throwable);
            throwables.add(throwable);
        }
    }

    /**
     * Method to return if the ThrowableCollector has any Throwables
     *
     * @return true if there are no Throwables, else false
     */
    public boolean isEmpty() {
        return throwables.isEmpty();
    }

    /**
     * Method to return if the ThrowableCollector has any Throwables
     *
     * @return true if there are Throwables, else false
     */
    public boolean isNotEmpty() {
        return !throwables.isEmpty();
    }

    /**
     * Method to get the list of Throwables
     *
     * @return the list of Throwables
     */
    public List<Throwable> getThrowables() {
        return throwables;
    }

    /**
     * Method to convert the first Throwable to a TestExecutionResult
     *
     * @return a TestExecutionResult
     */
    public TestExecutionResult toTestExecutionResult() {
        if (this.isEmpty()) {
            return TestExecutionResult.successful();
        } else {
            return TestExecutionResult.failed(this.throwables.get(0));
        }
    }
}
