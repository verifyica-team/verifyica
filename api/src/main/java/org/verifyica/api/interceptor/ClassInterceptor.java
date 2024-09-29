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

import java.lang.reflect.Method;

/** Interface to implement ClassInterceptor */
public interface ClassInterceptor {

    /**
     * ClassInterceptor onInitialize callback
     *
     * @throws Throwable throwable
     */
    default void onInitialize() throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * ClassInterceptor preInstantiate callback
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @param testClass testClass
     * @throws Throwable Throwable
     */
    default void preInstantiate(EngineInterceptorContext engineInterceptorContext, Class<?> testClass)
            throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * ClassInterceptor postInstantiate callback
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
        rethrow(throwable);
    }

    /**
     * ClassInterceptor prePrepare callback
     *
     * @param classInterceptorContext classInterceptorContext
     * @throws Throwable Throwable
     */
    default void prePrepare(ClassInterceptorContext classInterceptorContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * ClassInterceptor postPrepare callback
     *
     * @param classInterceptorContext classInterceptorContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postPrepare(ClassInterceptorContext classInterceptorContext, Throwable throwable) throws Throwable {
        rethrow(throwable);
    }

    /**
     * ClassInterceptor preBeforeAll callback
     *
     * @param argumentInterceptorContext argumentInterceptorContext
     * @throws Throwable Throwable
     */
    default void preBeforeAll(ArgumentInterceptorContext argumentInterceptorContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * ClassInterceptor postBeforeAll callback
     *
     * @param argumentInterceptorContext argumentInterceptorContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postBeforeAll(ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
            throws Throwable {
        rethrow(throwable);
    }

    /**
     * ClassInterceptor preBeforeEach callback
     *
     * @param argumentInterceptorContext argumentInterceptorContext
     * @throws Throwable Throwable
     */
    default void preBeforeEach(ArgumentInterceptorContext argumentInterceptorContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * ClassInterceptor postBeforeEach callback
     *
     * @param argumentInterceptorContext argumentInterceptorContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postBeforeEach(ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
            throws Throwable {
        rethrow(throwable);
    }

    /**
     * ClassInterceptor preTest callback
     *
     * @param argumentInterceptorContext argumentInterceptorContext
     * @param testMethod testMethod
     * @throws Throwable Throwable
     */
    default void preTest(ArgumentInterceptorContext argumentInterceptorContext, Method testMethod) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * ClassInterceptor postTest callback
     *
     * @param argumentInterceptorContext argumentInterceptorContext
     * @param testMethod testMethod
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postTest(ArgumentInterceptorContext argumentInterceptorContext, Method testMethod, Throwable throwable)
            throws Throwable {
        rethrow(throwable);
    }

    /**
     * ClassInterceptor preAfterEach callback
     *
     * @param argumentInterceptorContext argumentInterceptorContext
     * @throws Throwable Throwable
     */
    default void preAfterEach(ArgumentInterceptorContext argumentInterceptorContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * ClassInterceptor postAfterEach callback
     *
     * @param argumentInterceptorContext argumentInterceptorContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postAfterEach(ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
            throws Throwable {
        rethrow(throwable);
    }

    /**
     * ClassInterceptor preAfterAll callback
     *
     * @param argumentInterceptorContext argumentInterceptorContext
     * @throws Throwable Throwable
     */
    default void preAfterAll(ArgumentInterceptorContext argumentInterceptorContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * ClassInterceptor postAfterAll callback
     *
     * @param argumentInterceptorContext argumentInterceptorContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postAfterAll(ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
            throws Throwable {
        rethrow(throwable);
    }

    /**
     * ClassInterceptor preConclude callback
     *
     * @param classInterceptorContext classInterceptorContext
     * @throws Throwable Throwable
     */
    default void preConclude(ClassInterceptorContext classInterceptorContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * ClassInterceptor postConclude callback
     *
     * @param classInterceptorContext classInterceptorContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postConclude(ClassInterceptorContext classInterceptorContext, Throwable throwable) throws Throwable {
        rethrow(throwable);
    }

    /**
     * ClassInterceptor onDestroy callback
     *
     * @param classInterceptorContext classInterceptorContext
     * @throws Throwable Throwable
     */
    default void onDestroy(ClassInterceptorContext classInterceptorContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * ClassInterceptor onDestroy callback
     */
    default void onDestroy() throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Rethrows a Throwable if not null
     *
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void rethrow(Throwable throwable) throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }
}