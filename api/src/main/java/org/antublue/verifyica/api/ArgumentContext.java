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

/** Interface to implement ArgumentContext */
@SuppressWarnings("unchecked")
public interface ArgumentContext extends Context {

    /**
     * Method to get the EngineContext
     *
     * @return the EngineContext
     */
    default EngineContext getEngineContext() {
        return getClassContext().getEngineContext();
    }

    /**
     * Method to get ClassContext
     *
     * @return the ClassContext
     */
    ClassContext getClassContext();

    /**
     * Method to get the Argument
     *
     * @return the Argument
     */
    Argument<?> getArgument();

    /**
     * Method to get the Argument
     *
     * @param type type
     * @return the Argument
     * @param <T> type
     */
    <T> Argument<T> getArgument(Class<T> type);

    /**
     * Method to get the Argument payload
     *
     * @return the Argument payload
     * @param <T> type
     */
    default <T> T getArgumentPayload() {
        return (T) getArgument().getPayload();
    }

    /**
     * Method to get the Argument payload
     *
     * @param type type
     * @return the Argument payload
     * @param <T> type
     */
    default <T> T getArgumentPayload(Class<T> type) {
        return type.cast(getArgument().getPayload());
    }
}
