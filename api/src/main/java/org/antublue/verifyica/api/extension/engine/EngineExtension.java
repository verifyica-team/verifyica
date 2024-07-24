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
import java.util.Map;
import java.util.Set;

/** Interface to implement EngineExtension */
public interface EngineExtension {

    /**
     * Engine afterInitialize callback
     *
     * @param engineExtensionContext engineExtensionContext
     * @throws Throwable Throwable
     */
    default void afterInitialize(EngineExtensionContext engineExtensionContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Engine afterTestDiscovery callback
     *
     * @param engineExtensionContext engineExtensionContext
     * @param testClassMethodMap testClassMethodMap
     * @return a Map containing test classes and associated test class methods
     * @throws Throwable Throwable
     */
    default Map<Class<?>, Set<Method>> afterTestDiscovery(
            EngineExtensionContext engineExtensionContext,
            Map<Class<?>, Set<Method>> testClassMethodMap)
            throws Throwable {
        return testClassMethodMap;
    }

    /**
     * Engine beforeExecute callback
     *
     * @param engineExtensionContext engineExtensionContext
     * @throws Throwable Throwable
     */
    default void beforeExecute(EngineExtensionContext engineExtensionContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Engine beforeDestroy callback
     *
     * @param engineExtensionContext engineExtensionContext
     * @throws Throwable Throwable
     */
    default void beforeDestroy(EngineExtensionContext engineExtensionContext) throws Throwable {
        // INTENTIONALLY BLANK
    }
}
