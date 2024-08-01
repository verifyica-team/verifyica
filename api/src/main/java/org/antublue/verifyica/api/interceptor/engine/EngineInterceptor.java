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

package org.antublue.verifyica.api.interceptor.engine;

import java.util.List;

/** Interface to implement EngineInterceptor */
public interface EngineInterceptor {

    /**
     * Engine onInitialize callback
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @throws Throwable Throwable
     */
    default void onInitialize(EngineInterceptorContext engineInterceptorContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Engine on onTestDiscovery callback
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @param classDefinitions classDefinitions
     * @throws Throwable Throwable
     */
    default void onTestDiscovery(
            EngineInterceptorContext engineInterceptorContext,
            List<ClassDefinition> classDefinitions)
            throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Engine on onTestDiscovery callback
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @param classDefinition classDefinition
     * @throws Throwable Throwable
     */
    default void onTestDiscovery(
            EngineInterceptorContext engineInterceptorContext, ClassDefinition classDefinition)
            throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Engine beforeExecute callback
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @throws Throwable Throwable
     */
    default void preExecute(EngineInterceptorContext engineInterceptorContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Engine beforeDestroy callback
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @throws Throwable Throwable
     */
    default void postExecute(EngineInterceptorContext engineInterceptorContext) throws Throwable {
        // INTENTIONALLY BLANK
    }
}
