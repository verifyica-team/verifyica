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

import java.lang.reflect.Method;
import java.util.function.Predicate;

/**
 * Interface to implement ClassInterceptor
 */
public interface ClassInterceptor {

    /**
     * Predicate to accept any ClassContext
     */
    Predicate<ClassContext> ACCEPT_ALL = classContext -> true;

    /**
     * Initialize the ClassInterceptor
     *
     * @param engineContext engineContext
     * @throws Throwable throwable
     */
    default void initialize(EngineContext engineContext) throws Throwable {
        // INTENTIONALLY EMPTY
    }

    /**
     * Gets the Predicate to filter ClassContext
     *
     * @return a Predicate
     */
    default Predicate<ClassContext> predicate() {
        return ACCEPT_ALL;
    }

    /**
     * Pre Instantiate callback
     *
     * @param engineContext engineContext
     * @param testClass testClass
     * @throws Throwable Throwable
     */
    default void preInstantiate(EngineContext engineContext, Class<?> testClass) throws Throwable {
        // INTENTIONALLY EMPTY
    }

    /**
     * Post Instantiate callback
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
     * Pre Prepare callback
     *
     * @param classContext classContext
     * @throws Throwable Throwable
     */
    default void prePrepare(ClassContext classContext) throws Throwable {
        // INTENTIONALLY EMPTY
    }

    /**
     * Post Prepare callback
     *
     * @param classContext classContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postPrepare(ClassContext classContext, Throwable throwable) throws Throwable {
        rethrow(throwable);
    }

    /**
     * Pre BeforeAll callback
     *
     * @param argumentContext argumentContext
     * @throws Throwable Throwable
     */
    default void preBeforeAll(ArgumentContext argumentContext) throws Throwable {
        // INTENTIONALLY EMPTY
    }

    /**
     * Post BeforeAll callback
     *
     * @param argumentContext argumentContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postBeforeAll(ArgumentContext argumentContext, Throwable throwable) throws Throwable {
        rethrow(throwable);
    }

    /**
     * Pre BeforeEach callback
     *
     * @param argumentContext argumentContext
     * @throws Throwable Throwable
     */
    default void preBeforeEach(ArgumentContext argumentContext) throws Throwable {
        // INTENTIONALLY EMPTY
    }

    /**
     * Post BeforeEach callback
     *
     * @param argumentContext argumentContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postBeforeEach(ArgumentContext argumentContext, Throwable throwable) throws Throwable {
        rethrow(throwable);
    }

    /**
     * Pre Test callback
     *
     * @param argumentContext argumentContext
     * @param testMethod testMethod
     * @throws Throwable Throwable
     */
    default void preTest(ArgumentContext argumentContext, Method testMethod) throws Throwable {
        // INTENTIONALLY EMPTY
    }

    /**
     * Post Test callback
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
     * Pre AfterEach callback
     *
     * @param argumentContext argumentContext
     * @throws Throwable Throwable
     */
    default void preAfterEach(ArgumentContext argumentContext) throws Throwable {
        // INTENTIONALLY EMPTY
    }

    /**
     * Post AfterEach callback
     *
     * @param argumentContext argumentContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postAfterEach(ArgumentContext argumentContext, Throwable throwable) throws Throwable {
        rethrow(throwable);
    }

    /**
     * Pre AfterAll callback
     *
     * @param argumentContext argumentContext
     * @throws Throwable Throwable
     */
    default void preAfterAll(ArgumentContext argumentContext) throws Throwable {
        // INTENTIONALLY EMPTY
    }

    /**
     * Post afterAll callback
     *
     * @param argumentContext argumentContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postAfterAll(ArgumentContext argumentContext, Throwable throwable) throws Throwable {
        rethrow(throwable);
    }

    /**
     * Pre Conclude callback
     *
     * @param classContext classContext
     * @throws Throwable Throwable
     */
    default void preConclude(ClassContext classContext) throws Throwable {
        // INTENTIONALLY EMPTY
    }

    /**
     * Post Conclude callback
     *
     * @param classContext classContext
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postConclude(ClassContext classContext, Throwable throwable) throws Throwable {
        rethrow(throwable);
    }

    /**
     * OnDestroy callback
     *
     * @param classContext classContext
     * @throws Throwable Throwable
     */
    default void onDestroy(ClassContext classContext) throws Throwable {
        // INTENTIONALLY EMPTY
    }

    /**
     * Destroy callback
     *
     * @param engineContext engineContext
     * @throws Throwable Throwable
     */
    default void destroy(EngineContext engineContext) throws Throwable {
        // INTENTIONALLY EMPTY
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
