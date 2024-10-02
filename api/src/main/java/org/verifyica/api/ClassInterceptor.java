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

package org.verifyica.api;

import java.lang.reflect.Method;
import java.util.function.Predicate;

/** Interface to implement ClassInterceptor */
public interface ClassInterceptor {

    /**
     * Predicate to accept any ClassContext
     */
    Predicate<ClassContext> ACCEPT_ALL = classContext -> true;

    /**
     * ClassInterceptor initialize
     *
     * @param engineContext engineContext
     * @throws Throwable throwable
     */
    default void initialize(EngineContext engineContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * ClassInterceptor predicate
     *
     * @return a Predicate
     */
    default Predicate<ClassContext> predicate() {
        return ACCEPT_ALL;
    }

    /**
     * ClassInterceptor preInstantiate callback
     *
     * @param engineContext engineContext
     * @param testClass testClass
     * @throws Throwable Throwable
     */
    default void preInstantiate(EngineContext engineContext, Class<?> testClass) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * ClassInterceptor postInstantiate callback
     *
     * @param engineContext engineContext
     * @param testClass testClass
     * @param testInstance testInstance
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postInstantiate(
            EngineContext engineContext, Class<?> testClass, Object testInstance, Throwable throwable)
            throws Throwable {
        rethrow(throwable);
    }

    /**
     * ClassInterceptor prePrepare callback
     *
     * @param classContext classContext
     * @throws Throwable Throwable
     */
    default void prePrepare(ClassContext classContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * ClassInterceptor postPrepare callback
     *
     * @param classContext classContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postPrepare(ClassContext classContext, Throwable throwable) throws Throwable {
        rethrow(throwable);
    }

    /**
     * ClassInterceptor preBeforeAll callback
     *
     * @param argumentContext argumentContext
     * @throws Throwable Throwable
     */
    default void preBeforeAll(ArgumentContext argumentContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * ClassInterceptor postBeforeAll callback
     *
     * @param argumentContext argumentContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postBeforeAll(ArgumentContext argumentContext, Throwable throwable) throws Throwable {
        rethrow(throwable);
    }

    /**
     * ClassInterceptor preBeforeEach callback
     *
     * @param argumentContext argumentContext
     * @throws Throwable Throwable
     */
    default void preBeforeEach(ArgumentContext argumentContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * ClassInterceptor postBeforeEach callback
     *
     * @param argumentContext argumentContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postBeforeEach(ArgumentContext argumentContext, Throwable throwable) throws Throwable {
        rethrow(throwable);
    }

    /**
     * ClassInterceptor preTest callback
     *
     * @param argumentContext argumentContext
     * @param testMethod testMethod
     * @throws Throwable Throwable
     */
    default void preTest(ArgumentContext argumentContext, Method testMethod) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * ClassInterceptor postTest callback
     *
     * @param argumentContext argumentContext
     * @param testMethod testMethod
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postTest(ArgumentContext argumentContext, Method testMethod, Throwable throwable) throws Throwable {
        rethrow(throwable);
    }

    /**
     * ClassInterceptor preAfterEach callback
     *
     * @param argumentContext argumentContext
     * @throws Throwable Throwable
     */
    default void preAfterEach(ArgumentContext argumentContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * ClassInterceptor postAfterEach callback
     *
     * @param argumentContext argumentContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postAfterEach(ArgumentContext argumentContext, Throwable throwable) throws Throwable {
        rethrow(throwable);
    }

    /**
     * ClassInterceptor preAfterAll callback
     *
     * @param argumentContext argumentContext
     * @throws Throwable Throwable
     */
    default void preAfterAll(ArgumentContext argumentContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * ClassInterceptor postAfterAll callback
     *
     * @param argumentContext argumentContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postAfterAll(ArgumentContext argumentContext, Throwable throwable) throws Throwable {
        rethrow(throwable);
    }

    /**
     * ClassInterceptor preConclude callback
     *
     * @param classContext classContext
     * @throws Throwable Throwable
     */
    default void preConclude(ClassContext classContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * ClassInterceptor postConclude callback
     *
     * @param classContext classContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postConclude(ClassContext classContext, Throwable throwable) throws Throwable {
        rethrow(throwable);
    }

    /**
     * ClassInterceptor onDestroy callback
     *
     * @param classContext classContext
     * @throws Throwable Throwable
     */
    default void onDestroy(ClassContext classContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * ClassInterceptor destroy
     *
     * @param engineContext engineContext
     * @throws Throwable Throwable
     */
    default void destroy(EngineContext engineContext) throws Throwable {
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
