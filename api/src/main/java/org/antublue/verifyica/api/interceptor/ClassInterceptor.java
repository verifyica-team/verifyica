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

package org.antublue.verifyica.api.interceptor;

import java.lang.reflect.Method;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptorContext;

/** Interface to implement ClassInterceptor */
public interface ClassInterceptor {

    /**
     * Class preInstantiate callback
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @param testClass testClass
     * @throws Throwable Throwable
     */
    default void preInstantiate(
            EngineInterceptorContext engineInterceptorContext, Class<?> testClass)
            throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Class postInstantiate
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @param testClass testClass
     * @param testInstance testInstance
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postInstantiate(
            EngineInterceptorContext engineInterceptorContext,
            Class<?> testClass,
            Object testInstance,
            Throwable throwable)
            throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Class prePrepare callback
     *
     * @param classInterceptorContext classInterceptorContext
     * @throws Throwable Throwable
     */
    default void prePrepare(ClassInterceptorContext classInterceptorContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Class postPrepare callback
     *
     * @param classInterceptorContext classInterceptorContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postPrepare(ClassInterceptorContext classInterceptorContext, Throwable throwable)
            throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Class preBeforeAll callback
     *
     * @param argumentInterceptorContext argumentInterceptorContext
     * @throws Throwable Throwable
     */
    default void preBeforeAll(ArgumentInterceptorContext argumentInterceptorContext)
            throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Class postBeforeAll callback
     *
     * @param argumentInterceptorContext argumentInterceptorContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postBeforeAll(
            ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
            throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Class preBeforeEach callback
     *
     * @param argumentInterceptorContext argumentInterceptorContext
     * @throws Throwable Throwable
     */
    default void preBeforeEach(ArgumentInterceptorContext argumentInterceptorContext)
            throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Class postBeforeEach callback
     *
     * @param argumentInterceptorContext argumentInterceptorContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postBeforeEach(
            ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
            throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Class preTest callback
     *
     * @param argumentInterceptorContext argumentInterceptorContext
     * @param testMethod testMethod
     * @throws Throwable Throwable
     */
    default void preTest(ArgumentInterceptorContext argumentInterceptorContext, Method testMethod)
            throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Class postTest callback
     *
     * @param argumentInterceptorContext argumentInterceptorContext
     * @param testMethod testMethod
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postTest(
            ArgumentInterceptorContext argumentInterceptorContext,
            Method testMethod,
            Throwable throwable)
            throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Class preAfterEach callback
     *
     * @param argumentInterceptorContext argumentInterceptorContext
     * @throws Throwable Throwable
     */
    default void preAfterEach(ArgumentInterceptorContext argumentInterceptorContext)
            throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Class postAfterEach callback
     *
     * @param argumentInterceptorContext argumentInterceptorContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postAfterEach(
            ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
            throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Class preAfterAll callback
     *
     * @param argumentInterceptorContext argumentInterceptorContext
     * @throws Throwable Throwable
     */
    default void preAfterAll(ArgumentInterceptorContext argumentInterceptorContext)
            throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Class postAfterAll callback
     *
     * @param argumentInterceptorContext argumentInterceptorContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postAfterAll(
            ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
            throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Class preConclude callback
     *
     * @param classInterceptorContext classInterceptorContext
     * @throws Throwable Throwable
     */
    default void preConclude(ClassInterceptorContext classInterceptorContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Class postConclude callback
     *
     * @param classInterceptorContext classInterceptorContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postConclude(ClassInterceptorContext classInterceptorContext, Throwable throwable)
            throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Class onDestroy callback
     *
     * @param classInterceptorContext classInterceptorContext
     * @throws Throwable Throwable
     */
    default void onDestroy(ClassInterceptorContext classInterceptorContext) throws Throwable {
        // INTENTIONALLY BLANK
    }
}
