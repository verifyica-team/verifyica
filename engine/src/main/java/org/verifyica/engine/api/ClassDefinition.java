/*
 * Copyright (C) Verifyica project authors and contributors
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

package org.verifyica.engine.api;

import java.util.List;
import java.util.Set;
import org.verifyica.api.Argument;

/** Class to implement ClassDefinition */
public interface ClassDefinition {

    /**
     * Get the test class
     *
     * @return the test class
     */
    Class<?> getTestClass();

    /**
     * Set the test class display name
     *
     * @param displayName displayName
     */
    void setDisplayName(String displayName);

    /**
     * Get the test class display name
     *
     * @return the test class display name
     */
    String getDisplayName();

    /**
     * Get the Set of test class tags
     *
     * @return a Set of test class tags
     */
    Set<String> getTags();

    /**
     * Get the set test method definition set
     *
     * @return a set test method definition set
     */
    Set<MethodDefinition> getTestMethodDefinitions();

    /**
     * Get the test argument list
     *
     * @return the test argument list
     */
    List<Argument<?>> getArguments();

    /**
     * Get the test argument parallelism
     *
     * @return the test argument parallelism
     */
    int getArgumentParallelism();
}
