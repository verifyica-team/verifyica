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

package org.antublue.verifyica.api.engine;

import java.lang.reflect.Method;
import java.util.List;

/** Interface to implement EngineCallback */
public interface EngineExtension {

    /**
     * Engine initialize callback
     *
     * @param engineExtensionContext engineExtensionContext
     * @return the CallbackResult
     * @throws Throwable Throwable
     */
    default ExtensionResult onInitialize(EngineExtensionContext engineExtensionContext)
            throws Throwable {
        return ExtensionResult.PROCEED;
    }

    /**
     * Engine test class discovery callback
     *
     * @param engineExtensionContext engineExtensionContext
     * @param testClasses testClasses
     * @return the CallbackResult
     * @throws Throwable Throwable
     */
    default ExtensionResult onTestClassDiscovery(
            EngineExtensionContext engineExtensionContext, List<Class<?>> testClasses)
            throws Throwable {
        return ExtensionResult.PROCEED;
    }

    /**
     * Engine test class test method discovery callback
     *
     * @param engineExtensionContext engineExtensionContext
     * @param testClass testClass
     * @param testMethods testMethods
     * @return the CallbackResult
     * @throws Throwable Throwable
     */
    default ExtensionResult onTestClassTestMethodDiscovery(
            EngineExtensionContext engineExtensionContext,
            Class<?> testClass,
            List<Method> testMethods)
            throws Throwable {
        return ExtensionResult.PROCEED;
    }

    /**
     * Method to invoke engine execution
     *
     * @param engineExtensionContext engineExtensionContext
     * @return the CallbackResult
     * @throws Throwable Throwable
     */
    default ExtensionResult onExecute(EngineExtensionContext engineExtensionContext)
            throws Throwable {
        return ExtensionResult.PROCEED;
    }

    /**
     * Engine destroy callback
     *
     * @param engineExtensionContext engineExtensionContext
     * @throws Throwable Throwable
     */
    default void onDestroy(EngineExtensionContext engineExtensionContext) throws Throwable {
        // DO NOTHING
    }
}
