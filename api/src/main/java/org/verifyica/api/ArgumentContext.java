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

package org.verifyica.api;

/** Interface to implement ArgumentContext */
public interface ArgumentContext extends Context {

    /**
     * Get the EngineContext
     *
     * <p>Equivalent to getClassContext().getEngineContext()
     *
     * @return the Engine Configuration
     */
    default EngineContext engineContext() {
        return getEngineContext();
    }

    /**
     * Get the ClassContext
     *
     * @return the ClassContext
     */
    default ClassContext classContext() {
        return getClassContext();
    }

    /**
     * Get the Argument index
     *
     * @return the Argument index
     */
    default int testArgumentIndex() {
        return getTestArgumentIndex();
    }

    /**
     * Get the Argument
     *
     * @return the Argument
     */
    default Argument<?> testArgument() {
        return getTestArgument();
    }

    /**
     * Get the Argument
     *
     * @param type type
     * @return the Argument
     * @param <V> type
     */
    default <V> Argument<V> testArgument(Class<V> type) {
        return getTestArgument(type);
    }

    /**
     * Get the Argument payload
     *
     * @return the Argument payload
     * @param <V> type
     */
    default <V> V testArgumentPayload() {
        return getTestArgumentPayload();
    }

    /**
     * Get the Argument
     *
     * @param type type
     * @return the Argument
     * @param <V> type
     */
    default <V> V testArgumentPayload(Class<V> type) {
        return getTestArgumentPayload(type);
    }

    /**
     * Get the EngineContext
     *
     * <p>Equivalent to getClassContext().getEngineContext()
     *
     * @return the Engine Configuration
     */
    default EngineContext getEngineContext() {
        return getClassContext().getEngineContext();
    }

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
    int getTestArgumentIndex();

    /**
     * Get the Argument
     *
     * @return the Argument
     */
    Argument<?> getTestArgument();

    /**
     * Get the Argument
     *
     * @param type type
     * @return the Argument
     * @param <V> type
     */
    <V> Argument<V> getTestArgument(Class<V> type);

    /**
     * Get the Argument payload
     *
     * @return the Argument payload
     * @param <V> type
     */
    <V> V getTestArgumentPayload();

    /**
     * Get the Argument payload
     *
     * @param type type
     * @return the Argument payload
     * @param <V> type
     */
    <V> V getTestArgumentPayload(Class<V> type);
}
