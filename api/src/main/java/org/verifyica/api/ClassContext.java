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

import java.util.Set;

/** Interface to implement ClassContext */
public interface ClassContext extends Context {

    /**
     * Get the EngineContext
     *
     * @return the EngineContext
     */
    EngineContext engineContext();

    /**
     * Get the test class
     *
     * @return the test class
     */
    Class<?> testClass();

    /**
     * Get the test class display name
     *
     * @return the test class display name
     */
    String testClassDisplayName();

    /**
     * Get the test class tags
     *
     * @return a Set of test class tags; may be empty
     */
    Set<String> testClassTags();

    /**
     * Get the test argument parallelism
     *
     * @return the test argument parallelism
     */
    int testArgumentParallelism();

    /**
     * Get the test class instance
     *
     * @return the test class instance
     */
    Object testInstance();

    /**
     * Get the test class instance
     *
     * @param type type
     * @return the test class instance
     * @param <V> the type
     */
    <V> V testInstance(Class<V> type);

    /**
     * Get the EngineContext
     *
     * @return the EngineContext
     */
    EngineContext getEngineContext();

    /**
     * Get the test class
     *
     * @return the test class
     */
    Class<?> getTestClass();

    /**
     * Get the test class display name
     *
     * @return the test class display name
     */
    String getTestClassDisplayName();

    /**
     * Get the test class tags
     *
     * @return a Set of test class tags; may be empty
     */
    Set<String> getTestClassTags();

    /**
     * Get the test argument parallelism
     *
     * @return the test argument parallelism
     */
    int getTestArgumentParallelism();

    /**
     * Get the test class instance
     *
     * @return the test class instance
     */
    Object getTestInstance();

    /**
     * Get the test class instance
     *
     * @param type type
     * @return the test class instance
     * @param <V> the type
     */
    <V> V getTestInstance(Class<V> type);
}
