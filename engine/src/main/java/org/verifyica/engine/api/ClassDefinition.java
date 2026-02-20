/*
 * Copyright (C) Verifyica project authors and contributors. All rights reserved.
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

/**
 * Interface to provide a class definition.
 */
public interface ClassDefinition {

    /**
     * Returns the test class.
     *
     * @return the test class
     */
    Class<?> getTestClass();

    /**
     * Sets the test class display name.
     *
     * @param displayName the display name
     */
    void setDisplayName(String displayName);

    /**
     * Returns the test class display name.
     *
     * @return the test class display name
     */
    String getDisplayName();

    /**
     * Returns the Set of test class tags.
     *
     * @return a Set of test class tags
     */
    Set<String> getTags();

    /**
     * Returns the test method definition set.
     *
     * @return a test method definition set
     */
    Set<MethodDefinition> getTestMethodDefinitions();

    /**
     * Returns the test argument list.
     *
     * @return the test argument list
     */
    List<Argument<?>> getArguments();

    /**
     * Returns the test argument parallelism.
     *
     * @return the test argument parallelism
     */
    int getArgumentParallelism();
}
