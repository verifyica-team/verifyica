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

package org.verifyica.api;

/**
 * Interface to implement ArgumentContext
 */
public interface ArgumentContext extends Context {

    /**
     * Get the ClassContext
     *
     * @return the ClassContext
     */
    ClassContext getClassContext();

    /**
     * Get the Argument index
     *
     * @return the Argument index
     */
    int getArgumentIndex();

    /**
     * Get the Argument
     *
     * @return the Argument
     */
    Argument<?> getArgument();

    /**
     * Get the Argument casting to the specified type
     *
     * @param type the type to cast the Argument to
     * @return the Argument
     * @param <V> type
     */
    <V> Argument<V> getArgumentAs(Class<V> type);
}
