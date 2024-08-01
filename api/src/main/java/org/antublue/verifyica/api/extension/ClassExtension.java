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

package org.antublue.verifyica.api.extension;

import java.lang.reflect.Method;
import org.antublue.verifyica.api.extension.engine.EngineExtensionContext;

/** Interface to implement ClassExtension */
public interface ClassExtension {

    /**
     * Class beforeInstantiate callback
     *
     * @param engineExtensionContext engineExtensionContext
     * @param testClass testClass
     * @throws Throwable Throwable
     */
    default void beforeInstantiate(
            EngineExtensionContext engineExtensionContext, Class<?> testClass) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Class afterInstantiate
     *
     * @param engineExtensionContext engineExtensionContext
     * @param testClass testClass
     * @param testInstance testInstance
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void afterInstantiate(
            EngineExtensionContext engineExtensionContext,
            Class<?> testClass,
            Object testInstance,
            Throwable throwable)
            throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Class beforePrepare callback
     *
     * @param classExtensionContext classExtensionContext
     * @throws Throwable Throwable
     */
    default void beforePrepare(ClassExtensionContext classExtensionContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Class afterPrepare callback
     *
     * @param classExtensionContext classExtensionContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void afterPrepare(ClassExtensionContext classExtensionContext, Throwable throwable)
            throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Class beforeBeforeAll callback
     *
     * @param argumentExtensionContext argumentExtensionContext
     * @throws Throwable Throwable
     */
    default void beforeBeforeAll(ArgumentExtensionContext argumentExtensionContext)
            throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Class afterBeforeAll callback
     *
     * @param argumentExtensionContext argumentExtensionContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void afterBeforeAll(
            ArgumentExtensionContext argumentExtensionContext, Throwable throwable)
            throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Class beforeBeforeEach callback
     *
     * @param argumentExtensionContext argumentExtensionContext
     * @throws Throwable Throwable
     */
    default void beforeBeforeEach(ArgumentExtensionContext argumentExtensionContext)
            throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Class afterBeforeEach callback
     *
     * @param argumentExtensionContext argumentExtensionContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void afterBeforeEach(
            ArgumentExtensionContext argumentExtensionContext, Throwable throwable)
            throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Class beforeTest callback
     *
     * @param argumentExtensionContext argumentExtensionContext
     * @param testMethod testMethod
     * @throws Throwable Throwable
     */
    default void beforeTest(ArgumentExtensionContext argumentExtensionContext, Method testMethod)
            throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Class afterTest callback
     *
     * @param argumentExtensionContext argumentExtensionContext
     * @param testMethod testMethod
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void afterTest(
            ArgumentExtensionContext argumentExtensionContext,
            Method testMethod,
            Throwable throwable)
            throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Class beforeAfterEach callback
     *
     * @param argumentExtensionContext argumentExtensionContext
     * @throws Throwable Throwable
     */
    default void beforeAfterEach(ArgumentExtensionContext argumentExtensionContext)
            throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Class afterAfterEach callback
     *
     * @param argumentExtensionContext argumentExtensionContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void afterAfterEach(
            ArgumentExtensionContext argumentExtensionContext, Throwable throwable)
            throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Class beforeAfterAll callback
     *
     * @param argumentExtensionContext argumentExtensionContext
     * @throws Throwable Throwable
     */
    default void beforeAfterAll(ArgumentExtensionContext argumentExtensionContext)
            throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Class afterAfterAll callback
     *
     * @param argumentExtensionContext argumentExtensionContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void afterAfterAll(
            ArgumentExtensionContext argumentExtensionContext, Throwable throwable)
            throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Class beforeConclude callback
     *
     * @param classExtensionContext classExtensionContext
     * @throws Throwable Throwable
     */
    default void beforeConclude(ClassExtensionContext classExtensionContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Class afterConclude callback
     *
     * @param classExtensionContext classExtensionContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void afterConclude(ClassExtensionContext classExtensionContext, Throwable throwable)
            throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Class onDestroy callback
     *
     * @param classExtensionContext classExtensionContext
     * @throws Throwable Throwable
     */
    default void onDestroy(ClassExtensionContext classExtensionContext) throws Throwable {
        // INTENTIONALLY BLANK
    }
}
