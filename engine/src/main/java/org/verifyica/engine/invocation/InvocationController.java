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

package org.verifyica.engine.invocation;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.interceptor.EngineInterceptorContext;
import org.verifyica.engine.interceptor.ClassInterceptorManager;
import org.verifyica.engine.interceptor.EngineInterceptorManager;

/** Class to implement InvocationController */
public class InvocationController {

    private final EngineInterceptorManager engineInterceptorManager;
    private final ClassInterceptorManager classInterceptorManager;

    /**
     * Constructor
     *
     * @param engineInterceptorManager engineInterceptorManager
     * @param classInterceptorManager classInterceptorManager
     */
    public InvocationController(
            EngineInterceptorManager engineInterceptorManager,
            ClassInterceptorManager classInterceptorManager) {
        this.engineInterceptorManager = engineInterceptorManager;
        this.classInterceptorManager = classInterceptorManager;
    }

    /**
     * Method to invoke onInitialize
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @throws Throwable Throwable
     */
    public void invokeOnInitialize(EngineInterceptorContext engineInterceptorContext)
            throws Throwable {
        engineInterceptorManager.onInitialize(engineInterceptorContext);
    }

    public void invokeInstantiate(Class<?> testClass, AtomicReference<Object> testInstanceReference)
            throws Throwable {
        classInterceptorManager.instantiate(testClass, testInstanceReference);
    }

    public void invokePrepareMethods(List<Method> prepareMethods, ClassContext classContext)
            throws Throwable {
        classInterceptorManager.prepare(prepareMethods, classContext);
    }

    /**
     * Method to invoke beforeAll methods
     *
     * @param beforeAllMethods beforeAllMethods
     * @param argumentContext argumentContext
     * @throws Throwable Throwable
     */
    public void invokeBeforeAllMethods(
            List<Method> beforeAllMethods, ArgumentContext argumentContext) throws Throwable {
        classInterceptorManager.beforeAll(beforeAllMethods, argumentContext);
    }

    /**
     * Method to invoke beforeEach methods
     *
     * @param beforeEachMethods beforeEachMethods
     * @param argumentContext argumentContext
     * @throws Throwable Throwable
     */
    public void invokeBeforeEachMethods(
            List<Method> beforeEachMethods, ArgumentContext argumentContext) throws Throwable {
        classInterceptorManager.beforeEach(beforeEachMethods, argumentContext);
    }

    /**
     * Method to invoke a test method
     *
     * @param testMethod testMethod
     * @param argumentContext argumentContext
     * @throws Throwable Throwable
     */
    public void invokeTestMethod(Method testMethod, ArgumentContext argumentContext)
            throws Throwable {
        classInterceptorManager.test(testMethod, argumentContext);
    }

    /**
     * Method to invoke afterEach methods
     *
     * @param afterEachMethods afterEachMethods
     * @param argumentContext argumentContext
     * @throws Throwable Throwable
     */
    public void invokeAfterEachMethods(
            List<Method> afterEachMethods, ArgumentContext argumentContext) throws Throwable {
        classInterceptorManager.afterEach(afterEachMethods, argumentContext);
    }

    /**
     * Method to invoke afterAll methods
     *
     * @param afterAllMethods afterAllMethods
     * @param argumentContext argumentContext
     * @throws Throwable Throwable
     */
    public void invokeAfterAllMethods(List<Method> afterAllMethods, ArgumentContext argumentContext)
            throws Throwable {
        classInterceptorManager.afterAll(afterAllMethods, argumentContext);
    }

    /**
     * Method to invoke conclude methods
     *
     * @param concludeMethods concludeMethods
     * @param classContext classContext
     * @throws Throwable Throwable
     */
    public void invokeConcludeMethods(List<Method> concludeMethods, ClassContext classContext)
            throws Throwable {
        classInterceptorManager.conclude(concludeMethods, classContext);
    }

    public void invokeOnDestroy(ClassContext classContext) throws Throwable {
        classInterceptorManager.onDestroy(classContext);
    }
}
