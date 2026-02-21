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
 * Provides context for an argument within a test execution.
 */
public interface ArgumentContext extends Context {

    /**
     * Returns the ClassContext.
     *
     * @return the ClassContext
     */
    ClassContext getClassContext();

    /**
     * Returns the Argument index.
     *
     * @return the Argument index
     */
    int getArgumentIndex();

    /**
     * Returns the Argument.
     *
     * @return the Argument
     */
    Argument<?> getArgument();

    /**
     * Returns the Argument cast to the specified type.
     *
     * @param type the type to cast the Argument to
     * @return the Argument cast to the specified type
     * @param <V> the type to cast the Argument to
     */
    <V> Argument<V> getArgumentAs(final Class<V> type);
}
