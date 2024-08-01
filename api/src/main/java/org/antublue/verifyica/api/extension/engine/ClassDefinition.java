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

package org.antublue.verifyica.api.extension.engine;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import org.antublue.verifyica.api.Argument;

/** Class to implement ClassDefinition */
public interface ClassDefinition {

    /**
     * Get the test class
     *
     * @return the test class
     */
    Class<?> getTestClass();

    /**
     * Get the test methods
     *
     * @return the test methods
     */
    Set<Method> getTestMethods();

    /**
     * Get the test arguments
     *
     * @return the test arguments
     */
    List<Argument<?>> getTestArguments();

    /**
     * Get test argument parallelism
     *
     * @return test argument parallelism
     */
    int getTestArgumentParallelism();

    /**
     * Set test argument parallelism
     *
     * @param parallelism parallelism
     */
    void setTestArgumentParallelism(int parallelism);
}
