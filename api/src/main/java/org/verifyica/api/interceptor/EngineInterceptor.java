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

package org.verifyica.api.interceptor;

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
     * Method to get a Predicate to filter ClassDefinitions onTestDiscovery
     *
     * @return a ClassDefinition Predicate
     */
    /*
    default Predicate<ClassDefinition> onTestDiscoveryPredicate() {
        return classDefinition -> true;
    }
    */

    /**
     * Engine onTestDiscovery callback
     *
     * <p>Default implementation checks/tests onTestDiscoveryPredicate and dispatches to
     * onTestDiscovery(ClassDefinition)
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @param classDefinitions classDefinitions
     * @throws Throwable Throwable
     */
    /*
    default void onTestDiscovery(
            EngineInterceptorContext engineInterceptorContext, List<ClassDefinition> classDefinitions)
            throws Throwable {
        Predicate<ClassDefinition> classDefinitionPredicate = onTestDiscoveryPredicate();

        for (ClassDefinition classDefinition : classDefinitions) {
            if (classDefinitionPredicate == null || classDefinitionPredicate.test(classDefinition)) {
                onTestDiscovery(engineInterceptorContext, classDefinition);
            }
        }
    }
    */

    /**
     * Engine onTestDiscovery callback
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @param classDefinition classDefinition
     * @throws Throwable Throwable
     */
    /*
    default void onTestDiscovery(EngineInterceptorContext engineInterceptorContext, ClassDefinition classDefinition)
            throws Throwable {
        // INTENTIONALLY BLANK
    }
    */

    /**
     * Engine preExecute callback
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @throws Throwable Throwable
     */
    default void onExecute(EngineInterceptorContext engineInterceptorContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Engine postExecute callback
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @throws Throwable Throwable
     */
    /*
    default void postExecute(EngineInterceptorContext engineInterceptorContext) throws Throwable {
        // INTENTIONALLY BLANK
    }
    */

    /**
     * Engine onDestroy callback
     *
     * @param engineInterceptorContext engineInterceptorContext
     */
    default void onDestroy(EngineInterceptorContext engineInterceptorContext) {
        // INTENTIONALLY BLANK
    }
}
