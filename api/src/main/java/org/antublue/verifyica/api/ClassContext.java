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

package org.antublue.verifyica.api;

/** Class to implement ClassContext */
public interface ClassContext extends Context {

    /**
     * Returns the EngineContext
     *
     * @return the EngineContext
     */
    EngineContext getEngineContext();

    /**
     * Returns the test class
     *
     * @return the test class
     */
    Class<?> getTestClass();

    /**
     * Returns the test class display name
     *
     * @return the test class display name
     */
    String getTestClassDisplayName();

    /**
     * Returns the test class instance
     *
     * @return the test class instance
     */
    Object getTestInstance();

    /**
     * Returns the test class instance
     *
     * @param type type
     * @return the test class instance
     * @param <V> the type
     */
    <V> V getTestInstance(Class<V> type);

    /**
     * Returns the test argument parallelism
     *
     * @return the test argument parallelism
     */
    int getTestArgumentParallelism();
}
